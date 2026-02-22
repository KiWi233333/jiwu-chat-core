package com.jiwu.api.common.main.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrencyType {
    BALANCE(0, "金钱"),
    POINT(1, "积分");
    private final Integer key;
    private final String val;
}