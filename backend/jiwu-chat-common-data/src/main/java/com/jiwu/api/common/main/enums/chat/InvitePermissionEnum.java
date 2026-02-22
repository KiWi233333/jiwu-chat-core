package com.jiwu.api.common.main.enums.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 邀请权限枚举
 * JSON 序列化/反序列化使用 type 值（0/1/2）。
 * Date: 2025-02-11
 */
@AllArgsConstructor
@Getter
public enum InvitePermissionEnum {
    /**
     * 邀请权限枚举
     * 0-任意成员可邀请
     * 1-管理员和群主可邀请
     * 2-仅群主可邀请
     */
    ANY(0, "任意成员可邀请"),
    ADMIN(1, "管理员和群主可邀请"),
    OWNER_ONLY(2, "仅群主可邀请"),
    ;

    @Getter(onMethod_ = {@JsonValue})
    private final Integer type;
    private final String desc;

    private static final Map<Integer, InvitePermissionEnum> CACHE = Arrays.stream(values())
        .collect(Collectors.toMap(InvitePermissionEnum::getType, Function.identity()));

    /** JSON 反序列化：根据 0/1/2 解析为枚举 */
    @JsonCreator
    public static InvitePermissionEnum fromType(Integer type) {
        return type == null ? null : CACHE.get(type);
    }

    public static InvitePermissionEnum of(Integer type) {
        return type == null ? null : CACHE.get(type);
    }
}
