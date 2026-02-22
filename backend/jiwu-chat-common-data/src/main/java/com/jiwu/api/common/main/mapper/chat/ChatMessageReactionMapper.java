package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessageReaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表情反应 Mapper
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Mapper
public interface ChatMessageReactionMapper extends BaseMapper<ChatMessageReaction>, MPJBaseMapper<ChatMessageReaction> {
}
