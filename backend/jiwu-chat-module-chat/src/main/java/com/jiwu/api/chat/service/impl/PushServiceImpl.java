package com.jiwu.api.chat.service.impl;

import com.jiwu.api.chat.common.vo.WsBaseVO;
import com.jiwu.api.common.main.constant.chat.ChatMqConstant;
import com.jiwu.api.chat.common.dto.PushMessageDTO;
import com.jiwu.api.chat.common.event.producer.MqProducer;
import com.jiwu.api.chat.service.PushService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Description:
 * Date: 2023-08-12
 */
@Service
public class PushServiceImpl implements PushService {

    @Resource
    private MqProducer<PushMessageDTO> mqProducer;

    @Override
    public void sendPushMsg(WsBaseVO<?> msg, List<String> uidList) {
        mqProducer.sendMsg(ChatMqConstant.DIRECT_PUSH_CHAT_KEY, new PushMessageDTO(uidList, msg));
    }

    @Override
    public void sendPushMsg(WsBaseVO<?> msg, String uid) {
        mqProducer.sendMsg(ChatMqConstant.DIRECT_PUSH_CHAT_KEY, new PushMessageDTO(uid, msg));
    }

    @Override
    public void sendPushMsg(WsBaseVO<?> msg) {
        mqProducer.sendMsg(ChatMqConstant.DIRECT_PUSH_CHAT_KEY, new PushMessageDTO(msg));
    }

}
