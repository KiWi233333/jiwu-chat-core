package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.chat.common.event.UserOfflineEvent;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.common.main.cache.user.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 用户下线监听器
 */
@Slf4j
@Component
public class UserOfflineListener {
    @Resource
    private WebSocketService webSocketService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserCache userCache;
    @Resource
    private WsAdapter wsAdapter;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        userCache.offline(user.getId(), user.getLastLoginTime());
        //推送给所有在线用户，该用户下线
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyVO(event.getUser()), user.getId());
    }

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveDB(UserOfflineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(user.getLastLoginTime());
        update.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        userMapper.updateById(update);
    }

}
