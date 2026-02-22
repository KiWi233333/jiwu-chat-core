package com.jiwu.api.chat.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.jiwu.api.common.main.enums.chat.MessageStatusEnum;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.main.dto.chat.msg.body.TextMsgDTO;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.chat.common.strategy.msg.AbstractMsgHandler;
import com.jiwu.api.chat.common.strategy.msg.MsgHandlerFactory;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.chat.common.vo.ChatMessageVO;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息适配器
 *
 * @className: ChatMessageAdapter
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/15 11:35
 */
public class ChatMessageAdapter {


    /**
     * 构建基本消息体
     *
     * @param dto    参数
     * @param userId 发送人id
     * @return 消息
     */
    public static ChatMessage buildMsgSave(ChatMessageDTO dto, String userId) {
        // 敏感词过滤
        // String content = dto.getContent();
        // if (StringUtils.isNotBlank(dto.getContent())) {
        //     content = SensitiveWordHelper.replace(content);
        //     dto.checkContentLen(); // 检测长度
        // }
        return new ChatMessage()
                .setFromUid(userId)
                .setContent(dto.getContent())
                .setRoomId(dto.getRoomId())
                .setType(dto.getMsgType())
                .setStatus(MessageStatusEnum.NORMAL.getStatus());
    }

    /**
     * 成为好友
     *
     * @param roomId 房间id
     * @param msg    信息
     * @return ChatMessageDTO
     */
    public static ChatMessageDTO buildAgreeMsg(Long roomId, String msg) {
        ChatMessageDTO chatMessageReq = new ChatMessageDTO();
        chatMessageReq.setRoomId(roomId);
        chatMessageReq.setContent(msg);
        chatMessageReq.setMsgType(MessageTypeEnum.TEXT.getType());
        TextMsgDTO textMsgReq = new TextMsgDTO();
        chatMessageReq.setBody(textMsgReq);
        return chatMessageReq;
    }

    public static final String AGREE_DEFAULT_MSG = "我们已经是好友啦，一起来聊天吧！";
    public static final String AGREE_ROBOT_DEFAULT_MSG = "欢迎使用Jiwu机器人，请问有什么可以帮助您？";

    // 构建一个回复消息
    public static ChatMessageDTO buildAgreeMsg(Long roomId) {
        return buildAgreeMsg(roomId, AGREE_DEFAULT_MSG);
    }

    // 构建一个回复消息
    public static ChatMessageDTO buildAgreeRobotMsg(Long roomId) {
        return buildAgreeMsg(roomId, AGREE_ROBOT_DEFAULT_MSG);
    }


    /**
     * 构架消息体
     *
     * @param list       消息列表
     * @param receiveUid 接收人id
     * @param userCache  用户缓存
     * @return 消息列表
     */
    public static List<ChatMessageVO> buildMsgVO(List<ChatMessage> list, String receiveUid, UserCache userCache) {
        return list.stream().map(a -> {
                    ChatMessageVO vo = new ChatMessageVO();
                    // 1、构建发送人
                    vo.setFromUser(buildFromUser(a.getFromUid(), userCache));
                    // 2、构建消息体
                    vo.setMessage(buildMessage(a));
                    return vo;
                })
//                .sorted(Comparator.comparing(a -> a.getMessage().getSendTime()))//帮前端排好序，更方便它展示
                .sorted(Comparator.comparing(a -> a.getMessage().getId()))//帮前端排好序，更方便它展示
                .collect(Collectors.toList());
    }

    /**
     * 构建展示消息体（策略模块）
     *
     * @param message 原始消息
     * @return 参数
     */
    private static ChatMessageVO.Message buildMessage(ChatMessage message) {
        ChatMessageVO.Message messageVO = new ChatMessageVO.Message();
        BeanUtil.copyProperties(message, messageVO);
        messageVO.setSendTime(message.getCreateTime());
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
        if (Objects.nonNull(msgHandler)) {
            Object body = msgHandler.showMsg(message);
            messageVO.setBody(body);
            // 如果是隐藏消息
            if (messageVO.getType().equals(MessageTypeEnum.RECALL.getType()) || messageVO.getType().equals(MessageTypeEnum.DEL_MSG.getType())) {
                messageVO.setContent(String.valueOf(body));
            } else if (MessageTypeEnum.RTC_MSG.getType().equals(message.getType())) {
                messageVO.setContent("通话消息");
            }
        }
        return messageVO;
    }

    /**
     * 构建发送人
     *
     * @param fromUid   发送人id
     * @param userCache 用户缓存
     * @return 发送人信息
     */
    private static ChatMessageVO.UserInfo buildFromUser(String fromUid, UserCache userCache) {
        User user = userCache.getUserInfo(fromUid);
        if (user == null) {
            return new ChatMessageVO.UserInfo()
                    .setUserId(fromUid);
        } else {
            return new ChatMessageVO.UserInfo()
                    .setUserId(fromUid)
                    .setNickName(user.getNickname())
                    .setAvatar(user.getAvatar())
                    .setGender(user.getGender());
        }
    }


}
