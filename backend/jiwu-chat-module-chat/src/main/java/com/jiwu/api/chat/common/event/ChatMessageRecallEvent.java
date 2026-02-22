package com.jiwu.api.chat.common.event;

import com.jiwu.api.chat.common.dto.ChatMsgRecallDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 撤回消息事件
 */
@Getter
public class ChatMessageRecallEvent extends ApplicationEvent {

    private final ChatMsgRecallDTO recallDTO;

    public ChatMessageRecallEvent(Object source, ChatMsgRecallDTO recallDTO) {
        super(source);
        this.recallDTO = recallDTO;
    }

}
