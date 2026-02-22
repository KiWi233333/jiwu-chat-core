package com.jiwu.api.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserType {

    CUSTOMER(0, "前台", "ROLE_CUSTOMER_DEFAULT"),
    ADMIN(1, "后台", "ROLE_ADMIN_DEFAULT"),
    SERVICE(2, "客服", "ROLE_SERVICE"),
    ROBOT(3, "AI机器人", "ROLE_ROBOT"),
    ;
    private final int code;
    private final String message;
    private final String roleCode;

    public static final String[] IGNORE_ROLES = {
            "ROLE_CUSTOMER_DEFAULT",
            "ROLE_ADMIN_DEFAULT",
            "SUPER_ADMIN",
            "ROLE_CUSTOMER",
            "ROLE_ROBOT",
    };
}
