package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.common.main.event.user.UserRegisterEvent;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.common.main.constant.chat.ChatGroupConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户注册监听器
 * 监听用户注册事件，初始化聊天室
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserRegisterListener {
    @Autowired
    private ChatService chatService;

    @Async
    @EventListener(classes = UserRegisterEvent.class)
    public void initChatRoom(UserRegisterEvent event) {
        try {
            // 初始化聊天室：将用户加入全员群并标记消息已读
            final Long msgReadStatus = chatService.msgRead(event.getUser().getId(), ChatGroupConstant.HOT_ROOM_TOTAL_ID);
            log.info("用户注册成功，初始化聊天室完成，userId={}, status={}", event.getUser().getId(), msgReadStatus);
        } catch (Exception e) {
            log.error("用户注册后初始化聊天室失败，userId={}", event.getUser().getId(), e);
            // 不抛出异常，避免影响用户注册流程
        }
    }

}
