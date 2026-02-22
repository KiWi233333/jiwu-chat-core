package com.jiwu.api.common.main.dto.bills;

import com.jiwu.api.common.main.enums.total.TotalTimeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;

/**
 * BillsTimeTotalDTO
 *
 * @className: BillsTimeTotalDTO
 * @author: Kiwi23333
 * @description: BillsTimeTotalDTO
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BillsTimeTotalDTO {

    @Schema(description = "类型：0:支出1:收入")
    @Range(min = 0, max = 1, message = "收支类型可选值为0,1")
    Integer type;

    @Schema(description = "货币类型：0:支出1:收入")
    @Range(min = 0, max = 1, message = "货币类型可选值为0,1")
    Integer currencyType;

    @Schema(description = "时间分类:YEAR年,MONTH月,DAY日")
    @NotNull(message = "时间分类不能为空")
    TotalTimeType timeType;
}
