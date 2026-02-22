package com.jiwu.api.user.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 查询帖子dto
 *
 * @className: SelectCommPostDTO
 * @author: Kiwi23333
 * @description:
 * @date: 2023/6/9 2:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SelectBillsDTO {


    @Schema(description = "订单id")
    String orderId;

    @Schema(description = "类型：0:支出1:收入")
    Integer type;
    @Schema(description = "货币类型：0:支出1:收入")
    Integer currencyType;

    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @Past(message = "时间超过范围！")
    private Date startTime;

    @Schema(description = "截至时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @Future(message = "时间超过范围！")
    private Date endTime;
}
