package com.jiwu.api.common.main.enums.ai;

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
public enum AiRobotUserStatus {
    ON(1, "正常"),
    OFF(0, "禁用");
    private final Integer code;
    private final String desc;

    private AiRobotUserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
