package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

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
public class UserInfoPageDTO {

    @Schema(description = "用户id")
    @Length(min = 15, max = 25, message = "用户id长度15 - 25！")
    String userId;
    @Schema(description = "关键字（用户名、昵称、手机号、邮箱）")
    @Length(min = 1, max = 40, message = "关键字长度1 - 40！")
    String keyWord;
    @Schema(description = "是否禁用", example = "0否 1是")
    @Range(min = 0, max = 1, message = "参数值为0、1！")
    Integer status;
    @Schema(description = "是否只看客户", example = "0否 1是")
    @Range(min = 0, max = 1, message = "参数值为0、1！")
    Integer isCustomer;
    @Schema(description = "创建时间排序", example = "0asc, 1desc")
    @Range(min = 0, max = 1, message = "参数值为0、1！")
    Integer createTimeSort;
    @Schema(description = "是否隐藏信息", example = "0否 1是")
    @Range(min = 0, max = 1, message = "参数值为0、1！")
    Integer isSimpleCustomer;
}
