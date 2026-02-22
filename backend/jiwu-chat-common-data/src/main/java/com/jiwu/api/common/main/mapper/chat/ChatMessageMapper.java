package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage>, MPJBaseMapper<ChatMessage> {


    @Select("SELECT * FROM chat_message WHERE (room_id = #{roomId} AND status = ${status}) ORDER BY id DESC")
    @Options(resultSetType = ResultSetType.SCROLL_INSENSITIVE, fetchSize = Integer.MIN_VALUE)
    @ResultType(ChatMessage.class)
    Cursor<ChatMessage> getCursorList(@Param("roomId") Long roomId, @Param("status") Integer status);

}