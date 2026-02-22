package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author: Quan
 * @description:    文件基类
 * @date: 2023/10/07 下午 2:47
 */


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "大小（字节）")
    private Long size;

    @Schema(description = "下载地址")
    @NotBlank
    private String url;
}
