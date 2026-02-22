package com.jiwu.api.chat.common.strategy.msg.type;

import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageExtra;
import com.jiwu.api.common.main.dto.chat.msg.body.SoundMsgDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:语音消息
 * Date: 2024-07-03
 */
@Component
public class SoundMsgHandler extends AbstractMsgHandler<SoundMsgDTO> {
    @Autowired
    private ChatMessageMapper messageMapper;
    @Autowired
    private OssFileUtil ossFileUtil;

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SOUND;
    }

    @Override
    public void saveMsg(ChatMessage msg, SoundMsgDTO body) {
        ChatMessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new ChatMessageExtra());
        ChatMessage update = new ChatMessage();
        update.setId(msg.getId());
        extra.setSoundMsgDTO(body);

        // 1、消费语音文件
        String key = extra.getSoundMsgDTO().getUrl();
        if (StringUtil.isNullOrEmpty(key) || !ossFileUtil.deleteRedisKey(msg.getFromUid(), key)) {
            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "语音已失效或不存在！");
        }
        // 2、修改消息
        update.setExtra(extra);
        messageMapper.updateById(update);
    }

    @Override
    public Object showMsg(ChatMessage msg) {
        return msg.getExtra().getSoundMsgDTO();
    }

    @Override
    public Object showReplyMsg(ChatMessage msg) {
        return getSoundFormatContactText(msg);
    }

    @Override
    public String showContactMsg(ChatMessage msg) {
        return getSoundFormatContactText(msg);
    }

    private String getSoundFormatContactText(ChatMessage msg) {
        if (msg.getExtra() != null && msg.getExtra().getSoundMsgDTO() != null) {
            Long length = msg.getExtra().getSoundMsgDTO().getSecond();
            return String.format("[语音] %s", getSoundFormatText(length));
        }
        return "[语音]";
    }

    private String getSoundFormatText(Long length) {
        return length > 60 ? length / 60 + "'" + length % 60 + "\"" : length + "\"";
    }


}
