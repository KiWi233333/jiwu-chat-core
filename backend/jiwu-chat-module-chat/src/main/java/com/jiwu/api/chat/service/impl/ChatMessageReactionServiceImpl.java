package com.jiwu.api.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.jiwu.api.chat.common.cache.ChatMessageReactionCache;
import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.vo.ReactionVO;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.chat.common.vo.ws.WSMsgReaction;
import com.jiwu.api.chat.service.ChatMessageReactionService;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomSelfCache;
import com.jiwu.api.common.main.dao.chat.ChatMessageDAO;
import com.jiwu.api.common.main.dao.chat.ChatMessageReactionDAO;
import com.jiwu.api.common.main.dto.chat.reaction.ReactionToggleDTO;
import com.jiwu.api.common.main.enums.chat.EmojiTypeEnum;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.chat.ChatMessageReaction;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.util.common.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息表情反应 Service 实现
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Slf4j
@Service
public class ChatMessageReactionServiceImpl implements ChatMessageReactionService {

    @Resource
    private ChatMessageReactionDAO chatMessageReactionDAO;

    @Resource
    private ChatMessageReactionCache chatMessageReactionCache;

    @Resource
    private ChatMessageDAO chatMessageDAO;

    @Resource
    private ChatRoomCache chatRoomCache;

    @Resource
    private ChatRoomSelfCache chatRoomSelfCache;

    @Resource
    private ChatGroupMemberCache chatGroupMemberCache;

    @Resource
    private PushService pushService;

    /**
     * 添加/取消操作标识
     */
    private static final int ACTION_ADD = 1;
    private static final int ACTION_REMOVE = 0;

    @Override
    public WSMsgReaction toggleReaction(Long roomId, ReactionToggleDTO dto, String userId) {
        Long msgId = dto.getMsgId();
        String emojiType = dto.getEmojiType();

        // 1、校验
        checkToggle(roomId, msgId, emojiType, userId);

        // 2、Toggle：尝试 INSERT，捕获 DuplicateKey 则 DELETE
        int action;
        try {
            ChatMessageReaction reaction = new ChatMessageReaction()
                    .setMsgId(msgId)
                    .setRoomId(roomId)
                    .setUserId(userId)
                    .setEmojiType(emojiType);
            chatMessageReactionDAO.save(reaction);
            action = ACTION_ADD;
            // 更新缓存
            chatMessageReactionCache.onReactionAdded(msgId, emojiType, userId);
        } catch (DuplicateKeyException e) {
            // 已存在则删除（取消反应）
            chatMessageReactionDAO.removeByUnique(msgId, userId, emojiType);
            action = ACTION_REMOVE;
            // 更新缓存
            chatMessageReactionCache.onReactionRemoved(msgId, emojiType, userId);
        }

        // 3、查询最新 reaction 聚合
        List<ReactionVO> reactions = chatMessageReactionCache.getReactions(msgId);

        // 4、构建推送 VO
        WSMsgReaction wsMsg = WSMsgReaction.builder()
                .msgId(msgId)
                .roomId(roomId)
                .emojiType(emojiType)
                .userId(userId)
                .action(action)
                .reactions(reactions)
                .build();

        // 5、推送给房间成员
        pushToRoomMembers(roomId, wsMsg, userId);

        return wsMsg;
    }

    @Override
    public Map<Long, List<ReactionVO>> batchGetReactions(Collection<Long> msgIds, String currentUserId) {
        if (CollUtil.isEmpty(msgIds)) {
            return Collections.emptyMap();
        }

        // 1、从缓存批量获取 reaction 聚合（不含 isCurrentUser）
        Map<Long, List<ReactionVO>> reactionMap = chatMessageReactionCache.batchGetReactions(msgIds);

        // 2、批量 SQL 查询当前用户的 reaction 记录
        Set<Long> msgIdsWithReaction = reactionMap.entrySet().stream()
                .filter(e -> CollUtil.isNotEmpty(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(msgIdsWithReaction)) {
            return reactionMap;
        }

        // 查询当前用户在这些消息上的 reaction
        List<ChatMessageReaction> userReactions = chatMessageReactionDAO
                .listByMsgIdsAndUserId(msgIdsWithReaction, currentUserId);
        Set<String> userReactionKeys = userReactions.stream()
                .map(r -> r.getMsgId() + ":" + r.getEmojiType())
                .collect(Collectors.toSet());

        // 3、填充 isCurrentUser 标记
        for (Map.Entry<Long, List<ReactionVO>> entry : reactionMap.entrySet()) {
            Long msgId = entry.getKey();
            for (ReactionVO vo : entry.getValue()) {
                vo.setIsCurrentUser(userReactionKeys.contains(msgId + ":" + vo.getEmojiType()));
            }
        }

        return reactionMap;
    }

    @Override
    public List<ReactionVO> getReactionDetail(Long msgId, String currentUserId) {
        // 从 DB 查询全量数据（不走缓存截断）
        List<ChatMessageReaction> reactions = chatMessageReactionDAO.listByMsgId(msgId);
        if (CollUtil.isEmpty(reactions)) {
            return Collections.emptyList();
        }

        // 当前用户的 reaction 集合
        Set<String> currentUserEmojis = reactions.stream()
                .filter(r -> r.getUserId().equals(currentUserId))
                .map(ChatMessageReaction::getEmojiType)
                .collect(Collectors.toSet());

        // 按 emojiType 分组聚合
        Map<String, List<ChatMessageReaction>> grouped = reactions.stream()
                .collect(Collectors.groupingBy(ChatMessageReaction::getEmojiType,
                        LinkedHashMap::new, Collectors.toList()));

        List<ReactionVO> result = new ArrayList<>();
        for (Map.Entry<String, List<ChatMessageReaction>> entry : grouped.entrySet()) {
            String emojiType = entry.getKey();
            List<ChatMessageReaction> emojiReactions = entry.getValue();
            result.add(ReactionVO.builder()
                    .emojiType(emojiType)
                    .count(emojiReactions.size())
                    .userIds(emojiReactions.stream()
                            .map(ChatMessageReaction::getUserId)
                            .collect(Collectors.toList()))
                    .isCurrentUser(currentUserEmojis.contains(emojiType))
                    .build());
        }
        return result;
    }

    // ===================== 校验 =====================

    private void checkToggle(Long roomId, Long msgId, String emojiType, String userId) {
        // 校验 emoji 编码合法性
        AssertUtil.isTrue(EmojiTypeEnum.isValid(emojiType), "不支持的表情类型！");

        // 校验房间
        ChatRoom room = chatRoomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间不存在！");
        AssertUtil.isTrue(room.isRoomFriend() || room.isRoomGroup(), "该房间类型不支持表情反应！");

        // 校验消息
        ChatMessage message = chatMessageDAO.getById(msgId);
        AssertUtil.isNotEmpty(message, "消息不存在！");
        AssertUtil.equal(message.getRoomId(), roomId, "消息不属于该房间！");
        AssertUtil.equal(MessageStatusEnum.NORMAL.getStatus(), message.getStatus(), "消息已被删除！");
        AssertUtil.notEqual(MessageTypeEnum.RECALL.getType(), message.getType(), "已撤回的消息不支持表情反应！");
        AssertUtil.notEqual(MessageTypeEnum.DEL_MSG.getType(), message.getType(), "已删除的消息不支持表情反应！");
    }

    // ===================== 推送 =====================

    private void pushToRoomMembers(Long roomId, WSMsgReaction wsMsg, String operatorUid) {
        ChatRoom room = chatRoomCache.get(roomId);
        if (room == null) {
            return;
        }

        WsBaseVO<WSMsgReaction> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.MSG_REACTION.getType());
        vo.setData(wsMsg);

        List<String> uidList;
        if (room.isRoomFriend()) {
            // 单聊：推送给双方
            ChatRoomSelf roomSelf = chatRoomSelfCache.get(roomId);
            uidList = Arrays.asList(roomSelf.getUid1(), roomSelf.getUid2());
        } else if (room.isRoomGroup()) {
            // 群聊：推送给群成员
            uidList = chatGroupMemberCache.getMemberUidList(roomId);
        } else {
            return;
        }

        if (CollUtil.isNotEmpty(uidList)) {
            pushService.sendPushMsg(vo, uidList);
        }
    }
}
