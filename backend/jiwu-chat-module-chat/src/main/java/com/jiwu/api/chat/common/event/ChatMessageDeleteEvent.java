package com.jiwu.api.chat.common.event;

import com.jiwu.api.chat.common.dto.ChatMsgDeleteDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 删除消息事件
 */
@Getter
public class ChatMessageDeleteEvent extends ApplicationEvent {

    private final ChatMsgDeleteDTO deleteDTO;

    public ChatMessageDeleteEvent(Object source, ChatMsgDeleteDTO deleteDTO) {
        super(source);
        this.deleteDTO = deleteDTO;
    }

}
