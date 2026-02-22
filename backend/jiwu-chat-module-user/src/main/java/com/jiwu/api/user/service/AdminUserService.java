package com.jiwu.api.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertOrUpdateRobotDTO;
import com.jiwu.api.user.common.dto.InsertAdminUserDTO;
import com.jiwu.api.user.common.dto.UserInfoPageDTO;

public interface AdminUserService {


    /**
     * 获取用户列表
     *
     * @param page 页码
     * @param size 每页个数
     * @return Result
     */
    Page<User> getUserInfoPage(Integer page, Integer size, UserInfoPageDTO userInfoPageDTO);

    /**
     * 强制用户下辖
     *
     * @param userId id
     * @return Result
     */
    Result<Integer> loginOutById(String userId);

    /**
     * 用户禁用
     *
     * @param userId 用户id
     * @return Result
     */
    Result<Integer> toUserDisableToggle(String userId, Integer disable);


    /**
     * 添加后台用户
     * @param insertAdminUserDTO 参数
     * @return 添加行数
     */
    Result<Integer> addAdminUser(InsertAdminUserDTO insertAdminUserDTO);

    /**
     * 刷新或保存机器人
     *
     * @param dto 参数
     * @return 添加user
     */
    User refreshOrSaveRobot(InsertOrUpdateRobotDTO dto);
}
