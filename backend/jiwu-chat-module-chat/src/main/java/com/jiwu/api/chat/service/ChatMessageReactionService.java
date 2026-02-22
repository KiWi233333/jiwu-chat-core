package com.jiwu.api.chat.service;

import com.jiwu.api.chat.common.vo.ReactionVO;
import com.jiwu.api.chat.common.vo.ws.WSMsgReaction;
import com.jiwu.api.common.main.dto.chat.reaction.ReactionToggleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 消息表情反应 Service
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
public interface ChatMessageReactionService {

    /**
     * Toggle 添加/取消表情反应
     *
     * @param roomId 房间ID
     * @param dto    请求参数
     * @param userId 操作用户ID
     * @return 推送VO（包含最新的reaction聚合）
     */
    WSMsgReaction toggleReaction(Long roomId, ReactionToggleDTO dto, String userId);

    /**
     * 批量获取消息的 reaction 聚合数据（含当前用户 isCurrentUser 标记）
     *
     * @param msgIds        消息ID集合
     * @param currentUserId 当前用户ID
     * @return msgId → reaction 聚合列表
     */
    Map<Long, List<ReactionVO>> batchGetReactions(Collection<Long> msgIds, String currentUserId);

    /**
     * 获取单条消息的 reaction 详情（全量用户列表）
     *
     * @param msgId         消息ID
     * @param currentUserId 当前用户ID
     * @return reaction 聚合列表（用户列表不截断）
     */
    List<ReactionVO> getReactionDetail(Long msgId, String currentUserId);
}
