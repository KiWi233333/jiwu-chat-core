package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.mapper.chat.ChatMessageReactionMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessageReaction;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 消息表情反应 DAO
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Service
public class ChatMessageReactionDAO extends ServiceImpl<ChatMessageReactionMapper, ChatMessageReaction> {

    /**
     * 批量查询指定消息的所有反应记录
     *
     * @param msgIds 消息ID集合
     * @return 反应记录列表
     */
    public List<ChatMessageReaction> listByMsgIds(Collection<Long> msgIds) {
        return lambdaQuery()
                .in(ChatMessageReaction::getMsgId, msgIds)
                .orderByAsc(ChatMessageReaction::getCreateTime)
                .list();
    }

    /**
     * 查询当前用户在指定消息上的所有反应
     *
     * @param msgIds 消息ID集合
     * @param userId 用户ID
     * @return 反应记录列表
     */
    public List<ChatMessageReaction> listByMsgIdsAndUserId(Collection<Long> msgIds, String userId) {
        return lambdaQuery()
                .in(ChatMessageReaction::getMsgId, msgIds)
                .eq(ChatMessageReaction::getUserId, userId)
                .list();
    }

    /**
     * 查询单条消息的所有反应记录
     *
     * @param msgId 消息ID
     * @return 反应记录列表
     */
    public List<ChatMessageReaction> listByMsgId(Long msgId) {
        return lambdaQuery()
                .eq(ChatMessageReaction::getMsgId, msgId)
                .orderByAsc(ChatMessageReaction::getCreateTime)
                .list();
    }

    /**
     * 根据唯一条件查询单条反应
     *
     * @param msgId     消息ID
     * @param userId    用户ID
     * @param emojiType emoji编码
     * @return 反应记录（可能为null）
     */
    public ChatMessageReaction getByUnique(Long msgId, String userId, String emojiType) {
        return lambdaQuery()
                .eq(ChatMessageReaction::getMsgId, msgId)
                .eq(ChatMessageReaction::getUserId, userId)
                .eq(ChatMessageReaction::getEmojiType, emojiType)
                .one();
    }

    /**
     * 根据唯一条件删除反应
     *
     * @param msgId     消息ID
     * @param userId    用户ID
     * @param emojiType emoji编码
     * @return 是否删除成功
     */
    public boolean removeByUnique(Long msgId, String userId, String emojiType) {
        return lambdaUpdate()
                .eq(ChatMessageReaction::getMsgId, msgId)
                .eq(ChatMessageReaction::getUserId, userId)
                .eq(ChatMessageReaction::getEmojiType, emojiType)
                .remove();
    }
}
