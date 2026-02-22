package com.jiwu.api.chat.common.event;

import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserApplyEvent extends ApplicationEvent {

    private final ChatUserApply userApply;

    public UserApplyEvent(Object source, ChatUserApply userApply) {
        super(source);
        this.userApply = userApply;
    }

}
