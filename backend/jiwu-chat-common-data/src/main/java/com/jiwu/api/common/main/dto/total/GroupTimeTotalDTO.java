package com.jiwu.api.common.main.dto.total;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * 查询订单统计参数
 *
 * @className: OrdersTotalDTO
 * @author: Kiwi23333
 * @description: TODO查询订单统计参数
 * @date: 2023/8/29 13:52
 */
@Data
public class GroupTimeTotalDTO {

    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "请选择起始时间！")
    private Date startTime;

    @Schema(description = "截至时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "请选择结束时间！")
    private Date endTime;


    @Schema(description = "日期分组（0:日，1:月，2:年）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 0, message = "日期分组取值范围为0-2")
    @Max(value = 2, message = "日期分组取值范围为0-2")
    @NotNull(message = "请选择筛选类型！")
    private Integer timeType;
}
