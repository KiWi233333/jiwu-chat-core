package com.jiwu.api.common.main.enums.ai;



public enum AiBaseRoleEnum {
    system,
    user,
    assistant,
    tool,
    ;

    public static AiBaseRoleEnum getByRoleStr(String role) {
        switch (role) {
            case "system":
                return system;
            case "user":
                return user;
            case "assistant":
                return assistant;
            case "tool":
                return tool;
            default:
                return null;
        }
    }
}
