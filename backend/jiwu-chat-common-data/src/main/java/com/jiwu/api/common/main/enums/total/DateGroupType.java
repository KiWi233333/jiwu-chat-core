package com.jiwu.api.common.main.enums.total;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DateGroupType {

    DAY(0, "'%Y-%m-%d'"),
    MONTH(0, "'%Y-%m'"),
    YEAR(1, "'%Y'");
    private final int code;
    private final String formatBat;

    public static String getVal(Integer groupType) {
        if (groupType == 0) {
            return DAY.getFormatBat();
        } else if (groupType == 1) {
            return MONTH.getFormatBat();
        } else if (groupType == 2) {
            return YEAR.getFormatBat();
        } else {
            return null;
        }
    }
}

