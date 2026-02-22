package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.cache.user.UserInfoCache;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.OSS.ResConstant;
import com.jiwu.api.user.common.dto.UpdatePwdDTO;
import com.jiwu.api.user.service.AdminService;
import com.jiwu.api.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 管理员系统服务
 *
 * @className: AdminServiceImpl
 * @author: Kiwi23333
 * @description: 管理员系统服务
 * @date: 2023/8/25 14:44
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Resource
    private RedisUtil<String, Object> redisUtil;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private UserService userService;
    @Resource
    private UserInfoCache userInfoCache;
    @Resource
    private UserCache userCache;

    @Override
    public String updateAvatar(String userId, String key) {
        // 1、查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getAvatar)
                .eq(User::getId, userId)
                .eq(User::getUserType, UserType.ADMIN.getCode())
                .last("LIMIT 1")
        );
        AssertUtil.isNotEmpty(user, "修改头像失败，目标不存在！");
        // 2、获取redis
        Object filePath = redisUtil.get(ResConstant.UPLOAD_NAME + userId + key);
        AssertUtil.isNotEmpty(filePath, "修改头像失败，redis中文件不存在！");
        // 3、更新头像
        AssertUtil.isTrue(userMapper.updateById(
                new User()
                        .setAvatar(String.valueOf(key))
                        .setId(userId)) == 1, "修改头像失败，请稍后再试！");
        // 4、删除旧文件
        if (StringUtils.isNotBlank(user.getAvatar())) {
            ossFileUtil.deleteFile(user.getAvatar());
        }
        // 5、消费文件
        redisUtil.delete(ResConstant.UPLOAD_NAME + userId + key);
        // 6、删除缓存
        userCache.delUserInfo(userId);
        userCache.refreshUserModifyTime(userId);
        return key;
    }


    /**
     * 修改密码（新旧密码）
     *
     * @param userId 用户id
     * @param dto    参数
     * @return 结果
     */
    @Override
    public Boolean updateUserPwd(String userId, UpdatePwdDTO dto) {
        // 1、校验参数
        AssertUtil.isFalse(dto.getNewPassword().equals(dto.getOldPassword()), "修改失败，新密码不能与旧密码相同！");
        return userService.updatePwdByOldNewPwd(dto, userId);
    }
}
