package com.jiwu.api.chat.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Description: 添加管理员
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberAdminAddDTO {

    @Schema(description = "房间id")
    @NotNull(message = "房间号不能为空！")
    private Long roomId;
    @Schema(description = "用户id")
    @NotNull(message = "用户不能为空！")
    private String userId;
}
