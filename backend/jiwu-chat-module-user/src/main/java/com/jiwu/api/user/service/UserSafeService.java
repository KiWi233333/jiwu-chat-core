package com.jiwu.api.user.service;

import com.jiwu.api.common.config.interceptor.UserAgentVO;
import com.jiwu.api.user.common.dto.DeleteSafeDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 账户与安全业务
 *
 * @className: UserService
 * @author: Kiwi23333
 * @description: 账户与安全业务
 * @date: 2023/4/13 14:54
 */
public interface UserSafeService {
    /**
     * 获取用户登录设备
     * @param userId 用户id
     * @return List<UserAgentVO>
     */
    List<UserAgentVO> getUserLoginDevice(String userId, HttpServletRequest request);

    /**
     *
     * 用户下线指定用户
     * @param userId 用户id
     * @param dto dto
     * @return 下线设备数量
     */
    Integer toUserOfflineByOne(String userId, DeleteSafeDTO dto);
}
