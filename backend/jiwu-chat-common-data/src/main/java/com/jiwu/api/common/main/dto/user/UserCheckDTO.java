package com.jiwu.api.common.main.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 校验密码登录参数类
 *
 * @className: UserTokenDto
 * @author: Kiwi2333
 * @description: 密码登录密码+盐和后端的比对
 * @date: 2023/4/13 1:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserCheckDTO implements Serializable {
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "盐", requiredMode = Schema.RequiredMode.REQUIRED)
    private String salt;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
