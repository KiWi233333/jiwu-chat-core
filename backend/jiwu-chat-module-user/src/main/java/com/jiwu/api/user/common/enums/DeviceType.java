package com.jiwu.api.user.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单节点类型 （1 页面，2按钮）
 */
@Getter
@AllArgsConstructor
public enum DeviceType {

    PHONE(0, "手机号"),
    EMAIL(1, "邮箱");

    private final Integer value;
    private final String name;


}
