package com.jiwu.api.common.main.enums.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiRobotType {

    HTTP(1, "Http请求"),
    WS(2, "Websocket请求"),
    ;

    private final int code;
    private final String name;

    public static AiRobotType getByCode(int code) {
        for (AiRobotType value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }

}
