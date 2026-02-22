package com.jiwu.api.common.main.cache.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.cache.AbstractRedisStringCache;
import com.jiwu.api.common.main.mapper.chat.ChatRoomSelfMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:  单聊用户信息缓存
 * Date: 2023-06-10
 */
@Component
public class ChatRoomSelfCache extends AbstractRedisStringCache<Long, ChatRoomSelf> {
    @Autowired
    private ChatRoomSelfMapper chatRoomSelfMapper;
    public static final String CHAT_ROOM_SELF_INFO = "chat:group:self:info:%d";

    @Override
    protected String getKey(Long groupId) {
        return RedisKeyUtil.getKey(CHAT_ROOM_SELF_INFO, groupId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, ChatRoomSelf> load(List<Long> roomIds) {
        List<ChatRoomSelf> roomGroups = chatRoomSelfMapper.selectList(new LambdaQueryWrapper<ChatRoomSelf>()
                .in(ChatRoomSelf::getRoomId, roomIds));
        return roomGroups.stream().collect(Collectors.toMap(ChatRoomSelf::getRoomId, Function.identity()));
    }
}
