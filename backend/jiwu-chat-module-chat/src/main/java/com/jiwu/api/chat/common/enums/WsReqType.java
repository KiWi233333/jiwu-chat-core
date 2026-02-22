package com.jiwu.api.chat.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ws连接 枚举类
 */
@Getter
@AllArgsConstructor
public enum WsReqType {
    CHECK_TOKEN(1, "登录认证"),
    HEARTBEAT(2, "心跳包"),
    ;

    private final Integer type;
    private final String desc;

    // 缓存
    private static final Map<Integer, WsReqType> cache;

    static {
        cache = Arrays.stream(WsReqType.values()).collect(Collectors.toMap(WsReqType::getType, Function.identity()));
    }

    public static WsReqType of(Integer type) {
        return cache.get(type);
    }
}
