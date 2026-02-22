package com.jiwu.api.common.main.event.user;

import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户退出登录事件
 * 用于解耦 user 模块和 chat 模块之间的依赖
 *
 * @author Kiwi23333
 */
@Getter
public class UserLogoutEvent extends ApplicationEvent {
    
    /**
     * 用户令牌信息
     */
    private final UserTokenDTO userTokenDTO;
    
    /**
     * 是否退出所有设备
     * true: 退出所有设备
     * false: 只退出当前设备
     */
    private final Boolean isAll;

    public UserLogoutEvent(Object source, UserTokenDTO userTokenDTO, Boolean isAll) {
        super(source);
        this.userTokenDTO = userTokenDTO;
        this.isAll = isAll;
    }
}

