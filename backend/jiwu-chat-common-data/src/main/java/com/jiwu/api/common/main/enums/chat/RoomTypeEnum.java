package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 消息状态
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum RoomTypeEnum {
    GROUP(1, "群聊"),
    FRIEND(2, "单聊"),
    AI(3, "AI聊"),
    ;

    private final Integer type;
    private final String desc;

    private static final Map<Integer, RoomTypeEnum> cache;

    static {
        cache = Arrays.stream(RoomTypeEnum.values()).collect(Collectors.toMap(RoomTypeEnum::getType, Function.identity()));
    }

    public static RoomTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
