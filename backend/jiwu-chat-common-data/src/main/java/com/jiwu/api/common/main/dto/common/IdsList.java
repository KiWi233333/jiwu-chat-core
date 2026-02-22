package com.jiwu.api.common.main.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * ids集合
 *
 * @className: IdList
 * @author: Kiwi23333
 * @description: ids集合
 * @date: 2023/5/1 21:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdsList {

    @Schema(description = "ids", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ids不能为空！")
    @Size(min = 1, max = 50, message = "选择条数应为1-50！")
    List<String> ids;
}
