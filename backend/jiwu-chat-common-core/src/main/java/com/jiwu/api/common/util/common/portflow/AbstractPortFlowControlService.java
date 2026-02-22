package com.jiwu.api.common.util.common.portflow;

import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.common.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽象类频控服务 其他类如果要实现限流服务 直接注入使用通用限流类
 * 后期会通过继承此类实现令牌桶等算法
 *
 * @author linzhihan
 * @date 2023/07/03
 */
@Slf4j
public abstract class AbstractPortFlowControlService<K extends PortFlowControlDTO> {

    @PostConstruct
    protected void registerMyselfToFactory() {
        PortFlowControlStrategyFactory.registerPortFlowController(getStrategyName(), this);
    }

    /**
     * @param frequencyControlMap 定义的注解频控 Map中的Key-对应redis的单个频控的Key Map中的Value-对应redis的单个频控的Key限制的Value
     * @param supplier            函数式入参-代表每个频控方法执行的不同的业务逻辑
     * @return 业务方法执行的返回值
     */
    private <T> T executeWithPortFlowControlMap(Map<String, K> frequencyControlMap, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        if (reachRateLimit(frequencyControlMap)) {
            throw new PortFlowControlException(ResultStatus.BUSY_ERR);
        }
        try {
            return supplier.get();
        } finally {
            //不管成功还是失败，都增加次数
            addPortFlowControlStatisticsCount(frequencyControlMap);
        }
    }



    /**
     * 多限流策略的编程式调用方法 无参的调用方法
     *
     * @param frequencyControlList 频控列表 包含每一个频率控制的定义以及顺序
     * @param supplier             函数式入参-代表每个频控方法执行的不同的业务逻辑
     * @return 业务方法执行的返回值
     * @throws Throwable 被限流或者限流策略定义错误
     */
    @SuppressWarnings("unchecked")
    public <T> T executeWithPortFlowControlList(List<K> frequencyControlList, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        boolean existsPortFlowControlHasNullKey = frequencyControlList.stream().anyMatch(frequencyControl -> ObjectUtils.isEmpty(frequencyControl.getKey()));
        AssertUtil.isFalse(existsPortFlowControlHasNullKey, "限流策略的Key字段不允许出现空值!");
        Map<String, PortFlowControlDTO> frequencyControlDTOMap = frequencyControlList.stream().collect(Collectors.groupingBy(PortFlowControlDTO::getKey, Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
        return executeWithPortFlowControlMap((Map<String, K>) frequencyControlDTOMap, supplier);
    }

    /**
     * 单限流策略的调用方法-编程式调用
     *
     * @param frequencyControl 单个频控对象
     * @param supplier         服务提供着
     * @return 业务方法执行结果
     */
    public <T> T executeWithPortFlowControl(K frequencyControl, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        return executeWithPortFlowControlList(Collections.singletonList(frequencyControl), supplier);
    }


    @FunctionalInterface
    public interface SupplierThrowWithoutParam<T> {


        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface Executor {

        /**
         * Gets a result.
         */
        void execute() throws Throwable;
    }

    /**
     * 是否达到限流阈值 子类实现 每个子类都可以自定义自己的限流逻辑判断
     *
     * @param frequencyControlMap 定义的注解频控 Map中的Key-对应redis的单个频控的Key Map中的Value-对应redis的单个频控的Key限制的Value
     * @return true-方法被限流 false-方法没有被限流
     */
    protected abstract boolean reachRateLimit(Map<String, K> frequencyControlMap);

    /**
     * 增加限流统计次数 子类实现 每个子类都可以自定义自己的限流统计信息增加的逻辑
     *
     * @param frequencyControlMap 定义的注解频控 Map中的Key-对应redis的单个频控的Key Map中的Value-对应redis的单个频控的Key限制的Value
     */
    protected abstract void addPortFlowControlStatisticsCount(Map<String, K> frequencyControlMap);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    protected abstract String getStrategyName();

}
