package com.jiwu.api.common.main.dto.chat.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 用户列表搜索参数类
 *
 * @className: UserListDTO
 * @author: Kiwi23333
 * @description: 用户列表搜索参数嘞
 * @date: 2023/5/14 12:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChatUserInfoPageDTO {

    @Schema(description = "用户id")
    @Length(min = 15, max = 25, message = "用户id长度15 - 25！")
    String userId;

    @Schema(description = "关键字（用户名、昵称、手机号、邮箱）")
    @Length(min = 1, max = 40, message = "关键字长度1 - 40！")
    String keyWord;

}
