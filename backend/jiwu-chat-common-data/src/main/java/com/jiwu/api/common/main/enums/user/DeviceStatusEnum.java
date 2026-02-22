package com.jiwu.api.common.main.enums.user;

public enum DeviceStatusEnum {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    UNACTIVATED(2, "未激活/未绑定"),
    DISABLED(3, "禁用");

    private final int code;
    private final String desc;

    DeviceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DeviceStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (DeviceStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
} 
