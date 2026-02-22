package com.jiwu.api.common.main.enums.ai;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiBaseStatusEnum {

    LOADING(1, "加载中"),
    END(2, "结束"),
    ERROR(3, "错误");
    private final int code;
    private final String desc;


}
