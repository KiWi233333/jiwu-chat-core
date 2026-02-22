package com.jiwu.api.common.main.vo.bills;

import com.jiwu.api.common.main.dto.bills.BillsTimeTotalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单统计（总的每年每月每日）
 *
 * @className: BillsTimeTotalVO
 * @author: Kiwi23333
 * @description: BillsTimeTotalVO
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BillsTimeTotalVO implements Serializable {
    /**
     * 时间
     */
    String time;

    /**
     * 0：支出，1：收入
     */
    private Integer type;

    /**
     * 货币类型 0：余额，1：积分
     */
    private Integer currencyType;
    /**
     * 总计
     */
    private BigDecimal total;

    /**
     * 过滤结果
     *
     * @param list list
     * @param dto  dto
     * @return list
     */
    public static List<BillsTimeTotalVO> filterByDTO(List<BillsTimeTotalVO> list, BillsTimeTotalDTO dto) {
        return list.stream().filter(p -> (dto.getType() == null || dto.getType().equals(p.getType())) && (dto.getCurrencyType() == null || dto.getCurrencyType().equals(p.getCurrencyType()))).collect(Collectors.toList());
    }
}
