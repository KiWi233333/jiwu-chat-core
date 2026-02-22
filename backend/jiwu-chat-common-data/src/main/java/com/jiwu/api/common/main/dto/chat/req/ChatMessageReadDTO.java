package com.jiwu.api.common.main.dto.chat.req;

import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Description:
 * Date: 2023-07-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageReadDTO extends CursorPageBaseDTO { // 游标
    @Schema(description = "消息id")
    @NotNull
    private Long msgId;

    @Schema(description = "查询类型 1已读 2未读")
    @NotNull
    private Long searchType;
}
