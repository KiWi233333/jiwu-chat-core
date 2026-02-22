package com.jiwu.api.common.main.enums.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 成员角色枚举
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum GroupRoleEnum {
    /**
     * 群聊：群主、管理员
     */
    HOME(1, "群主"),
    MANAGER(2, "管理员"),
    MEMBER(3, "普通成员"),
    ;

    private final Integer type;
    private final String desc;

    public static final List<Integer> ADMIN_LIST = Arrays.asList(GroupRoleEnum.HOME.getType(), GroupRoleEnum.MANAGER.getType());

    private static Map<Integer, GroupRoleEnum> cache;

    static {
        cache = Arrays.stream(GroupRoleEnum.values()).collect(Collectors.toMap(GroupRoleEnum::getType, Function.identity()));
    }

    public static GroupRoleEnum of(Integer type) {
        return cache.get(type);
    }

    public static boolean isAdmin(Integer type) {
        return ADMIN_LIST.contains(type);
    }


}
