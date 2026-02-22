package com.jiwu.api.admin.total.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiwu.api.common.main.mapper.pay.UserBillsMapper;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.main.vo.total.IndexTotalVO;
import com.jiwu.api.common.main.vo.total.UsersTotalVO;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.main.dto.total.GroupTimeTotalDTO;
import com.jiwu.api.common.main.enums.total.DateGroupType;
import com.jiwu.api.admin.total.service.TotalService;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import com.jiwu.api.common.constant.UserConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.sql.Date;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jiwu.api.common.main.constant.total.TotalConstant.TOTAL_BILLS_SALES_LIST;

/**
 * 统计业务（开源版仅保留用户与账单统计）
 */
@Service
public class TotalServiceImpl implements TotalService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public UsersTotalVO getUsersTotal() {
        UsersTotalVO vo = new UsersTotalVO();
        vo.setAllUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserType, UserType.CUSTOMER.getCode())));
        LocalDate now = LocalDate.now();
        vo.setMonthNewUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserType, UserType.CUSTOMER.getCode()).ge(User::getCreateTime, now.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth())).le(User::getCreateTime, now)));
        redisUtil.get(UserConstant.USER_REFRESH_TOKEN_KEY);
        return vo;
    }

    @Override
    public IndexTotalVO getIndexTotal() {
        Object obj = redisUtil.get("total:main");
        IndexTotalVO vo;
        if (obj == null) {
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = LocalDate.now().atTime(LocalTime.MAX);
            vo = new IndexTotalVO();
            vo.setUsersTotal(this.getUsersTotal());
            vo.setCreateTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
            vo.setUpdateTime(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
            redisUtil.set("total:main", vo, Duration.between(startTime, endTime).getSeconds());
        } else {
            vo = (IndexTotalVO) obj;
        }
        return vo;
    }

    @Autowired
    UserBillsMapper billsMapper;

    @Override
    public List<BillsTimeTotalVO> getBillsTotalList(GroupTimeTotalDTO dto) {
        Object obj = redisUtil.get(TOTAL_BILLS_SALES_LIST + DigestUtils.md5DigestAsHex((dto.toString()).getBytes()));
        if (obj != null) {
            return (List<BillsTimeTotalVO>) obj;
        }
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getEndTime().getTime() - dto.getStartTime().getTime() <= 0) {
                return new ArrayList<>();
            }
        }
        List<BillsTimeTotalVO> list = billsMapper.getBillsTotal(DateGroupType.getVal(dto.getTimeType()), dto.getStartTime(), dto.getEndTime());
        if (!list.isEmpty()) {
            redisUtil.set(TOTAL_BILLS_SALES_LIST + DigestUtils.md5DigestAsHex((dto.toString()).getBytes()), list, 1, TimeUnit.DAYS);
        }
        return list;
    }
}
