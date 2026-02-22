package com.jiwu.api.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultStatus {
    SUCCESS(20000, "操作成功"),
    INSERT_ERR(20001, "添加或已经存在"),
    DELETE_ERR(20002, "被删或不存在"),
    SELECT_ERR(20003, "查询不到"),
    UPDATE_ERR(20004, "修改失败"),
    LINK_NULL_ERR(20005, "链接元素不存在"),
    NULL_ERR(40001, "空值错误"),
    TOKEN_ERR(40002, "认证失败"),
    PARAM_ERR(40003, "参数错误"),
    BUSY_ERR(40004, "阻塞或被占用繁忙"),
    DEFAULT_ERR(40005, "操作错误，请重试！"),
    PERMISSION_ERR(40006, "权限不足"),
    TOKEN_EXPIRED_ERR(40007, "身份过期，请重新登录！"),
    STATUS_OFF_ERR(40008, "账号被封禁，详情联系客服！"),
    TOKEN_DEVICE_ERR(40009, "认证失败，登录设备有误！"),
    ;
    private final int code;
    private final String message;
}
