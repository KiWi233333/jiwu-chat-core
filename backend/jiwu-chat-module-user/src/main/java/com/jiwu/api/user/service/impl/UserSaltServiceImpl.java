package com.jiwu.api.user.service.impl;

import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.mapper.sys.UserSaltMapper;
import com.jiwu.api.common.main.pojo.sys.UserSalt;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.dto.user.UserCheckDTO;
import com.jiwu.api.user.service.UserSaltService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;


/**
 * 个人盐值业务层
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
@Service
public class UserSaltServiceImpl implements UserSaltService {
    @Resource
    private UserSaltMapper userSaltMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisUtil<String, Object> redisUtil;

    /**
     * 获取用户的加密密码和专属盐 （通过用户名/邮箱/手机号 为key存储盐）
     *
     * @param username 用户名/邮箱/手机号
     * @return UserCheckDTO
     */
    @Override
    public UserCheckDTO getUserSalt(@NonNull String username, Integer userType) {
        UserCheckDTO saltVO = userMapper.selectUserCheckByUname(username, userType);
        // 未注册
        if (saltVO == null) {
            return null;
        }
        // 封禁
        if (saltVO.getStatus() != null && NormalOrNoEnum.NOT_NORMAL.getStatus().equals(saltVO.getStatus())) {
            throw new BusinessException(ResultStatus.STATUS_OFF_ERR.getCode(), "账号被封禁，详情联系客服！");
        }
        // 缓存
        redisUtil.set(UserConstant.userSaltDTOKey(saltVO.getId()), saltVO);
        return saltVO;
    }

    /**
     * 获取用户的加密密码和专属盐 （通过用户名/邮箱/手机号 为key存储盐）
     *
     * @param username        用户名/邮箱/手机号
     * @param adminLoginTypes 登录类型
     * @return UserCheckDTO
     */
    @Override
    public UserCheckDTO getUserSaltByTypes(String username, List<Integer> adminLoginTypes) {
        UserCheckDTO saltVO = userMapper.selectUserCheckByUnameTypes(username, adminLoginTypes);
        // 未注册
        if (saltVO == null) {
            return null;
        }
        // 封禁
        if (saltVO.getStatus() != null && NormalOrNoEnum.NOT_NORMAL.getStatus().equals(saltVO.getStatus())) {
            throw new BusinessException(ResultStatus.STATUS_OFF_ERR.getCode(), "账号被封禁，详情联系客服！");
        }
        // 缓存
        redisUtil.set(UserConstant.userSaltDTOKey(saltVO.getId()), saltVO);
        return saltVO;
    }

    /**
     * 获取用户的加密密码和专属盐 （通过id 为key存储盐）
     *
     * @param userId 用户id
     * @return UserCheckDTO
     */
    @Override
    public UserCheckDTO getUserSaltById(String userId) {
        UserCheckDTO userCheckDTO = (UserCheckDTO) redisUtil.get(UserConstant.userSaltDTOKey(userId));
        if (userCheckDTO == null) {// 空则从数据库取
            userCheckDTO = userMapper.selectUserCheckById(userId);
            // 缓存
            redisUtil.set(UserConstant.userSaltDTOKey(userCheckDTO.getId()), userCheckDTO);
            return userCheckDTO;
        }
        return userCheckDTO;
    }

    /**
     * 添加用户盐
     *
     * @param userId   用户id
     * @param password 加密后密码
     * @param salt     盐值
     * @return Boolean 是否成功
     */
    @Override
    public Boolean addUserSalt(String userId, String password, String salt) {
        UserSalt userSalt = new UserSalt(userId, salt);
        UserCheckDTO userCheckDTO = new UserCheckDTO()
                .setId(userId)
                .setPassword(password)
                .setStatus(NormalOrNoEnum.NORMAL.getStatus())
                .setSalt(userSalt.getSalt());
        // 2、添加用户盐操作
        if (userSaltMapper.insert(userSalt) > 0) {
            redisUtil.set(UserConstant.userSaltDTOKey(userId), userCheckDTO);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建用户盐值记录（用于无密码注册等场景）
     *
     * @param userId   用户ID
     * @param password 密码（可为null）
     * @return 是否成功
     */
    @Override
    public Boolean createUserSalt(String userId, String password) {
        // 生成随机盐值
        String salt = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        
        UserSalt userSalt = new UserSalt(userId, salt);
        UserCheckDTO userCheckDTO = new UserCheckDTO()
                .setId(userId)
                .setPassword(password)
                .setStatus(NormalOrNoEnum.NORMAL.getStatus())
                .setSalt(salt);
        
        if (userSaltMapper.insert(userSalt) > 0) {
            redisUtil.set(UserConstant.userSaltDTOKey(userId), userCheckDTO);
            return true;
        }
        return false;
    }
}
