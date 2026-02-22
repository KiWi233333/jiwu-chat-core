package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyTypeEnum {

    ADD_FRIEND(1, "加好友"),
//    ADD_ROBOT(2, "加机器人");
    ;
    private final Integer code;

    private final String desc;
}
