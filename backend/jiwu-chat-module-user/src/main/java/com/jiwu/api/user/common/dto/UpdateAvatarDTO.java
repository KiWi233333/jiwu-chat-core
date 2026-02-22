package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新用户头像DTO
 *
 * @className: UpDateAvatarDTO
 * @author: Kiwi23333
 * @description: 更新用户头像DTO
 * @date: 2023/8/25 14:56
 */
@Data
public class UpdateAvatarDTO {
    @Schema(description = "上传回调路径")
    @NotBlank(message = "参数不能为空！")
    private String fileName;

}
