package com.jiwu.api.user.common.vo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.main.enums.user.Gender;
import com.jiwu.api.common.main.pojo.sys.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 描述
 *
 * @className: UserVo
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/4/30 14:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserVO {


    /**
     * id
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别 （男|女|保密）
     */
    private Gender gender;

    /**
     * 头像icon
     */
    private String avatar;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date birthday;

    /**
     * 账号创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String lastLoginIp;

    /**
     * 登录状态
     */
    private Integer status;

    /**
     * 是否有密码
     */
    private Integer isPasswordVerified;
    /**
     * 邮箱是否验证（绑定）
     */
    private Integer isEmailVerified;
    /**
     * 手机号是否验证（绑定）
     */
    private Integer isPhoneVerified;


    /**
     * 个性签名
     */
    private String slogan;

    public static UserVO toUserVo(User user) {
        return new UserVO()
                .setId(user.getId())
                .setLastLoginIp(user.getLastLoginIp())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setPhone(user.getPhone())
                .setNickname(user.getNickname())
                .setAvatar(user.getAvatar())
                .setBirthday(user.getBirthday())
                .setUpdateTime(user.getUpdateTime())
                .setGender(user.getGender())
                .setCreateTime(user.getCreateTime())
                .setLastLoginTime(user.getLastLoginTime())
                .setIsEmailVerified(user.getIsEmailVerified())
                .setIsPhoneVerified(user.getIsPhoneVerified())
                .setSlogan(user.getSlogan())
                .setIsPasswordVerified(StringUtils.isNotBlank(user.getPassword())? 1 : 0)
                .setStatus(user.getStatus());
    }

}
