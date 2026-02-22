package com.jiwu.api.chat.common.event.producer;

import com.jiwu.api.common.annotation.SecureInvoke;
import com.jiwu.api.common.config.rabbitmq.MyMessageConverter;
import com.jiwu.api.common.main.constant.chat.ChatMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: 发送mq工具类
 * Date: 2023-08-12
 */
@Slf4j
public class MqProducer<T> {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(String routeKey, T body) {
        // 1、设置转换器
        rabbitTemplate.setMessageConverter(new MyMessageConverter());
        // 2、发送
        rabbitTemplate.convertAndSend(ChatMqConstant.DIRECT_CHAT_EXCHANGE,
                routeKey,
                body);
    }

    public void sendMsg(String exchange,String routeKey, T body) {
        // 1、设置转换器
        rabbitTemplate.setMessageConverter(new MyMessageConverter());
        // 2、发送
        rabbitTemplate.convertAndSend(exchange,
                routeKey,
                body);
    }


    /**
     * 发送可靠消息，在事务提交后保证发送成功
     *
     * @param routeKey 路由routeKey
     * @param body     消息体
     */
    @SecureInvoke
    public void sendSecureMsg(String routeKey, T body, Object ack) {
        log.info("发送可靠消息，routeKey:{},body:{}ack:{}", routeKey, body, ack);
        // 1、设置转换器
        rabbitTemplate.setMessageConverter(new MyMessageConverter());
        // 2、发送
        rabbitTemplate.convertAndSend(ChatMqConstant.DIRECT_CHAT_EXCHANGE,
                routeKey,
                body);
    }

    /**
     * 发送可靠消息，在事务提交后保证发送成功
     *
     * @param exchange 交换机
     * @param routeKey 路由routeKey
     * @param body     消息体
     */
    @SecureInvoke
    public void sendSecureMsg(String exchange, String routeKey, T body, Object ack) {
        log.info("发送可靠消息，exchange:{} routeKey:{},body:{}ack:{}", exchange, routeKey, body, ack);
        // 1、设置转换器
        rabbitTemplate.setMessageConverter(new MyMessageConverter());
        // 2、发送
        rabbitTemplate.convertAndSend(exchange,
                routeKey,
                body);
    }
}
