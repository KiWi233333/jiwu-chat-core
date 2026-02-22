package com.jiwu.api.common.main.dto.chat.friend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;


/**
 * Description: 好友申请数量
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectApplyReadDTO {

    @Schema(description = "查询类型 1已读 2未读")
    @NotNull(message = "查询类型不能为空！")
    @Range(min = 0, max = 1, message = "查询类型错误！")
    private Integer type;

}
