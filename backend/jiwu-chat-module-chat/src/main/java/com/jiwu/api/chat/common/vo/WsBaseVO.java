package com.jiwu.api.chat.common.vo;

import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import lombok.Data;

/**
 * ws 参数
 *
 * @className: WsBaseReq
 * @author: Kiwi23333
 * @description: TODOws 参数
 * @date: 2023/12/1 16:05
 */
@Data
public class WsBaseVO<T> {

    /**
     * @see WsRespTypeEnum
     * ws返回类型
     */
    private Integer type;
    private T data;
}
