package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 提及信息（at）
 *
 * @className: MentionInfo
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2025/6/17 2:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MentionInfo {
    /**
     * 被提及用户的ID
     */
    @Schema(description = "被提及用户的ID", example = "被提及用户的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "@用户不能为空！")
    private String uid;


    /**
     * 被提及用户昵称
     */
    @Schema(description = "被提及用户昵称", example = "被提及用户昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "@用户昵称不能为空！")
    @Pattern(regexp = "^@", message = "@用户昵称格式错误！")
    private String displayName;
}
