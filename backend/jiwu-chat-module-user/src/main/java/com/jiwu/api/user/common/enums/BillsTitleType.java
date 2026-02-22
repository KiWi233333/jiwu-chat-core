package com.jiwu.api.user.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BillsTitleType {
    IN_RECHARGE(0, "钱包充值"),
    IN_RECHARGE_POINT(1, "充值送积分"),
    OUT_SHOP(2, "购物消费"),
    OUT_SHOP_POINT(3, "积分消费");
    private final Integer key;
    private final String val;
}