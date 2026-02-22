package com.jiwu.api.user.common.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 权限表
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PermissionVO {


    /**
     * 权限ID
     */
    private String id;

    /**
     * 所属父级权限ID
     */
    private String parentId;

    /**
     * 权限唯一CODE代码
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限介绍
     */
    private String intro;

    /**
     * 创建人id
     */
    private String creator;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
