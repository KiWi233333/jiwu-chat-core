package com.jiwu.api.common.main.cache.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.cache.AbstractRedisStringCache;
import com.jiwu.api.common.main.mapper.chat.ChatRoomGroupMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 群组基本信息的缓存
 */
@Component
public class ChatRoomGroupCache extends AbstractRedisStringCache<Long, ChatRoomGroup> {
    @Autowired
    private ChatRoomGroupMapper roomGroupMapper;
    public static final String CHAT_ROOM_GROUP_INFO = "chat:room:group:info:%d";

    @Override
    protected String getKey(Long roomId) {
        return RedisKeyUtil.getKey(CHAT_ROOM_GROUP_INFO, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, ChatRoomGroup> load(List<Long> roomIds) {
        List<ChatRoomGroup> roomGroups = roomGroupMapper.selectList(new LambdaQueryWrapper<ChatRoomGroup>()
                .in(ChatRoomGroup::getRoomId, roomIds));
        return roomGroups.stream().collect(Collectors.toMap(ChatRoomGroup::getRoomId, Function.identity()));
    }
}
