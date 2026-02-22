package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 角色表实体类
 *
 * @className: Role
 * @author: Kiwi23333
 * @description: 角色表实体类
 * @date: 2023/5/2 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_role")
public class Role {

        /**
         * 角色ID
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private String id;

        /**
         * 父id
         */
        private String parentId;
        /**
         * 创建人
         */
        private String creator;

        /**
         * 角色名称
         */
        private String name;
        /**
         * 角色唯一CODE代码
         */
        private String code;

        /**
         * 角色介绍
         */
        private String intro;

        /**
         * 创建时间
         */
        @TableField(fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        /**
         * 修改时间
         */
        @TableField(fill = FieldFill.INSERT_UPDATE)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;

}
