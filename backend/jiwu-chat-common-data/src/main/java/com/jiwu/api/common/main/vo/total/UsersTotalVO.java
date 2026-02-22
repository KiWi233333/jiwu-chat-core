package com.jiwu.api.common.main.vo.total;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 *
 * @className: OrderTotalVO
 * @author: Kiwi23333
 * @description:
 * @date: 2023/8/26 19:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UsersTotalVO {

    @Schema(description = "全部用户量")
    private Long allUsers;

    @Schema(description = "本月新增用户量")
    private Long monthNewUsers;

//    @Schema(description = "今日活跃用户量")
//    private Long todayActiveUsers;
}
