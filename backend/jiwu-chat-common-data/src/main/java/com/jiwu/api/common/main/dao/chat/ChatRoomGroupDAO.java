package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.mapper.chat.ChatRoomGroupMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ChatRoomGroupDAO extends ServiceImpl<ChatRoomGroupMapper, ChatRoomGroup> {

    public ChatRoomGroup getByRoomId(Long roomId) {
        return lambdaQuery()
                .eq(ChatRoomGroup::getRoomId, roomId)
                .one();
    }

    public boolean removeByRoomId(Long roomId) {
        return lambdaUpdate()
                .eq(ChatRoomGroup::getRoomId, roomId)
                .remove();
    }

    public List<ChatRoomGroup> getListByIds(List<Long> groupIds) {
        return lambdaQuery()
                .in(ChatRoomGroup::getId, groupIds)
                .list();
    }
}
