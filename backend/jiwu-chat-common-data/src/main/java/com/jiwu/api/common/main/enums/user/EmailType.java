package com.jiwu.api.common.main.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件类型
 */
@Getter
@AllArgsConstructor
public enum EmailType {

    LOGIN(0, "登录"),
    REGISTER(1, "注册"),
    CHECK(2, "校验邮箱"),
    ;

    private final Integer value;
    private final String name;


}
