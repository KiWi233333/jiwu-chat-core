package com.jiwu.api.common.main.cache.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.dao.chat.ChatGroupMemberDAO;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;

import java.util.Collections;

import com.jiwu.api.common.util.common.AssertUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.jiwu.api.common.main.mapper.chat.ChatGroupMemberMapper;
import com.jiwu.api.common.main.mapper.chat.ChatRoomGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description: 群成员相关缓存
 */
@Component
public class ChatGroupMemberCache {

    @Autowired
    private ChatRoomGroupMapper chatRoomGroupMapper;
    @Autowired
    private ChatGroupMemberDAO chatGroupMemberDAO;
    @Autowired
    private ChatGroupMemberMapper chatGroupMemberMapper;

    // 成员记录
    public static final String CHAT_GROUP_MEMBER = "chat:group:member:";
    public static final String CHAT_GROUP_MEMBER_LIST = "chat:group:member:list:";

    @Cacheable(cacheNames = CHAT_GROUP_MEMBER, key = "#roomId")
    public List<String> getMemberUidList(Long roomId) {
        ChatRoomGroup roomGroup = chatRoomGroupMapper.selectOne(new LambdaQueryWrapper<ChatRoomGroup>().eq(ChatRoomGroup::getRoomId, roomId));
        if (Objects.isNull(roomGroup)) {
            return Collections.emptyList();
        }
        return chatGroupMemberDAO.getMemberUidList(roomGroup.getId());
    }

    @CacheEvict(cacheNames = CHAT_GROUP_MEMBER, key = "#roomId")
    public List<Long> evictMemberUidList(Long roomId) {
        return null;
    }


    /**
     * 确认是否有权限
     *
     * @param groupRoleEnum 角色
     * @param roomId        房间号
     * @param uid           用户
     * @return list
     */
    public List<ChatGroupMember> hasPermission(GroupRoleEnum groupRoleEnum, Long roomId, String uid) {
        List<ChatGroupMember> list = getRoleByRoomId(roomId);
        return list.stream().filter(p -> p.getRole().equals(groupRoleEnum.getType()) && p.getUserId().equals(uid)).collect(Collectors.toList());
    }
//    public List<ChatGroupMember> getListByUid(Long roomId, String uid) {
//        List<ChatGroupMember> list = getRoleByRoomId(roomId);
//        return list.stream().filter(p -> p.getUserId().equals(uid)).collect(Collectors.toList());
//    }

    public ChatGroupMember getRoleByUid(Long roomId, String uid) {
        List<ChatGroupMember> list = getRoleByRoomId(roomId);
        AssertUtil.isTrue(!list.isEmpty(), "暂时没有操作权限！");
        for (ChatGroupMember member : list) {
            if (member.getUserId().equals(uid)) {
                return member;
            }
        }
        return null;
    }

    /**
     * 获取房间所有用户以及权限
     *
     * @param roomId 房间号
     * @return list
     */
    @Cacheable(value = CHAT_GROUP_MEMBER_LIST, key = "#roomId", unless = "#result.size() == 0")
    public List<ChatGroupMember> getRoleByRoomId(Long roomId) {
        MPJLambdaWrapper<ChatGroupMember> qw = new MPJLambdaWrapper<ChatGroupMember>()
                .selectAll(ChatGroupMember.class)
                .eq(ChatRoomGroup::getRoomId, roomId)
                .leftJoin(ChatRoomGroup.class, ChatRoomGroup::getId, ChatGroupMember::getGroupId);
        return chatGroupMemberMapper.selectJoinList(ChatGroupMember.class, qw);
    }


    @CacheEvict(cacheNames = CHAT_GROUP_MEMBER_LIST, key = "#roomId")
    public boolean clearCacheByRoomId(Long roomId) {
        return true;
    }

    @CacheEvict(cacheNames = CHAT_GROUP_MEMBER_LIST)
    public boolean clearCache(Long roomId) {
        return true;
    }


}
