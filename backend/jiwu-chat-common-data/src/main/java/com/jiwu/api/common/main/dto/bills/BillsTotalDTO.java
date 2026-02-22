package com.jiwu.api.common.main.dto.bills;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * 描述
 *
 * @className: BillsTotalVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BillsTotalDTO {

    @Schema(description = "类型：0:支出1:收入")
    @Range(min = 0, max = 1, message = "收支类型可选值为0,1")
    Integer type;

    @Schema(description = "货币类型：0:支出1:收入")
    @NotNull(message = "货币类型不能为空")
    @Range(min = 0, max = 1, message = "货币类型可选值为0,1")
    Integer currencyType;

    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "截至时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
