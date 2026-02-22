package com.jiwu.api.common.main.secure_invoke.aspect;

import cn.hutool.core.date.DateUtil;
import com.jiwu.api.common.annotation.SecureInvoke;
import com.jiwu.api.common.main.pojo.sys.SysSecureInvokeRecord;
import com.jiwu.api.common.main.secure_invoke.dto.SysSecureInvokeDTO;
import com.jiwu.api.common.main.secure_invoke.service.SecureInvokeHolder;
import com.jiwu.api.common.main.secure_invoke.service.SysSecureInvokeService;
import com.jiwu.api.common.util.common.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: 安全执行切面
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)//确保最先执行
@Component
public class SecureInvokeAspect {

    @Resource
    private SysSecureInvokeService sysSecureInvokeService;

    @Around("@annotation(secureInvoke)")
    public Object around(ProceedingJoinPoint joinPoint, SecureInvoke secureInvoke) throws Throwable {
        boolean async = secureInvoke.async();
        boolean isTransactional = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (SecureInvokeHolder.isInvoking() || !isTransactional) {
            return joinPoint.proceed();
        }
        //事务的保证
        log.info("安全事务开始执行，async={},isTransactional={}", async, isTransactional);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<String> parameters = Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
        SysSecureInvokeDTO dto = SysSecureInvokeDTO.builder()
                .args(JsonUtil.toStr(joinPoint.getArgs()))
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(JsonUtil.toStr(parameters))
                .build();
        SysSecureInvokeRecord inRecord = SysSecureInvokeRecord.builder()
                .sysSecureInvokeDTO(dto)
                .maxRetryTimes(secureInvoke.maxRetryTimes())
                .nextRetryTime(DateUtil.offsetMinute(new Date(), (int) SysSecureInvokeService.RETRY_INTERVAL_MINUTES))
                .build();
        sysSecureInvokeService.invoke(inRecord, async);
        return null;
    }
}
