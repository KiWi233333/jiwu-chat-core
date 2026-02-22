package com.jiwu.api.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.cache.chat.*;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.cache.user.UserInfoCache;
import com.jiwu.api.common.main.dao.chat.*;
import com.jiwu.api.common.main.dao.sys.UserDAO;
import com.jiwu.api.common.main.dto.chat.contact.ContactPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.req.InsertRoomDTO;
import com.jiwu.api.common.main.dto.chat.req.RoomGroupExtJson;
import com.jiwu.api.common.main.dto.chat.req.SelectGroupMemberPageDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomVO;
import com.jiwu.api.common.main.enums.chat.GroupRoleAPPEnum;
import com.jiwu.api.common.main.enums.chat.GroupRoleEnum;
import com.jiwu.api.common.main.enums.chat.HotFlagEnum;
import com.jiwu.api.common.main.enums.chat.InvitePermissionEnum;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.pojo.chat.*;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.chat.common.dto.RoomBaseInfo;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.chat.service.ChatRoomService;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.ChatAdapter;
import com.jiwu.api.chat.service.adapter.ChatRoomAdapter;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.common.main.vo.chat.ChatMemberListVO;
import com.jiwu.api.chat.common.vo.room.ChatRoomInfoVO;
import com.jiwu.api.chat.common.vo.ws.WSPinContactMsg;
import com.jiwu.api.chat.common.vo.ws.WSUpdateContactInfoMsg;
import com.jiwu.api.common.main.constant.chat.ChatGroupConstant;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 房间业务
 *
 * @className: ChatServiceImpl
 * @author: Kiwi23333
 * @description: 房间业务
 * @date: 2023/12/8 17:16
 */
@Slf4j
@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    @Resource
    private ChatContactDAO chatContactDAO;
    @Resource
    private ChatGroupMemberDAO chatGroupMemberDAO;
    @Resource
    private ChatRoomGroupDAO roomGroupDAO;
    @Resource
    private UserDAO userDAO;
    @Resource
    private ChatMessageDAO messageDAO;
    @Resource
    private ChatHotRoomCache chatHotRoomCache;
    @Resource
    private ChatRoomCache roomCache;
    @Resource
    private UserInfoCache userInfoCache;
    @Resource
    private ChatRoomGroupCache chatRoomGroupCache;
    @Resource
    private ChatRoomSelfCache roomSelfCache;
    @Resource
    private ChatRoomGroupCache roomGroupCache;
    @Resource
    private UserCache userCache;
    @Resource
    private ChatService chatService;
    @Resource
    private ChatRoomSelfDAO roomSelfDAO;
    @Resource
    private ChatRoomDAO roomDAO;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private WebSocketService webSocketService;


    /**
     * 获取会话列表（分页）
     *
     * @param dto 参数
     * @param uid 用户id
     * @return 数据
     */
    @Override
    public CursorPageBaseVO<ChatRoomVO> getContactPage(ContactPageBaseDTO dto, String uid) {
        // 查出用户要展示的会话列表
        CursorPageBaseVO<Long> page;
        Double hotEnd = getCursorOrNull(dto.getCursor());
        Double hotStart = null;
        // 用户基础会话
        CursorPageBaseVO<ChatContact> contactPage = chatContactDAO.getContactPage(uid, dto);
        List<Long> baseRoomIds = contactPage.getList().stream().map(ChatContact::getRoomId).collect(Collectors.toList());
        if (Boolean.FALSE.equals(contactPage.getIsLast())) {
            hotStart = getCursorOrNull(contactPage.getCursor());
        }
        // 会话map
        Map<Long, ChatContact> contactMap = contactPage.getList().stream().collect(Collectors.toMap(ChatContact::getRoomId, Function.identity()));
        // 热门房间
        if (dto.getType() == null || dto.isGroupType()) {
            Set<ZSetOperations.TypedTuple<String>> typedTuples = chatHotRoomCache.getRoomRange(hotStart, hotEnd);
            List<Long> hotRoomIds = typedTuples.stream().map(ZSetOperations.TypedTuple::getValue).filter(Objects::nonNull).map(Long::parseLong).collect(Collectors.toList());
            // 基础会话和热门房间合并
            baseRoomIds.addAll(hotRoomIds);
            // 过滤
            baseRoomIds = baseRoomIds.stream().distinct().filter(Objects::nonNull).collect(Collectors.toList());
            // 补充热聊信息 置顶、免打扰等
            if (!hotRoomIds.isEmpty()) {
                List<ChatContact> hotContacts = chatContactDAO.getContactListByIds(uid, hotRoomIds);
                contactMap.putAll(hotContacts.stream().collect(Collectors.toMap(ChatContact::getRoomId, Function.identity())));
            }
        }
        page = CursorPageBaseVO.init(contactPage, baseRoomIds);
        // 最后组装会话信息（名称，头像，未读数等）
        List<ChatRoomVO> result = buildContactVO(uid, page.getList(), contactMap);
        return CursorPageBaseVO.init(page, result);
    }

    private Double getCursorOrNull(String cursor) {
        if (StringUtils.isNotBlank(cursor)) {
            return Double.parseDouble(cursor);
        } else {
            return null;
        }
    }

    @NotNull
    private List<ChatRoomVO> buildContactVO(String uid, List<Long> roomIds, Map<Long, ChatContact> contactMap) {
        // 表情和头像
        Map<Long, RoomBaseInfo> roomBaseInfoMap = getRoomBaseInfoMap(roomIds, uid);
        // 最后一条消息
        List<Long> msgIds = roomBaseInfoMap.values().stream().map(RoomBaseInfo::getLastMsgId).collect(Collectors.toList());
        List<ChatMessage> messages = CollUtil.isEmpty(msgIds) ? new ArrayList<>() : messageDAO.listByIds(msgIds);
        Map<Long, ChatMessage> msgMap = messages.stream().collect(Collectors.toMap(ChatMessage::getId, Function.identity()));
        Map<String, User> lastMsgUidMap = userInfoCache.getBatch(messages.stream().map(ChatMessage::getFromUid).collect(Collectors.toList()));
        // 消息未读数
        Map<Long, Long> unReadCountMap = getUnReadCountMap(uid, roomIds);
        return roomBaseInfoMap.values().stream().map(room -> {
            ChatRoomVO vo = new ChatRoomVO();
            RoomBaseInfo roomBaseInfo = roomBaseInfoMap.get(room.getRoomId());
            vo.setAvatar(roomBaseInfo.getAvatar());
            vo.setRoomId(room.getRoomId());
            vo.setActiveTime(room.getActiveTime());
            vo.setHotFlag(roomBaseInfo.getHotFlag());
            vo.setType(roomBaseInfo.getType());
            vo.setName(roomBaseInfo.getName());
            ChatMessage message = msgMap.get(room.getLastMsgId());
            if (message != null) {
                String lastMsgTxt = MsgHandlerFactory.getStrategyNoNull(message.getType()).showContactMsg(message); // 策略获取
                if (RoomTypeEnum.FRIEND.getType().equals(room.getType()) || RoomTypeEnum.AI.getType().equals(room.getType())) { // 私聊
                    vo.setText(Optional.ofNullable(lastMsgTxt).orElse("还没有新消息..."));
                } else { // 群聊
                    vo.setText(Optional.ofNullable(lastMsgUidMap.get(message.getFromUid())).map(User::getNickname).orElse("匿名用户") + "：" + Optional.ofNullable(lastMsgTxt).orElse("还没有新消息..."));
                }
            }

            final ChatContact chatContact = contactMap.get(room.getRoomId()); // 获取会话信息
            if (chatContact != null) {
                vo.setPinTime(chatContact.getPinTime()); // 置顶信息
                vo.setNoticeStatus(chatContact.getNoticeStatus()); // 提醒状态码
                vo.setShieldStatus(chatContact.getShieldStatus()); // 提醒状态码
            }
//                vo.setLastMsgId(chatContact.getLastMsgId()); // 最后一条消息id
            vo.setLastMsgId(room.getLastMsgId());
            vo.setUnreadCount(unReadCountMap.getOrDefault(room.getRoomId(), 0L));
            return vo;
        }).sorted(Comparator.comparing(ChatRoomVO::getActiveTime).reversed()).collect(Collectors.toList());
    }


    /**
     * 获取房间基本消息Map
     *
     * @param roomIds 房间ids
     * @param uid     用户id
     * @return 数据
     */
    private Map<Long, RoomBaseInfo> getRoomBaseInfoMap(List<Long> roomIds, String uid) {
        Map<Long, ChatRoom> roomMap = roomCache.getBatch(roomIds);
        // 1、房间根据好友和群组类型分组
        Map<Integer, List<Long>> groupRoomIdMap = roomMap.values().stream().collect(Collectors.groupingBy(ChatRoom::getType, // 按分类分组
                Collectors.mapping(ChatRoom::getId, Collectors.toList())));
        // 2、获取群组信息
        List<Long> groupRoomId = groupRoomIdMap.get(RoomTypeEnum.GROUP.getType());
        Map<Long, ChatRoomGroup> roomInfoBatch = chatRoomGroupCache.getBatch(groupRoomId);
        // 3、获取私聊房间信息
        List<Long> selfRoomIds = Optional.ofNullable(groupRoomIdMap.get(RoomTypeEnum.FRIEND.getType())).orElse(new ArrayList<>());
        final List<Long> aiRoomIds = groupRoomIdMap.get(RoomTypeEnum.AI.getType());
        if (!CollUtil.isEmpty(aiRoomIds)) {
            selfRoomIds.addAll(aiRoomIds);
        }
        Map<Long, User> selfRoomMap = getSelfRoomMap(selfRoomIds, uid);
        // 4、获取AI信息
        return roomMap.values().stream().map(room -> {
            RoomBaseInfo roomBaseInfo = new RoomBaseInfo();
            roomBaseInfo.setRoomId(room.getId());
            roomBaseInfo.setType(room.getType());
            roomBaseInfo.setHotFlag(room.getHotFlag());
            roomBaseInfo.setLastMsgId(room.getLastMsgId());
            roomBaseInfo.setActiveTime(room.getUpdateTime());
            if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.GROUP) {
                ChatRoomGroup roomGroup = roomInfoBatch.get(room.getId());
                roomBaseInfo.setName(roomGroup.getName());
                roomBaseInfo.setAvatar(roomGroup.getAvatar());
            } else if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.FRIEND
                    || RoomTypeEnum.of(room.getType()) == RoomTypeEnum.AI) {
                User user = selfRoomMap.get(room.getId());
                roomBaseInfo.setName(user.getNickname());
                roomBaseInfo.setAvatar(user.getAvatar());
            }
            return roomBaseInfo;
        }).collect(Collectors.toMap(RoomBaseInfo::getRoomId, Function.identity()));
    }

    /**
     * 获取未读数
     */
    private Map<Long, Long> getUnReadCountMap(String uid, List<Long> roomIds) {
        if (StringUtil.isNullOrEmpty(uid) || roomIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ChatContact> contacts = chatContactDAO.getByRoomIds(roomIds, uid);
        return contacts.parallelStream().map(contact -> Pair.of(contact.getRoomId(),
                // 未读
                messageDAO.getUnReadCount(contact.getRoomId(), contact.getReadTime()))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * 组装用户的好友信息（）
     *
     * @param roomIds 用户房间ids
     * @param uid     用户id
     * @return Map<Long, User>
     */
    private Map<Long, User> getSelfRoomMap(List<Long> roomIds, String uid) {
        if (CollUtil.isEmpty(roomIds)) {
            return new HashMap<>();
        }
        Map<Long, ChatRoomSelf> roomSelfList = roomSelfCache.getBatch(roomIds);
        Set<String> friendUidSet = ChatAdapter.getFriendUidSet(roomSelfList.values(), uid);
        Map<String, User> userBatch = userInfoCache.getBatch(new ArrayList<>(friendUidSet));
        return roomSelfList.values().stream().collect(Collectors.toMap(ChatRoomSelf::getRoomId, roomFriend -> {
            String friendUid = ChatAdapter.getFriendUid(roomFriend, uid);
            return userBatch.get(friendUid);
        }));
    }


    /**
     * 获取用户房间的会话详情（群聊）
     *
     * @param uid    用户id
     * @param roomId 房间id
     * @return 数据
     */
    @Override
    public ChatRoomVO getContactDetail(String uid, Long roomId) {
        ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        // 1、获取会话信息
        ChatContact chatContact = chatContactDAO.get(uid, roomId);
        final HashMap<Long, ChatContact> contactMap = new HashMap<>();
        AssertUtil.isNotEmpty(chatContact, ResultStatus.DELETE_ERR, "会话已经不存在！");
        contactMap.put(roomId, chatContact);
        // 2、获取群组信息
        ChatRoomVO roomVO = buildContactVO(uid, Collections.singletonList(roomId), contactMap).get(0);
        if (room.isRoomGroup()) {
            ChatRoomGroup roomGroup = chatRoomGroupCache.get(roomVO.getRoomId());
            roomVO.setRoomGroup(ChatRoomAdapter.chatRoomGroupVOBuildVO(RoomTypeEnum.of(room.getType()), roomGroup, chatGroupMemberDAO.getMember(roomGroup.getId(), uid)));// 房间详细信息
            roomVO.setMember(chatGroupMemberDAO.getMember(roomGroup.getId(), uid));// 房间权限信息
            roomVO.setSelfExist(Boolean.TRUE.equals(chatGroupMemberDAO.isGroupShip(roomId, uid)) || roomVO.getHotFlag() == 1 ? 1 : 0);// 大群聊| 是否退出群聊
        } else if (room.isRoomFriend() || room.isAiRoom()) {
            final ChatRoomSelf self = roomSelfCache.get(roomId);
            roomVO.setTargetUid(self.getUid1().equals(uid) ? self.getUid2() : self.getUid1()); // 获取对方用户id
        }
        // } else if (room.isRoomFriend() || room.isRoomAI()) { // 剩余的
        // }
        return roomVO;
    }

    /**
     * 获取会话详情（单聊）
     *
     * @param uid    用户id
     * @param roomId 好友id
     * @return 数据
     */
    @Override
    public ChatRoomVO getContactDetailByRoom(String uid, Long roomId) {
        // 获取会话
        // 1、获取会话信息
        ChatContact chatContact = chatContactDAO.get(uid, roomId);
        final HashMap<Long, ChatContact> contactMap = new HashMap<>();
        AssertUtil.isNotEmpty(chatContact, ResultStatus.DELETE_ERR, "会话已经不存在！");
        contactMap.put(roomId, chatContact);
        // 2、获取群组信息
        ChatRoomVO roomVO = buildContactVO(uid, Collections.singletonList(roomId), contactMap).get(0);
        ChatRoomSelf roomSelf = roomSelfCache.get(roomId);
        if (roomSelf != null) {
            roomVO.setTargetUid(roomSelf.getUid1().equals(uid) ? roomSelf.getUid2() : roomSelf.getUid1());
            roomVO.setSelfExist(Objects.equals(roomSelf.getStatus(), NormalOrNoEnum.NOT_NORMAL.getStatus()) ? NormalOrNoEnum.NOT_NORMAL.getStatus() : NormalOrNoEnum.NORMAL.getStatus());
        }
        return roomVO;
    }

    /**
     * 获取会话详情（单聊）
     *
     * @param uid       用户id
     * @param friendUid 好友id
     * @return 数据
     */
    @Override
    public ChatRoomVO getContactDetailByFriend(String uid, String friendUid) {
        ChatRoomSelf selfRoom = getFriendRoom(uid, friendUid);
        AssertUtil.isNotEmpty(selfRoom, "对方不是您的好友！");
        // 1、获取会话信息
        ChatContact chatContact = chatContactDAO.get(uid, selfRoom.getRoomId());
        final HashMap<Long, ChatContact> contactMap = new HashMap<>();
        AssertUtil.isNotEmpty(chatContact, ResultStatus.DELETE_ERR, "会话已经不存在！");
        contactMap.put(selfRoom.getRoomId(), chatContact);
        // 2、获取组装房间信息
        ChatRoomVO roomVO = buildContactVO(uid, Collections.singletonList(selfRoom.getRoomId()), contactMap).get(0);
        roomVO.setSelfExist(NormalOrNoEnum.of(selfRoom.getStatus()).getStatus());
        // 3、组装数据
        roomVO.setTargetUid(friendUid);
        return roomVO;
    }


    /**
     * 获取群聊信息（群聊信息）
     *
     * @param uid    用户id
     * @param roomId id
     * @return 参数
     */
    @Override
    public ChatRoomInfoVO getGroupDetail(String uid, Long roomId) {
        ChatRoomGroup roomGroup = roomGroupCache.get(roomId);
        ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(roomGroup, "roomId有误");
        Long onlineNum;
        long allLineNum;
        if (isHotGroup(room)) {// 热点群从redis取人数
            onlineNum = userCache.getOnlineNum();
            final Long offlineNum = userCache.getOfflineNum();
            allLineNum = offlineNum + onlineNum;
        } else {
            List<String> memberUidList = chatGroupMemberDAO.getMemberUidList(roomGroup.getId());// 获取全部成员id
            onlineNum = userDAO.getOnlineCount(memberUidList);// 获取在线人数
            allLineNum = (long) memberUidList.size();
        }
        // 构建角色
        GroupRoleAPPEnum groupRole = getGroupRole(uid, roomGroup, room);
        return ChatRoomInfoVO.builder()
                .avatar(roomGroup.getAvatar())
                .roomId(roomId)
                .groupName(roomGroup.getName())
                .onlineNum(onlineNum)
                .allUserNum(allLineNum)
                .hotFlag(room.getHotFlag())
                .createTime(roomGroup.getCreateTime())
                .detail(ChatRoomAdapter.buildExtra(roomGroup.getExtJson()))
                .role(groupRole.getType())
                .build();
    }

    // 获取在群中的角色（群）
    private GroupRoleAPPEnum getGroupRole(String uid, ChatRoomGroup roomGroup, ChatRoom room) {
        ChatGroupMember member = Objects.isNull(uid) ? null : chatGroupMemberDAO.getMember(roomGroup.getId(), uid);
        if (Objects.nonNull(member)) {// 其他为被移除
            return GroupRoleAPPEnum.of(member.getRole());
        } else if (isHotGroup(room)) {
            return GroupRoleAPPEnum.MEMBER; // 热点群默认是成员
        } else {
            return GroupRoleAPPEnum.REMOVE; // 其他为被移除
        }
    }

    // 是否热点群聊（大群）
    private boolean isHotGroup(ChatRoom room) {
        return HotFlagEnum.YES.getType().equals(room.getHotFlag());
    }


    /**
     * 创建房间（单聊）
     *
     * @param roomType 聊天房间类型
     * @param uidList  uid list
     * @return 房间信息
     */
    @Override
    public ChatRoomSelf createFriendRoom(RoomTypeEnum roomType, List<String> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对！");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对！");
        String key = ChatAdapter.generateRoomKey(uidList);
        ChatRoomSelf self = roomSelfDAO.getByKey(key);
        if (Objects.nonNull(self)) { //如果存在房间就恢复，适用于恢复好友场景
            restoreRoomIfNeed(self);
        } else {//新建房间 - 包括AI聊天室
            ChatRoom room = createRoom(roomType);
            self = createFriendRoom(room.getId(), uidList);
        }
        return self;
    }

    private void restoreRoomIfNeed(ChatRoomSelf room) {
        if (Objects.equals(room.getStatus(), NormalOrNoEnum.NOT_NORMAL.getStatus())) {
            roomSelfDAO.restoreRoom(room.getId());
        }
    }

    private ChatRoomSelf createFriendRoom(Long roomId, List<String> uidList) {
        ChatRoomSelf insert = ChatAdapter.buildFriendRoom(roomId, uidList);
        roomSelfDAO.save(insert);
        return insert;
    }

    // 创建房间
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom createRoom(RoomTypeEnum typeEnum) {
        ChatRoom insert = ChatAdapter.buildRoom(typeEnum);
        roomDAO.save(insert);
        return insert;
    }

    /**
     * 获取单聊房间信息（单聊）
     *
     * @param uid1 用户1
     * @param uid2 用户2
     */
    @Override
    public ChatRoomSelf getFriendRoom(String uid1, String uid2) {
        String key = ChatAdapter.generateRoomKey(Arrays.asList(uid1, uid2));
        return roomSelfDAO.getByKey(key);
    }

    /**
     * 禁用单聊房间（单聊）
     *
     * @param uidList 用户id
     */
    @Override
    public Long disableFriendRoom(List<String> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间删除失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        String key = ChatAdapter.generateRoomKey(uidList);
        boolean b = roomSelfDAO.disableRoom(key);
        if (!b) {
            throw new BusinessException(ResultStatus.UPDATE_ERR.getCode(), "房间不存在！");
        }
        return 1L;
    }

    // 最大群聊数
    private static final int MAX_GROUP_NUM = 30;

    /**
     * 创建一个群聊房间
     *
     * @param dto 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoomGroup createGroupRoom(InsertRoomDTO dto) {
        // 判断是否已经创建了
        List<ChatGroupMember> selfGroup = chatGroupMemberDAO.getSelfGroup(dto.getUserId());
        if (selfGroup.size() >= MAX_GROUP_NUM) {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "每个人只能创建" + MAX_GROUP_NUM + "个群");
        }
        User user = userInfoCache.get(dto.getUserId());
        // 1、创建群聊
        ChatRoom room = createRoom(RoomTypeEnum.GROUP);
        // 2、插入群详细
        ChatRoomGroup roomGroup = ChatAdapter.buildGroupRoom(user, room.getId(), dto.getAvatar());
        // 消费头像文件
        boolean isDelKey = ossFileUtil.deleteRedisKey(dto.getUserId(), dto.getAvatar());
        AssertUtil.isTrue(isDelKey, "头像文件已不存在！");
        // 设置默认邀请权限为 ADMIN (管理员和群主可邀请)
        RoomGroupExtJson extJson = new RoomGroupExtJson();
        extJson.setInvitePermission(InvitePermissionEnum.ADMIN);
        roomGroup.setExtJson(extJson);
        roomGroupDAO.save(roomGroup);
        // 3、插入群主
        ChatGroupMember leader = new ChatGroupMember().setRole(GroupRoleEnum.HOME.getType()).setGroupId(roomGroup.getId()).setUserId(dto.getUserId());
        chatGroupMemberDAO.save(leader);
        return roomGroup;
    }

    /**
     * 获取群聊成员列表（分页）
     *
     * @param dto 参数
     * @return 数据
     */
    @Override
    public CursorPageBaseVO<ChatMemberVO> getGroupMemberPage(SelectGroupMemberPageDTO dto) {
        ChatRoom room = roomCache.get(dto.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        List<String> memberUidList;
        if (isHotGroup(room)) {// 全员群展示所有用户
            memberUidList = null;
        } else {// 只展示房间内的群成员
            ChatRoomGroup roomGroup = roomGroupCache.get(dto.getRoomId());
            memberUidList = chatGroupMemberDAO.getMemberUidList(roomGroup.getId());
        }
        return chatService.getMemberPage(memberUidList, dto);
    }

    /**
     * 获取群聊成员列表
     *
     * @param roomId 房间id
     * @return 列表
     */
    @Override
    public List<ChatMemberListVO> getMemberList(Long roomId) {
        // 查询群聊角色
        if (ChatGroupConstant.HOT_ROOM_TOTAL_ID.equals(roomId)) { // 全员群
            final List<User> memberList = userDAO.getMemberList();
            List<ChatMemberListVO> chatMemberListVOList = new ArrayList<>();
            Map<String, ChatGroupMember> memberMap = chatGroupMemberDAO.getAllGroupRoleMap(roomId);
            for (User user : memberList) {
                if (UserType.ROBOT.getCode() == user.getUserType()) { // 过滤机器人
                    continue;
                }
                chatMemberListVOList.add(ChatMemberListVO.builder()
                        .username(user.getUsername())
                        .nickName(user.getNickname())
                        .avatar(user.getAvatar())
                        .userId(user.getId())
                        .role(Optional.ofNullable(memberMap.get(user.getId())).map(ChatGroupMember::getRole).orElse(GroupRoleEnum.MEMBER.getType()))
                        .build());
            }
            // 查询对应的群聊角色
            return chatMemberListVOList;
        } else {
            return chatGroupMemberDAO.getMemberListByRoomId(roomId);
        }
    }

    /**
     * 删除会话（后续可重新拉取）
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @return 结果
     */
    @Override
    public Integer deleteContact(Long roomId, String uid) {
        AssertUtil.isFalse(ChatGroupConstant.HOT_ROOM_TOTAL_ID.equals(roomId), "不能删除全员群会话！");
        final ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "该会话不存在！");
        if (room.isRoomGroup()) {
            ChatRoomGroup roomGroup = roomGroupCache.get(roomId);
            AssertUtil.isNotEmpty(roomGroup, "该群不存在！");
        } else if (room.isRoomFriend()) {
            ChatRoomSelf roomSelf = roomSelfCache.get(roomId);
            AssertUtil.isNotEmpty(roomSelf, "该好友不存在！");
        } else if (room.isRoomAI()) {
            ChatRoomSelf roomSelf = roomSelfCache.get(roomId);
            AssertUtil.isNotEmpty(roomSelf, "该机器人已不存在！");
        } else {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "该会话类型不存在！");
        }
        // 删除会话
        boolean isDelContact = chatContactDAO.removeByRoomId(roomId, Collections.singletonList(uid));
        AssertUtil.isTrue(isDelContact, "删除会话失败！");
        return 1;
    }

    /**
     * 恢复会话
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoomVO restoreContactByRoomId(Long roomId, String uid) {
        AssertUtil.isFalse(ChatGroupConstant.HOT_ROOM_TOTAL_ID.equals(roomId), "全员群会话不需要恢复！");
        // 校验房间是否存在 并校验是否为禁用状态
        ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "该会话不存在！");
        if (room.isRoomGroup()) {
            ChatRoomGroup roomGroup = roomGroupCache.get(roomId);
            AssertUtil.isNotEmpty(roomGroup, "该群不存在！");
        } else if (room.isRoomFriend()) {
            ChatRoomSelf roomSelf = roomSelfDAO.getByRoomId(roomId);
            AssertUtil.isNotEmpty(roomSelf, "该好友不存在！");
        } else if (room.isRoomAI()) {
            ChatRoomSelf roomSelf = roomSelfDAO.getByRoomId(roomId);
            AssertUtil.isNotEmpty(roomSelf, "该机器人已不存在！");
        } else {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "该会话类型不存在！");
        }
        // 恢复会话
        int isRestoreContact = chatContactDAO.refreshOrCreateActiveTime(roomId, Collections.singletonList(uid), roomCache.get(roomId).getLastMsgId(), new Date());
        AssertUtil.isTrue(isRestoreContact == 1, "没有权限，请问恢复会话！");
        // 构建会话信息
        final HashMap<Long, ChatContact> contactMap = new HashMap<>();
        ChatContact chatContact = chatContactDAO.get(uid, roomId);
        AssertUtil.isNotEmpty(chatContact, ResultStatus.DELETE_ERR, "会话已经不存在！");
        contactMap.put(roomId, chatContact);
        // 构建房间信息
        List<ChatRoomVO> roomVOList = buildContactVO(uid, Collections.singletonList(roomId), contactMap);
        return roomVOList.get(0);
    }

    /**
     * 恢复会话 （私聊）
     *
     * @param friendId 好友id
     * @param uid      用户id
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoomVO restoreContactByFriendId(String friendId, String uid) {
        // check friendId
        ChatRoomSelf roomSelf = roomSelfDAO.getByKey(ChatAdapter.generateRoomKey(Arrays.asList(uid, friendId)));
        AssertUtil.isNotEmpty(roomSelf, "与对方已不是好友关系！");
        AssertUtil.isTrue(roomSelf.getStatus().equals(NormalOrNoEnum.NORMAL.getStatus()), "已经被对方删除拉黑！");
        // 恢复会话
        int isRestoreContact = chatContactDAO.refreshOrCreateActiveTime(roomSelf.getRoomId(), Collections.singletonList(uid), roomCache.get(roomSelf.getRoomId()).getLastMsgId(), new Date());
        AssertUtil.isTrue(isRestoreContact == 1, "没有权限，请问恢复会话！");
        // 构建会话信息
        final HashMap<Long, ChatContact> contactMap = new HashMap<>();
        ChatContact chatContact = chatContactDAO.get(uid, roomSelf.getRoomId());
        AssertUtil.isNotEmpty(chatContact, ResultStatus.DELETE_ERR, "会话已经不存在！");
        contactMap.put(roomSelf.getRoomId(), chatContact);
        // 构建房间信息
        List<ChatRoomVO> roomVOList = buildContactVO(uid, Collections.singletonList(roomSelf.getRoomId()), contactMap);
        return roomVOList.get(0);
    }

    private ChatRoom checkRoomStatus(Long roomId) {
        final ChatRoom room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "该会话不存在！");
        if (room.isRoomGroup()) {
            ChatRoomGroup roomGroup = roomGroupCache.get(roomId);
            AssertUtil.isNotEmpty(roomGroup, "该群不存在！");
        } else if (room.isRoomFriend()) {
            ChatRoomSelf roomSelf = roomSelfDAO.getByRoomId(roomId);
            AssertUtil.isNotEmpty(roomSelf, "该好友不存在！");
        } else if (room.isRoomAI()) {
            ChatRoomSelf roomAi = roomSelfDAO.getByRoomId(roomId);
            AssertUtil.isNotEmpty(roomAi, "该机器人已不存在！");
        } else {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "该会话类型不存在！");
        }
        return room;
    }

    /**
     * 置顶会话
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param type   置顶类型
     * @return 结果
     */
    @Override
    public WSPinContactMsg pinContact(Long roomId, String uid, Integer type) {
        checkRoomStatus(roomId);
        // 置顶会话
        Date pinTime = type == 1 ? new Date() : null;
        boolean isPinContact = chatContactDAO.updatePin(roomId, uid, pinTime);
        AssertUtil.isTrue(isPinContact, "置顶会话失败！");
        // 通知同一个账号的所有
        final WsBaseVO<WSPinContactMsg> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.PIN_CONTACT.getType());
        final WSPinContactMsg contactMsg = WSPinContactMsg
                .builder()
                .roomId(roomId)
                .isPin(type)
                .pinTime(pinTime != null ? pinTime.getTime() : null)
                .build();
        vo.setData(contactMsg);
        webSocketService.sendToUidList(vo, Collections.singletonList(uid));
        return contactMsg;
    }


    /**
     * 会话通知状态
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param status 状态
     * @return 结果
     */
    @Override
    public WSUpdateContactInfoMsg setNoticeStatus(Long roomId, String uid, Integer status) {
        checkRoomStatus(roomId);
        // 设置通知状态
        boolean isPinContact = chatContactDAO.updateNoticeStatus(roomId, uid, status);
        AssertUtil.isTrue(isPinContact, "设置消息通知状态失败！");
        // 通知同一个账号的所有
        final WsBaseVO<WSUpdateContactInfoMsg> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.PIN_CONTACT.getType());
        final WsBaseVO<WSUpdateContactInfoMsg> wsBaseVO = WSUpdateContactInfoMsg.buildWsNoticeBaseVO(roomId, status);
        webSocketService.sendToUidList(wsBaseVO, Collections.singletonList(uid));
        return wsBaseVO.getData();
    }

    /**
     * 设置免打扰状态
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param status 状态
     * @return 结果
     */
    @Override
    public WSUpdateContactInfoMsg setShieldStatus(Long roomId, String uid, Integer status) {
        checkRoomStatus(roomId);
        // 设置免打扰状态
        boolean done = chatContactDAO.updateShieldStatus(roomId, uid, status);
        AssertUtil.isTrue(done, "设置消息通知状态失败！");
        // 通知同一个账号的所有
        final WsBaseVO<WSUpdateContactInfoMsg> vo = WSUpdateContactInfoMsg.buildWsShieldBaseVO(roomId, status);
        webSocketService.sendToUidList(vo, Collections.singletonList(uid));
        return vo.getData();
    }
}
