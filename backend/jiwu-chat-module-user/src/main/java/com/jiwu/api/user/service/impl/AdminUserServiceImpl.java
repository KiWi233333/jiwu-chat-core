package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.cache.user.UserCache;
import com.jiwu.api.common.main.cache.user.UserInfoCache;
import com.jiwu.api.common.main.cache.user.UserNameCache;
import com.jiwu.api.common.main.dao.sys.UserDAO;
import com.jiwu.api.common.main.enums.user.UserStatus;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.common.BcryptPwdUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertOrUpdateRobotDTO;
import com.jiwu.api.user.common.dto.InsertAdminUserDTO;
import com.jiwu.api.user.common.dto.UserInfoPageDTO;
import com.jiwu.api.common.main.vo.user.UserWithSaltVO;
import com.jiwu.api.user.service.AdminUserService;
import com.jiwu.api.user.service.UserSaltService;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.common.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private RedisUtil<String, Object> redisUtil;
    @Resource
    private UserCache userCache;
    @Resource
    private UserInfoCache userInfoCache;
    @Resource
    private UserNameCache userNameCache;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserDAO userDAO;
    @Resource
    private UserService userService;
    @Resource
    private UserSaltService userSaltService;
    @Resource
    private OssFileUtil ossFileUtil;

    /**
     * 用户禁用
     *
     * @param userId 用户id
     * @return Result
     */
    @Override
    public Result<Integer> toUserDisableToggle(String userId, Integer status) {
        // 1、是否禁用
        AssertUtil.isTrue(status == 0 || status == 1, ResultStatus.DEFAULT_ERR, "禁用参数错误！");
        // 2、sql
        if (userMapper.updateById(new User().setId(userId).setStatus(status)) <= 0) {
            return Result.fail("操作失败！");
        }
        // 3、清空用户登录
        userService.logoutAll(userId, null);
        // 4、清空用户缓存
        userCache.remove(userId);
        userInfoCache.delete(userId);
        return Result.ok("操作成功！", 1);
    }


    /**
     * 添加后台用户
     *
     * @param dto 参数
     * @return 添加行数
     */
    @Override
    public Result<Integer> addAdminUser(InsertAdminUserDTO dto) {
        User user = InsertAdminUserDTO.toUser(dto);
        // 1、生成盐和密码加密
        String randSalt = BcryptPwdUtil.getRandomSalt();
        user.setPassword(BcryptPwdUtil.encodeBySalt(user.getPassword(), randSalt));
        // 2、插入用户、盐值信息 （3）
        if (userMapper.insert(user) <= 0 || Boolean.TRUE.equals(!userSaltService.addUserSalt(user.getId(), user.getPassword(), randSalt))) {
            log.warn("Error registering 注册失败！");
            throw new BusinessException(ResultStatus.INSERT_ERR.getCode(), "添加失败，请稍后再试！");
        } else {
            // 3、消费头像
            if (StringUtils.isNotBlank(dto.getAvatar())) {
                ossFileUtil.deleteRedisKey(user.getId(), dto.getAvatar());
            }
            // 缓存数据
            userNameCache.setUsernameMapping(user.getUsername(), user.getUsername());
            return Result.ok("添加成功！", 1);
        }
    }

    /**
     * 刷新或保存机器人
     *
     * @param dto 参数
     * @return 添加user
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User refreshOrSaveRobot(InsertOrUpdateRobotDTO dto) {
        // 判断是否存在
        UserWithSaltVO oldUser = userDAO.getAndSaltById(dto.getRobotId());
        final User newUser = InsertOrUpdateRobotDTO.toUser(dto);
        if (oldUser == null) {
            // 1、生成盐和密码加密
            String randSalt = BcryptPwdUtil.getRandomSalt();
            newUser.setPassword(BcryptPwdUtil.encodeBySalt(newUser.getPassword(), randSalt));
            // 2、插入用户、盐值信息 （3）
            if (userMapper.insert(newUser) <= 0 || Boolean.TRUE.equals(!userSaltService.addUserSalt(newUser.getId(), newUser.getPassword(), randSalt))) {
                log.warn("Error registering 注册失败！");
                throw new BusinessException(ResultStatus.INSERT_ERR.getCode(), "添加失败，请稍后再试！");
            } else {
                // 消费图片
                ossFileUtil.deleteRedisKey(RequestHolderUtil.get().getId(), dto.getAvatar());
                return newUser;
            }
        }
        // 刷新机器人
        if (dto.getPassword() != null) { // 修改秘密啊
            newUser.setPassword(BcryptPwdUtil.encodeBySalt(newUser.getPassword(), oldUser.getSalt()));
        }
        Assert.isTrue(userMapper.updateById(newUser) == 1, "更新失败，请重试！");
        // 缓存数据
        userInfoCache.delete(oldUser.getId());
        userCache.delUserInfo(oldUser.getId());
        // 更新头像
        if (StringUtils.isNotBlank(dto.getAvatar())) {
            ossFileUtil.diffDeleteFile(RequestHolderUtil.get().getId(), newUser.getId(), dto.getAvatar());
        }
        return newUser;
    }


    /**
     * 获取用户列表
     *
     * @param page 页码
     * @param size 每页个数
     * @return Result
     */
    @Override
    public Page<User> getUserInfoPage(Integer page, Integer size, UserInfoPageDTO dto) {
        Page<User> pages = new Page<>(page, size); // 创建分页对象，指定当前页码和每页记录数
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>(); // 创建查询条件
        // 只看用户
        if (dto.getIsCustomer() != null) {
            qw.eq(User::getUserType, dto.getIsCustomer() == 0 ? UserType.ADMIN.getCode() : UserType.CUSTOMER.getCode());// 限制查询用户
        }
        // id查询
        if (dto.getUserId() != null) {
            qw.eq(User::getId, dto.getUserId());
        }

        // 是否状态正常
        if (dto.getStatus() != null) {
            qw.eq(User::getStatus, dto.getStatus() == 0 ? UserStatus.OFF.getCode() : UserStatus.ON.getCode());
        }
        // keyWord查询
        if (dto.getKeyWord() != null) {
            String k = dto.getKeyWord();
            qw.and(w -> w.like(User::getUsername, k).or().like(User::getNickname, k).or().like(User::getPhone, k).or().like(User::getEmail, k));
        }
        // 创建时间排序
        if (dto.getCreateTimeSort() != null) {
            qw.orderByDesc(dto.getCreateTimeSort() == 1, User::getCreateTime);
        }

        if (dto.getIsSimpleCustomer() != null && dto.getIsSimpleCustomer() == 1) {
            qw.eq(User::getUserType, UserType.CUSTOMER.getCode());// 限制查询用户
            qw.select(User::getId,
                    User::getUsername,
                    User::getAvatar,
                    User::getNickname,
                    User::getEmail,
                    User::getCreateTime,
                    User::getGender,
                    User::getUserType);
        }
        return userMapper.selectPage(pages, qw); // 调用Mapper接口方法进行分页查询;
    }

    /**
     * 强制用户下辖
     *
     * @param userId id
     * @return Result
     */
    @Override
    public Result<Integer> loginOutById(String userId) {
        //  清空用户登录
        userService.logoutAll(userId, null);
        redisUtil.delete(UserConstant.USER_REFRESH_TOKEN_KEY + userId);
        return Result.ok("下线成功！", 1);
    }

}
