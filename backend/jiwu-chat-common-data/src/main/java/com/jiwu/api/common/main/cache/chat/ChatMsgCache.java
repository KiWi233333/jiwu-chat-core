package com.jiwu.api.common.main.cache.chat;

import com.jiwu.api.common.main.dao.chat.ChatMessageDAO;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Description: 消息相关缓存
 * Date: 2023-03-27
 */
@Component
public class ChatMsgCache {

    @Resource
    private ChatMessageDAO messageDAO;
    public static final String $CHAT_CHAT_MSG = "chat:msg:";

    @Cacheable(cacheNames = $CHAT_CHAT_MSG, key = "#msgId",  unless = "#result == null")
    public ChatMessage getMsg(Long msgId) {
        return messageDAO.getById(msgId);
    }

    @CacheEvict(cacheNames = $CHAT_CHAT_MSG, key = "#msgId")
    public void evictMsg(Long msgId) {
    }
}
