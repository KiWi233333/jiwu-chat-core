package com.jiwu.api.common.main.enums.total;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TotalTimeType {
    DAY(0, "按日"),
    MONTH(1, "按月"),
    YEAR(2, "按年");
    private final Integer key;
    private final String val;
}