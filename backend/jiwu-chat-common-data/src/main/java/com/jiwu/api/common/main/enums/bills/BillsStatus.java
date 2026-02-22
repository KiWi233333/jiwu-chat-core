package com.jiwu.api.common.main.enums.bills;

import lombok.Getter;

/**
 * 描述
 *
 * @className: UserStatus
 * @author: Author作者
 * @description: TODO描述
 * @date: 2023/4/11 16:12
 */
@Getter
public enum BillsStatus {

    /**
     * 账单状态
     * 0:待付款，
     * 1:已付款，
     * 2:已发货，
     */
    SHOP_IN(1, "退款收入"),
    SHOP_OUT(2, "购物消费"),

    POINT_IN(3, "积分收入"),
    POINT_OUT(4, "积分消费");


    private final Integer key;
    private final String val;

    private BillsStatus(Integer key, String val) {
        this.key = key;
        this.val = val;
    }


}
