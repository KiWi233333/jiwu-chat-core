package com.jiwu.api.common.main.vo.total;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 主页每日统计
 *
 * @className: OrderTotalVO
 * @author: Kiwi23333
 * @description: 主页每日统计
 * @date: 2023/8/26 19:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IndexTotalVO {

    @Schema(description = "用户统计")
    private UsersTotalVO usersTotal;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
