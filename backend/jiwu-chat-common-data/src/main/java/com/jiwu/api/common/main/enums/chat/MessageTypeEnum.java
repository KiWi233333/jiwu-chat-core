package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 消息状态
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum {
    TEXT(1, "正常消息"),
    RECALL(2, "撤回消息"),
    IMG(3, "图片"),
    FILE(4, "文件"),
    SOUND(5, "语音"),
    VIDEO(6, "视频"),
    //    EMOJI(7, "表情"),
    SYSTEM(8, "系统消息"),
    AI_CHAT(9, "AI会话"),
    DEL_MSG(10, "删除消息"),
    /** RTC 消息（开源版已移除能力，保留枚举兼容历史数据） */
    RTC_MSG(11, "RTC消息（语音、视频等）"),
    /** AI 回复消息（开源版已移除能力，保留枚举兼容历史数据） */
    AI_CHAT_REPLY(12, "AI回复消息"),
    GROUP_NOTICE(13, "群通知"), // 可在修改群公告后发出
    ;

    /**
     * 用户允许发送的消息类型
     */
    public static final Set<Integer> USER_SEND_MSG_TYPES = new HashSet<>();

    static {
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.TEXT.getType()); // 1 正常消息
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.RECALL.getType()); // 2 撤回消息
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.IMG.getType()); // 3 图片
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.FILE.getType()); // 4 文件
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.SOUND.getType()); // 5 语音
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.VIDEO.getType()); // 6 视频
//        USER_SEND_MSG_TYPES.add(MessageTypeEnum.EMOJI.getType()); // 7 表情
//        USER_SEND_MSG_TYPES.add(MessageTypeEnum.SYSTEM.getType()); // 8 系统 拉人等通知
        // 开源版不支持 AI 聊天，不再允许发送 AI_CHAT
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.DEL_MSG.getType());// 10 删除
        USER_SEND_MSG_TYPES.add(MessageTypeEnum.GROUP_NOTICE.getType());// 13 群通知
    }

    /**
     * 检查是否禁止发送的消息类型
     * @param msgType 消息类型
     * @return true 禁止发送
     */
    public static boolean checkDisable(Integer msgType) {
        if (msgType == null) {
            throw new IllegalArgumentException("msgType 不能为 null");
        }
        return !USER_SEND_MSG_TYPES.contains(msgType);
    }


    private final Integer type;
    private final String desc;

    /**
     * oss消息文件类型
     */
    public static final Integer[] OSS_MSG_FILE_TYPES = {
            MessageTypeEnum.IMG.getType(),
            MessageTypeEnum.FILE.getType(),
            MessageTypeEnum.SOUND.getType(),
            MessageTypeEnum.VIDEO.getType()
    };

    private static final Map<Integer, MessageTypeEnum> cache;

    static {
        cache = Arrays.stream(MessageTypeEnum.values()).collect(Collectors.toMap(MessageTypeEnum::getType, Function.identity()));
    }

    public static MessageTypeEnum of(Integer type) {
        return cache.get(type);
    }

}
