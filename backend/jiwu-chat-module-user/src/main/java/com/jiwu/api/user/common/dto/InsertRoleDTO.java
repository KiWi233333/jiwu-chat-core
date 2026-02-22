package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.sys.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色添加
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 角色添加
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsertRoleDTO {


    @Schema(description = "角色唯一Code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色码不能为空！")
    @Length(min = 1, max = 50, message = "角色码长度为1-50！")
    @Pattern(regexp = "^[A-Z](_*[A-Z])*_[A-Z]+$", message = "CODE要求为首尾大写字母中间_分隔！")
    private String code;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 50, message = "角色名称长度为1-50！")
    @NotBlank(message = "角色名称不能为空！")
    private String name;

    @Schema(description = "权限集合")
    @Size(min = 1, max = 80, message = "权限集合长度为1-80！")
    @NotNull(message = "权限集合不能为空！")
    private List<String> permissionList;

    @Schema(description = "角色备注", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 100, message = "角色备注长度为1-100！")
    private String intro;

    @Schema(description = "角色ID")
    private String parentId;


    public static Role toRole(InsertRoleDTO dto) {
        return new Role()
                .setCode(dto.getCode())
                .setName(dto.getName())
                .setIntro(dto.getIntro())
                .setParentId(dto.getParentId());
    }

}
