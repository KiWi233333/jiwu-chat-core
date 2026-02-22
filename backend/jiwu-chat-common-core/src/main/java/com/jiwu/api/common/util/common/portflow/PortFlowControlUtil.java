package com.jiwu.api.common.util.common.portflow;

import com.jiwu.api.common.util.common.AssertUtil;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 限流工具类 提供编程式的限流调用方法
 */
public class PortFlowControlUtil {

    /**
     * 单限流策略的调用方法-编程式调用
     *
     * @param strategyName     策略名称
     * @param frequencyControl 单个频控对象
     * @param supplier         服务提供着
     * @return 业务方法执行结果
     */
    public static <T, K extends PortFlowControlDTO> T executeWithPortFlowControl(String strategyName, K frequencyControl, AbstractPortFlowControlService.SupplierThrowWithoutParam<T> supplier) throws Throwable {
        AbstractPortFlowControlService<K> frequencyController = PortFlowControlStrategyFactory.getPortFlowControllerByName(strategyName);
        return frequencyController.executeWithPortFlowControl(frequencyControl, supplier);
    }

    public static <K extends PortFlowControlDTO> void executeWithPortFlowControl(String strategyName, K frequencyControl, AbstractPortFlowControlService.Executor executor) throws Throwable {
        AbstractPortFlowControlService<K> frequencyController = PortFlowControlStrategyFactory.getPortFlowControllerByName(strategyName);
        frequencyController.executeWithPortFlowControl(frequencyControl, () -> {
            executor.execute();
            return null;
        });
    }


    /**
     * 多限流策略的编程式调用方法调用方法
     *
     * @param strategyName         策略名称
     * @param frequencyControlList 频控列表 包含每一个频率控制的定义以及顺序
     * @param supplier             函数式入参-代表每个频控方法执行的不同的业务逻辑
     * @return 业务方法执行的返回值
     * @throws Throwable 被限流或者限流策略定义错误
     */
    public static <T, K extends PortFlowControlDTO> T executeWithPortFlowControlList(String strategyName, List<K> frequencyControlList, AbstractPortFlowControlService.SupplierThrowWithoutParam<T> supplier) throws Throwable {
        boolean existsPortFlowControlHasNullKey = frequencyControlList.stream().anyMatch(frequencyControl -> ObjectUtils.isEmpty(frequencyControl.getKey()));
        AssertUtil.isFalse(existsPortFlowControlHasNullKey, "限流策略的Key字段不允许出现空值！");
        AbstractPortFlowControlService<K> frequencyController = PortFlowControlStrategyFactory.getPortFlowControllerByName(strategyName);
        return frequencyController.executeWithPortFlowControlList(frequencyControlList, supplier);
    }

    /**
     * 构造器私有
     */
    private PortFlowControlUtil() {

    }

}
