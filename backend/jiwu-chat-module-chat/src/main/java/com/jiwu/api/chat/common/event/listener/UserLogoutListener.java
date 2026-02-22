package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.common.main.event.user.UserLogoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 用户退出登录事件监听器
 * 监听来自 user 模块的退出登录事件，处理 WebSocket 连接断开
 *
 * @author Kiwi23333
 */
@Slf4j
@Component
public class UserLogoutListener {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 处理用户退出登录事件
     * 断开对应的 WebSocket 连接
     *
     * @param event 退出登录事件
     */
    @Async
    @EventListener(classes = UserLogoutEvent.class)
    public void handleUserLogout(UserLogoutEvent event) {
        try {
            log.info("收到用户退出登录事件，userId: {}, isAll: {}", 
                    event.getUserTokenDTO().getId(), event.getIsAll());
            webSocketService.logout(event.getUserTokenDTO(), event.getIsAll());
        } catch (Exception e) {
            log.error("处理用户退出登录事件失败，userId: {}", 
                    event.getUserTokenDTO().getId(), e);
        }
    }
}

