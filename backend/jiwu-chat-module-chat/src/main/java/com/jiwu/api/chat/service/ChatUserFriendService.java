package com.jiwu.api.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.dto.chat.friend.*;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomSelfVO;
import com.jiwu.api.chat.common.vo.ChatUserFriendUnReadVO;
import com.jiwu.api.chat.common.vo.PageBaseVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendApplyVO;
import com.jiwu.api.chat.common.vo.friend.ChatUserFriendCheckVO;
import com.jiwu.api.common.main.vo.chat.ChatUserFriendVO;

public interface ChatUserFriendService {


    /**
     * 获取分页联系人列表
     *
     * @param uid  用户uid
     * @param dto 参数
     * @return 列表
     */
    CursorPageBaseVO<ChatUserFriendVO> friendPage(String uid, CursorPageBaseDTO dto);

    IPage<ChatUserFriendVO> friendPageV2(Integer page, Integer size, String uid, ChatFriendPageDTO dto);

    /**
     * 获取私聊房间列表（分页）
     *
     * @param dto  参数
     * @param uid  用户id
     * @return 列表
     */
    IPage<ChatRoomSelfVO> getFriendRoomPage(ChatFriendPageBaseDTO dto, String uid);

    /**
     * 确认是否存在好友关系
     *
     * @param uid 用户id
     * @param dto 参数
     * @return ChatUserFriendCheckVO
     */
    ChatUserFriendCheckVO check(String uid, ChatUserFriendCheckDTO dto);

    /**
     * 好友申请
     *
     * @param targetUId 用户id
     * @param dto       参数
     * @return 影响
     */
    Integer apply(String targetUId, ChatUserFriendApplyDTO dto);


    /**
     * 好友申请
     *
     * @param userId 用户id
     * @param dto    参数
     * @return 影响
     */
    PageBaseVO<ChatUserFriendApplyVO> pageApplyFriend(String userId, PageBaseDTO dto);

    /**
     * 同意好友申请
     *
     * @param selfUserId selfU户userId
     * @param dto        参数
     * @return 影响
     */
    Integer applyApprove(String selfUserId, ChatUserFriendApproveDTO dto);

    /**
     * 拒绝好友申请
     *
     * @param uid     用户id
     * @param applyId 好友申请id
     * @return 影响
     */
    Integer deleteApply(String uid, ChatUserFriendRejectDTO applyId);

    /**
     * 删除好友
     *
     * @param id        用户id
     * @param targetUid 目标用户id
     * @return 影响
     */
    Integer deleteFriend(String id, String targetUid);

    /**
     * 获取申请未读数
     *
     * @param id 用户id
     * @return ChatUserFriendUnReadVO
     */
    ChatUserFriendUnReadVO getUnread(String id);

    /**
     * 获取阅\未读数
     *
     * @param uid 用户id
     * @param dto 参数
     * @return 数量
     */
    Integer getReadCount(String uid, SelectApplyReadDTO dto);

}
