package com.jiwu.api.chat.service.adapter;

import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatUserFriend;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.common.main.vo.chat.ChatMemberListVO;
import com.jiwu.api.chat.common.vo.ws.WSMemberChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description: 成员适配器
 */
@Component
@Slf4j
public class ChatMemberAdapter {
    @Autowired
    private UserCache userCache;

    // 构建群聊成员列表信息
    public static List<ChatMemberVO> buildMember(List<User> list) {
        return list.stream().map(a -> {
            return ChatMemberVO.build(a);
        }).collect(Collectors.toList());
    }
    // 构建群聊成员列表信息
    public static List<ChatMemberVO> buildMember(List<ChatUserFriend> list, List<User> userList) {
        Map<String, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        return list.stream().map(userFriend -> {
            ChatMemberVO resp = new ChatMemberVO();
            resp.setUserId(userFriend.getFriendUid());
            User user = userMap.get(userFriend.getFriendUid());
            if (Objects.nonNull(user)) {
                resp.setActiveStatus(user.getActiveStatus());
                resp.setAvatar(user.getAvatar());
                resp.setUsername(user.getUsername());
                resp.setNickName(user.getNickname());
                resp.setLastOptTime(user.getLastLoginTime());
            }
            return resp;
        }).collect(Collectors.toList());
    }

    public static List<ChatMemberListVO> buildMemberList(List<User> memberList) {
        return memberList.stream()
                .map(a -> {
                    ChatMemberListVO resp = new ChatMemberListVO();
                    BeanUtils.copyProperties(a, resp);
                    resp.setUserId(a.getId());
                    return resp;
                }).collect(Collectors.toList());
    }

    public static List<ChatMemberListVO> buildMemberList(Map<Long, User> batch) {
        return buildMemberList(new ArrayList<>(batch.values()));
    }

    public static List<ChatGroupMember> buildMemberAdd(Long groupId, List<String> waitAddUidList) {
        return waitAddUidList.stream().map(a -> {
            ChatGroupMember member = new ChatGroupMember();
            member.setGroupId(groupId);
            member.setUserId(a);
            member.setRole(GroupRoleEnum.MEMBER.getType());
            return member;
        }).collect(Collectors.toList());
    }

    public static WsBaseVO<WSMemberChange> buildMemberAddWS(Long roomId, User user) {
        WsBaseVO<WSMemberChange> wsBaseResp = new WsBaseVO<>();
        wsBaseResp.setType(WsRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setActiveStatus(user.getActiveStatus());
        wsMemberChange.setLastOptTime(user.getLastLoginTime());
        wsMemberChange.setUid(user.getId());
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(WSMemberChange.CHANGE_TYPE_ADD);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }

    public static WsBaseVO<WSMemberChange> buildMemberRemoveWS(Long roomId, String uid) {
        WsBaseVO<WSMemberChange> wsBaseResp = new WsBaseVO<>();
        wsBaseResp.setType(WsRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setUid(uid);
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(WSMemberChange.CHANGE_TYPE_REMOVE);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }
    public static WsBaseVO<WSMemberChange> buildGroupDeleteWs(Long roomId, String uid) {
        WsBaseVO<WSMemberChange> wsBaseResp = new WsBaseVO<>();
        wsBaseResp.setType(WsRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setUid(uid);
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(WSMemberChange.CHANGE_TYPE_GROUP_DEL);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }
}
