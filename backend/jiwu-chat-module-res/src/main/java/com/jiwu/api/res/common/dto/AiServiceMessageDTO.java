package com.jiwu.api.res.common.dto;

import cn.hutool.json.JSONObject;
import com.jiwu.api.common.main.enums.ai.AiBaseRoleEnum;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AiServiceMessageDTO {

    @Parameter(description = "消息角色", required = true)
    @NotBlank(message = "消息角色不能为空!")
    @Pattern(regexp = "^(user)$", message = "消息角色只能是user!")
    private AiBaseRoleEnum role;

    @Parameter(description = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空!")
    @Range(min = 1, max = 2048, message = "消息内容长度不能超过2048个字符!")
    private String content;

    public static AiServiceMessageDTO userContent(String content) {
        return AiServiceMessageDTO.builder().role(AiBaseRoleEnum.user).content(content).build();
    }

    public static AiServiceMessageDTO assistantContent(String content) {
        return AiServiceMessageDTO.builder().role(AiBaseRoleEnum.assistant).content(content).build();
    }

    public static AiServiceMessageDTO systemContent(String content) {
        return AiServiceMessageDTO.builder().role(AiBaseRoleEnum.system).content(content).build();
    }

    public static JSONObject toolsContent(String name, String toolCallId, String content) {
        return new JSONObject()
                .set("role", AiBaseRoleEnum.tool.name())
                .set("content", content)
                .set("tool_call_id", toolCallId)
                .set("name", name);
    }
}
