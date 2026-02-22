package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.sys.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

/**
 * 权限添加
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 权限添加
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsertPermissionDTO {


    @Schema(description = "权限唯一Code", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 100, message = "权限码长度为1-100！")
    @NotBlank(message = "权限码不能为空！")
    private String code;

    @Schema(description = "权限名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 50, message = "权限名称长度为1-50！")
    @NotBlank(message = "权限名称不能为空！")
    private String name;

    @Schema(description = "权限备注", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 100, message = "权限备注长度为1-100！")
    private String intro;

    @Schema(description = "所属父级权限ID")
    @Length(min = 10, max = 50, message = "权限备注长度为10-50！")
    private String parentId;


    public static Permission toPermission(InsertPermissionDTO dto) {
        return new Permission()
                .setCode(dto.getCode())
                .setName(dto.getName())
                .setIntro(dto.getIntro())
                .setParentId(dto.getParentId());
    }

}
