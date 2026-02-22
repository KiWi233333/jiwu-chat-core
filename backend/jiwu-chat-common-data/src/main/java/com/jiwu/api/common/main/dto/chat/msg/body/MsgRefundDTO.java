package com.jiwu.api.common.main.dto.chat.msg.body;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: 消息撤回
 * Date: 2023-06-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgRefundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "撤回者的uid")
    private String recallUid;

    @Schema(description = "撤回的时间点")
    private Date recallTime;
}
