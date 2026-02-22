package com.jiwu.api.common.main.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiRoleEnums {

    SYSTEM("system", "系统角色，可以理解为系统提示词的提供者"),

    USER("user", "用户角色"),

    ASSISTANT("assistant", "助手角色，可以理解为机器人"),
    ;

    private final String role;
    private final String desc;

}
