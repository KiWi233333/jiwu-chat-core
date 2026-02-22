package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.chat.common.dto.ChatMsgDeleteDTO;
import com.jiwu.api.chat.common.event.ChatMessageDeleteEvent;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.common.main.cache.chat.ChatMsgCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

/**
 * 消息撤回监听器
 *
 */
@Slf4j
@Component
public class ChatMsgDeleteListener {
    @Resource
    private ChatMsgCache msgCache;
    @Resource
    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = ChatMessageDeleteEvent.class, fallbackExecution = true)
    public void evictMsg(ChatMessageDeleteEvent event) {
        ChatMsgDeleteDTO deleteDTO = event.getDeleteDTO();
        msgCache.evictMsg(deleteDTO.getMsgId());
    }

    @Async
    @TransactionalEventListener(classes = ChatMessageDeleteEvent.class, fallbackExecution = true)
    public void sendToAll(ChatMessageDeleteEvent event) {
        pushService.sendPushMsg(WsAdapter.buildMsgDelete(event.getDeleteDTO()));
    }
}
