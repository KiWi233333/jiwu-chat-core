package com.jiwu.api.chat.common.dto;

import com.jiwu.api.chat.common.enums.WsReqType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ws 参数
 *
 * @className: WsBaseReq
 * @author: Kiwi23333
 * @description: TODOws 参数
 * @date: 2023/12/1 16:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsBaseDTO {

    /**
     * @see WsReqType
     * ws客户端req类型
     */
    private Integer type;

    private String data;
}
