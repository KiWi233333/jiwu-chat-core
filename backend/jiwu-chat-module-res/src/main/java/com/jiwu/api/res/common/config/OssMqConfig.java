package com.jiwu.api.res.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OssMqConfig {

    /*---------------------------文件oss延迟--------------------------*/
    /**
     * 文件上传超时
     */
    public static final String OSS_DELETE_DELAY_EXCHANGE = "oss_delete_delay_exchange";
    public static final String OSS_DELETE_DELAY_QUEUE = "oss_delete_delay_queue";
    /**
     * 延迟消息交换机（文件上传超时）
     */
    @Bean
    public CustomExchange ossDeleteExchange() {
        Map<String, Object> args = new HashMap<>();
        // 设置自定义交换机消息的类型，direct类似direct交换机消息的模式，也可以传递topic、fanout,或者其它插件提供的自定义的交换机类型
        args.put("x-delayed-type", "direct"); // 延迟交换机
        return new CustomExchange(OSS_DELETE_DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * 延迟队列（文件上传超时）
     */
    @Bean
    public Queue ossDeleteDelayedQueue() {
        return new Queue(OSS_DELETE_DELAY_QUEUE, true, false, false);
    }

    /**
     * 延迟队列绑定交换机
     */
    @Bean
    public Binding bindingOssDeleteDelay() {
        return BindingBuilder.bind(ossDeleteDelayedQueue()).to(ossDeleteExchange()).with(OSS_DELETE_DELAY_QUEUE).noargs();
    }
}
