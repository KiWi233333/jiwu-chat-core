package com.jiwu.api.chat.service;


import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatRoomGroup;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.main.dto.chat.contact.ContactPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.req.InsertRoomDTO;
import com.jiwu.api.common.main.dto.chat.req.SelectGroupMemberPageDTO;
import com.jiwu.api.chat.common.vo.ChatMemberVO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.main.vo.chat.ChatMemberListVO;
import com.jiwu.api.chat.common.vo.room.ChatRoomInfoVO;
import com.jiwu.api.chat.common.vo.ws.WSPinContactMsg;
import com.jiwu.api.chat.common.vo.ws.WSUpdateContactInfoMsg;

import java.util.List;

public interface ChatRoomService {


    /**
     * 获取房间列表（分页）
     *
     * @param dto 参数
     * @param uid 用户id
     * @return 数据
     */
    CursorPageBaseVO<ChatRoomVO> getContactPage(ContactPageBaseDTO dto, String uid);


    /**
     * 获取用户房间的会话详情（群聊）
     *
     * @param uid    用户id
     * @param roomId 房间id
     * @return 数据
     */
    ChatRoomVO getContactDetail(String uid, Long roomId);

    /**
     * 获取会话详情（单聊）
     *
     * @param uid    用户id
     * @param roomId 好友id
     * @return 数据
     */
    ChatRoomVO getContactDetailByRoom(String uid, Long roomId);

    /**
     * 获取会话详情（单聊）
     *
     * @param uid      用户id
     * @param friendId 好友id
     * @return 数据
     */
    ChatRoomVO getContactDetailByFriend(String uid, String friendId);

    /**
     * 获取群聊信息
     *
     * @param uid    用户id
     * @param roomId id
     * @return 参数
     */
    ChatRoomInfoVO getGroupDetail(String uid, Long roomId);


    /**
     * 创建房间（单聊）
     *
     * @param roomType 房间类型
     * @param uidList  uid list
     * @return 房间信息
     */
    ChatRoomSelf createFriendRoom(RoomTypeEnum roomType, List<String> uidList);

    /**
     * 获取单聊房间信息（单聊）
     *
     * @param uid1 用户1
     * @param uid2 用户2
     * @return
     */
    ChatRoomSelf getFriendRoom(String uid1, String uid2);

    /**
     * 禁用单聊房间（单聊）
     *
     * @param uidList 用户id
     */
    Long disableFriendRoom(List<String> uidList);


    /**
     * 创建一个群聊房间
     */
    ChatRoomGroup createGroupRoom(InsertRoomDTO dto);


    /**
     * 获取群聊成员列表（分页）
     *
     * @param dto 参数
     * @return 数据
     */
    CursorPageBaseVO<ChatMemberVO> getGroupMemberPage(SelectGroupMemberPageDTO dto);

    /**
     * 获取群聊成员列表
     *
     * @param roomId 房间id
     * @return 列表
     */
    List<ChatMemberListVO> getMemberList(Long roomId);

    /**
     * 删除会话（后续可重新拉取）
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @return 结果
     */
    Integer deleteContact(Long roomId, String uid);

    /**
     * 置顶会话
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param type   类型
     * @return 结果
     */
    WSPinContactMsg pinContact(Long roomId, String uid, Integer type);

    /**
     * 恢复会话
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @return 结果
     */
    ChatRoomVO restoreContactByRoomId(Long roomId, String uid);

    /**
     * 恢复会话 （私聊）
     *
     * @param friendId 好友id
     * @param uid      用户id
     * @return 结果
     */
    ChatRoomVO restoreContactByFriendId(String friendId, String uid);

    /**
     * 设置会话通知状态
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param status 状态
     * @return 结果
     */
    WSUpdateContactInfoMsg setNoticeStatus(Long roomId, String uid, Integer status);

    /**
     * 设置免打扰状态
     *
     * @param roomId 房间id
     * @param uid    用户id
     * @param status 状态
     * @return 结果
     */
    WSUpdateContactInfoMsg setShieldStatus(Long roomId, String uid, Integer status);
}
