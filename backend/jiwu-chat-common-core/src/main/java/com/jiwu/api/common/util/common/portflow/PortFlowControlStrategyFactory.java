package com.jiwu.api.common.util.common.portflow;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流策略工厂
 */
public class PortFlowControlStrategyFactory {
    /**
     * 指定时间内总次数限流
     */
    public static final String REDIS_COUNT_PORT_FLOW_CONTROL = "total:count:with:in:fix:time";
    /**
     * 限流策略集合
     */
    static Map<String, AbstractPortFlowControlService<?>> portFlowServiceStrategyMap = new ConcurrentHashMap<>(8);

    /**
     * 将策略类放入工厂
     *
     * @param strategyName                    策略名称
     * @param abstractFrequencyControlService 策略类
     */
    public static <K extends PortFlowControlDTO> void registerPortFlowController(String strategyName, AbstractPortFlowControlService<K> abstractFrequencyControlService) {
        portFlowServiceStrategyMap.put(strategyName, abstractFrequencyControlService);
    }

    /**
     * 根据名称获取策略类
     *
     * @param strategyName 策略名称
     * @return 对应的限流策略类
     */
    @SuppressWarnings("unchecked")
    public static <K extends PortFlowControlDTO> AbstractPortFlowControlService<K> getPortFlowControllerByName(String strategyName) {
        return (AbstractPortFlowControlService<K>) portFlowServiceStrategyMap.get(strategyName);
    }

    /**
     * 构造器私有
     */
    private PortFlowControlStrategyFactory() {

    }

}
