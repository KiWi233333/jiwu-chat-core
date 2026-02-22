package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 提醒状态码
 */
@Getter
@AllArgsConstructor
public enum ContactNoticeStatus {

    DEFAULT(0, "默认提醒"), // 默认提醒
    NOT_NOTICE(1, "接收消息但不提醒"),
    NOT_NOTICE_BY_GROUP(2, "收进群助手且不提醒"),
    SHIELD_GROUP(3, "屏蔽群消息"),
    ;
    private final Integer code;

    private final String desc;

    public static boolean isValid(Integer status) {
        return status != null && Arrays.stream(values()).anyMatch(e -> e.code.equals(status));
    }
}
