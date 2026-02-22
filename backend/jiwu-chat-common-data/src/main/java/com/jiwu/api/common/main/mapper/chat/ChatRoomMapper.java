package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom>, MPJBaseMapper<ChatRoom> {



}