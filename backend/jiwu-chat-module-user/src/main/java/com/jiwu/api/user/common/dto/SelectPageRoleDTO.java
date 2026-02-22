package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * 查询角色列表分页DTO
 *
 * @className: UpDateAvatarDTO
 * @author: Kiwi23333
 * @description: 查询角色列表DTO
 * @date: 2023/8/25 14:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SelectPageRoleDTO {

    @Schema(description = "角色ID")
    @Length(min = 10, max = 25, message = "创建人长度为10-20！")
    private String id;

    @Schema(description = "父角色ID")
    @Length(min = 10, max = 25, message = "创建人长度为10-20！")
    private String parentId;

    @Schema(description = "角色名称")
    @Length(min = 1, max = 20, message = "搜索名称长度为1-20！")
    private String name;

    @Schema(description = "角色码")
    @Length(min = 1, max = 50, message = "角色码长度为1-50！")
    private String code;

    @Schema(description = "时间排序", example = "0 asc, 1 desc")
    @Range(min = 0, max = 1, message = "时间排序参数错误")
    Integer timeSort;


}
