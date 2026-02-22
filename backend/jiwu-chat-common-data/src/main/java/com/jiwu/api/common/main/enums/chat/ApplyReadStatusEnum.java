package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请阅读状态枚举
 * @description :
 * @date : 2023/07/20
 */
@Getter
@AllArgsConstructor
public enum ApplyReadStatusEnum {

    UNREAD(0, "未读"),

    READ(1, "已读");

    private final Integer code;

    private final String desc;
}
