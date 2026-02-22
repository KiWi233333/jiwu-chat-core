package com.jiwu.api.chat.service.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.dao.chat.ChatContactDAO;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import com.jiwu.api.common.main.mapper.chat.ChatGroupMemberMapper;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.mapper.chat.ChatRoomMapper;
import com.jiwu.api.common.main.mapper.chat.ChatRoomSelfMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.chat.ChatRoom;
import com.jiwu.api.common.main.pojo.chat.ChatRoomSelf;
import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.main.constant.chat.ChatMqConstant;
import com.jiwu.api.common.main.dto.chat.mq.ChatMsgMqDTO;
import com.jiwu.api.chat.service.ChatService;
import com.jiwu.api.chat.service.PushService;
import com.jiwu.api.chat.service.WebSocketService;
import com.jiwu.api.chat.service.adapter.WsAdapter;
import com.jiwu.api.common.main.cache.chat.ChatGroupMemberCache;
import com.jiwu.api.common.main.cache.chat.ChatHotRoomCache;
import com.jiwu.api.common.main.cache.chat.ChatRoomCache;
import com.jiwu.api.chat.common.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
 */
@Slf4j
@Component
@RabbitListener(bindings = @QueueBinding(
        exchange = @Exchange(value = ChatMqConstant.DIRECT_CHAT_EXCHANGE, durable = "true", type = ExchangeTypes.DIRECT),
        value = @Queue(value = ChatMqConstant.DIRECT_SEND_CHAT_QUEUE, durable = "true"),
        key = ChatMqConstant.DIRECT_SEND_CHAT_KEY
))
public class ChatConsumer {

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatContactDAO contactDAO;
    @Autowired
    private ChatMessageMapper messageMapper;
    @Autowired
    private ChatRoomCache roomCache;
    @Autowired
    private ChatRoomMapper roomMapper;
    @Autowired
    private ChatGroupMemberMapper chatGroupMemberMapper;
    @Autowired
    private ChatGroupMemberCache chatGroupMemberCache;
    @Autowired
    private ChatRoomSelfMapper roomSelfMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PushService pushService;
    @Autowired
    private ChatHotRoomCache chatHotRoomCache;


    /**
     * 发送消息监听
     *
     * @param data 消息体
     */
    @RabbitHandler
    public void onWsSendMsgChatConsumer(ChatMsgMqDTO data) {
        if (data == null) {
            return;
        }
        doSendAndPush(data);
    }

    private void doSendAndPush(ChatMsgMqDTO data) {
        log.info("收到消息:{}", data.getMsgId());
        ChatMessage message = messageMapper.selectById(data.getMsgId());
        ChatRoom room = roomCache.get(message.getRoomId());
        ChatMessageVO msgResp = chatService.getMsgDetail(message.getId(), null);
        // 添加客户端id （不做保存）
        msgResp.setClientId(data.getClientId());
        // 所有房间更新房间最新消息
        roomMapper.updateById(new ChatRoom()
                .setId(room.getId())
                .setLastMsgId(message.getId()));
        roomCache.delete(room.getId());
        if (room.isHotRoom()) {// 热门群聊推送所有在线的人
            // 更新热门群聊时间-redis
            chatHotRoomCache.refreshActiveTime(room.getId(), message.getCreateTime());
            // 推送所有人
            pushService.sendPushMsg(WsAdapter.buildMsgSend(msgResp));
        } else {
            // 推送给每一个人
            List<String> memberUidList = new ArrayList<>();
            if (Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType())) {// 普通群聊推送所有群成员
                memberUidList = chatGroupMemberCache.getMemberUidList(room.getId());
            } else if (Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType()) ||
                    Objects.equals(room.getType(), RoomTypeEnum.AI.getType())) {//单聊对象 或者 AI聊天室
                //对单人推送
                ChatRoomSelf roomFriend = roomSelfMapper.selectOne(new LambdaQueryWrapper<ChatRoomSelf>()
                        .eq(ChatRoomSelf::getRoomId, room.getId()));
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            //更新所有群成员的会话时间
            contactDAO.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), message.getCreateTime());
            //推送房间成员
            pushService.sendPushMsg(WsAdapter.buildMsgSend(msgResp), memberUidList);
        }
    }


    /**
     * 发送消息监听
     *
     * @param msg 消息体
     */
    @RabbitHandler
    public void onWsSendMsgChatConsumerByStr(String msg) {
        log.info("收到消息 String:{}", msg);
        try {
            ChatMsgMqDTO dto = JacksonUtil.parseJSON(msg, ChatMsgMqDTO.class);
            doSendAndPush(dto);
        }catch (Exception e) {
            log.error("发送失败 :{}", e.getMessage());
        }
    }

}

