package com.jiwu.api.chat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.dao.chat.*;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.chat.common.dto.MemberAdminAddDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomGroupVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatGroupMemberService;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.adapter.ChatMemberAdapter;
import com.jiwu.api.chat.service.adapter.ChatRoomAdapter;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.chat.common.vo.ws.WSMemberChange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群聊权限业务
 *
 * @className: GroupMemberService
 * @author: Kiwi23333
 * @description: 群聊权限业务
 * @date: 2023/12/18 20:21
 */
@Service
public class ChatGroupMemberServiceImpl implements ChatGroupMemberService {
    @Resource
    private ChatGroupMemberDAO chatGroupMemberDAO;
    @Resource
    private ChatGroupMemberCache chatGroupMemberCache;
    @Resource
    private ChatRoomGroupDAO chatRoomGroupDAO;
    @Resource
    private ChatRoomDAO roomDAO;
    @Resource
    private ChatContactDAO contactDAO;
    @Resource
    private ChatGroupMemberCache groupMemberCache;
    @Resource
    private ChatMessageDAO messageDAO;
    @Resource
    private PushService pushService;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 获取群聊房间分页列表
     *
     * @param dto 参数
     * @param uid 用户id
     * @return 分页列表
     */
    @Override
    public CursorPageBaseVO<ChatRoomGroupVO> getGroupRoomPage(CursorPageBaseDTO dto, String uid) {
        CursorPageBaseVO<ChatGroupMember> groupMembers = chatGroupMemberDAO.getPageByUid(uid, dto);
        if (CollectionUtils.isEmpty(groupMembers.getList())) {
            return CursorPageBaseVO.empty();
        }
        // 构建群聊列表
        List<Long> groupIds = groupMembers.getList().stream().map(ChatGroupMember::getGroupId).collect(Collectors.toList());
        List<ChatRoomGroup> roomGroups = chatRoomGroupDAO.getListByIds(groupIds);
        return CursorPageBaseVO.init(groupMembers, ChatRoomAdapter.buildRoomGroupVO(groupMembers.getList(), roomGroups));
    }

    private Double getCursorOrNull(String cursor) {
        if (StringUtils.isNotBlank(cursor)) {
            return Double.parseDouble(cursor);
        } else {
            return null;
        }
    }

    /**
     * 校验是否有该角色（群聊权限）
     *
     * @param groupRoleEnum 参数
     * @param roomId        房间号
     * @param uid           用户id
     * @return 权限
     */
    @Override
    public List<ChatGroupMember> hasPermission(GroupRoleEnum groupRoleEnum, Long roomId, String uid) {
        return chatGroupMemberCache.hasPermission(groupRoleEnum, roomId, uid);
    }

    /**
     * 添加管理员
     *
     * @param uid 房主id
     * @param dto 参数
     * @return 影响
     */
    @Override
    public Integer addAdmin(String uid, MemberAdminAddDTO dto) {
        // 1、查询是否存在群聊
        ChatRoomGroup roomGroup = chatRoomGroupDAO.getByRoomId(dto.getRoomId());
        AssertUtil.isTrue(roomGroup != null, "群聊不存在！");
        // 2、查询是否为房主
        assert roomGroup != null;
        Boolean isLord = chatGroupMemberDAO.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, "您不是群主，没有权限添加管理员！");
        // 3、更新角色
        int update = chatGroupMemberDAO.updateOrReFlushRole(roomGroup.getId(), dto.getUserId(), GroupRoleEnum.MANAGER);
        AssertUtil.isTrue(update > 0, "添加管理员失败，用户不存在！");
        return 1;
    }

    /**
     * 撤销管理员
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 影响
     */
    @Override
    public Integer revokeAdmin(String uid, MemberAdminAddDTO dto) {
        // 1、查询是否存在群聊
        ChatRoomGroup roomGroup = chatRoomGroupDAO.getByRoomId(dto.getRoomId());
        AssertUtil.isTrue(roomGroup != null, "抱歉，群聊不存在！");
        // 2、查询是否为房主
        assert roomGroup != null;
        Boolean isLord = chatGroupMemberDAO.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, "您不是群主，没有权限添加管理员！");
        // 3、更新角色
        boolean update = chatGroupMemberDAO.updateRoleByUid(roomGroup.getId(), dto.getUserId(), GroupRoleEnum.MEMBER);
        AssertUtil.isTrue(update, "撤销管理失败，用户不存在！");
        return 1;
    }

    /**
     * 退出群聊
     *
     * @param uid    需要退出的用户ID
     * @param roomId 房间号
     */
    @Override
    public Boolean exitGroup(String uid, Long roomId) {
        // 1. 判断群聊是否存在
        ChatRoomGroup roomGroup = chatRoomGroupDAO.getByRoomId(roomId);
        AssertUtil.isNotEmpty(roomGroup, "该群聊已不存在！");
        // 2. 判断房间是否是大群聊 （大群聊禁止退出）
        ChatRoom room = roomDAO.getById(roomId);
        AssertUtil.isTrue(room != null && !room.isHotRoom(), "官方群聊禁止退出，或房间不存在！");
        // 3. 判断群成员是否在群中
        Boolean isGroupShip = chatGroupMemberDAO.isGroupShip(roomGroup.getRoomId(), uid);
        AssertUtil.isTrue(isGroupShip, "抱歉，您已不在该群聊中!");
        // 4. 判断该用户是否是群主
        Boolean isLord = chatGroupMemberDAO.isLord(roomGroup.getId(), uid);
        return transactionTemplate.execute(action -> {
            if (Boolean.TRUE.equals(isLord)) { // 解散群聊
                // 4.1 删除房间
                boolean isDelRoom = roomDAO.removeById(roomId);
                AssertUtil.isTrue(isDelRoom, "抱歉，群聊已不存在！");
                boolean isDelRoomGroup = chatRoomGroupDAO.removeByRoomId(roomId);
                AssertUtil.isTrue(isDelRoomGroup, "抱歉，群聊已不存在！");
                // 4.2 删除会话
                boolean isDelContact = contactDAO.removeByRoomId(roomId, Collections.emptyList());
//                AssertUtil.isTrue(isDelContact, "当前会话已经不存在！"); // TODO: 会话不是判断标准
                // 4.3 删除群成员
                Boolean isDelGroupMember = chatGroupMemberDAO.removeByGroupId(roomGroup.getId(), Collections.emptyList());
                AssertUtil.isTrue(isDelGroupMember, "群成员已不存在！");
                // 4.4 删除消息记录 (逻辑删除)
                Boolean isDelMessage = messageDAO.removeByRoomId(roomId, Collections.emptyList());
                AssertUtil.isTrue(isDelMessage, "群聊已不存在或已删除！");
                // 4.5 删除头像
                if (StringUtils.isNotBlank(roomGroup.getAvatar()))
                    ossFileUtil.deleteFile(roomGroup.getAvatar());// 开启校验删除
                // 告知群成员 群聊已被删除的消息
                List<String> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
                WsBaseVO<WSMemberChange> ws = ChatMemberAdapter.buildGroupDeleteWs(roomGroup.getRoomId(), uid);
                pushService.sendPushMsg(ws, memberUidList);
                groupMemberCache.evictMemberUidList(roomId); // 清空缓存
            } else { // 成员退出
                // 4.5 删除会话
                boolean isDelContact = contactDAO.removeByRoomId(roomId, Collections.singletonList(uid));
//                AssertUtil.isTrue(isDelContact, "当前会话已经不存在！"); // TODO: 会话不是判断标准
                // 4.6 删除群成员（本人）
                Boolean isDelGroupMember = chatGroupMemberDAO.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
                AssertUtil.isTrue(isDelGroupMember, "群成员已不存在！");
                // 4.7 发送移除事件告知群成员
                List<String> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
                WsBaseVO<WSMemberChange> ws = ChatMemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), uid);
                pushService.sendPushMsg(ws, memberUidList);
                groupMemberCache.evictMemberUidList(roomId);
            }
            return Boolean.TRUE;
        });
    }
}
