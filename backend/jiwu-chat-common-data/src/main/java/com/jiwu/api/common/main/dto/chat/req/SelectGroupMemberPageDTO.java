package com.jiwu.api.common.main.dto.chat.req;

import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询群聊成员列表信息DTO
 *
 * @className: SelectGroupMemberPage
 * @author: Kiwi23333
 * @description: 查询群聊成员列表信息DTO
 * @date: 2023/12/26 17:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectGroupMemberPageDTO extends CursorPageBaseDTO {

    @Schema(description = "房间id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long roomId = 1L;
    
}
