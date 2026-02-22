package com.jiwu.api.chat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.dao.chat.ChatGroupMemberDAO;
import com.jiwu.api.common.main.dao.chat.ChatRoomGroupDAO;
import com.jiwu.api.common.main.dao.chat.ChatMessageDAO;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.req.*;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.enums.chat.HotFlagEnum;
import com.jiwu.api.common.main.enums.chat.InvitePermissionEnum;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.chat.common.event.GroupMemberAddEvent;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatGroupRoomService;
import com.jiwu.api.chat.service.ChatRoomService;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.adapter.ChatMemberAdapter;
import com.jiwu.api.chat.service.adapter.ChatRoomAdapter;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomGroupCache;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.chat.common.vo.ws.WSMemberChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 群聊房间业务
 *
 * @className: ChatServiceImpl
 * @author: Kiwi23333
 * @description: 群聊房间业务
 * @date: 2023/12/8 17:16
 */
@Slf4j
@Service
public class ChatGroupRoomServiceImpl implements ChatGroupRoomService {
    @Resource
    private ChatGroupMemberDAO chatGroupMemberDAO;
    @Resource
    private ChatRoomGroupDAO chatRoomGroupDAO;
    @Resource
    private ChatMessageDAO chatMessageDAO;
    @Resource
    private ChatRoomCache roomCache;
    @Resource
    private ChatRoomGroupCache roomGroupCache;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private ChatGroupMemberCache groupMemberCache;
    @Resource
    private PushService pushService;
    @Resource
    private ChatRoomService roomService;
    @Resource
    private ChatService chatService;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserCache userCache;

    /**
     * 添加群聊
     *
     * @param userId 用户id
     * @param dto    参数
     * @return 房间号
     */
    @Override
    public Long addGroup(String userId, InsertRoomGroupDTO dto) {
        // 1、校验是否
        checkUidList(dto.getUidList());
        // 2、创建群聊
        return transactionTemplate.execute(status -> {
            ChatRoomGroup roomGroup = roomService.createGroupRoom(new InsertRoomDTO()
                    .setAvatar(dto.getAvatar())
                    .setUserId(userId));
            // 2、批量保存群成员
            List<ChatGroupMember> groupMembers = ChatRoomAdapter.buildGroupMemberBatch(dto.getUidList(), roomGroup.getId());
            chatGroupMemberDAO.saveBatch(groupMembers);
            // 3、发送邀请加群消息 触发每个人的会话
            applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMembers, userId));
            return roomGroup.getId();
        });
    }

    private void checkUidList(List<String> uidList) {
        AssertUtil.isNotEmpty(uidList, "请选择添加的好友");
        final HashSet<String> uidSet = new HashSet<>(uidList);
        final Map<String, User> userInfoBatch = userCache.getUserInfoBatch(uidSet);
        AssertUtil.isTrue(userInfoBatch != null && userInfoBatch.keySet().size() == uidList.size(), "部分好友不存在，请填入合规参数！");
//        // TODO: 不可私自邀请机器人
//        Set<String> robotUidSet = aiRobotService.getAvailableModels().stream()
//                .map(AiRobotUserVO::getUserId)
//                .collect(Collectors.toSet());
//        // 检查是否有机器人被邀请
//        for (String uid : uidSet) {
//            AssertUtil.isTrue(!robotUidSet.contains(uid), "不能邀请机器人入群！");
//        }
    }

    /**
     * 邀请好友
     *
     * @param uid 用户
     * @param dto 参数
     * @return 影响
     */
    @Override
    public Long addMember(String uid, AddMemberDTO dto) {
        // 1、查看房间
        checkUidList(dto.getUidList()); // 校验
        ChatRoom room = roomCache.get(dto.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        AssertUtil.isFalse(isHotGroup(room), "全员群无需邀请好友");
        ChatRoomGroup roomGroup = roomGroupCache.get(dto.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "房间号有误");
        // 2、查看是否是房间成员
        ChatGroupMember self = chatGroupMemberDAO.getMember(roomGroup.getId(), uid);
        AssertUtil.isNotEmpty(self, "您不是群成员");
        // 3、校验邀请权限
        checkInvitePermission(roomGroup, self);
        // 4、查看房间成员列表
        List<String> memberBatch = chatGroupMemberDAO.getMemberBatch(roomGroup.getId(), dto.getUidList());
        Set<String> existUid = new HashSet<>(memberBatch);
        // 过滤掉已经存在
        List<String> waitAddUidList = dto.getUidList().stream().filter(a -> !existUid.contains(a)).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(waitAddUidList)) {
            return 0L;
        }
        List<ChatGroupMember> groupMembers = ChatMemberAdapter.buildMemberAdd(roomGroup.getId(), waitAddUidList);
        chatGroupMemberDAO.saveBatch(groupMembers);
        // 5、发布构建消息
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMembers, uid));
        return (long) waitAddUidList.size();
    }

    /**
     * 校验邀请权限
     *
     * @param roomGroup 群组
     * @param self      当前成员
      */
    private void checkInvitePermission(ChatRoomGroup roomGroup, ChatGroupMember self) {
        InvitePermissionEnum invitePermission = Optional.ofNullable(roomGroup.getExtJson())
            .map(RoomGroupExtJson::getInvitePermission)
            .orElse(InvitePermissionEnum.ANY);

        Integer selfRole = self.getRole();

        // 0-任意成员可邀请
        if (InvitePermissionEnum.ANY.equals(invitePermission)) {
            return;
        }

        // 1-管理员可邀请
        if (InvitePermissionEnum.ADMIN.equals(invitePermission)) {
            AssertUtil.isTrue(GroupRoleEnum.isAdmin(selfRole), "抱歉，只有管理员和群主可以邀请好友！");
            return;
        }

        // 2-群主可邀请
        if (InvitePermissionEnum.OWNER_ONLY.equals(invitePermission)) {
            AssertUtil.isTrue(GroupRoleEnum.HOME.getType().equals(selfRole), "抱歉，只有群主可以邀请好友！");
            return;
        }
    }

    // 是否热点群聊（大群）
    private boolean isHotGroup(ChatRoom room) {
        return HotFlagEnum.YES.getType().equals(room.getHotFlag());
    }

    /**
     * 删除群成员
     *
     * @param uid      操作人用户id
     * @param roomId   房间id
     * @param targetId 移除id
     * @return 影响
     */
    @Override
    public Integer deleteMember(String uid, Long roomId, String targetId) {
        ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间不存在！");
        AssertUtil.isFalse(room.isHotRoom(), "热点群聊不可操作成员！");
        ChatRoomGroup roomGroup = roomGroupCache.get(roomId);
        AssertUtil.isNotEmpty(roomGroup, "房间号有误！");
        ChatGroupMember self = chatGroupMemberDAO.getMember(roomGroup.getId(), uid);
        AssertUtil.isNotEmpty(self, "非法操作，用户不存在群聊中！");
        // 1. 判断被移除的人是否是群主或者管理员  （群主不可以被移除，管理员只能被群主移除）
        // 1.1 群主
        AssertUtil.isFalse(chatGroupMemberDAO.isLord(roomGroup.getId(), targetId), "抱歉，你没有移除该成员的权限！");
        // 1.2 管理员 判断是否是群主操作
        if (Boolean.TRUE.equals(chatGroupMemberDAO.isManager(roomGroup.getId(), targetId))) {
            Boolean isLord = chatGroupMemberDAO.isLord(roomGroup.getId(), uid); // 高级权限
            AssertUtil.isTrue(isLord, "抱歉，你没有移除该成员的权限！");
        }
        // 1.3 普通成员 判断是否有权限操作
        AssertUtil.isTrue(hasPower(self), "抱歉，你没有移除该成员的权限！");
        ChatGroupMember member = chatGroupMemberDAO.getMember(roomGroup.getId(), targetId);
        AssertUtil.isNotEmpty(member, "用户已不在群聊！");
        chatGroupMemberDAO.removeById(member.getId());
        // 2 发送移除事件告知群成员
        List<String> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        WsBaseVO<WSMemberChange> ws = ChatMemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), member.getUserId());
        pushService.sendPushMsg(ws, memberUidList);
        groupMemberCache.evictMemberUidList(room.getId());
        return 1;
    }


    private boolean hasPower(ChatGroupMember self) { // 管理员、房主有权限
        return Objects.equals(self.getRole(), GroupRoleEnum.HOME.getType())
                || Objects.equals(self.getRole(), GroupRoleEnum.MANAGER.getType());
//                || iRoleService.hasPower(self.getUid(), RoleEnum.ADMIN);

    }

    /**
     * 更新群组信息
     *
     * @param id  群组id
     * @param dto 参数
     * @return 影响条数
     */
    @Override
    public Long updateGroup(Long id, UpdateRoomGroupDTO dto) {
        String uid = RequestHolderUtil.get().getId();
        ChatRoomGroup roomGroup = roomGroupCache.get(id);
        if (roomGroup == null) {
            roomGroup = chatRoomGroupDAO.getByRoomId(id);
        }
        AssertUtil.isTrue(dto.getName() == null || StringUtils.isNotBlank(dto.getName()), "群组名称不能为空！");
        AssertUtil.isNotEmpty(roomGroup, "房间号有误！");
        // 1、判断是否群组
        Boolean lord = chatGroupMemberDAO.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(lord, "抱歉，未有该权限！");
        // 2、备料
        ChatRoomGroup data = new ChatRoomGroup()
                .setId(roomGroup.getId())
                .setRoomId(id)
                .setName(dto.getName());
        // 3、消费头像 | 替换头像
        if (StringUtils.isNotBlank(dto.getAvatar()) && !dto.getAvatar().equals(roomGroup.getAvatar())) {
            boolean isExitKey = ossFileUtil.deleteRedisKey(uid, dto.getAvatar());
            AssertUtil.isTrue(isExitKey, "修改失败，上传头像已失效！");
            if (StringUtils.isNotBlank(roomGroup.getAvatar())) {// 删除旧头像
                ossFileUtil.deleteFile(roomGroup.getAvatar());
            }
            data.setAvatar(dto.getAvatar());
        }
        // 4、更新详情字段（包括群公告和邀请权限）
        InvitePermissionEnum oldPermission = null;
        boolean permissionChanged = false;

        RoomGroupExtJson extJsonDTO = roomGroup.getExtJson();
        if (extJsonDTO == null) {
            extJsonDTO = new RoomGroupExtJson();
        }

        // 获取旧的邀请权限
        if (extJsonDTO.getInvitePermission() != null) {
            oldPermission = extJsonDTO.getInvitePermission();
        }

        // 更新公告
        if (dto.getDetail() != null) {
            if (dto.getDetail().getNotice() != null) {
                extJsonDTO.setNotice(dto.getDetail().getNotice());
            }
            // 更新邀请权限
            if (dto.getDetail().getInvitePermission() != null) {
                extJsonDTO.setInvitePermission(dto.getDetail().getInvitePermission());
                permissionChanged = !Objects.equals(oldPermission, dto.getDetail().getInvitePermission());
            }
        }

        data.setExtJson(extJsonDTO);

        boolean update = chatRoomGroupDAO.updateById(data);
        if (update) {
            roomGroupCache.delete(id);
            if (permissionChanged && dto.getDetail() != null) {
                sendPermissionChangeNotification(roomGroup.getRoomId(), oldPermission, dto.getDetail().getInvitePermission());
            }
        }
        return update ? 1L : 0L;
    }

    private void sendPermissionChangeNotification(Long roomId, InvitePermissionEnum oldPermission, InvitePermissionEnum newPermission) {
        try {
            String content = String.format("群主已修改邀请权限：%s → %s",
                oldPermission != null ? oldPermission.getDesc() : "任意成员",
                newPermission.getDesc());

            ChatMessageDTO chatMessageReq = new ChatMessageDTO();
            chatMessageReq.setRoomId(roomId);
            chatMessageReq.setMsgType(MessageTypeEnum.SYSTEM.getType());
            chatMessageReq.setContent(content);
            chatMessageReq.setBody(content);

            chatService.sendMsg(chatMessageReq, "system");
        } catch (Exception e) {
            log.error("发送权限变更通知失败 roomId={}, old={}, new={}", roomId, oldPermission, newPermission, e);
        }
    }
}
