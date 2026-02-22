package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.enums.user.Gender;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Date;

/**
 * 添加管理员(管理员)
 *
 * @className: UpdateUserInfoDTO
 * @author: Kiwi23333
 * @description: 添加管理员
 * @date: 2023/5/9 3:06
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsertAdminUserDTO {

    @Schema(description = "用户名|员工号")
    @NotNull(message = "用户名不能为空！")
    @Length(min = 6, max = 30, message = "用户名6-30个字符！")
    private String username;

    @Schema(description = "昵称")
    @NotNull(message = "昵称不能为空！")
    @Length(max = 40, message = "1-40个字符！")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$", message = "昵称只能包含中文、英文、数字！")
    private String nickname;


    @Schema(description = "密码")
    @NotNull(message = "密码不能为空！")
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
    private Date birthday;


    @Schema(description = "用户类型（0-普通用户，1-后台，2-客服，3-机器人）", example = "1")
    @Range(min = 0, max = 3, message = "用户类型只能为0-3！")
    private Integer userType;


    public static User toUser(InsertAdminUserDTO u) {
        return new User()
                .setUsername(u.getUsername())
                .setNickname(u.getNickname())
                .setStatus(u.getStatus() == null ? 0 : u.getStatus())
                .setUserType(UserType.ADMIN.getCode())
                .setPassword(u.getPassword())
                .setBirthday(u.getBirthday())
                .setAvatar(u.getAvatar())
                .setGender(u.getGender())
                .setUserType(u.getUserType() == null ? UserType.ADMIN.getCode() : u.getUserType())
                .setBirthday(u.getBirthday());
    }
}
