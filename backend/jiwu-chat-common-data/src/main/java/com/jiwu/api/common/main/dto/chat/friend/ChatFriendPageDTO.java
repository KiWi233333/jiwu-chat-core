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
public class ChatFriendPageDTO {

    @Schema(description = "关键字（用户名、昵称、手机号、邮箱）")
    @Length(max = 40, message = "关键字不超过40字符！")
    String keyWord;
}
