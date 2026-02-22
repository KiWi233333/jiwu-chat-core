package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @className: UpdatePwdDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/9 2:30
 */
@Data
public class UpdateNewPwdDTO {

    @Schema(description = "新密码")
    @Password
    String newPassword;

}
