package com.jiwu.api.chat.service.adapter;

import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatContact;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatMessageReadVO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomGroupVO;
import com.jiwu.api.common.main.dto.chat.vo.RoomGroupExtJsonVO;
import com.jiwu.api.common.main.dto.chat.req.RoomGroupExtJson;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 房间模块适配器
 */
public class ChatRoomAdapter {

    /**
     * 构建已读会话（用户列表）
     *
     * @param list 会话
     * @return 会话列表
     */
    public static List<ChatMessageReadVO> buildReadVO(List<ChatContact> list) {
        return list.stream().map(contact -> {
            ChatMessageReadVO resp = new ChatMessageReadVO();
            resp.setUid(contact.getUserId());
            return resp;
        }).collect(Collectors.toList());
    }


    public static List<ChatGroupMember> buildGroupMemberBatch(List<String> uidList, Long groupId) {
        return uidList.stream()
                .distinct()
                .map(uid -> {
                    ChatGroupMember member = new ChatGroupMember();
                    member.setRole(GroupRoleEnum.MEMBER.getType());
                    member.setUserId(uid);
                    member.setGroupId(groupId);
                    return member;
                }).collect(Collectors.toList());
    }

    public static ChatMessageDTO buildGroupAddMessage(ChatRoomGroup groupRoom, User inviter, Map<String, User> member) {
        ChatMessageDTO chatMessageReq = new ChatMessageDTO();
        chatMessageReq.setRoomId(groupRoom.getRoomId());
        chatMessageReq.setMsgType(MessageTypeEnum.SYSTEM.getType());
        StringBuilder strB = new StringBuilder();
        strB.append("\"")
                .append(inviter.getNickname())
                .append("\"")
                .append("邀请")
                .append(member.values().stream().map(u -> "\"" + u.getNickname() + "\"").collect(Collectors.joining(",")))
                .append("加入群聊");
        chatMessageReq.setContent(strB.toString()); // 内容不能为空
        chatMessageReq.setBody(strB.toString());
        return chatMessageReq;
    }

    public static List<ChatRoomGroupVO> buildRoomGroupVO(List<ChatGroupMember> list, List<ChatRoomGroup> roomGroups) {
        // 将 roomGroups 转换为 Map，key 为 roomId，value 为 ChatRoomGroup
        Map<Long, ChatRoomGroup> groupMap = roomGroups.stream()
                .collect(Collectors.toMap(ChatRoomGroup::getId, Function.identity()));
        return list.stream()
                .map(member -> {
                    if (member == null)
                        return null;
                    ChatRoomGroup group = groupMap.get(member.getGroupId());
                    if (group == null)
                        return null;
                    return chatRoomGroupVOBuildVO(RoomTypeEnum.GROUP,group, member);
                })
                .filter(Objects::nonNull) // 过滤掉 null 值
                .collect(Collectors.toList());
    }


    public static ChatRoomGroupVO chatRoomGroupVOBuildVO(RoomTypeEnum type, ChatRoomGroup roomGroup, ChatGroupMember member) {
        return new ChatRoomGroupVO()
                .setId(roomGroup.getId())
                .setName(roomGroup.getName())
                .setDetail(ChatRoomAdapter.buildExtra(roomGroup.getExtJson()))
                .setAvatar(roomGroup.getAvatar())
                .setDeleteStatus(roomGroup.getDeleteStatus())
                .setUpdateTime(roomGroup.getUpdateTime())
                .setCreateTime(roomGroup.getCreateTime())
                .setRoomId(roomGroup.getRoomId())
                .setRole(member != null ? member.getRole() : null)
                .setJoinTime(member != null ? member.getCreateTime() : null)
                .setMemberUpdateTime(member != null ? member.getUpdateTime() : null)
                ;
    }

    public static RoomGroupExtJsonVO buildExtra(RoomGroupExtJson extJson) {
        if (extJson == null) {
            return new RoomGroupExtJsonVO();
        }
        // 显式拷贝字段，避免 DTO 与 VO 的 JSON 键名不一致（invite_permission vs invitePermission）导致 VO 字段为 null
        return new RoomGroupExtJsonVO()
            .setNotice(extJson.getNotice())
            .setInvitePermission(extJson.getInvitePermission());
    }
}
