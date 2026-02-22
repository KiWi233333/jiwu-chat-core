package com.jiwu.api.common.util.service;

import cn.hutool.json.JSONUtil;
import com.jiwu.api.common.enums.ResultStatus;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 接口返回结果模板
 *
 * @className: Result
 * @author: Author作者
 * @description: 接口返回模板
 * @date: 2023/4/7 21:35
 */
@Data
@AllArgsConstructor
public class Result<T> {


    @Schema(description = "状态码", example = "20001添加或已经存在， 增20002被删或不存在， 删20003查询不到，查20004修改失败，改20005链接元素不存在40001不能为空40002认证失败40004阻塞或被占用 繁忙")
    private Integer code;
    @Schema(description = "描述信息")
    private String message;
    @Schema(description = "返回数据")
    private T data;


    /**
     * 成功
     **/
    // 成功请求--普通
    public static Result<Object> ok() {
        return new Result<Object>(ResultStatus.SUCCESS.getCode(), "操作成功!", null);
    }

    // 成功请求--传入数据
    public static <T> Result<T> ok(T data) {
        return new Result<T>(ResultStatus.SUCCESS.getCode(), "操作成功!", data);
    }

    // 成功请求--传入消息和数据
    public static <T> Result<T> ok(String message, T data) {
        return new Result<T>(ResultStatus.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败
     **/
    // 失败请求--普通
    public static <T> Result<Object> fail() {
        return new Result<Object>(ResultStatus.NULL_ERR.getCode(), "操作失败！", null);
    }

    // 失败请求--传入消息
    public static <T> Result<T> fail(String message) {
        return new Result<T>(ResultStatus.NULL_ERR.getCode(), message, null);
    }

    // 失败请求--传入消息和数据
    public static <T> Result<T> fail(int code, String message) {
        return new Result<T>(code, message, null);
    }
    // 失败请求--传入消息
    public static <T> Result<T> fail(ResultStatus resultStatus, String message) {
        return new Result<T>(resultStatus.getCode(), message, null);
    }
    // 失败请求--传入消息和数据
    public static <T> Result<T> fail(ResultStatus status) {
        return new Result<T>(status.getCode(), status.getMessage(), null);
    }

    // 失败请求--传入消息和数据
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<T>(code, message, data);
    }

    public static TextWebSocketFrame wsRes(Result<?> result) {
        return new TextWebSocketFrame(JSONUtil.toJsonStr(result));
    }

}
