package com.jiwu.api.user.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户下线
 *
 * @className: BillsTotalVO
 * @author: Kiwi23333
 * @description: 用户下线
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DeleteSafeDTO {

    @Schema(description = "用户凭证")
    @NotNull(message = "参数不能为空！")
    List<String> userAgent;

}
