package com.jiwu.api.common.main.dto.chat.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

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
public class AddMemberDTO {
    @NotNull
    @Schema(description = "房间id")
    private Long roomId;

    @NotNull(message = "群成员不能为空！")
    @Size(min = 1, max = 300, message = "群成员不能超过300人！")
    @Schema(description = "邀请的uid")
    private List<String> uidList;
}
