package com.jiwu.api.user.common.config;

/**
 * 账单 MQ 常量，供 BillsListener（@QueueBinding 自动声明）与 BillsMqServiceImpl 发送端使用。
 */
public final class BillsMQConfig {
    /** 直连交换机 */
    public static final String DIRECT_BILLS_EXCHANGE = "direct_bills_exchange";
    /** 账单队列 */
    public static final String DIRECT_BILLS_QUEUE = "direct_bills_queue";
    /** 路由键：保存/更新账单统计 */
    public static final String DIRECT_SAVE_BILLS_ROUTING_KEY = "save_total_bills_key";

    private BillsMQConfig() {}
}
