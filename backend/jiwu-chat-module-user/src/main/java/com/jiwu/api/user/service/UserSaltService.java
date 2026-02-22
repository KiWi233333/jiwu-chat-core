package com.jiwu.api.user.service;

import com.jiwu.api.common.main.dto.user.UserCheckDTO;

import java.util.List;


/**
 * 个人盐值业务层
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
public interface UserSaltService {

    /**
     * 获取用户的加密密码和专属盐 （通过用户名/邮箱/手机号 为key存储盐）
     *
     * @param username 用户名/邮箱/手机号
     * @return UserCheckDTO
     */
    UserCheckDTO getUserSalt(String username, Integer userType);

    /**
     * 获取用户的加密密码和专属盐 （通过用户名/邮箱/手机号 为key存储盐）
     *
     * @param username 用户名/邮箱/手机号
     * @return UserCheckDTO
     */
    UserCheckDTO getUserSaltByTypes(String username, List<Integer> adminLoginTypes);

    /**
     * 获取用户的加密密码和专属盐 （通过id 为key存储盐）
     *
     * @param userId 用户id
     * @return UserCheckDTO
     */
    UserCheckDTO getUserSaltById(String userId);

    /**
     * 添加用户盐
     *
     * @param userId   用户id
     * @param password 加密后密码
     * @param salt     盐值
     * @return Boolean 是否成功
     */
    Boolean addUserSalt(String userId, String password, String salt);

    /**
     * 创建用户盐值记录（用于OAuth等无密码注册场景）
     *
     * @param userId   用户ID
     * @param password 密码（可为null）
     * @return 是否成功
     */
    Boolean createUserSalt(String userId, String password);

}
