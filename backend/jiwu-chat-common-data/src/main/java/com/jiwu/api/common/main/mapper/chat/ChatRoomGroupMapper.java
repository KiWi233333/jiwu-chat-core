package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRoomGroupMapper extends BaseMapper<ChatRoomGroup>, MPJBaseMapper<ChatRoomGroup> {

}