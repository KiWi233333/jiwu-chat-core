package com.jiwu.api.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.event.ChatMessageSendEvent;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.chat.common.strategy.msg.type.DelMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.type.RecallMsgHandler;
import com.jiwu.api.chat.common.vo.ChatMemberStatisticVO;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.chat.common.vo.ChatMessageVO;
import com.jiwu.api.chat.service.ChatMessageReactionService;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.ChatMemberAdapter;
import com.jiwu.api.chat.service.adapter.ChatMessageAdapter;
import com.jiwu.api.chat.service.adapter.ChatRoomAdapter;
import com.jiwu.api.common.annotation.RedissonLock;
import com.jiwu.api.common.main.cache.chat.*;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.dao.chat.*;
import com.jiwu.api.common.main.dao.sys.UserDAO;
import com.jiwu.api.common.main.dto.chat.helper.ChatMemberHelper;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessagePageDTO;
import com.jiwu.api.common.main.dto.chat.req.ChatMessageReadDTO;
import com.jiwu.api.common.main.dto.chat.req.SelectGroupMemberPageDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatMessageReadVO;
import com.jiwu.api.common.main.enums.chat.*;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.mapper.chat.ChatContactMapper;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.*;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天业务
 *
 * @className: ChatServiceImpl
 * @author: Kiwi23333
 * @description: 聊天业务
 * @date: 2023/12/8 17:16
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final Long MAX_RECALL_SECOND = 5L;
    @Resource
    private ChatRoomCache chatRoomCache;
    @Resource
    private ChatRoomSelfCache chatRoomSelfCache;
    @Resource
    private ChatContactDAO chatContactDAO;
    @Resource
    private ChatRoomGroupCache chatRoomGroupCache;
    @Resource
    private ChatGroupMemberCache chatGroupMemberCache;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private ChatContactMapper contactMapper;
    @Resource
    private ChatMessageDAO chatMessageDAO;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private RecallMsgHandler recallMsgHandler;
    @Resource
    private DelMsgHandler delMsgHandler;
    @Resource
    private UserCache userCache;
    @Resource
    private UserDAO userDAO;
    @Resource
    private ChatRoomGroupDAO chatRoomGroupDAO;
    @Resource
    private ChatGroupMemberDAO chatGroupMemberDAO;
    @Resource
    private ChatRoomSelfDAO chatRoomSelfDAO;
    @Resource
    private ChatRoomDAO chatRoomDAO;
    @Resource
    private WebSocketService webSocketService;

    @Resource
    private ChatMessageReactionService chatMessageReactionService;


    /**
     * 游标查询
     *
     * @param dto    参数
     * @param userId 接收方 用户id
     * @return true
     */
    @Override
    public CursorPageBaseVO<ChatMessageVO> getMsgPage(ChatMessagePageDTO dto, String userId) {
        //用最后一条消息id，来限制被踢出的人能看见的最大一条消息
        Long lastMsgId = getLastMsgId(dto.getRoomId(), userId);
        CursorPageBaseVO<ChatMessage> cursorPage = chatMessageDAO.getCursorPage(dto.getRoomId(), dto, lastMsgId);
        if (Boolean.TRUE.equals(cursorPage.isEmpty())) {
            return CursorPageBaseVO.empty();
        }
        return CursorPageBaseVO.init(cursorPage, getMsgVOBatch(cursorPage.getList(), userId));
    }

    private Long getLastMsgId(Long roomId, String userId) {
        ChatRoom room = chatRoomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误！");
        if (room.isHotRoom()) {
            return null;
        }
        ChatContact contact = contactMapper.selectOne(new LambdaQueryWrapper<ChatContact>()
                .select(ChatContact::getLastMsgId,
                        ChatContact::getRoomId,
                        ChatContact::getActiveTime) // 注意
                .eq(ChatContact::getUserId, userId)
                .eq(ChatContact::getRoomId, roomId));
        AssertUtil.isNotEmpty(contact, "聊天记录不存在！");
        return contact.getLastMsgId();
    }

    /**
     * 发送消息
     *
     * @param dto    参数
     * @param userId 用户id
     * @return 消息id
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageDTO dto, String userId) {
        // 1、校验 （是否存在、是否在线、是否在群聊、是否在黑名单、是否超过限制）
        check(dto, userId);
        // 2、sql
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(dto.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(dto, userId);
        // 3、发布消息发送事件
        applicationEventPublisher.publishEvent(new ChatMessageSendEvent(this, msgId, dto.getClientId()));
        return msgId;
    }

    // 校验
    private void check(ChatMessageDTO dto, String userId) {
        ChatRoom room = chatRoomCache.get(dto.getRoomId());
        AssertUtil.isNotEmpty(room, "房间不存在！");
        if (room.isHotRoom()) {
            return;
        }
        if (room.isRoomFriend()) {  // 单聊
            ChatRoomSelf roomFriend = chatRoomSelfDAO.getByRoomId(dto.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您和对方已不是好友！");
            AssertUtil.isTrue(userId.equals(roomFriend.getUid1()) || userId.equals(roomFriend.getUid2()), "您和对方已不是好友！");
        } else if (room.isAiRoom()) { // ai聊
            ChatRoomSelf roomFriend = chatRoomSelfDAO.getByRoomId(dto.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您与机器人已不是好友！");
            AssertUtil.isTrue(userId.equals(roomFriend.getUid1()) || userId.equals(roomFriend.getUid2()), "您与机器人已不是好友！");
        } else if (room.isRoomGroup()) { // 群聊
            ChatRoomGroup roomGroup = chatRoomGroupCache.get(dto.getRoomId());
            ChatGroupMember member = chatGroupMemberDAO.getMember(roomGroup.getId(), userId);
            AssertUtil.isNotEmpty(member, "您已经被移除该群聊！");
        }

    }

    /**
     * 返回消息所有物料
     *
     * @param msgId  消息id
     * @param userId 用户id（发送人）
     * @return 数据
     */
    @Override
    public ChatMessageVO getMsgDetail(Long msgId, String userId) {
        return getMsgVO(chatMessageDAO.getById(msgId), userId);
    }

    // 构建消息
    public ChatMessageVO getMsgVO(ChatMessage message, String userId) {
        return CollUtil.getFirst(getMsgVOBatch(Collections.singletonList(message), userId));
    }

    /**
     * 批量构建消息
     *
     * @param messages 消息集
     * @param userId   接收这id
     * @return 数据列表
     */
    public List<ChatMessageVO> getMsgVOBatch(List<ChatMessage> messages, String userId) {
        // 空
        if (CollUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        // 构建消息
        List<ChatMessageVO> voList = ChatMessageAdapter.buildMsgVO(messages, userId, userCache);
        // 填充 reaction 数据
        fillReactions(voList, userId);
        return voList;
    }

    /**
     * 批量填充消息的 reaction 聚合数据
     */
    private void fillReactions(List<ChatMessageVO> voList, String userId) {
        if (CollUtil.isEmpty(voList)) {
            return;
        }
        List<Long> msgIds = voList.stream()
                .map(vo -> vo.getMessage().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(msgIds)) {
            return;
        }
        Map<Long, List<com.jiwu.api.chat.common.vo.ReactionVO>> reactionMap =
                chatMessageReactionService.batchGetReactions(msgIds, userId);
        for (ChatMessageVO vo : voList) {
            Long msgId = vo.getMessage().getId();
            if (msgId != null && reactionMap.containsKey(msgId)) {
                vo.getMessage().setReactions(reactionMap.get(msgId));
            }
        }
    }


    /**
     * 撤回消息
     *
     * @param userId 用户id
     * @param roomId 房间id
     * @param msgId  消息id
     * @return 影响
     */
    @Override
    public Integer recallMsg(String userId, Long roomId, Long msgId) {
        ChatMessage message = chatMessageDAO.getById(msgId);
        // 1、校验能不能执行撤回
        checkRecall(userId, message);
        // 2执行消息撤回
        recallMsgHandler.recall(userId, message);
        return 1;
    }

    private void checkRecall(String uid, ChatMessage message) {
        AssertUtil.isNotEmpty(message, "消息已经撤回或不存在！");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法再撤回！");
        // 1、是否自己
        boolean self = Objects.equals(uid, message.getFromUid());
        AssertUtil.isTrue(self, "抱歉,您没有权限！");
        // 判断是否可以撤回
        AssertUtil.isTrue(checkRecallOk(message.getType()), "本消息类型无法撤回！");
        // 2、非本人 (群主、管理员权限)
        if (!self) {
            ChatGroupMember role = chatGroupMemberCache.getRoleByUid(message.getRoomId(), uid);
            ChatGroupMember targetRole = chatGroupMemberCache.getRoleByUid(message.getRoomId(), message.getFromUid());
            AssertUtil.isTrue(hasPermissionDelMsg(role.getRole(), targetRole.getRole()), "您没有权限撤回消息！");
        }
        // 3、是否超时
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between < MAX_RECALL_SECOND, "超过" + MAX_RECALL_SECOND + "分钟，已无法撤回~");
    }

    private boolean checkRecallOk(Integer type) {
        return !MessageTypeEnum.AI_CHAT.getType().equals(type)
                && !MessageTypeEnum.RTC_MSG.getType().equals(type)
                && !MessageTypeEnum.AI_CHAT_REPLY.getType().equals(type);
    }


    /**
     * 删除消息（软删除）
     *
     * @param userId 用户id
     * @param roomId 房间id
     * @param msgId  消息id
     * @return 影响
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteMsg(String userId, Long roomId, Long msgId) {
        ChatMessage message = chatMessageDAO.getById(msgId);
        // 1、校验能不能执行删除
        checkDelete(userId, message);
        // 2执行消息删除
        delMsgHandler.delete(userId, message);
        return 1;
    }

    private void checkDelete(String uid, ChatMessage targetMsg) {
        AssertUtil.isNotEmpty(targetMsg, "消息已经删除或不存在！");
        AssertUtil.notEqual(targetMsg.getType(), MessageTypeEnum.DEL_MSG.getType(), "消息无法再删除！");
        // 权限向下，才能删除
        // 1、删除人权限
        ChatGroupMember role = chatGroupMemberCache.getRoleByUid(targetMsg.getRoomId(), uid);
        AssertUtil.isNotEmpty(role, "您没有权限删除消息！");
        // 2、发送人权限
        ChatGroupMember msgRole = chatGroupMemberCache.getRoleByUid(targetMsg.getRoomId(), targetMsg.getFromUid());
        AssertUtil.isTrue( // 管理员及以上权限，或管理员发送人本人
                hasPermissionDelMsg(role.getRole(), Optional.ofNullable(msgRole).map(ChatGroupMember::getRole).orElse(GroupRoleEnum.MEMBER.getType()))
                        || role.getRole().equals(GroupRoleEnum.MANAGER.getType()) && Objects.equals(uid, targetMsg.getFromUid()), "您没有权限删除消息！");
    }

    private boolean hasPermissionDelMsg(Integer role, Integer targetRole) {
        // 1、群主最高级权限
        if (role.equals(GroupRoleEnum.HOME.getType())) {
            return true;
        }
        // 2、管理 向下删除
        return role.equals(GroupRoleEnum.MANAGER.getType()) && targetRole.equals(GroupRoleEnum.MEMBER.getType());
    }


    /**
     * 获取群聊成员统计信息
     *
     * @return 数据
     */
    @Override
    public ChatMemberStatisticVO getMemberStatistic() {
        Long onlineNum = userCache.getOnlineNum();
        ChatMemberStatisticVO resp = new ChatMemberStatisticVO();
        resp.setOnlineNum(onlineNum);
//        Long offlineNum = userCache.getOfflineNum(); // 不展示总人数
//        resp.setTotalNum(onlineNum + offlineNum);
        return resp;
    }


    /**
     * 获取已读未读游标信息
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 数据
     */
    @Override
    public CursorPageBaseVO<ChatMessageReadVO> getReadPage(String uid, ChatMessageReadDTO dto) {
        ChatMessage message = chatMessageMapper.selectById(dto.getMsgId());
        AssertUtil.isNotEmpty(message, "消息id有误");
        AssertUtil.equal(uid, message.getFromUid(), "只能查看自己的消息");
        CursorPageBaseVO<ChatContact> page;
        if (dto.getSearchType() == 1) {//已读
            page = chatContactDAO.getReadPage(message, dto);
        } else {// 未读
            page = chatContactDAO.getUnReadPage(message, dto);
        }
        if (CollUtil.isEmpty(page.getList())) {
            return CursorPageBaseVO.empty(); // 空
        }
        return CursorPageBaseVO.init(page, ChatRoomAdapter.buildReadVO(page.getList())); // 构建房间读写信息
    }

    /**
     * 获取群成员列表
     *
     * @param memberUidList 用户列表
     * @param dto           参数
     * @return 游标列表
     */
    @Override
    public CursorPageBaseVO<ChatMemberVO> getMemberPage(List<String> memberUidList, SelectGroupMemberPageDTO dto) {
        Pair<ChatActiveStatusEnum, String> pair = ChatMemberHelper.getCursorPair(dto.getCursor());
        ChatActiveStatusEnum activeStatusEnum = pair.getKey();
        String timeCursor = pair.getValue();
        List<ChatMemberVO> resultList = new ArrayList<>();//最终列表
        Boolean isLast = Boolean.FALSE;
        if (activeStatusEnum == ChatActiveStatusEnum.ONLINE) {//在线列表
            CursorPageBaseVO<User> cursorPage = userDAO.getCursorPage(memberUidList, new CursorPageBaseDTO(dto.getPageSize(), timeCursor), ChatActiveStatusEnum.ONLINE);
            resultList.addAll(ChatMemberAdapter.buildMember(cursorPage.getList()));//添加在线列表
            if (Boolean.TRUE.equals(cursorPage.getIsLast())) {//如果是最后一页,从离线列表再补点数据
                activeStatusEnum = ChatActiveStatusEnum.OFFLINE;
                Integer leftSize = dto.getPageSize() - cursorPage.getList().size();
                cursorPage = userDAO.getCursorPage(memberUidList, new CursorPageBaseDTO(leftSize, null), ChatActiveStatusEnum.OFFLINE);
                resultList.addAll(ChatMemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            }
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        } else if (activeStatusEnum == ChatActiveStatusEnum.OFFLINE) {//离线列表
            CursorPageBaseVO<User> cursorPage = userDAO.getCursorPage(memberUidList, new CursorPageBaseDTO(dto.getPageSize(), timeCursor), ChatActiveStatusEnum.OFFLINE);
            resultList.addAll(ChatMemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        }
        // 获取群成员角色ID
        List<String> uidList = resultList.stream().map(ChatMemberVO::getUserId).collect(Collectors.toList());
        ChatRoomGroup roomGroup = chatRoomGroupDAO.getByRoomId(dto.getRoomId());
        Map<String, Integer> uidMapRole = chatGroupMemberDAO.getMemberMapRole(roomGroup.getId(), uidList);
        resultList.forEach(member -> {
            member.setRoleType(uidMapRole.get(member.getUserId()));
            // 获取列表
            User user = userCache.getUserInfo(member.getUserId());
            member.setNickName(user.getNickname());
            member.setUsername(user.getUsername());
            member.setUserId(user.getId());
            member.setAvatar(user.getAvatar());
        });
        //组装结果
        return new CursorPageBaseVO<>(ChatMemberHelper.generateCursor(activeStatusEnum, timeCursor), isLast, resultList);
    }

    /**
     * 阅读消息
     *
     * @param userId 用户id
     * @param roomId 房间号id
     * @return 阅读行
     */
    @Override
    @RedissonLock(key = "#userId")
    @Transactional(rollbackFor = Exception.class)
    public Long msgRead(String userId, Long roomId) {
        // 1、校验
        final ChatRoom room = chatRoomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误，请刷新重试！");
        final int res = chatContactDAO.refreshOrCreateReadTime(roomId, Collections.singletonList(userId), new Date()); // 0 失败 1 新建 2 更新
        if (res == 1) {// 生成角色
            List<ChatGroupMember> groupMembers = ChatRoomAdapter.buildGroupMemberBatch(Collections.singletonList(userId), room.getId());
            chatGroupMemberDAO.saveBatch(groupMembers);
        }
        AssertUtil.isFalse(res == 0, "阅读消息失败，请稍后再试！");
        return 1L;
    }


}
