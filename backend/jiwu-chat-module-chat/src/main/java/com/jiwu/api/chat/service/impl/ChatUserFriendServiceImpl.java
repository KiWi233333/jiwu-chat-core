package com.jiwu.api.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.dao.chat.ChatContactDAO;
import com.jiwu.api.common.main.dao.chat.ChatRoomSelfDAO;
import com.jiwu.api.common.main.dao.chat.ChatUserApplyDAO;
import com.jiwu.api.common.main.dao.chat.ChatUserFriendDAO;
import com.jiwu.api.common.main.dao.sys.UserDAO;
import com.jiwu.api.common.main.dto.chat.friend.*;
import com.jiwu.api.common.main.enums.chat.ApplyTypeEnum;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.enums.chat.FriendApplyStatusEnum;
import com.jiwu.api.common.main.enums.user.UserStatus;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import com.jiwu.api.common.main.pojo.chat.ChatUserFriend;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.config.thread.ThreadPoolConfig;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomSelfVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.chat.common.event.UserApplyEvent;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.chat.service.ChatRoomService;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.ChatUserFriendService;
import com.jiwu.api.chat.service.adapter.ChatAdapter;
import com.jiwu.api.chat.service.adapter.ChatMessageAdapter;
import com.jiwu.api.chat.service.adapter.ChatUserFriendAdapter;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.chat.common.vo.ChatUserFriendUnReadVO;
import com.jiwu.api.chat.common.vo.PageBaseVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendApplyVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendCheckVO;
import com.jiwu.api.common.main.vo.chat.ChatUserFriendVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatUserFriendServiceImpl implements ChatUserFriendService {

    @Resource
    private UserDAO userDAO;
    @Resource
    private ChatUserFriendDAO chatUserFriendDAO;
    @Resource
    private ChatRoomSelfDAO chatRoomSelfDAO;
    @Resource
    private ChatContactDAO chatContactDAO;
    @Resource
    private ChatUserApplyDAO chatUserApplyDAO;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private ChatRoomService roomService;
    @Resource
    private ChatService chatService;
    @Resource
    private UserCache userCache;
    // 线程池
    @Resource
    @Qualifier(value = ThreadPoolConfig.JIWU_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    /**
     * 分页获取好友申请列表
     *
     * @param id  用户id
     * @param dto 参数
     * @return 数据
     */
    @Override
    public PageBaseVO<ChatUserFriendApplyVO> pageApplyFriend(String id, PageBaseDTO dto) {
        IPage<ChatUserApply> iPage = chatUserApplyDAO.friendApplyPage(id, dto.plusPage());
        if (CollUtil.isEmpty(iPage.getRecords())) {
            return PageBaseVO.empty();
        }
        // 将这些申请列表设为已读
        readApples(id, iPage);
        // 返回消息
        return PageBaseVO.init(iPage, ChatUserFriendAdapter.buildFriendApplyList(iPage.getRecords(), userCache));
    }

    // 设置 阅读申请好友消息列表
    private void readApples(String uid, IPage<ChatUserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords() // 申请的表单ids
                .stream().map(ChatUserApply::getId)
                .collect(Collectors.toList());// 读取ids
        chatUserApplyDAO.readApples(uid, applyIds);
    }

    /**
     * 获取分页联系人列表
     *
     * @param id  用户id
     * @param dto 参数
     * @return 列表
     */
    @Override
    public CursorPageBaseVO<ChatUserFriendVO> friendPage(String id, CursorPageBaseDTO dto) {
        CursorPageBaseVO<ChatUserFriend> friendPage = chatUserFriendDAO.getFriendPage(id, dto);
        if (CollectionUtils.isEmpty(friendPage.getList())) {
            return CursorPageBaseVO.empty();
        }
        List<String> uidList = friendPage.getList()
                .stream().map(ChatUserFriend::getFriendUid)
                .collect(Collectors.toList());
        List<User> userList = userDAO.getFriendList(uidList);
        return CursorPageBaseVO.init(friendPage, ChatUserFriendAdapter.buildFriend(friendPage.getList(), userList));
    }

    @Override
    public IPage<ChatUserFriendVO> friendPageV2(Integer page, Integer size, String uid, ChatFriendPageDTO dto) {
        Page<ChatUserFriendVO> pages = new Page<>(page, size); // 创建分页对象，指定当前页码和每页记录数
        return chatUserFriendDAO.friendPageV2(pages, uid, dto);
    }

    /**
     * 获取私聊房间列表（分页）
     *
     * @param dto 参数
     * @param uid 用户id
     * @return 列表
     */
    @Override
    public IPage<ChatRoomSelfVO> getFriendRoomPage(ChatFriendPageBaseDTO dto, String uid) {
        final IPage<ChatRoomSelfVO> friendPage = chatUserFriendDAO.getFriendPage(uid, dto);
        List<String> friendUidList = friendPage.getRecords()
                .stream().map(ChatRoomSelfVO::getTargetUid)
                .collect(Collectors.toList());
        // 获取roomId
        Map<String, Long> friednRoomIdMap = chatRoomSelfDAO.getFriednRoomIdMap(uid, friendUidList);
        friendPage.getRecords().forEach(item -> {
            Long roomId = friednRoomIdMap.get(item.getTargetUid());
            item.setRoomId(roomId);
        });
        return friendPage;
    }

    /**
     * 好友申请
     *
     * @param userId 用户id
     * @param dto    参数
     * @return 影响
     */
    @Override
    // @Transactional(rollbackFor = Exception.class)
    public Integer apply(String userId, ChatUserFriendApplyDTO dto) {
        final String targetUid = dto.getTargetUid();
        User user = userDAO.getById(targetUid);
        AssertUtil.isNotEmpty(user, "不存在该用户，请确认是否正确！");
        ChatUserFriend friend = chatUserFriendDAO.getByFriend(userId, targetUid);// 是否有好友关系
        AssertUtil.isEmpty(friend, "你们已经是好友了！");
        // 是否有待审批的申请记录(自己的) 待审批
        ChatUserApply selfApproving = chatUserApplyDAO.getFriendApproving(userId, targetUid);
        AssertUtil.isEmpty(selfApproving, "已有好友申请记录，请等待回复！");
        // if (selfApproving!= null) {
        // AssertUtil.isFalse(ApplyStatusEnum.WAIT_APPROVAL.getCode().equals(selfApproving.getStatus()),
        // "已有好友申请记录，请等待回复！");
        // AssertUtil.isFalse(ApplyStatusEnum.REJECT.getCode().equals(selfApproving.getStatus()),
        // "已经被对方拒绝，等待对方添加你！");
        // AssertUtil.isFalse(ApplyStatusEnum.AGREE.getCode().equals(selfApproving.getStatus()),
        // "已经同意对方申请，无需重复申请！");
        // }
        // 是否有待审批的申请记录(别人请求自己的)
        ChatUserApply friendApproving = chatUserApplyDAO.getFriendApproving(targetUid, userId);
        if (Objects.nonNull(friendApproving)) {
            ((ChatUserFriendService) AopContext.currentProxy()).applyApprove(userId,
                    new ChatUserFriendApproveDTO(friendApproving.getId()));
            throw new BusinessException(ResultStatus.INSERT_ERR.getCode(), "对方已经申请添加你，当前申请已同意！");
        }
        // 申请入库
        ChatUserApply insert = ChatUserFriendAdapter.buildFriendApply(userId, dto);
        chatUserApplyDAO.save(insert);
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
        return 1;
    }

    /**
     * 确认是否存在好友关系
     *
     * @param uid 用户id
     * @param dto 参数
     * @return ChatUserFriendCheckVO
     */
    @Override
    public ChatUserFriendCheckVO check(String uid, ChatUserFriendCheckDTO dto) {
        List<ChatUserFriend> friendList = chatUserFriendDAO.getByFriends(uid, dto.getUidList());
        Set<String> friendUidSet = friendList.stream().map(ChatUserFriend::getFriendUid).collect(Collectors.toSet());
        List<ChatUserFriendCheckVO.FriendCheck> friendCheckList = dto.getUidList().stream().map(friendUid -> {
            ChatUserFriendCheckVO.FriendCheck friendCheck = new ChatUserFriendCheckVO.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid) ? 1 : 0);
            return friendCheck;
        }).collect(Collectors.toList());
        return new ChatUserFriendCheckVO(friendCheckList);
    }

    /**
     * 同意好友申请
     *
     * @param dto 参数
     * @return 影响
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyApprove(String uid1, ChatUserFriendApproveDTO dto) {
        ChatUserApply userApply = chatUserApplyDAO.getById(dto.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录！");
        AssertUtil.equal(userApply.getTargetId(), uid1, "不存在申请记录！");
        AssertUtil.isTrue(userApply.getType().equals(ApplyTypeEnum.ADD_FRIEND.getCode()), "申请记录为其他类型！");
        AssertUtil.equal(userApply.getStatus(), FriendApplyStatusEnum.WAIT_APPROVAL.getCode(), "已同意过好友申请！");
        // 同意申请
        AssertUtil.isTrue(chatUserApplyDAO.setAgree(dto.getApplyId()), "同意好友申请失败！");
        // 查看对方是否机器人
        final User user = userCache.getUserInfo(userApply.getTargetId());
        AssertUtil.isTrue(user != null && user.getStatus().equals(UserStatus.ON.getCode()), "对方已被封禁，不能添加！");
        // 创建双方好友关系
        createFriend(uid1, userApply.getUserId());
        ChatRoomSelf selfRoom = roomService.createFriendRoom(RoomTypeEnum.FRIEND, Arrays.asList(uid1, userApply.getUserId()));
        // 发送同意消息 - eg: 我们已同意你成为好友 拉起会话
        chatService.sendMsg(ChatMessageAdapter.buildAgreeMsg(selfRoom.getRoomId()), uid1);
        return 1;
    }

    /**
     * 拒绝好友申请
     *
     * @param uid 用户id
     * @param dto 好友申请id
     * @return 影响
     */
    @Override
    public Integer deleteApply(String uid, ChatUserFriendRejectDTO dto) {
        ChatUserApply userApply = chatUserApplyDAO.getById(dto.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录！");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录！");
        AssertUtil.isTrue(userApply.getType().equals(ApplyTypeEnum.ADD_FRIEND.getCode()), "申请记录为其他类型！");
        AssertUtil.equal(userApply.getStatus(), FriendApplyStatusEnum.WAIT_APPROVAL.getCode(), "已同意过好友申请！");
        // AssertUtil.notEqual(userApply.getStatus(), ApplyStatusEnum.REJECT.getCode(),
        // "已拒绝过好友申请！");
        // 同意申请
        AssertUtil.isTrue(chatUserApplyDAO.setReject(dto.getApplyId()), "拒绝好友申请失败！");
        // TODO 发送通知
        return 1;
    }

    private void createFriend(String uid, String targetUid) {
        ChatUserFriend u1 = new ChatUserFriend();
        u1.setUserId(uid);
        u1.setFriendUid(targetUid);
        ChatUserFriend u2 = new ChatUserFriend();
        u2.setUserId(targetUid);
        u2.setFriendUid(uid);
        // 批量保存
        chatUserFriendDAO.saveBatch(Lists.newArrayList(u1, u2));
    }

    /**
     * 获取申请未读数
     *
     * @param id 用户id
     * @return ChatUserFriendUnReadVO
     */
    @Override
    public ChatUserFriendUnReadVO getUnread(String id) {
        Long unReadCount = chatUserApplyDAO.getUnReadCount(id);
        return new ChatUserFriendUnReadVO(unReadCount);
    }

    /**
     * 删除好友 (软删除) 删除关系、删除发起人会话、禁用房间
     *
     * @param currentUid 当前用户id
     * @param friendUid  目标用户id
     * @return 影响
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteFriend(String currentUid, String friendUid) {
        List<ChatUserFriend> userFriends = chatUserFriendDAO.getUserFriend(currentUid, friendUid);
        AssertUtil.isFalse(CollUtil.isEmpty(userFriends), "您和对方已不是好友关系！");
        List<Long> friendRecordIds = userFriends.stream().map(ChatUserFriend::getId).collect(Collectors.toList());
        // 删除好友关系
        AssertUtil.isTrue(chatUserFriendDAO.removeByIds(friendRecordIds), "删除好友关系失败！");
        // 删除对应的会话（发起人）
        final ChatRoomSelf roomSelf = chatRoomSelfDAO
                .getByKey(ChatAdapter.generateRoomKey(Arrays.asList(currentUid, friendUid)));
        if (Objects.nonNull(roomSelf)) {
            chatContactDAO.removeByRoomId(roomSelf.getRoomId(), Collections.singletonList(currentUid)); // 删除会话 不一定存在
        }
        // 禁用房间
        AssertUtil.isTrue(roomService.disableFriendRoom(Arrays.asList(currentUid, friendUid)) > 0, "好友已经被删除！");
        return 1;
    }

    /**
     * 获取阅\未读数
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 数量
     */
    @Override
    public Integer getReadCount(String uid, SelectApplyReadDTO dto) {
        return null;
    }

}
