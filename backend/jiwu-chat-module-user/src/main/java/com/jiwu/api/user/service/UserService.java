package com.jiwu.api.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.*;
import com.jiwu.api.user.common.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户业务
 *
 * @className: UserService
 * @author: Kiwi23333
 * @description: 用户业务
 * @date: 2023/4/13 14:54
 */
public interface UserService {
    /** -------------------User 登录相关操作--------------------- **/

    /**
     * 密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result
     */
    String toUserLoginByPwd(String username, String password, Integer userType, HttpServletRequest request);

    /**
     * 管理员密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result
     */
    String toUserLoginByPwdAndTypes(String username, String password, List<Integer> adminLoginTypes, HttpServletRequest request);
    /**
     * 管理员密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result
     */
    String toAdminLoginByPwd(String username, String password, HttpServletRequest request);

    /**
     * 手机验证码登录 code
     *
     * @param phone 手机号
     * @param code  验证码
     * @return Result
     */
    String toUserLoginByPhoneCode(String phone, String code, HttpServletRequest request);

    /**
     * 邮箱验证码登录 code
     *
     * @param email 邮箱
     * @param code  验证码
     * @return Result
     */
    String toUserLoginByEmailCode(String email, String code, HttpServletRequest request);

    // 1) 获取登录手机验证码
    Boolean getLoginCodeByPhone(String phone);

    // 2) 获取登录邮箱验证码
    Boolean getLoginCodeByEmail(String email);


    /**-------------------注册相关操作--------------------- */

    /**
     * 用户注册
     *
     * @param u       UserRegisterDTO对象
     * @param request HttpServletRequest对象
     * @return Result
     */
    String toRegister(UserRegisterDTO u, HttpServletRequest request);


    /**
     * 用户快速注册（手机号、邮箱免密码）
     *
     * @param dto UserRegisterDTO对象
     * @return Result
     */
    String toRegisterV2(UserRegisterV2DTO dto, HttpServletRequest request);

    // 1) 手机号注册-获取验证码
    Boolean getRegisterCodeByPhone(String phone);

    // 2) 邮箱注册-获取验证码
    Boolean getRegisterCodeByEmail(String email);

    /**
     * 创建用户（核心注册方法，供内部注册等场景调用）
     *
     * @param user 用户对象（需设置 username, nickname, avatar 等基本信息）
     * @return 创建成功的用户ID
     */
    String createUser(User user);


    /**
     * 验证-用户是否存在
     *
     * @param username 用户名
     * @return Result
     */
    Result<Object> checkUserIsExist(String username);


    /**
     * 退出登录（单个）
     *
     * @param userId 用户id
     * @return Result
     */
    Boolean logoutOne(String userId, HttpServletRequest request);

    /**
     * 退出登录（所有）停用
     *
     * @param userId 用户id
     * @param ip     ip
     * @return Result
     */
    Result<Object> logoutAll(String userId, String ip);

    /* * ---------------------用户信息相关------------------------ */

    /**
     * 获取用户所有信息
     *
     * @param userId 用户id
     * @return Result<UserVO>
     */
    UserVO getUserInfoById(String userId);


    /**
     * 修改用户头像
     *
     * @param file   头像文件
     * @param userId 用户id
     * @return Result
     */
    String updateUserAvatar(MultipartFile file, String userId);

    /**
     * 修改密码
     *
     * @param updatePwdDto 参数
     * @param userId       用户id
     * @return 用户密码
     */
    Boolean updatePwdByOldNewPwd(UpdatePwdDTO updatePwdDto, String userId);


    /**
     * 修改用户密码
     *
     * @param type 类型
     * @param dto  参数
     * @return Result
     */
    Integer updatePwdByCode(Integer type, UpdateSecondPwdDTO dto);

    /**
     * 修改用户基本信息
     *
     * @param updateUserInfoDTO updateUserInfoDTO
     * @param userId            用户id
     * @return Result
     */
    Boolean updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO, String userId);

    /**
     * 修改用户全部信息（管理员权限）
     *
     * @param updateUserInfoDTO updateUserInfoDTO
     * @param userId            用户id
     * @return Result
     */
    Integer updateUserAllInfo(UpdateUserAllInfoDTO updateUserInfoDTO, String userId);

    /**
     * 管理员修改用户密码
     *
     * @param userId 用户id
     * @param dto    参数
     * @return Result 是否成功
     */
    Integer updateUserPwdByAdmin(String userId, UpdateNewPwdDTO dto);

    /**
     * 修改手机号
     *
     * @param updatePhoneDTO 参数
     * @param userId         用户id
     * @return Result
     */
    Boolean updateUserPhone(UpdatePhoneDTO updatePhoneDTO, String userId);

    /**
     * 修改邮箱
     *
     * @param updateEmailDTO 参数
     * @param userId         用户id
     * @return Result
     */
    Boolean updateUserEmail(UpdateEmailDTO updateEmailDTO, String userId);

    /**
     * 获取新手机/邮箱验证码
     *
     * @param key 手机号、邮箱
     * @return Result
     */
    Boolean sendUpdateCode(String key, Integer type);

    /**
     * 发送验证手机号|邮箱验证码
     *
     * @param key  手机号|邮箱
     * @param type Type
     * @return Result
     */
    Boolean sendCheckCode(String key, Integer type);

    /**
     * 验证手机号
     *
     * @param userId 用户id
     * @param type   标识 0:手机号，1:邮箱
     * @param key    手机号|邮箱
     * @param code   验证码
     * @return Result
     */
    Boolean onCheckCode(String userId, Integer type, String key, String code);


}
