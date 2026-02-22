package com.jiwu.api.common.main.cache.chat;

import com.jiwu.api.common.cache.AbstractRedisStringCache;
import com.jiwu.api.common.main.mapper.chat.ChatRoomMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 房间基本信息的缓存
 */
@Component
public class ChatRoomCache extends AbstractRedisStringCache<Long, ChatRoom> {
    @Autowired
    private ChatRoomMapper roomMapper;

    public static final String CHAT_ROOM_INFO_LIST = "chat:room:info:list:%d";

    @Override
    protected String getKey(Long roomId) {
        return RedisKeyUtil.getKey(CHAT_ROOM_INFO_LIST, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, ChatRoom> load(List<Long> roomIds) {
        List<ChatRoom> rooms = roomMapper.selectBatchIds(roomIds);
        return rooms.stream().collect(Collectors.toMap(ChatRoom::getId, Function.identity()));
    }
}
