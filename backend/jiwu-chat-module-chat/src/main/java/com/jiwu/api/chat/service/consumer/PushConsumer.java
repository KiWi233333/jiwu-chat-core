package com.jiwu.api.chat.service.consumer;

import com.jiwu.api.chat.common.enums.WSPushTypeEnum;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.main.constant.chat.ChatMqConstant;
import com.jiwu.api.chat.common.dto.PushMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;


/**
 * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
 */
@Slf4j
@RabbitListener(bindings = @QueueBinding(
        exchange = @Exchange(value = ChatMqConstant.DIRECT_CHAT_EXCHANGE, durable = "true", type = ExchangeTypes.DIRECT),
        value = @Queue(value = ChatMqConstant.DIRECT_PUSH_CHAT_QUEUE, durable = "true"),
        key = ChatMqConstant.DIRECT_PUSH_CHAT_KEY
))
@Component
public class PushConsumer {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 发送消息监听 （默认）
     *
     * @param dto 消息体
     */
    @RabbitHandler(isDefault = true)
    public void onWsGroupMsgChatConsumer(@Payload PushMessageDTO dto) {
        log.info("广播推送消息obj：{}", dto.getWsBaseMsg().getData());
        doPushAllOline(dto);
    }

    /**
     * 发送消息监听
     *
     * @param msg 消息体
     */
    @RabbitHandler
    public void onWsGroupMsgChatConsumerByStr(String msg) {
//        log.info("广播推送消息Str：{}",  dto.getWsBaseMsg().getData());
        PushMessageDTO dto = JacksonUtil.parseJSON(msg, PushMessageDTO.class);
        if (dto != null)
            doPushAllOline(dto);
    }

    private void doPushAllOline(PushMessageDTO dto) {
        WSPushTypeEnum wsPushTypeEnum = WSPushTypeEnum.of(dto.getPushType());
        switch (wsPushTypeEnum) {
            case USER:
                dto.getUidList().forEach(uid -> webSocketService.sendToUid(dto.getWsBaseMsg(), uid));
                break;
            case ALL:
                webSocketService.sendToAllOnline(dto.getWsBaseMsg(), null);
                break;
        }
    }


}

