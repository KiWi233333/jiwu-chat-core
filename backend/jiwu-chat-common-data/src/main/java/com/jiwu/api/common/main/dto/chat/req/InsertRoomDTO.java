package com.jiwu.api.common.main.dto.chat.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 添加群聊房间
 *
 * @className: InsertRoomGroupDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/26 12:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsertRoomDTO {

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "房主ID")
    private String userId;
}
