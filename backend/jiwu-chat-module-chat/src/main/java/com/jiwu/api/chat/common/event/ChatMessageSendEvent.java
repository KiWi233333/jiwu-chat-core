package com.jiwu.api.chat.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatMessageSendEvent extends ApplicationEvent {
    private Long msgId;
    private String clientId;

    public ChatMessageSendEvent(Object source, Long msgId) {
        super(source);
        this.msgId = msgId;
    }
    public ChatMessageSendEvent(Object source, Long msgId, String clientId) {
        super(source);
        this.msgId = msgId;
        this.clientId = clientId;
    }
}
