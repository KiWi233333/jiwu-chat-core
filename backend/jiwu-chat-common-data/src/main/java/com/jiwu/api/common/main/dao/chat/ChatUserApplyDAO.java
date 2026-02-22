package com.jiwu.api.common.main.dao.chat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.enums.chat.ApplyReadStatusEnum;
import com.jiwu.api.common.main.enums.chat.ApplyTypeEnum;
import com.jiwu.api.common.main.enums.chat.FriendApplyStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatUserApplyMapper;
import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友表 服务实现类
 *
 * @since 2023-03-25
 */
@Service
public class ChatUserApplyDAO extends ServiceImpl<ChatUserApplyMapper, ChatUserApply> {


    public void readApples(String uid, List<Long> applyIds) {
        lambdaUpdate()
                .set(ChatUserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())// 设置为已读
                .eq(ChatUserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())// 筛选未读的
                .in(ChatUserApply::getId, applyIds)
                .eq(ChatUserApply::getTargetId, uid)
                .update();
    }

    // 获取申请好友列表
    public IPage<ChatUserApply> friendApplyPage(String uid, Page page) {
        return lambdaQuery() // 添加 ChatUserApply.class
                .eq(ChatUserApply::getTargetId, uid)
                .eq(ChatUserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .orderByDesc(ChatUserApply::getCreateTime)
                .page((Page<ChatUserApply>) page);
    }

    public ChatUserApply getFriendApproving(String userId, String targetUid) {
        return lambdaQuery().eq(ChatUserApply::getUserId, userId)
                .eq(ChatUserApply::getTargetId, targetUid)
                .eq(ChatUserApply::getStatus,   FriendApplyStatusEnum.WAIT_APPROVAL.getCode())
                .eq(ChatUserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();
    }

    public ChatUserApply getFriendApply(String userId, String targetUid) {
        return getFriendApply(userId, targetUid, null);
    }

    public ChatUserApply getFriendApply(String userId, String targetUid, FriendApplyStatusEnum statusEnum) {
        return lambdaQuery().eq(ChatUserApply::getUserId, userId)
                .eq(ChatUserApply::getTargetId, targetUid)
                .eq(statusEnum != null, ChatUserApply::getStatus, statusEnum != null ? statusEnum.getCode() : null)
                .eq(ChatUserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();
    }

    public Long getUnReadCount(String userId) {
        return lambdaQuery().eq(ChatUserApply::getTargetId, userId)
                .eq(ChatUserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .count();
    }

    public boolean setAgree(Long applyId) {
        return lambdaUpdate().eq(ChatUserApply::getId, applyId)
                .set(ChatUserApply::getStatus, FriendApplyStatusEnum.AGREE.getCode())
                .update();
    }

    public boolean setReject(Long applyId) {
        return lambdaUpdate().eq(ChatUserApply::getId, applyId)
                .set(ChatUserApply::getStatus, FriendApplyStatusEnum.REJECT.getCode())
                .update();
    }
}
