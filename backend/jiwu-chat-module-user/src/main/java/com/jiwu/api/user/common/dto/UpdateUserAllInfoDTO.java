package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.enums.user.Gender;
import com.jiwu.api.common.main.pojo.sys.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.util.Date;

/**
 * 用户基本信息修改参数DTO(管理员)
 *
 * @className: UpdateUserInfoDTO
 * @author: Kiwi23333
 * @description: 用户基本信息修改参数DTO
 * @date: 2023/5/9 3:06
 */

@Data
public class UpdateUserAllInfoDTO {

    @Schema(description = "昵称")
    @Length(min = 2, max = 30, message = "昵称为2-30字符！")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$", message = "昵称只能包含中文、英文、数字！")
    private String nickname;


    @Schema(description = "密码")
    @Length(min = 6, max = 20, message = "密码6-20个字符！")
    private String password;

    @Schema(description = "性别（男|女|保密）")
    private Gender gender;

    @Schema(description = "状态（0|1）")
    @Range(min = 0, max = 1, message = "状态只能为0-1！")
    private Integer status;


    @Schema(description = "头像")
    @Length(max = 200, message = "头像长度不超过200字符！")
    private String avatar;

    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Past(message = "生日应为过去日期！")
    private Date birthday;



    public static User toUser(UpdateUserAllInfoDTO u) {
        return new User()
                .setStatus(u.getStatus())
                .setNickname(u.getNickname())
                .setGender(u.getGender())
                .setAvatar(u.getAvatar())
                .setBirthday(u.getBirthday());
    }
}
