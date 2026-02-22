package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色绑定
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 角色绑定
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserRoleBindDTO {

    @Schema(description = "角色集合")
    @Size( max = 25, message = "角色集合长度为0-25！")
    @NotNull(message = "角色集合不能为空！")
    private List<String> roleIds;

}
