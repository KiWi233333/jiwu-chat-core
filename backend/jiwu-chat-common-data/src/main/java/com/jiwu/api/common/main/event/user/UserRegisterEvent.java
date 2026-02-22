package com.jiwu.api.common.main.event.user;

import com.jiwu.api.common.main.pojo.sys.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 * 用于解耦 user 模块和 chat 模块之间的依赖
 *
 * @author Kiwi23333
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {
    private final User user;

    public UserRegisterEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}

