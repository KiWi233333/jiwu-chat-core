package com.jiwu.api.common.main.dao.chat;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiwu.api.common.main.dto.chat.contact.ContactPageBaseDTO;
import com.jiwu.api.common.main.mapper.chat.ChatContactMapper;
import com.jiwu.api.common.main.pojo.chat.ChatContact;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话表 服务实现类
 * </p>
 *
 * @since 2023-03-25
 */
@Service
public class ChatContactDAO extends ServiceImpl<ChatContactMapper, ChatContact> {

    public ChatContact get(String uid, Long roomId) {
        return lambdaQuery()
                .eq(ChatContact::getUserId, uid)
                .eq(ChatContact::getRoomId, roomId)
                .one();
    }

    public Long getReadCount(ChatMessage message) {
        return lambdaQuery()
                .eq(ChatContact::getRoomId, message.getRoomId())
                .ne(ChatContact::getUserId, message.getFromUid())// 不需要查询出自己
                .ge(ChatContact::getReadTime, message.getCreateTime())
                .count();
    }

    public Long getTotalCount(Long roomId) {
        return lambdaQuery()
                .eq(ChatContact::getRoomId, roomId)
                .count();
    }

    public Long getUnReadCount(ChatMessage message) {
        return lambdaQuery()
                .eq(ChatContact::getRoomId, message.getRoomId())
                .lt(ChatContact::getReadTime, message.getCreateTime())
                .count();
    }

    public CursorPageBaseVO<ChatContact> getReadPage(ChatMessage message, CursorPageBaseDTO cursorPageBaseReq) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq, wrapper -> {
            wrapper.eq(ChatContact::getRoomId, message.getRoomId());
            wrapper.ne(ChatContact::getUserId, message.getFromUid());// 不需要查询出自己
            wrapper.ge(ChatContact::getReadTime, message.getCreateTime());// 已读时间大于等于消息发送时间
        }, ChatContact::getReadTime, Date.class);
    }

    public CursorPageBaseVO<ChatContact> getUnReadPage(ChatMessage message, CursorPageBaseDTO cursorPageBaseReq) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq, wrapper -> {
            wrapper.eq(ChatContact::getRoomId, message.getRoomId());
            wrapper.ne(ChatContact::getUserId, message.getFromUid());// 不需要查询出自己
            wrapper.lt(ChatContact::getReadTime, message.getCreateTime());// 已读时间小于消息发送时间
        }, ChatContact::getReadTime, Date.class);
    }

    /**
     * 获取用户会话列表
     */
    public CursorPageBaseVO<ChatContact> getContactPage(String uid, ContactPageBaseDTO dto) {
        return CursorUtils.getCursorJoinPageByMysql(this, dto, wq -> {
            wq.selectAll(ChatContact.class).eq(ChatContact::getUserId, uid);
            // 类型
            if (dto.getType() != null) {
                wq.eq(ChatRoom::getType, dto.getType())
                        .leftJoin(ChatRoom.class, ChatRoom::getId, ChatContact::getRoomId);
            }
        }, ChatContact::getActiveTime, Date.class);
    }

    public List<ChatContact> getByRoomIds(List<Long> roomIds, String uid) {
        return lambdaQuery()
                .in(ChatContact::getRoomId, roomIds)
                .eq(ChatContact::getUserId, uid)
                .list();
    }

    /**
     * 更新所有人的会话时间，没有就直接插入
     */
    public int refreshOrCreateActiveTime(Long roomId, List<String> memberUidList, Long msgId, Date activeTime) {
        if (memberUidList.isEmpty()) {
            return 0;
        }
        return baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, activeTime);
    }

    /**
     * 更新所有人的会话时间，没有就直接插入
     */
    public int refreshOrCreateReadTime(Long roomId, List<String> memberUidList, Date readTime) {
        if (memberUidList.isEmpty()) {
            return 0;
        }
        return baseMapper.refreshOrCreateReadTime(roomId, memberUidList, readTime);
    }

    /**
     * 删除会话(房间号)
     *
     * @param roomId  房间id
     * @param uidList 用户
     * @return 是否删除
     */
    public boolean removeByRoomId(Long roomId, List<String> uidList) {
        LambdaQueryWrapper<ChatContact> wrapper = new QueryWrapper<ChatContact>().lambda()
                .eq(ChatContact::getRoomId, roomId)
                .in(CollUtil.isNotEmpty(uidList), ChatContact::getUserId, uidList);
        return this.remove(wrapper);
    }

    /**
     * 更新置顶状态
     *
     * @param roomId  房间号
     * @param uid     用户id
     * @param pinTime 置顶时间
     */
    public boolean updatePin(Long roomId, String uid, Date pinTime) {
        return lambdaUpdate()
                .eq(ChatContact::getRoomId, roomId)
                .eq(ChatContact::getUserId, uid)
                .set(ChatContact::getPinTime, pinTime)
                .update();
    }

    /**
     * 更新提醒状态
     *
     * @param roomId       房间号
     * @param uid          用户id
     * @param noticeStatus 提醒状态
     */
    public boolean updateNoticeStatus(Long roomId, String uid, Integer noticeStatus) {
        return lambdaUpdate()
                .eq(ChatContact::getRoomId, roomId)
                .eq(ChatContact::getUserId, uid)
                .set(ChatContact::getNoticeStatus, noticeStatus)
                .update();
    }

    public boolean updateShieldStatus(Long roomId, String uid, Integer status) {
        return lambdaUpdate()
                .eq(ChatContact::getRoomId, roomId)
                .eq(ChatContact::getUserId, uid)
                .set(ChatContact::getShieldStatus, status)
                .update();
    }

    public List<ChatContact> getContactListByIds(String uid, List<Long> hotRoomIds) {
        return lambdaQuery()
                .eq(ChatContact::getUserId, uid)
                .in(ChatContact::getRoomId, hotRoomIds)
                .list();
    }
}
