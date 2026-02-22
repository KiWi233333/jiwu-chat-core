package com.jiwu.api.common.main.dao.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.mapper.chat.ChatGroupMemberMapper;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.vo.chat.ChatMemberListVO;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jiwu.api.common.main.enums.chat.GroupRoleEnum.ADMIN_LIST;


@Service
public class ChatGroupMemberDAO extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember> {

    @Resource
    @Lazy
    private ChatGroupMemberCache groupMemberCache;


    public List<String> getMemberUidList(Long groupId) {
        List<ChatGroupMember> list = lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .select(ChatGroupMember::getUserId)
                .list();
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
    }

    public List<String> getMemberBatch(Long groupId, List<String> uidList) {
        List<ChatGroupMember> list = lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .in(ChatGroupMember::getUserId, uidList)
                .select(ChatGroupMember::getUserId)
                .list();
        return list.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
    }

    /**
     * 批量获取成员群角色
     *
     * @param groupId 群ID
     * @param uidList 用户列表
     * @return 成员群角色列表
     */
    public Map<String, Integer> getMemberMapRole(Long groupId, List<String> uidList) {
        if (uidList == null || uidList.isEmpty()) {
            return new HashMap<>();
        }
        List<ChatGroupMember> list = lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .in(ChatGroupMember::getUserId, uidList)
                .in(ChatGroupMember::getRole, ADMIN_LIST)
                .select(ChatGroupMember::getUserId, ChatGroupMember::getRole)
                .list();
        return list.stream().collect(Collectors.toMap(ChatGroupMember::getUserId, ChatGroupMember::getRole));
    }

    public ChatGroupMember getMember(Long groupId, String uid) {
        return lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, uid)
                .one();
    }


    public List<ChatGroupMember> getSelfGroup(String uid) {
        return lambdaQuery()
                .eq(ChatGroupMember::getUserId, uid)
                .eq(ChatGroupMember::getRole, GroupRoleEnum.HOME.getType())
                .list();
    }

    /**
     * 判断用户是否在房间中
     *
     * @param roomId  房间ID
     * @param uidList 用户ID
     * @return 是否在群聊中
     */
    public Boolean isGroupShip(Long roomId, List<String> uidList) {
        List<String> memberUidList = groupMemberCache.getMemberUidList(roomId);
        return memberUidList.containsAll(uidList);
    }

    /**
     * 是否是群成员
     *
     * @param roomId 房间号
     * @param userId 用户id
     * @return Boolean
     */
    public Boolean isGroupShip(Long roomId, String userId) {
        List<String> memberUidList = groupMemberCache.getMemberUidList(roomId);
        return memberUidList.contains(userId);
    }

    /**
     * 是否是群主
     *
     * @param id  群组ID
     * @param uid 用户ID
     * @return 是否是群主
     */
    public Boolean isLord(Long id, String uid) {
        ChatGroupMember groupMember = this.lambdaQuery()
                .eq(ChatGroupMember::getGroupId, id)
                .eq(ChatGroupMember::getUserId, uid)
                .eq(ChatGroupMember::getRole, GroupRoleEnum.HOME.getType())
                .one();
        return ObjectUtil.isNotNull(groupMember);
    }

    /**
     * 是否是管理员
     *
     * @param id  群组ID
     * @param uid 用户ID
     * @return 是否是管理员
     */
    public Boolean isManager(Long id, String uid) {
        ChatGroupMember groupMember = this.lambdaQuery()
                .eq(ChatGroupMember::getGroupId, id)
                .eq(ChatGroupMember::getUserId, uid)
                .eq(ChatGroupMember::getRole, GroupRoleEnum.MANAGER.getType())
                .one();
        return ObjectUtil.isNotNull(groupMember);
    }

    /**
     * 获取管理员uid列表
     *
     * @param homeId 群主ID
     * @return 管理员uid列表
     */
    public List<String> getManageUidList(String homeId) {
        return this.lambdaQuery()
                .eq(ChatGroupMember::getGroupId, homeId)
                .eq(ChatGroupMember::getRole, GroupRoleEnum.MANAGER.getType())
                .list()
                .stream()
                .map(ChatGroupMember::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 增加管理员
     *
     * @param id      群组ID
     * @param uidList 用户列表
     * @return 是否更新
     */
    public boolean addAdmin(Long id, List<String> uidList) {
        LambdaUpdateWrapper<ChatGroupMember> wrapper = new UpdateWrapper<ChatGroupMember>().lambda()
                .eq(ChatGroupMember::getGroupId, id)
                .in(ChatGroupMember::getUserId, uidList)
                .set(ChatGroupMember::getRole, GroupRoleEnum.MANAGER.getType());
        return this.update(wrapper);
    }

    /**
     * 撤销管理员
     *
     * @param id      群组ID
     * @param uidList 用户列表
     * @return 是否成功
     */
    public boolean revokeAdmin(Long id, List<String> uidList) {
        LambdaUpdateWrapper<ChatGroupMember> wrapper = new UpdateWrapper<ChatGroupMember>().lambda()
                .eq(ChatGroupMember::getGroupId, id)
                .in(ChatGroupMember::getUserId, uidList)
                .set(ChatGroupMember::getRole, GroupRoleEnum.MEMBER.getType());
        return this.update(wrapper);
    }

    /**
     * 根据群组ID删除群成员
     *
     * @param groupId 群组ID
     * @param uidList 群成员列表
     * @return 是否删除成功
     */
    public Boolean removeByGroupId(Long groupId, List<String> uidList) {
        LambdaQueryWrapper<ChatGroupMember> wrapper = new QueryWrapper<ChatGroupMember>()
                .lambda()
                .eq(ChatGroupMember::getGroupId, groupId)
                .in(CollUtil.isNotEmpty(uidList), ChatGroupMember::getUserId, uidList);
        return this.remove(wrapper);
    }

    /**
     * 成员列表
     *
     * @param roomId 房间号
     * @return 列表
     */
    public List<ChatMemberListVO> getMemberListByRoomId(Long roomId) {
        MPJLambdaWrapper<ChatGroupMember> qw = new MPJLambdaWrapper<>();
        qw
                .leftJoin(ChatRoomGroup.class, ChatRoomGroup::getId, ChatGroupMember::getGroupId)
                .leftJoin(User.class, User::getId, ChatGroupMember::getUserId)
                .selectAs(User::getId, ChatMemberListVO::getUserId)
                .selectAs(User::getAvatar, ChatMemberListVO::getAvatar)
                .selectAs(User::getUsername, ChatMemberListVO::getUsername)
                .selectAs(User::getNickname, ChatMemberListVO::getNickName)
                .selectAs(ChatGroupMember::getRole, ChatMemberListVO::getRole)
                .eq(ChatRoomGroup::getRoomId, roomId);
        return this.getBaseMapper().selectJoinList(ChatMemberListVO.class, qw);
    }

    /**
     * 更新群成员角色
     *
     * @param groupId       群组id
     * @param targetUserId  用户id
     * @param groupRoleEnum 角色类型
     * @return 影响
     */
    public boolean updateRoleByUid(Long groupId, String targetUserId, GroupRoleEnum groupRoleEnum) {
        LambdaUpdateWrapper<ChatGroupMember> wrapper = new UpdateWrapper<ChatGroupMember>()
                .lambda()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, targetUserId)
                .set(ChatGroupMember::getRole, groupRoleEnum.getType());
        return this.update(wrapper);
    }

    /**
     * 更新或者刷新角色
     *
     * @param groupId       群组id
     * @param userId        用户id
     * @param groupRoleEnum 角色类型
     * @return 影响
     */
    public int updateOrReFlushRole(Long groupId, String userId, GroupRoleEnum groupRoleEnum) {
        return this.getBaseMapper().updateOrReFlush(groupId, userId, groupRoleEnum.getType());
    }

    public CursorPageBaseVO<ChatGroupMember> getPageByUid(String uid, CursorPageBaseDTO dto) {
        return CursorUtils.getCursorPageByMysql(this, dto, wq -> wq
                        .eq(ChatGroupMember::getUserId, uid),
                ChatGroupMember::getCreateTime, Date.class);
    }

    public Map<String, ChatGroupMember> getAllGroupRoleMap(Long groupId) {
        List<ChatGroupMember> members = lambdaQuery()
                .eq(ChatGroupMember::getGroupId, groupId)
                .lt(ChatGroupMember::getRole, GroupRoleEnum.MEMBER.getType())
                .select(ChatGroupMember::getUserId, ChatGroupMember::getRole)
                .list();
        if (CollUtil.isEmpty(members)) {
            return new HashMap<>();
        }
        return members.stream().collect(Collectors.toMap(ChatGroupMember::getUserId, member -> member));
    }
}
