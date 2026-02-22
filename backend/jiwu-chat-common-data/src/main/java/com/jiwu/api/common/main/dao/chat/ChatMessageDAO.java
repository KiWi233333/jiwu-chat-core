package com.jiwu.api.common.main.dao.chat;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 * @since 2023-03-25
 */
@Service
public class ChatMessageDAO extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    // 分页获取消息
    public CursorPageBaseVO<ChatMessage> getCursorPage(Long roomId, CursorPageBaseDTO request, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(ChatMessage::getRoomId, roomId);
            wrapper.eq(ChatMessage::getStatus, MessageStatusEnum.NORMAL.getStatus());
            wrapper.le(Objects.nonNull(lastMsgId), ChatMessage::getId, lastMsgId);
        }, ChatMessage::getId, Long.class);
    }

    /**
     * 乐观更新消息类型
     */
    public boolean riseOptimistic(Long id, Integer oldType, Integer newType) {
        return lambdaUpdate()
                .eq(ChatMessage::getId, id)
                .eq(ChatMessage::getType, oldType)
                .set(ChatMessage::getType, newType)
                .update();
    }

    public Long getGapCount(Long roomId, Long fromId, Long toId) {
        return lambdaQuery()
                .eq(ChatMessage::getRoomId, roomId)
                .gt(ChatMessage::getId, fromId)
                .le(ChatMessage::getId, toId)
                .count();
    }

    public void invalidByUid(Long uid) {
        lambdaUpdate()
                .eq(ChatMessage::getFromUid, uid)
                .set(ChatMessage::getStatus, MessageStatusEnum.DELETE.getStatus())
                .update();
    }

    public Long getUnReadCount(Long roomId, Date readTime) {
        return lambdaQuery()
                .eq(ChatMessage::getRoomId, roomId)
                .gt(Objects.nonNull(readTime), ChatMessage::getCreateTime, readTime)
                .count();
    }

    /**
     * 根据房间ID逻辑删除消息
     *
     * @param roomId  房间ID
     * @param uidList 群成员列表
     * @return 是否删除成功
     */
    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new UpdateWrapper<ChatMessage>().lambda()
                .eq(ChatMessage::getRoomId, roomId)
                .in(CollUtil.isNotEmpty(uidList), ChatMessage::getFromUid, uidList)
                .set(ChatMessage::getStatus, MessageStatusEnum.DELETE.getStatus());
        return this.update(wrapper);
    }


    public Boolean removeDeByRoomId(Long roomId, List<Long> uidList) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new UpdateWrapper<ChatMessage>().lambda()
                .eq(ChatMessage::getRoomId, roomId)
                .in(CollUtil.isNotEmpty(uidList), ChatMessage::getFromUid, uidList)
                .set(ChatMessage::getStatus, MessageStatusEnum.DELETE.getStatus());
        return this.remove(wrapper);
    }

}
