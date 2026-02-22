package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.sys.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 菜单添加
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 菜单添加
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsertMenuDTO {

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 50, message = "菜单名称长度为1-50！")
    @NotBlank(message = "菜单名称不能为空！")
    private String name;

    @Schema(description = "菜单唯一Code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单码不能为空！")
    @Length(min = 1, max = 200, message = "菜单唯一Code长度为1-200！")
    private String code;

    @Schema(description = "父菜单ID")
    @Length(min = 10, max = 25, message = "父菜单ID长度为10-25！")
    private String parentId;

    @Schema(description = "节点类型：（1页面，2按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Range(min = 1, max = 2, message = "节点类型范围1-2！")
    @NotNull(message = "节点类型不能为空！")
    private Integer type;

    @Schema(description = "操作系统（0前台，1管理...）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Range(min = 0, max = 1, message = "操作系统类型范围！")
    private Integer sysType;

    @Schema(description = "权重")
    @Range(min = 1, max = 99, message = "权重1-99！")
    @NotNull(message = "权重不能为空！")
    private Integer sortOrder;

    @Schema(description = "链接地址")
    @Length(min = 0, max = 200, message = "链接地址长度为0-200！")
    private String linkUrl;


    @Schema(description = "组件地址")
    @Length(min = 0, max = 255, message = "组件地址长度为0-255！")
    private String componentPath;

    @Schema(description = "图标样式")
    @Length(min = 0, max = 255, message = "图标样式长度为0-255！")
    private String icon;

    @Schema(description = "激活图标样式")
    @Length(min = 1, max = 255, message = "激活图标样式长度为1-255！")
    private String onIcon;

    public static Menu toMenu(InsertMenuDTO dto) {
        return new Menu()
                .setName(dto.getName())
                .setCode(dto.getCode())
                .setType(dto.getType())
                .setSysType(dto.getSysType())
                .setLinkUrl(dto.getLinkUrl())
                .setParentId(dto.getParentId())
                .setComponentPath(dto.getComponentPath())
                .setIcon(dto.getIcon())
                .setOnIcon(dto.getOnIcon())
                .setSortOrder(dto.getSortOrder())
                ;
    }

}
