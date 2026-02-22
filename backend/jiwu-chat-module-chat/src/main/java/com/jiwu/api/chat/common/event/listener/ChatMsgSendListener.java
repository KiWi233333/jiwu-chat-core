package com.jiwu.api.chat.common.event.listener;

import com.jiwu.api.common.main.constant.chat.ChatMqConstant;
import com.jiwu.api.common.main.dto.chat.mq.ChatMsgMqDTO;
import com.jiwu.api.chat.common.event.ChatMessageSendEvent;
import com.jiwu.api.chat.common.event.producer.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

/**
 * 用户发送消息监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class ChatMsgSendListener {

    @Resource
    private MqProducer<ChatMsgMqDTO> mQProducer;


    /**
     * 消息监听事件 (事务提交前)
     * 也是可靠消息，影响事务提交
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = ChatMessageSendEvent.class, fallbackExecution = true)
    public void messageRoute(ChatMessageSendEvent event) {
        Long msgId = event.getMsgId();
        String clientId = event.getClientId();
        log.info("消息监听事件，msgId={}", msgId);
        mQProducer.sendSecureMsg(ChatMqConstant.DIRECT_SEND_CHAT_KEY, new ChatMsgMqDTO(msgId, clientId), msgId);
    }


}
