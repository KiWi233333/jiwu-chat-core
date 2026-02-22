package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.chat.common.dto.ChatMsgRecallDTO;
import com.jiwu.api.chat.common.event.ChatMessageRecallEvent;
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
public class ChatMsgRecallListener {
    @Resource
    private ChatMsgCache msgCache;
    @Resource
    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = ChatMessageRecallEvent.class, fallbackExecution = true)
    public void evictMsg(ChatMessageRecallEvent event) {
        ChatMsgRecallDTO recallDTO = event.getRecallDTO();
        msgCache.evictMsg(recallDTO.getMsgId());
    }

    @Async
    @TransactionalEventListener(classes = ChatMessageRecallEvent.class, fallbackExecution = true)
    public void sendToAll(ChatMessageRecallEvent event) {
        pushService.sendPushMsg(WsAdapter.buildMsgRecall(event.getRecallDTO()));
    }

}
