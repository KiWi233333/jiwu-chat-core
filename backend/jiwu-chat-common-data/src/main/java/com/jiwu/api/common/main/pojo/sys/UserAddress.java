package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 描述
 *
 * @className: UserAddress
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/16 2:31
 */
@Data
@Accessors(chain = true)
@TableName("sys_user_address")
public class UserAddress {


    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 收货人
     */
    private String name;

    /**
     * 用户id
     */
    private String userId;
    /**
     * 是否默认
     */
    private Integer isDefault;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String county;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 邮编
     */
    private String postalCode;

    /**
     * 手机号
     */
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

