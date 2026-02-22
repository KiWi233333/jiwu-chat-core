package com.jiwu.api.common.main.enums.user;

import lombok.Getter;

/**
 * 描述
 *
 * @className: UserStatus
 * @author: Author作者
 * @description: TODO描述
 * @date: 2023/4/11 16:12
 */
@Getter
public enum UserStatus {
    ON(1, "正常"),
    OFF(0, "禁用");
    private final Integer code;
    private final String desc;

    private UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
