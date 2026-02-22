package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.chat.common.event.UserOnlineEvent;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.common.main.cache.user.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户上线监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserOnlineListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCache userCache;
    @Autowired
    private WsAdapter wsAdapter;
    @Autowired
    private PushService pushService;

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveRedisAndPush(UserOnlineEvent event) {
        User user = event.getUser();
        userCache.online(user.getId(), user.getLastLoginTime());
        //推送给所有在线用户，该用户登录成功
        pushService.sendPushMsg(wsAdapter.buildOnlineNotifyVO(event.getUser()));
    }

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(user.getLastLoginTime());
        update.setLastLoginIp(user.getLastLoginIp());
        update.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        userMapper.updateById(update);
    }

}
