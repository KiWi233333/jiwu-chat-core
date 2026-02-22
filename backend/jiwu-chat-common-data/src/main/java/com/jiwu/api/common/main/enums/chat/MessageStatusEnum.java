package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息状态枚举
 * Description: 消息状态
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum MessageStatusEnum {
    DELETE(0, "删除"),
    NORMAL(1, "正常"),
    ;

    private final Integer status;
    private final String desc;

    private static final Map<Integer, MessageStatusEnum> cache;

    static {
        cache = Arrays.stream(MessageStatusEnum.values()).collect(Collectors.toMap(MessageStatusEnum::getStatus, Function.identity()));
    }

    public static MessageStatusEnum of(Integer type) {
        return cache.get(type);
    }
}
