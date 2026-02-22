package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.mapper.pay.UserBillsMapper;
import com.jiwu.api.common.main.pojo.pay.UserBills;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.user.common.dto.InsertBillsDTO;
import com.jiwu.api.user.common.dto.SelectBillsDTO;
import com.jiwu.api.common.main.dto.bills.BillsTimeTotalDTO;
import com.jiwu.api.common.main.dto.bills.BillsTotalDTO;
import com.jiwu.api.common.main.enums.total.TotalTimeType;
import com.jiwu.api.common.main.enums.user.WalletType;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import com.jiwu.api.user.common.vo.BillsTotalVO;
import com.jiwu.api.user.service.BillsMQService;
import com.jiwu.api.user.service.UserBillsService;
import com.jiwu.api.common.constant.PayConstant;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserBillsServiceImpl implements UserBillsService {

    @Autowired
    private UserBillsMapper userBillsMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BillsMQService billsMQService;

    /**
     * 保存账单
     *
     * @param dto InsertBillsDTO
     * @return boolean
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean saveBills(InsertBillsDTO dto) {
        UserBills bills = InsertBillsDTO.toUserBills(dto);
        int flag = userBillsMapper.insert(bills);
        if (flag <= 0) {// 失败
            throw new RuntimeException("账单生成错误！");
        }
        // 成功
        redisUtil.delete(PayConstant.USER_BILLS_TOTAL_MAPS + dto.getUserId());// 删除统计信息
        redisUtil.hDelete(PayConstant.USER_BILLS_TOTAL, bills.getUserId() + ":day", bills.getUserId() + ":month", bills.getUserId() + ":year");// 删除统计信息
        redisUtil.hPut(PayConstant.USER_BILLS_MAPS + bills.getUserId(), bills.getId(), bills);
        try {
            billsMQService.autoUpdateWallet(bills.getUserId());
        } catch (BusinessException e) {
            log.info("频控统计钱包 info 频控 {}", e.getMessage());
        }
        return true;
    }


    /**
     * 保存充值账单
     *
     * @param title        类型
     * @param currencyType 货币类型：0：金钱 1：积分
     * @param amount       额度
     * @return boolean
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean saveRechargeBills(String userId, String title, Integer currencyType, BigDecimal amount) {
        InsertBillsDTO dto = new InsertBillsDTO()
                .setUserId(userId)
                .setTitle(title)
                .setCurrencyType(currencyType)
                .setIncomeOrOut(WalletType.IN.getKey())
                .setAmount(amount);
        UserBills bills = InsertBillsDTO.toUserBills(dto);
        int flag = userBillsMapper.insert(bills);
        if (flag <= 0) {// 失败
            throw new RuntimeException("账单生成错误！");
        }
        // 成功
        redisUtil.delete(PayConstant.USER_BILLS_TOTAL_MAPS + dto.getUserId());// 删除统计信息
        redisUtil.hDelete(PayConstant.USER_BILLS_TOTAL, bills.getUserId() + ":day", bills.getUserId() + ":month", bills.getUserId() + ":year");// 删除统计信息
        redisUtil.hPut(PayConstant.USER_BILLS_MAPS + bills.getUserId(), bills.getId(), bills);
        try {
            billsMQService.autoUpdateWallet(bills.getUserId());
        } catch (BusinessException e) {
            log.info("频控统计钱包 info 频控 {}", e.getMessage());
        }
        return true;
    }


    /**
     * 获取支出账单
     *
     * @param userId  用户id
     * @param orderId 订单id
     * @return List<UserBills>
     */
    public List<UserBills> getBillsOutByOrder(String userId, String orderId) {
        return this.getBillsByOrder(userId, orderId, 0);
    }

    /**
     * 获取收入账单
     *
     * @param userId  用户id
     * @param orderId 订单id
     * @return List<UserBills>
     */
    public List<UserBills> getBillsInByOrder(String userId, String orderId) {
        return this.getBillsByOrder(userId, orderId, 1);
    }


    public List<UserBills> getBillsByOrder(String userId, String orderId, Integer type) {
        return userBillsMapper.selectList(new LambdaQueryWrapper<UserBills>().eq(UserBills::getOrdersId, orderId).eq(UserBills::getUserId, userId));
    }

    /**
     * 分页获取账单数据
     *
     * @param userId 用户id
     * @param dto    参 数
     * @param page   页码
     * @param size   个数
     * @return IPage<UserBills>
     */
    public IPage<UserBills> getBillsByDto(String userId, SelectBillsDTO dto, int page, int size) {
        LambdaQueryWrapper<UserBills> qw = new LambdaQueryWrapper<>();
        if (!StringUtil.isNullOrEmpty(userId)) {
            qw.eq(UserBills::getUserId, userId);
        }
        // 订单id查询
        if (dto.getOrderId() != null) {
            qw.like(UserBills::getOrdersId, dto.getOrderId());
        }

        // 类型查询
        if (dto.getType() != null) {
            qw.eq(UserBills::getType, dto.getType());
        }

        // 货币类型查询
        if (dto.getCurrencyType() != null) {
            qw.eq(UserBills::getCurrencyType, dto.getCurrencyType());
        }
        // 时间筛选
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getEndTime().getTime() - dto.getStartTime().getTime() < 0) {
                throw new BusinessException(ResultStatus.SELECT_ERR.getCode(), "时间区间错误！");
            }
            qw.between(UserBills::getCreateTime, dto.getStartTime(), dto.getEndTime());
        }
        IPage<UserBills> pages = userBillsMapper.selectPage(new Page<UserBills>(page, size), qw);
        // redis缓存
        if (!pages.getRecords().isEmpty()) {
            pages.getRecords().forEach(p -> {
                redisUtil.hPut(PayConstant.USER_BILLS_MAPS + userId, p.getId(), p);
            });
        }
        // 结果
        return pages;
    }


    /**
     * 获取账单统计信息
     *
     * @param userId 用户id
     * @param dto    参 数
     * @return Result
     */
    public BillsTotalVO getBillsTotalInfo(String userId, BillsTotalDTO dto) {
        Object data = redisUtil.hGet(PayConstant.USER_BILLS_TOTAL_MAPS + userId, DigestUtils.md5DigestAsHex((dto.toString()).getBytes()));
        BillsTotalVO billsTotalVO;
        if (data != null) {
            return (BillsTotalVO) data;
        }
        QueryWrapper<UserBills> qw = new QueryWrapper<>();
        // 用户 收入
        qw.select("SUM(amount) as total", "type", "currency_type");
        qw.eq("user_id", userId);

        // 1、收入、支出类型查询
        qw.eq(dto.getType() != null, "type", dto.getType());

        // 2、货币类型查询
        qw.eq(dto.getCurrencyType() != null, "currency_type", dto.getCurrencyType());

        // 3、时间筛选
        long dateTime = System.currentTimeMillis();
        boolean timeFlag = dto.getStartTime() != null && dto.getEndTime() != null;
        if (timeFlag) {
            if (dto.getEndTime().getTime() - dto.getStartTime().getTime() < 0) {
                throw new BusinessException(ResultStatus.PARAM_ERR.getCode(),"参数错误，时间区间错误！");
            } else if (dto.getStartTime().getTime() - dateTime > 0) {
                throw new BusinessException(ResultStatus.PARAM_ERR.getCode(),"参数错误，时间是未来时间！");
            } else if (dto.getEndTime().getTime() - dateTime > 0) {
                dto.setEndTime(new Date());
            }
        }
        qw.between(timeFlag, "create_time", dto.getStartTime(), dto.getEndTime());
        // 分组 金钱（00 01）积分（10 11）
        qw.groupBy("type", "currency_type");
        qw.orderByAsc("type", "currency_type");
        List<UserBills> list = userBillsMapper.selectList(qw);
        // 结果
        billsTotalVO = new BillsTotalVO();
        list.forEach(p -> {
            if (p.getType() == 0) { // 支出
                billsTotalVO.setTotalOut(p.getTotal());
            } else {
                billsTotalVO.setTotalIn(p.getTotal());
            }
        });
        if (!list.isEmpty()) {
            // 5、redis缓存
            redisUtil.hPut(PayConstant.USER_BILLS_TOTAL_MAPS + userId, DigestUtils.md5DigestAsHex((dto.toString()).getBytes()), billsTotalVO);
        }
        return billsTotalVO;
    }


    /**
     * 筛选固定时间账单统计
     *
     * @param userId 用户id
     * @param dto    dto
     * @return List<BillsTimeTotalVO>
     */
    public List<BillsTimeTotalVO> getBillsTotal(String userId, BillsTimeTotalDTO dto) {
        // 条件
        String str = "DATE(create_time)";
        String field = userId + ":day";
        switch (dto.getTimeType()) {
            case TotalTimeType.DAY:
                str = "DATE(create_time)";
                field = userId + ":day";
                break;
            case TotalTimeType.MONTH:
                str = "DATE_FORMAT(create_time, '%Y-%m')";
                field = userId + ":month";
                break;
            case TotalTimeType.YEAR:
                str = "DATE_FORMAT(create_time, '%Y')";
                field = userId + ":year";
                break;
        }
        Object data = redisUtil.hGet(PayConstant.USER_BILLS_TOTAL, field);
        if (data != null) {
            return BillsTimeTotalVO.filterByDTO((List<BillsTimeTotalVO>) data, dto);
        }
        List<BillsTimeTotalVO> list = userBillsMapper.getUserTimeBills(str, userId);
        if (!list.isEmpty()) {
            redisUtil.hPut(PayConstant.USER_BILLS_TOTAL, field, list);
        }
        return BillsTimeTotalVO.filterByDTO(list, dto);
    }


}
