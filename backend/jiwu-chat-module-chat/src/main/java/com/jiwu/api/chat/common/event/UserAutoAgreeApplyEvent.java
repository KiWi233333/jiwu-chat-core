package com.jiwu.api.chat.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserAutoAgreeApplyEvent extends ApplicationEvent {
    private final String userId;
    private final Long applyId;

    public UserAutoAgreeApplyEvent(Object source, String userId, Long applyId) {
        super(source);
        this.userId = userId;
        this.applyId = applyId;
    }

}
