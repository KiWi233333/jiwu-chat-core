package com.jiwu.api.chat.common.enums;

import com.jiwu.api.chat.common.vo.ws.*;
import com.jiwu.api.common.util.service.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ws连接 枚举类 系统
 */
@Getter
@AllArgsConstructor
public enum WsRespTypeEnum {
    MESSAGE(1, "新消息", Result.class),
    ONLINE_OFFLINE_NOTIFY(2, "上下线通知", WSOnlineOfflineNotify.class),
    RECALL(3, "消息撤回", WSMsgRecall.class),
    APPLY(4, "好友申请", WSFriendApply.class),
    MEMBER_CHANGE(5, "成员变动", WSMemberChange.class),
    TOKEN_EXPIRED_ERR(6, "使前端的token失效，意味着前端需要重新登录", null),
    LOGIN_IN(7, "登录成功返回个人信息", null),
    DELETE(8, "消息删除", WSMsgDelete.class),
    PIN_CONTACT(10, "置顶会话", null),
    UPDATE_CONTACT_INFO(12, "更新会话信息", WSUpdateContactInfoMsg.class),
    MSG_REACTION(13, "消息表情反应", WSMsgReaction.class)
    ;

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    private static final Map<Integer, WsRespTypeEnum> cache;

    static {
        cache = Arrays.stream(WsRespTypeEnum.values()).collect(Collectors.toMap(WsRespTypeEnum::getType, Function.identity()));
    }

    public static WsRespTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
