package com.jiwu.api.common.main.mapper.pay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.pay.UserBills;
import com.jiwu.api.common.main.vo.bills.BillsTimeTotalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;


@Mapper
public interface UserBillsMapper extends BaseMapper<UserBills> {

    // 获取账单统计
    @Select("SELECT ${sql} time, type, currency_type, SUM(amount) AS total FROM user_bills WHERE user_id = #{userId} GROUP BY ${sql}, type, currency_type ORDER BY ${sql} LIMIT 80")
    List<BillsTimeTotalVO> getUserTimeBills(@Param("sql") String selectSql, @Param("userId") String userId);

    // 获取账单统计
    @Select("SELECT DATE_FORMAT(create_time,${type}) time, type, currency_type, SUM(amount) AS total FROM user_bills WHERE update_time BETWEEN #{startTime} AND #{endTime} GROUP BY DATE_FORMAT(create_time,${type}), type, currency_type LIMIT 100")
    List<BillsTimeTotalVO> getBillsTotal(@Param("type") String timeType, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}

