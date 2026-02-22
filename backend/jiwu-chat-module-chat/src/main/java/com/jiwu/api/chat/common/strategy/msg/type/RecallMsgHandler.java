package com.jiwu.api.chat.common.strategy.msg.type;

import com.jiwu.api.chat.common.utils.OssCheckUtil;
import com.jiwu.api.common.main.dao.chat.ChatMessageDAO;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.chat.common.dto.ChatMsgRecallDTO;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.MsgRefundDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.event.ChatMessageRecallEvent;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.common.main.cache.user.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.Objects;


/**
 * Description: 撤回文本消息
 * Date: 2023-12-04
 */
@Component
@Slf4j
public class RecallMsgHandler extends AbstractMsgHandler<ChatMsgRecallDTO> {
    @Resource
    private ChatMessageDAO messageDao;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private OssCheckUtil ossCheckUtil;
    @Resource
    private UserCache userCache;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    /**
     * 保存消息
     *
     * @param message 消息
     * @param body    参数
     */
    @Override
    protected void saveMsg(ChatMessage message, ChatMsgRecallDTO body) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object showMsg(ChatMessage msg) {
        return getRecallMsg(msg);
    }

    @Override
    public Object showReplyMsg(ChatMessage msg) {
        return "原消息已被撤回";
    }

    @Transactional(rollbackFor = Exception.class)
    public void recall(String recallUid, ChatMessage message) {
        // 1.更新消息
        ChatMessageExtra extra = message.getExtra();
        extra.setRecall(new MsgRefundDTO(recallUid, new Date()));
        ChatMessage update = new ChatMessage();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.RECALL.getType());
        update.setExtra(extra);
        messageDao.updateById(update);
        // 2.需要删除文件的
        final boolean checkAndDeleteOssFile = ossCheckUtil.checkAndDeleteOssFile(message);
        if (!checkAndDeleteOssFile) {
            log.error("OSS文件删除删除失败，消息附件不存在，{}", message);
        }
        applicationEventPublisher.publishEvent(new ChatMessageRecallEvent(this,
                new ChatMsgRecallDTO()
                        .setRecallUid(recallUid)
                        .setMsgId(message.getId())
                        .setRoomId(message.getRoomId())
        ));
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return getRecallMsg(msg);
    }

    private String getRecallMsg(ChatMessage msg) {
        final MsgRefundDTO recall = msg.getExtra().getRecall();
        User userInfo = userCache.getUserInfo(msg.getFromUid());
        if (!Objects.equals(recall.getRecallUid(), msg.getFromUid())) {
            return String.format("管理员撤回了一条\"%s\"成员消息", userInfo.getNickname());
        }
        String myUid = RequestHolderUtil.get().getId();
        return String.format("%s撤回了一条消息", msg.getFromUid().equals(myUid) ? "我" : userInfo.getNickname());
    }


}
