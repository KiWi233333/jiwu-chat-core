package com.jiwu.api.common.main.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WalletType {
    OUT(0, "消费"),
    IN(1, "收入");
    private final Integer key;
    private final String val;
}