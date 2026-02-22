package com.jiwu.api.common.util.common.portflow;

import com.jiwu.api.common.util.service.RedisStaticUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.jiwu.api.common.util.common.portflow.PortFlowControlStrategyFactory.REDIS_COUNT_PORT_FLOW_CONTROL;


/**
 * 抽象类频控服务 -使用redis实现 固定时间内不超过固定次数的限流类
 */
@Slf4j
@Service
public class RedisDefaultPortFlowController extends AbstractPortFlowControlService<PortFlowControlDTO> {


    /**
     * 是否达到限流阈值 子类实现 每个子类都可以自定义自己的限流逻辑判断
     *
     * @param portFlowControlMap 定义的注解频控 Map中的Key-对应redis的单个频控的Key Map中的Value-对应redis的单个频控的Key限制的Value
     * @return true-方法被限流 false-方法没有被限流
     */
    @Override
    protected boolean reachRateLimit(Map<String, PortFlowControlDTO> portFlowControlMap) {
        //批量获取redis统计的值
        List<String> portFlowKeys = new ArrayList<>(portFlowControlMap.keySet());
        List<Integer> countList = RedisStaticUtil.mget(portFlowKeys, Integer.class);
        for (int i = 0; i < portFlowKeys.size(); i++) {
            String key = portFlowKeys.get(i);
            Integer count = countList.get(i);
            int portFlowControlCount = portFlowControlMap.get(key).getCount();
            if (Objects.nonNull(count) && count >= portFlowControlCount) {
                //频率超过了
                log.warn("频控 limit key:{},count:{}", key, count);
                return true;
            }
        }
        return false;
    }

    /**
     * 增加限流统计次数 子类实现 每个子类都可以自定义自己的限流统计信息增加的逻辑
     *
     * @param portFlowControlMap 定义的注解频控 Map中的Key-对应redis的单个频控的Key Map中的Value-对应redis的单个频控的Key限制的Value
     */
    @Override
    protected void addPortFlowControlStatisticsCount(Map<String, PortFlowControlDTO> portFlowControlMap) {
        portFlowControlMap.forEach((k, v) -> RedisStaticUtil.inc(k, v.getTime(), v.getUnit()));
    }

    @Override
    protected String getStrategyName() {
        return REDIS_COUNT_PORT_FLOW_CONTROL;
    }
}
