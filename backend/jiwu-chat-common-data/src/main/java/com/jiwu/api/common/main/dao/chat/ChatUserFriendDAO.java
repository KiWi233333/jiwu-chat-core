package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.dto.chat.friend.ChatFriendPageBaseDTO;
import com.jiwu.api.common.main.dto.chat.friend.ChatFriendPageDTO;
import com.jiwu.api.common.main.dto.chat.vo.ChatRoomSelfVO;
import com.jiwu.api.common.main.mapper.chat.ChatUserFriendMapper;
import com.jiwu.api.common.main.pojo.chat.ChatUserFriend;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.vo.chat.ChatUserFriendVO;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;


/**
 * 好友表 服务实现类
 */
@Service
public class ChatUserFriendDAO extends MPJBaseServiceImpl<ChatUserFriendMapper, ChatUserFriend> {

    /**
     * 获取好友列表
     *
     * @param id  用户id
     * @param dto 分页
     * @return 数据
     */
    public CursorPageBaseVO<ChatUserFriend> getFriendPage(String id, CursorPageBaseDTO dto) {
        return CursorUtils.getCursorPageByMysql(this, dto,
                wrapper -> wrapper.eq(ChatUserFriend::getUserId, id), ChatUserFriend::getId, String.class);
    }

    public IPage<ChatRoomSelfVO> getFriendPage(String id, ChatFriendPageBaseDTO dto) {
        MPJLambdaWrapper<ChatUserFriend> qw = new MPJLambdaWrapper<>();
        qw
                // 用户
                .selectAs(User::getNickname, ChatRoomSelfVO::getName)
                .selectAs(User::getAvatar, ChatRoomSelfVO::getAvatar)
                .selectAs(User::getId, ChatRoomSelfVO::getTargetUid)
                // 联系人 - 删除状态
                .selectAs(ChatUserFriend::getDeleteStatus, ChatRoomSelfVO::getDeleteStatus)
                .selectAs(ChatUserFriend::getCreateTime, ChatRoomSelfVO::getCreateTime)

                .eq(ChatUserFriend::getUserId, id)
                .like(StringUtils.isNotBlank(dto.getKeyWord()), User::getNickname, dto.getKeyWord())
                .or()
                .like(StringUtils.isNotBlank(dto.getKeyWord()), User::getUsername, dto.getKeyWord())
                .leftJoin(User.class, User::getId, ChatUserFriend::getFriendUid);
        return this.selectJoinListPage(dto.plusPage(), ChatRoomSelfVO.class, qw);
    }

    public Page<ChatUserFriendVO> friendPageV2(Page<ChatUserFriendVO> page, String uid, ChatFriendPageDTO dto) {
        MPJLambdaWrapper<ChatUserFriend> qw = new MPJLambdaWrapper<>();
        qw
                .leftJoin(User.class, User::getId, ChatUserFriend::getFriendUid)
                .selectAs(User::getId, ChatUserFriendVO::getUserId)
                .selectAs(User::getAvatar, ChatUserFriendVO::getAvatar)
                .selectAs(User::getNickname, ChatUserFriendVO::getNickName)
                .selectAs(User::getUserType, ChatUserFriendVO::getType)
                .selectAs(User::getActiveStatus, ChatUserFriendVO::getActiveStatus)
                .eq(ChatUserFriend::getUserId, uid);
        if (StringUtils.isNotBlank(dto.getKeyWord())) {
            qw.like(User::getNickname, dto.getKeyWord());
        }
        return this.getBaseMapper().selectJoinPage(page, ChatUserFriendVO.class, qw);
    }


    public ChatUserFriend getByFriend(String userId, String targetUid) {
        return lambdaQuery()
                .eq(ChatUserFriend::getFriendUid, userId)
                .eq(ChatUserFriend::getFriendUid, targetUid)
                .one();
    }

    /**
     * 确认是否存在好友（优化）
     *
     * @param userId    用户id
     * @param targetUid 目标id
     * @return 数据
     */
    public ChatUserFriend checkFriend(String userId, String targetUid) {
        return lambdaQuery()
                .eq(ChatUserFriend::getFriendUid, userId)
                .eq(ChatUserFriend::getFriendUid, targetUid)
                .select(ChatUserFriend::getId, ChatUserFriend::getUserId)
                .one();
    }

    public List<ChatUserFriend> getUserFriend(String uid, String friendUid) {
        return lambdaQuery()
                .eq(ChatUserFriend::getUserId, uid)
                .eq(ChatUserFriend::getFriendUid, friendUid)
                .or()
                .eq(ChatUserFriend::getFriendUid, uid)
                .eq(ChatUserFriend::getUserId, friendUid)
                .select(ChatUserFriend::getId)
                .list();
    }

    public List<ChatUserFriend> getByFriends(String uid, List<String> friendUidList) {
        if (CollectionUtils.isEmpty(friendUidList)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(ChatUserFriend::getUserId, uid)
                .in(ChatUserFriend::getFriendUid, friendUidList)
                .or()
                .eq(ChatUserFriend::getFriendUid, uid)
                .in(ChatUserFriend::getUserId, friendUidList)
//                .select(ChatUserFriend::getUserId)
                .list();
    }

}
