package com.jiwu.api.common.main.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单节点类型 （1 页面，2按钮）
 */
@Getter
@AllArgsConstructor
public enum MenuNodeType {

    PAGE(1, "页面"),
    BUTTON(2, "按钮");

    private final Integer val;
    private final String name;


}
