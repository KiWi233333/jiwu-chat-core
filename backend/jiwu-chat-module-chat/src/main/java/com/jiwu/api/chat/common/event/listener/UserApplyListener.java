package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.common.main.dao.chat.ChatUserApplyDAO;
import com.jiwu.api.common.main.pojo.chat.ChatUserApply;
import com.jiwu.api.chat.common.event.UserApplyEvent;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.chat.common.vo.ws.WSFriendApply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

/**
 * 好友申请监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserApplyListener {
    @Resource
    private ChatUserApplyDAO userApplyDao;

    @Resource
    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = UserApplyEvent.class, fallbackExecution = true)
    public void notifyFriend(UserApplyEvent event) {
        ChatUserApply userApply = event.getUserApply();
        log.info("好友申请监听器，{} 目标 {}", userApply.getUserId(), userApply.getTargetId());
        Long unReadCount = userApplyDao.getUnReadCount(userApply.getTargetId());
        WSFriendApply vo = WSFriendApply.builder()
                 .apply(userApply)
                 .uid(userApply.getUserId())
                 .unreadCount(unReadCount)
                .build();
        pushService.sendPushMsg(WsAdapter.buildApplySend(vo), userApply.getTargetId()); // 推送给目标用户
    }

}
