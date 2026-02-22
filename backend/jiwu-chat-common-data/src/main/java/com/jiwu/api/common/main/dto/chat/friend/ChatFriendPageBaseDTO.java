package com.jiwu.api.common.main.dto.chat.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChatFriendPageBaseDTO extends PageBaseDTO {

    @Schema(description = "关键字（用户名、昵称、手机号、邮箱）")
    @Length(max = 50, message = "关键字不超过50字符！")
    String keyWord;
}
