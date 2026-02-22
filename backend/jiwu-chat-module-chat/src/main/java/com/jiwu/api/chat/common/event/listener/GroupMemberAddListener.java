package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.dao.sys.UserDAO;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.chat.common.event.GroupMemberAddEvent;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.adapter.ChatMemberAdapter;
import com.jiwu.api.chat.service.adapter.ChatRoomAdapter;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.cache.user.UserInfoCache;
import com.jiwu.api.chat.common.vo.ws.WSMemberChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加群成员监听器
 *
 */
@Slf4j
@Component
public class GroupMemberAddListener {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private UserDAO userDao;
    @Autowired
    private ChatGroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;


    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<ChatGroupMember> memberList = event.getMemberList();
        ChatRoomGroup roomGroup = event.getRoomGroup();
        String inviteUid = event.getInviteUid(); // 发起拉起人
        User user = userInfoCache.get(inviteUid);
        List<String> uidList = memberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
        ChatMessageDTO chatMessageReq = ChatRoomAdapter.buildGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, inviteUid);
    }

    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<ChatGroupMember> memberList = event.getMemberList();
        ChatRoomGroup roomGroup = event.getRoomGroup();
        List<String> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<String> uidList = memberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WsBaseVO<WSMemberChange> ws = ChatMemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}
