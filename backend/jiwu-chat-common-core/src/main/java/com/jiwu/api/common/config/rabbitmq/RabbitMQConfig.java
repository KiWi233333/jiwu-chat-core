package com.jiwu.api.common.config.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * 动态创建队列、交换机初始化器
     */
//    @Bean
//    @ConditionalOnMissingBean
//    public RabbitMqDynamicInitializer rabbitMqDynamicInitializer(ConnectionFactory connectionFactory, AmqpAdmin amqpAdmin, RabbitModulePropertys rabbitModulePropertys, RabbitTemplate rabbitTemplate) {
//        return new RabbitMqDynamicInitializer(connectionFactory, amqpAdmin, rabbitModulePropertys, rabbitTemplate);
//    }

    /**
     * 生产者配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);

        /**
         * 1、消息发送回调
         */
        // 设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        // 确认消息送到交换机(Exchange)回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.debug("[确认消息送到交换机(Exchange)回调] 是否成功:[{}] 数据：[{}] 异常：[{}]", ack, JSONUtil.toJsonStr(correlationData), cause);
        });

        // 确认消息送到队列(Queue)回调 -- 只有在出现错误时才回调，延时队列也会触发！
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.error("[确认消息送到队列(Queue)回调] 返回信息：[{}]", JSONUtil.toJsonStr(returnedMessage));
        });

        /**
         * 2、配置自定义消息转换器
         * rabbitmq默认的消息转换器 {@link org.springframework.amqp.support.converter.SimpleMessageConverter}
         */
        rabbitTemplate.setMessageConverter(new MyMessageConverter());
        // json消息转换器
        // rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }


    /**
     * 消费者配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        /**
         * 配置自定义消息转换器
         * rabbitmq默认的消息转换器 {@link org.springframework.amqp.support.converter.SimpleMessageConverter}
         */
        factory.setMessageConverter(new MyMessageConverter());
        // json消息转换器
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }


}
