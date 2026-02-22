package com.jiwu.api.chat.service.adapter;


import com.jiwu.api.common.main.enums.chat.FriendApplyStatusEnum;
import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import com.jiwu.api.common.main.pojo.chat.ChatUserFriend;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.dto.chat.friend.ChatUserFriendApplyDTO;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendApplyVO;
import com.jiwu.api.common.main.vo.chat.ChatUserFriendVO;
import com.jiwu.api.common.main.enums.chat.ApplyReadStatusEnum;
import com.jiwu.api.common.main.enums.chat.ApplyTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友申请适配器
 */
public class ChatUserFriendAdapter {


    public static ChatUserApply buildFriendApply(String uid, ChatUserFriendApplyDTO request) {
        ChatUserApply userApplyNew = new ChatUserApply();
        userApplyNew.setUserId(uid);
        userApplyNew.setMsg(request.getMsg());
        userApplyNew.setType(ApplyTypeEnum.ADD_FRIEND.getCode());
        userApplyNew.setTargetId(request.getTargetUid());
        userApplyNew.setStatus(FriendApplyStatusEnum.WAIT_APPROVAL.getCode());
        userApplyNew.setReadStatus(ApplyReadStatusEnum.UNREAD.getCode()); // 未读
        return userApplyNew;
    }

    public static List<ChatUserFriendApplyVO> buildFriendApplyList(List<ChatUserApply> records) {
        return records.stream().map(p -> {
            ChatUserFriendApplyVO vo = new ChatUserFriendApplyVO();
            vo.setUserId(p.getUserId());
            vo.setType(p.getType());
            vo.setApplyId(p.getId());
            vo.setMsg(p.getMsg());
            vo.setUser(new ChatUserFriendApplyVO.User());
            vo.setStatus(p.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    public static List<ChatUserFriendApplyVO> buildFriendApplyList(List<ChatUserApply> records, UserCache userCache) {
        return records.stream().map(p -> {
            ChatUserFriendApplyVO vo = new ChatUserFriendApplyVO();
            vo.setUserId(p.getUserId());
            vo.setType(p.getType());
            vo.setApplyId(p.getId());
            vo.setMsg(p.getMsg());
            vo.setCreateTime(p.getCreateTime());
            User user = userCache.getUserInfo(vo.getUserId());
            vo.setUser(new ChatUserFriendApplyVO.User()
                    .setId(p.getUserId())
                    .setAvatar(user.getAvatar())
                    .setGender(user.getGender())
                    .setSlogan(user.getSlogan())
                    .setNickName(user.getNickname()));
            vo.setStatus(p.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    public static List<ChatUserFriendVO> buildFriend(List<ChatUserFriend> list, List<User> userList) {
        Map<String, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        return list.stream().map(userFriend -> {
            ChatUserFriendVO vo = new ChatUserFriendVO();
            User user = userMap.get(userFriend.getFriendUid());
            vo.setUserId(userFriend.getFriendUid());
            if (user != null) {
                vo.setNickName(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setType(user.getUserType());
                vo.setActiveStatus(user.getActiveStatus());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
