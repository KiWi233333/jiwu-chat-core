package com.jiwu.api.user.common.vo;

import com.jiwu.api.common.main.enums.user.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 社区用户个人信息（简约）
 *
 * @className: UserVo
 * @author: Kiwi23333
 * @description: 社区用户个人信息（简约）
 * @date: 2023/4/30 14:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CommUserVO {

    /**
     * 邮箱
     */
    private String email;


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
    private Date birthday;;

    /**
     * 个性签名
     */
    private String slogan;

    /**
     * 账号创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;


    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date lastLoginTime;
//
//    /**
//     * 登录状态
//     */
//    private Integer status




}
