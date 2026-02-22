package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.sys.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


/**
 * 权限修改
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 权限修改
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdatePermissionDTO {


    @Schema(description = "所属父级权限ID")
    private String parentId;

    @Schema(description = "权限唯一Code")
    @Length(min = 1, max = 100, message = "权限码长度为1-100！")
    private String code;

    @Schema(description = "权限唯一CODE代码")
    @Length(min = 1, max = 50, message = "权限名称长度为1-50！")
    private String name;

    @Schema(description = "权限备注")
    @Length(min = 1, max = 100, message = "权限备注长度为1-100！")
    private String intro;

    @Schema(description = "创建人ID")
    @Length(min = 1, max = 50, message = "创建人id为1-50字符！")
    private String creator;

    public static Permission toPermission(UpdatePermissionDTO dto) {
        return new Permission()
                .setCode(dto.getCode())
                .setName(dto.getName())
                .setIntro(dto.getIntro())
                .setParentId(dto.getParentId())
                .setCreator(dto.getCreator());
    }

}
