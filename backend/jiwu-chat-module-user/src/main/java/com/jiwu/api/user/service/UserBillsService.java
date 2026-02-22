package com.jiwu.api.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.pay.UserBills;
import com.jiwu.api.user.common.dto.InsertBillsDTO;
import com.jiwu.api.user.common.dto.SelectBillsDTO;
import com.jiwu.api.common.main.dto.bills.BillsTimeTotalDTO;
import com.jiwu.api.common.main.dto.bills.BillsTotalDTO;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import com.jiwu.api.user.common.vo.BillsTotalVO;

import java.math.BigDecimal;
import java.util.List;

public interface UserBillsService {


    /**
     * 保存账单
     *
     * @return boolean
     * @
     */
    Boolean saveBills(InsertBillsDTO bill);


    /**
     * 保存充值账单
     *
     * @param title        类型
     * @param currencyType 货币类型：0：金钱 1：积分
     * @param amount       额度
     * @return boolean
     */
    Boolean saveRechargeBills(String userId, String title, Integer currencyType, BigDecimal amount);


    /**
     * 获取支出账单
     *
     * @param userId  用户id
     * @param orderId 订单id
     * @return List<UserBills>
     */
    List<UserBills> getBillsOutByOrder(String userId, String orderId);

    /**
     * 获取收入账单
     *
     * @param userId  用户id
     * @param orderId 订单id
     * @return List<UserBills>
     */
    List<UserBills> getBillsInByOrder(String userId, String orderId);

    /**
     * 分页获取账单数据
     *
     * @param userId 用户id
     * @param dto    参 数
     * @param page   页码
     * @param size   个数
     * @return IPage<UserBills>
     */
    IPage<UserBills> getBillsByDto(String userId, SelectBillsDTO dto, int page, int size);


    /**
     * 获取账单统计信息
     * @param userId 用户id
     * @param dto 参数
     * @return BillsTotalVO
     */
    BillsTotalVO getBillsTotalInfo(String userId, BillsTotalDTO dto) ;

    /**
     * 筛选固定时间账单统计
     * @param userId 用户id
     * @param dto dto
     * @return List<BillsTimeTotalVO>
     */
    List<BillsTimeTotalVO> getBillsTotal(String userId, BillsTimeTotalDTO dto);

}
