package com.jiwu.api.admin.total.service;

import com.jiwu.api.common.main.dto.total.GroupTimeTotalDTO;
import com.jiwu.api.common.main.vo.total.IndexTotalVO;
import com.jiwu.api.common.main.vo.total.UsersTotalVO;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;

import java.util.List;

/**
 * 统计服务（开源版仅保留用户与账单统计）
 */
public interface TotalService {
    UsersTotalVO getUsersTotal();

    IndexTotalVO getIndexTotal();

    List<BillsTimeTotalVO> getBillsTotalList(GroupTimeTotalDTO dto);
}
