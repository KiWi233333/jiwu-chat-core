package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 角色权限关系表 实体类
 *
 * @className: RolePermission
 * @author: Kiwi23333
 * @description: 角色权限关系表
 * @date: 2023/5/2 13:54
 */
@Data
@Accessors(chain = true)
@TableName("sys_role_permission")
public class RolePermission {

        /**
         * 权限ID
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private String id;

        /**
         * 角色ID
         */
        private String roleId;

        /**
         * 权限ID
         */
        private String permissionId;

        /**
         * 创建人
         */
        private String creator;

        @TableField(fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date createTime;

        @TableField(fill = FieldFill.INSERT_UPDATE)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date updateTime;

    }
