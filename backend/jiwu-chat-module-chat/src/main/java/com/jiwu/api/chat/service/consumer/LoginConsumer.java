//package com.jiwu.api.core.chat.service.consumer;
//
//import common.util.other.common.com.jiwu.api.JacksonUtil;
//import com.jiwu.api.chat.common.dto.ws.LoginMessageDTO;
//import constant.common.chat.core.com.jiwu.api.ChatMqConstant;
//import service.chat.core.com.jiwu.api.WebSocketService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.ExchangeTypes;
//import org.springframework.amqp.rabbit.annotation.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
///**
// * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
// */
//@Slf4j
//@Component
//@RabbitListener(bindings = @QueueBinding(
//        exchange = @Exchange(value = ChatMqConstant.DIRECT_CHAT_EXCHANGE, durable = "true", type = ExchangeTypes.DIRECT),
//        value = @Queue(value = ChatMqConstant.DIRECT_LOGIN_PUSH_CHAT_QUEUE, durable = "true"),
//        key = ChatMqConstant.DIRECT_LOGIN_PUSH_KEY
//))
//public class LoginConsumer {
//
//    @Autowired
//    private WebSocketService webSocketService;
//
//    /**
//     * 发送消息监听
//     *
//     * @param data 消息体
//     */
//    @RabbitHandler
//    public void onWsSendMsgChatConsumer(LoginMessageDTO data) {
//        if (data == null) {
//            return;
//        }
//        doSendAndPush(data);
//    }
//
//    private void doSendAndPush(LoginMessageDTO data) {
//        log.info("登录聊天系统uid:{}", data.getUid());
//       //尝试登录
//        webSocketService.scanLoginSuccess(data.getUid());
//    }
//
//
//    /**
//     * 发送消息监听
//     *
//     * @param msg 消息体
//     */
//    @RabbitHandler
//    public void onWsSendMsgChatConsumerByStr(String msg) {
//        log.info("收到消息 String:{}", msg);
//        try {
//            LoginMessageDTO dto = JacksonUtil.parseJSON(msg, LoginMessageDTO.class);
//            doSendAndPush(dto);
//        } catch (Exception e) {
//            log.error("发送失败 :{}", e.getMessage());
//        }
//    }
//
//}
//
