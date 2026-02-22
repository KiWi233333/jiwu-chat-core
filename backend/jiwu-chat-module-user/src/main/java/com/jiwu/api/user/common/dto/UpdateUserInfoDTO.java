package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.enums.user.Gender;
import com.jiwu.api.common.main.pojo.sys.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Pattern;
import java.util.Date;

/**
 * 用户基本信息修改参数DTO
 *
 * @className: UpdateUserInfoDTO
 * @author: Kiwi23333
 * @description: 用户基本信息修改参数DTO
 * @date: 2023/5/9 3:06
 */

@Data
public class UpdateUserInfoDTO {


    @Schema(description = "昵称")
    @Length(min = 2, max = 30, message = "昵称为2-30字符！")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$", message = "昵称只能包含中文、英文、数字！")
    private String nickname;

    @Schema(description = "性别（男|女|保密）")
    private Gender gender;

    @Schema(description = "个性签名")
    @Length(max = 100, message = "个性签名长度不能超过100字符！")
    private String slogan;

    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;


    public static User toUser(UpdateUserInfoDTO u) {
        return new User()
                .setNickname(u.getNickname())
                .setGender(u.getGender())
                .setSlogan(u.getSlogan())
                .setBirthday(u.getBirthday());
    }
}
