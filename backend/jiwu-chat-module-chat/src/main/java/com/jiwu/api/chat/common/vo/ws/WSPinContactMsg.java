package com.jiwu.api.chat.common.vo.ws;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 置顶会话ws消息
 * Date: 2023-06-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSPinContactMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "房间id")
    private Long roomId;
    @Schema(description = "置顶时间戳")
    private Long pinTime;
    @Schema(description = "是否置顶")
    private Integer isPin;
}


