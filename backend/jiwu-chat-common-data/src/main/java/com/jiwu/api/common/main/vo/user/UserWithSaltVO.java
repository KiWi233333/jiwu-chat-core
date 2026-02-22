package com.jiwu.api.common.main.vo.user;

import com.baomidou.mybatisplus.annotation.*;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.enums.user.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UserWithSaltVO {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String nickname;

    private Gender gender;

    private String avatar;

    private Integer userType;

    private String slogan;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("status")
    private Integer status;

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

    @TableField("salt")
    private String salt;
}
