package com.jiwu.api.chat.common.strategy.msg.type;

import com.jiwu.api.chat.common.utils.OssCheckUtil;
import com.jiwu.api.common.main.dao.chat.ChatMessageDAO;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.chat.common.dto.ChatMsgDeleteDTO;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.DeleteMsgDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.event.ChatMessageDeleteEvent;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.common.main.cache.user.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * Description: 删除文本消息
 */
@Component
@Slf4j
public class DelMsgHandler extends AbstractMsgHandler<Object> {
    @Autowired
    private ChatMessageDAO messageDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private OssFileUtil ossFileUtil;
    @Autowired
    private OssCheckUtil ossCheckUtil;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.DEL_MSG;
    }

    @Override
    public void saveMsg(ChatMessage msg, Object body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object showMsg(ChatMessage msg) {
        return getDeleteMsgText(msg);
    }

    @Override
    public Object showReplyMsg(ChatMessage msg) {
        return "原消息已被删除";
    }

    /**
     * 会话列表——展示的消息
     */
    @Override
    public String showContactMsg(ChatMessage msg) {
        return getDeleteMsgText(msg);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String deleteId, ChatMessage message) {
        ChatMessageExtra extra = message.getExtra();
        extra.setDelete(new DeleteMsgDTO(deleteId, new Date()));
        // 1.更新数据库
        ChatMessage update = new ChatMessage();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.DEL_MSG.getType());
        update.setExtra(extra);
        messageDao.updateById(update);
        // 2.需要删除文件的
        final boolean checkAndDeleteOssFile = ossCheckUtil.checkAndDeleteOssFile(message);
        if (!checkAndDeleteOssFile) {
            log.error("OSS文件删除删除失败，消息附件不存在，{}", message);
        }
        applicationEventPublisher.publishEvent(new ChatMessageDeleteEvent(this,
                new ChatMsgDeleteDTO()
                        .setDeleteUid(deleteId)
                        .setMsgId(message.getId())
                        .setRoomId(message.getRoomId())
        ));
    }

    private String getDeleteMsgText(ChatMessage msg) {
        DeleteMsgDTO delete = msg.getExtra().getDelete();
        if (delete == null) {
            return "删除了一条消息";
        }
        User userInfo = userCache.getUserInfo(msg.getFromUid());
        if (!Objects.equals(delete.getDeleteUid(), msg.getFromUid())) {
            return String.format("管理员删除了一条\"%s\"成员消息", userInfo.getNickname());
        }
        String myUid = RequestHolderUtil.get().getId();
        return String.format("%s删除了一条消息", msg.getFromUid().equals(myUid) ? "我" : userInfo.getNickname());
    }

}
