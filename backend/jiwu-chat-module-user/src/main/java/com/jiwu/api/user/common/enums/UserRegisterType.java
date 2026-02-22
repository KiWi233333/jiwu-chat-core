package com.jiwu.api.user.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRegisterType {
    PHONE(0, "手机号"),
    EMAIL(1, "邮箱"),
    PWD(2, "密码")
    ;

    private final Integer type;
    private final String name;
}