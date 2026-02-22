package com.jiwu.api.common.main.dto.chat.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;


/**
 * Description: 申请好友信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendApplyDTO {

    @NotBlank(message = "申请理由不能为空!")
    @Schema(description = "申请信息")
    @Length(max = 200, message = "申请理由不能超过200字!")
    private String msg;

    @NotBlank(message = "添加对象不能为空!")
    @Schema(description = "好友uid")
    private String targetUid;

}
