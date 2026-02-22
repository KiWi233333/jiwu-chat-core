package com.jiwu.api.common.main.secure_invoke.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.mapper.sys.SysSecureInvokeRecordMapper;
import com.jiwu.api.common.main.pojo.sys.SysSecureInvokeRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.jiwu.api.common.main.secure_invoke.dto.SysSecureInvokeDTO;
import com.jiwu.api.common.util.common.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Description: 安全执行处理器
 * Date: 2023-08-20
 */
@Slf4j
@AllArgsConstructor
public class SysSecureInvokeService {

    public static final double RETRY_INTERVAL_MINUTES = 2D;

    private final SysSecureInvokeRecordMapper secureInvokeRecordMapper;

    private final Executor executor;

    //    @Scheduled(cron = "*/5 * * * * ?")
    public void retry() {
        List<SysSecureInvokeRecord> secureInvokeRecords = getWaitRetryRecords();
        for (SysSecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
            doAsyncInvoke(secureInvokeRecord);
        }
    }

    private List<SysSecureInvokeRecord> getWaitRetryRecords() {
        Date now = new Date();
        //查2分钟前的失败数据。避免刚入库的数据被查出来
        DateTime afterTime = DateUtil.offsetMinute(now, (int) SysSecureInvokeService.RETRY_INTERVAL_MINUTES);
        return secureInvokeRecordMapper.selectList(new LambdaQueryWrapper<SysSecureInvokeRecord>()
                .eq(SysSecureInvokeRecord::getStatus, SysSecureInvokeRecord.STATUS_WAIT)
                .lt(SysSecureInvokeRecord::getNextRetryTime, new Date())
                .lt(SysSecureInvokeRecord::getCreateTime, afterTime));
    }

    public void save(SysSecureInvokeRecord record) {
        secureInvokeRecordMapper.insert(record);
    }

    private void retryRecord(SysSecureInvokeRecord record, String errorMsg) {
        Integer retryTimes = record.getRetryTimes() + 1;
        SysSecureInvokeRecord update = new SysSecureInvokeRecord();
        update.setId(record.getId());
        update.setFailReason(errorMsg);
        update.setNextRetryTime(getNextRetryTime(retryTimes));
        if (retryTimes > record.getMaxRetryTimes()) {
            update.setStatus(SysSecureInvokeRecord.STATUS_FAIL);
        } else {
            update.setRetryTimes(retryTimes);
        }
        secureInvokeRecordMapper.updateById(update);
    }

    private Date getNextRetryTime(Integer retryTimes) {//或者可以采用退避算法
        double waitMinutes = Math.pow(RETRY_INTERVAL_MINUTES, retryTimes);//重试时间指数上升 2m 4m 8m 16m
        return DateUtil.offsetMinute(new Date(), (int) waitMinutes);
    }

    private void removeRecord(Long id) {
        secureInvokeRecordMapper.deleteById(id);
    }

    public void invoke(SysSecureInvokeRecord record, boolean async) {
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (!inTransaction) {
            return;
        }
        //保存执行数据
        save(record);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @SneakyThrows
            @Override
            public void afterCommit() {
                //事务后执行
                if (async) {
                    doAsyncInvoke(record);
                } else {
                    doInvoke(record);
                }
            }
        });
    }

    public void doAsyncInvoke(SysSecureInvokeRecord record) {
        executor.execute(() -> {
            doInvoke(record);
        });
    }

    public void doInvoke(SysSecureInvokeRecord record) {
        SysSecureInvokeDTO secureInvokeDTO = record.getSysSecureInvokeDTO();
        try {
            SecureInvokeHolder.setInvoking();
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);
            List<String> parameterStrings = JsonUtil.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterClasses.toArray(new Class[]{}));
            Object[] args = getArgs(secureInvokeDTO, parameterClasses);
            //执行方法
            method.invoke(bean, args);
            //执行成功更新状态
            removeRecord(record.getId());
        } catch (Throwable e) {
            log.error("SecureInvokeService invoke fail", e);
            //执行失败，等待下次执行
            retryRecord(record, e.getMessage());
        } finally {
            SecureInvokeHolder.invoked();
        }
    }

    @NotNull
    private Object[] getArgs(SysSecureInvokeDTO secureInvokeDTO, List<Class<?>> parameterClasses) {
        JsonNode jsonNode = JsonUtil.toJsonNode(secureInvokeDTO.getArgs());
        Object[] args = new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            Class<?> aClass = parameterClasses.get(i);
            args[i] = JsonUtil.nodeToValue(jsonNode.get(i), aClass);
        }
        return args;
    }

    @NotNull
    private List<Class<?>> getParameters(List<String> parameterStrings) {
        return parameterStrings.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
