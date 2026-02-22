package com.jiwu.api.res.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 删除文件dto
 *
 * @className: DeleteOssFileDTO
 * @author: Kiwi23333
 * @description: 删除文件dto
 * @date: 2023/8/17 2:43
 */
@Data
@AllArgsConstructor
public class DeleteOssFileDTO {
    @Schema(description = "文件路径文件名")
    @NotBlank(message = "文件名不能为空！")
    String key;
}
