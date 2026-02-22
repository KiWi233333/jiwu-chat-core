package com.jiwu.api.common.main.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式
 */
@Getter
@AllArgsConstructor
public enum EventStatus {


    READY(0, "活动未开始"),
    START(1, "活动开始"),
    END(2, "活动结束");

    /**
     * 类型码
     */
    private final int type;
    /**
     * 名称
     */
    private final String title;
}
