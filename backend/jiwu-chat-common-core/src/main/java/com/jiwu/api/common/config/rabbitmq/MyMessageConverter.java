package com.jiwu.api.common.config.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJackson2MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * <p> 自定义消息转换器 </p>
 *
 * @description 可参考rabbitmq默认的消息转换器 {@link org.springframework.amqp.support.converter.SimpleMessageConverter}
 */
@Slf4j
public class MyMessageConverter implements MessageConverter {

    @NotNull
    @Override
    public Message toMessage(@NotNull Object object, @NotNull MessageProperties messageProperties) {
        // 生产者发送消息转换
        String msg = "";
        if (object instanceof String) {
            msg = String.valueOf(object);
        } else {
            msg = JSONUtil.toJsonStr(object);
        }
        return new Message(msg.getBytes(), messageProperties);
    }

    @NotNull
    @Override
    public Object fromMessage(@NotNull Message message) {
        try {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            Type inferredArgumentType = message.getMessageProperties().getInferredArgumentType();
            if (inferredArgumentType == null) {
                return msg;
            }
            String targetClassName = inferredArgumentType.getTypeName();
            if (String.class.getName().equals(targetClassName)) {
                return msg;
            } else {
                // 返回反序列化后的对象 tips：可以有效解决由于实体类字段变更或类名修改等原因导致消费者序列化问题，很nice！！！
                Class<?> targetClass = Class.forName(targetClassName);
                return JSONUtil.toBean(msg, targetClass);
                /**
                 * 消费者也可配置使用 {@link Jackson2JsonMessageConverter} 转换器
                 * 最终走 {@link AbstractJackson2MessageConverter#fromMessage(Message, Object)}
                 */
//                return new Jackson2JsonMessageConverter().fromMessage(message);
            }
        } catch (Exception e) {
            log.error("[RabbitMQ] 消息转换器处理消息异常：", e);
        }
        return null;
    }

}
