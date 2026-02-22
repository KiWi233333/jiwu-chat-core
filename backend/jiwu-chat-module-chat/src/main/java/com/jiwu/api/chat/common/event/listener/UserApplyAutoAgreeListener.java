//package com.jiwu.api.core.chat.common.event.listener;
//
//import com.baomidou.mybatisplus.core.toolkit.Assert;
//import friend.dto.common.chat.core.com.jiwu.api.ChatUserFriendApproveDTO;
//import event.common.chat.core.com.jiwu.api.UserAutoAgreeApplyEvent;
//import service.chat.core.com.jiwu.api.ChatUserFriendService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//import jakarta.annotation.Resource;
//
///**
// * 好友申请监听器
// *
// * @author zhongzb create on 2022/08/26
// */
//@Slf4j
//@Component
//public class UserApplyAutoAgreeListener {
//
//    @Resource(name = "chatUserFriendServiceImpl")
//    private ChatUserFriendService chatUserFriendService;
//
//    @Async
//    @TransactionalEventListener(classes = UserAutoAgreeApplyEvent.class, fallbackExecution = false)
//    public void onApplicationEvent(UserAutoAgreeApplyEvent event) {
//        log.info("收到自动好友申请事件：{}", event);
//        final Integer integer = chatUserFriendService.applyApprove(event.getUserId(), ChatUserFriendApproveDTO
//                .builder()
//                .applyId(event.getApplyId())
//                .build());
//        Assert.isTrue(integer == 1, "自动同意好友申请失败！");
//    }
//
//}
