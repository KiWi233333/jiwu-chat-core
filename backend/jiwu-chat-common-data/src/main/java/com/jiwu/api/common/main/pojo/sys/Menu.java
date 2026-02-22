package com.jiwu.api.common.main.pojo.sys;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 菜单实体类
 *
 * @className: Menu
 * @author: Kiwi23333
 * @description: TODO 菜单实体类
 * @date: 2023/8/24 14:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "菜单表")
@TableName("sys_menu")
public class Menu {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "菜单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "父节点")
    private String parentId;

    @Schema(description = "节点类型：（1页面，2按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "用户类型（0前台，1管理...）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sysType;

    @Schema(description = "页面对应的地址")
    private String linkUrl;

    @Schema(description = "组件地址")
    private String componentPath;

    @Schema(description = "图标样式")
    private String icon;

    @Schema(description = "激活图标样式")
    private String onIcon;

    @Schema(description = "排序")
    private Integer sortOrder;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}

