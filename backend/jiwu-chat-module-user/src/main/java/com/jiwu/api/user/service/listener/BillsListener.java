package com.jiwu.api.user.service.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.jiwu.api.user.common.config.BillsMQConfig.*;

/**
 * 账单 MQ 消费者（开源版无订单，仅保留空实现避免队列声明缺失）
 * 队列/交换机/绑定通过 @QueueBinding 自动声明，与 ChatConsumer 等保持一致。
 */
@Slf4j
@Component
public class BillsListener {

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = DIRECT_BILLS_EXCHANGE, durable = "true", type = ExchangeTypes.DIRECT),
            value = @Queue(value = DIRECT_BILLS_QUEUE, durable = "true"),
            key = DIRECT_SAVE_BILLS_ROUTING_KEY
    ))
    public void refundOrdersListener(String userId) {
        // 开源版无订单退款逻辑，空实现
    }
}
