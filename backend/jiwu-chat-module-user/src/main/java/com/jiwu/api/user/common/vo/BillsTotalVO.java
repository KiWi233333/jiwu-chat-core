package com.jiwu.api.user.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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
public class BillsTotalVO {
    /**
     * 消费总额
     */
    BigDecimal totalIn;
    /**
     * 支出总额
     */
    BigDecimal totalOut;
}
