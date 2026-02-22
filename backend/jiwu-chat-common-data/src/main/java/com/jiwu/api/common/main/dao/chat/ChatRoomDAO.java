package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomSelfCache;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.mapper.chat.ChatRoomMapper;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 房间DAO
 *
 * @className: ChatRoomSelfDAO
 * @author: Kiwi23333
 * @description:
 * @date: 2023/12/26 11:47
 */
@Service
public class ChatRoomDAO extends ServiceImpl<ChatRoomMapper, ChatRoom> {

    @Autowired
    ChatRoomSelfCache chatRoomSelfCache;
    @Autowired
    ChatGroupMemberCache chatGroupMemberCache;
    @Autowired
    ChatGroupMemberDAO chatGroupMemberDAO;

    public boolean isExistRoomByUid(Long roomId, String userId) {
        ChatRoomSelf self = chatRoomSelfCache.get(roomId);
        if (self != null) {
            return self.getStatus().equals(NormalOrNoEnum.NORMAL.getStatus()) && self.getRoomKey().contains(userId);
        }
        return chatGroupMemberDAO.isGroupShip(roomId, userId);
    }

}
