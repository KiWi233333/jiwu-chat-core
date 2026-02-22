package com.jiwu.api.common.main.enums.common;

public enum DeletedEnum {
    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final int code;
    private final String desc;

    DeletedEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DeletedEnum fromCode(Integer code) {
        if (code == null) return null;
        for (DeletedEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
} 
