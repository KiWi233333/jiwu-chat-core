package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.enums.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String nickname;

    /**
     * 性别 （男|女|保密）
     */
    private Gender gender;

    private String avatar;

    /**
     * 用户类型
     */
    private Integer userType;


    /**
     * 个性签名
     */
    private String slogan;


    /**
     * 登录的ip
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 状态：0 封禁 1 正常
     */
    @TableField("status")
    private Integer status;


    /**
     * @see ChatActiveStatusEnum
     */
    @Schema(description = "在线状态 (0离线 1在线)", example = "1")
    private Integer activeStatus;


    @TableField("is_email_verified")
    private Integer isEmailVerified;

    @TableField("is_phone_verified")
    private Integer isPhoneVerified;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("last_login_time")
    private Date lastLoginTime;
}
