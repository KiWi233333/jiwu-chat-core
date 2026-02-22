package com.jiwu.api.common.main.dto.chat.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;


/**
 * Description: 申请好友信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendRejectDTO {

    @NotNull(message = "拒绝对象不能为空！")
    @Schema(description = "申请id")
    private Long applyId;

}
