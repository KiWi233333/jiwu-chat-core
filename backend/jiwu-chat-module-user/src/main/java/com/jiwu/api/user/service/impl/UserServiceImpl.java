package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.config.thread.ThreadPoolConfig;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.main.cache.user.*;
import com.jiwu.api.common.main.dto.user.UserCheckDTO;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.enums.user.EmailType;
import com.jiwu.api.common.main.event.user.UserRegisterEvent;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.common.BcryptPwdUtil;
import com.jiwu.api.common.util.common.CheckValidUtil;
import com.jiwu.api.common.util.service.*;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.auth.JWTUtil;
import com.jiwu.api.common.util.service.auth.UserTokenDTO;
import com.jiwu.api.sys.service.MailService;
import com.jiwu.api.user.common.dto.*;
import com.jiwu.api.user.common.enums.DeviceType;
import com.jiwu.api.user.common.vo.UserVO;
import com.jiwu.api.user.service.AdminUserRoleService;
import com.jiwu.api.user.service.UserSaltService;
import com.jiwu.api.user.service.UserService;
import com.jiwu.api.user.service.UserWalletService;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户业务
 *
 * @className: UserService
 * @author: Kiwi23333
 * @description: 用户业务
 * @date: 2023/4/13 14:54
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserSaltService userSaltService;
    @Resource
    private MailService mailService;
    @Resource
    private UserWalletService userWalletService;
    @Resource
    private AdminUserRoleService adminUserRoleService;
    @Resource
    private RedisUtil<String, Object> redisUtil;
    @Resource
    private UserInfoCache userInfoCache;
    @Resource
    private UserCache userCache;
    @Resource
    private UserSaltCache userSaltCache;
    @Resource
    private UserPhoneCache userPhoneCache;
    @Resource
    private UserEmailCache userEmailCache;
    @Resource
    private UserNameCache userNameCache;

    @Resource
    private SmsUtil smsUtil;
    @Resource
    private OssFileUtil ossFileUtil;

    // 线程池
    @Resource
    @Qualifier(value = ThreadPoolConfig.JIWU_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;


    /* -------------------User 登录相关操作--------------------- **/

    /**
     * 密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result
     */
    @Override
    public String toUserLoginByPwd(@NonNull String username, @NonNull String password, Integer userType, HttpServletRequest request) {
        // 1、获取用户的盐值
        UserCheckDTO userCheckDTO = userSaltService.getUserSalt(username, userType);
        AssertUtil.isNotEmpty(userCheckDTO, "登录失败，用户还未注册!");
        // 2、用户验证密码(密码验证)
        boolean flag = BcryptPwdUtil.matches(password, // 密码和数据库密码比对校验
                userCheckDTO.getPassword(),// 数据库
                userCheckDTO.getSalt());
        AssertUtil.isTrue(flag, "登录失败，用户名或密码错误！");
        // 4、获取角色权限信息
        UserTokenDTO userTokenDTO = new UserTokenDTO()
                .setId(userCheckDTO.getId());
        // 5、获取用户token
        String token = saveUserToken(userTokenDTO, request);
        saveLoginTime(userTokenDTO.getId(), IPUtil.getIpAddress(request), ChatActiveStatusEnum.ONLINE);
        return token;
    }

    /**
     * 管理员密码登录
     *
     * @param username        用户名
     * @param password        密码
     * @param adminLoginTypes 用户类型
     * @param request         请求
     * @return Result
     */
    @Override
    public String toUserLoginByPwdAndTypes(String username, String password, List<Integer> adminLoginTypes, HttpServletRequest request) {
        // 1、获取用户的盐值
        UserCheckDTO userCheckDTO = userSaltService.getUserSaltByTypes(username, adminLoginTypes);
        AssertUtil.isNotEmpty(userCheckDTO, "用户还未注册，或不支持该类用户!");
        // 2、用户验证密码(密码验证)
        boolean flag = BcryptPwdUtil.matches(password, // 密码和数据库密码比对校验
                userCheckDTO.getPassword(),// 数据库
                userCheckDTO.getSalt());
        AssertUtil.isTrue(flag, "登录失败，用户名或密码错误！");
        // 4、获取角色权限信息
        UserTokenDTO userTokenDTO = new UserTokenDTO()
                .setId(userCheckDTO.getId());
        // 5、获取用户token
        String token = saveUserToken(userTokenDTO, request);
        saveLoginTime(userTokenDTO.getId(), IPUtil.getIpAddress(request), ChatActiveStatusEnum.ONLINE);
        return token;
    }

    // 获取和缓存用户token 缓存
    private String saveUserToken(UserTokenDTO userTokenDTO, HttpServletRequest request) {
        // 添加ua信息 TODO 优化压缩体积
        String ua = String.valueOf(request.getHeader(UserConstant.USER_AGENT_KEY));
        userTokenDTO.setUa(ua);
//        String uaEq = MD5Encoder.encode(ua.getBytes());
        String token = JWTUtil.createToken(userTokenDTO);
        redisUtil.hPut(UserConstant.USER_REFRESH_TOKEN_KEY + userTokenDTO.getId(),
                ua,
                IPUtil.getIpAddress(request),
                JwtConstant.REDIS_TOKEN_TIME,
                TimeUnit.MINUTES);
        return token;
    }

    /**
     * 手机验证码登录 code
     *
     * @param phone 手机号
     * @param code  验证码
     * @return Result
     */
    @Override
    public String toUserLoginByPhoneCode(String phone, String code, HttpServletRequest request) {
        // 获取缓存验证码
        String resCode = userPhoneCache.getLoginCode(phone);
        AssertUtil.isNotEmpty(resCode, "验证码错误！");
        AssertUtil.isTrue(resCode.equals(code), "验证码错误！");
        // 3、验证
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getId) // 只查询id
                .eq(User::getPhone, phone));
        AssertUtil.isNotEmpty(user, "登录失败，用户不存在！");
        // 4、获取角色权限信息
        UserTokenDTO dto = new UserTokenDTO()
                .setId(user.getId());
        AssertUtil.isNotEmpty(dto, "登录失败，权限错误！");
        // 5、获取用户token
        String token = saveUserToken(dto, request);
        // 6、更新最后登录时间
        saveLoginTime(dto.getId(), IPUtil.getIpAddress(request), ChatActiveStatusEnum.ONLINE);
        // 7、删除验证码
        userPhoneCache.delLoginCode(phone);
        return token;
    }


    /**
     * 邮箱验证码登录 code
     *
     * @param email 邮箱
     * @param code  验证码
     * @return Result
     */
    @Override
    public String toUserLoginByEmailCode(String email, String code, HttpServletRequest request) {
        // 获取缓存验证码
        String emailCode = userEmailCache.getLoginCode(email);
        AssertUtil.isFalse(StringUtil.isNullOrEmpty(emailCode) || !emailCode.equals(code), "验证码错误！");
        // 3、验证
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getEmail, email));
        // 4、生成 Token
        AssertUtil.isNotEmpty(user, "登录失败，用户未注册！");
        // 5、获取角色权限信息
        UserTokenDTO userTokenDTO = new UserTokenDTO().setId(user.getId());
        AssertUtil.isNotEmpty(userTokenDTO, "登录失败，权限错误！");
        // 6、获取用户token
        String token = saveUserToken(userTokenDTO, request);
        // 7、更新最后登录时间
        saveLoginTime(userTokenDTO.getId(), IPUtil.getIpAddress(request), ChatActiveStatusEnum.ONLINE);
        // 8、删除验证码
        userEmailCache.delLoginCode(email);
        return token;
    }

    // 1) 获取登录手机验证码
    @Override
    public Boolean getLoginCodeByPhone(String phone) {
        return getCodeByPhone(phone, UserConstant.PHONE_CODE_KEY, SmsUtil.MsgType.LOGIN);
    }

    // 2) 获取登录邮箱验证码
    @Override
    public Boolean getLoginCodeByEmail(String email) {
        return getCodeByEmail(email, UserConstant.EMAIL_CODE_KEY, EmailType.LOGIN);
    }

    // 3) 记录登录时间
    private boolean saveLoginTime(String id, String ip, ChatActiveStatusEnum activeStatusEnum) {
        // 更新最后登录时间
        User user = new User().setId(id);
        if (ip != null) {
            user.setLastLoginIp(ip);
        }
        // 离线
        if (activeStatusEnum != null) {
            user.setActiveStatus(activeStatusEnum.getStatus());
        } else {
            user.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        }
        return userMapper.updateById(user) == 1;
    }

    /* -------------------注册相关操作--------------------- */

    /**
     * 创建用户（核心注册方法）
     * <p>供内部注册等场景统一调用</p>
     *
     * @param user 用户对象（需设置 username, nickname, avatar 等基本信息，可选 phone, email, password）
     * @return 创建成功的用户ID
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String createUser(User user) {
        // 1. 生成盐值
        String randSalt = BcryptPwdUtil.getRandomSalt();
        // 2. 如果有密码则加密
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(BcryptPwdUtil.encodeBySalt(user.getPassword(), randSalt));
        }
        // 3. 插入用户、盐值、角色、钱包信息
        final boolean isError = userMapper.insert(user) <= 0
                || !userSaltService.addUserSalt(user.getId(), user.getPassword(), randSalt)
                || adminUserRoleService.addUserRoleCustomer(user.getId()) <= 0
                || userWalletService.initUserWallet(user.getId()) <= 0;
        AssertUtil.isFalse(isError, "注册失败，请稍后重试！");

        // 4. 发布用户注册事件，由chat模块监听并初始化聊天室
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this, user));
        log.info("注册成功，已发布用户注册事件，userId={}", user.getId());

        // 5. 缓存用户信息映射
        if (StringUtils.isNotBlank(user.getPhone())) {
            userPhoneCache.setPhoneMapping(user.getPhone(), user.getId());
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            userEmailCache.setEmailMapping(user.getEmail(), user.getId());
        }
        userNameCache.setUsernameMapping(user.getUsername(), user.getId());

        return user.getId();
    }

    /**
     * 用户注册
     *
     * @param u       UserRegisterDTO对象
     * @param request 请求对象 (可选影响 token返回)
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String toRegister(UserRegisterDTO u, HttpServletRequest request) {
        User user = new User();
        // 1、判断注册类型（手机|邮箱） 和 验证 验证码真伪
        if (u.isPhone()) {
            AssertUtil.isTrue(checkCodeEffective(u.getCode(), UserConstant.PHONE_CHECK_CODE_KEY + u.getPhone()), "手机验证码错误!");
            user.setPhone(u.getPhone()).setIsPhoneVerified(1);
        } else if (u.isEmail()) {
            AssertUtil.isTrue(checkCodeEffective(u.getCode(), UserConstant.EMAIL_CHECK_CODE_KEY + u.getEmail()), "邮箱验证码错误!");
            user.setEmail(u.getEmail()).setIsEmailVerified(1);
        }
        // 2、用户基本信息准备
        user.setUsername(u.getUsername())
                .setPassword(u.getPassword())
                .setNickname(StringUtils.isNotBlank(u.getUsername()) ? u.getUsername() : "新用户")
                .setAvatar("");
        // 3、调用核心注册方法
        String userId = createUser(user);
        // 4、返回token
        String token = "";
        if (userId != null && request != null) {
            UserTokenDTO userTokenDTO = new UserTokenDTO().setId(userId);
            token = saveUserToken(userTokenDTO, request);
        }
        return token;
    }

    /**
     * 用户快速注册（手机号、邮箱免密码）
     *
     * @param u UserRegisterDTO对象
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String toRegisterV2(@NotNull UserRegisterV2DTO u, HttpServletRequest request) {
        User user = new User();
        // 1、判断注册类型（手机|邮箱|密码） 和 验证
        if (u.isPhone()) {
            AssertUtil.isTrue(checkCodeEffective(u.getCode(), UserConstant.PHONE_CHECK_CODE_KEY + u.getPhone()), "手机验证码错误!");
            user.setPhone(u.getPhone()).setIsPhoneVerified(1);
        } else if (u.isEmail()) {
            AssertUtil.isTrue(checkCodeEffective(u.getCode(), UserConstant.EMAIL_CHECK_CODE_KEY + u.getEmail()), "邮箱验证码错误!");
            user.setEmail(u.getEmail()).setIsEmailVerified(1);
        } else if (u.isPwd()) {
            AssertUtil.isFalse(StringUtils.isBlank(u.getSecondPassword()), "请输入二次密码！");
            AssertUtil.isTrue(u.getSecondPassword().equals(u.getPassword()), "两次密码不一致！");
        }
        // 2、用户基本信息准备
        user.setUsername(u.getUsername())
                .setPassword(u.getPassword())
                .setNickname(StringUtils.isNotBlank(u.getUsername()) ? u.getUsername() : "新用户")
                .setAvatar("");
        // 3、调用核心注册方法
        String userId = createUser(user);
        // 4、返回token
        String token = "";
        if (StringUtils.isNotBlank(userId) && request != null) {
            UserTokenDTO userTokenDTO = new UserTokenDTO().setId(userId);
            token = saveUserToken(userTokenDTO, request);
        }
        return token;
    }

    // 1) 手机号注册-获取验证码
    @Override
    public Boolean getRegisterCodeByPhone(String phone) {
        return getCodeByPhone(phone, UserConstant.PHONE_CHECK_CODE_KEY, SmsUtil.MsgType.REGISTER);// 1注册验证码
    }

    // 2) 邮箱注册-获取验证码
    @Override
    public Boolean getRegisterCodeByEmail(String email) {
        return getCodeByEmail(email, UserConstant.EMAIL_CHECK_CODE_KEY, EmailType.REGISTER);// 1注册验证码
    }

    /**
     * 验证-用户是否存在
     *
     * @param username 用户名
     * @return Result
     */
    @Override
    public Result<Object> checkUserIsExist(String username) {
        if (StringUtil.isNullOrEmpty(username)) {
            return Result.fail("用户名不能为空！");// 判空
        }
        if (!CheckValidUtil.checkUsername(username)) {
            return Result.fail("用户名不合法！");// 用户名不合法
        }
        User user;
        // 缓存是否存在
        if (userNameCache.getUsernameMapping(username) != null) {
            log.info("该用户已存在，用户名Redis");
            return Result.fail("该用户已存在");
        } else {
            user = userMapper.selectOne(new QueryWrapper<User>().lambda().select(User::getUsername).eq(User::getUsername, username));
        }
        // 数据库查询为不为空并保存redis
        if (user != null) {
            userNameCache.setUsernameMapping(user.getUsername(), user.getUsername());
            return Result.fail("该用户已存在！");
        }

        return Result.ok("用户名可用！", null);
    }

    /**
     * 手机号验证码有效期
     */
    private final long PHONE_CODE_TTL = 30;

    /**
     * 1) 获取手机短信验证码
     *
     * @param phone 手机号
     * @param key   缓存值
     * @param type  类型
     * @return Result
     */
    private Boolean getCodeByPhone(String phone, String key, SmsUtil.MsgType type) {
        AssertUtil.isTrue(CheckValidUtil.checkPhone(phone), "手机号不合法！");
        // 判断是登录验证码还是其他验证码
        boolean isLoginCode = UserConstant.PHONE_CODE_KEY.equals(key);
        // 1、获取缓存
        String existingCode = isLoginCode ? userPhoneCache.getLoginCode(phone) : userPhoneCache.getCheckCode(phone);
        AssertUtil.isFalse(existingCode != null, "验证码已发送，请30分钟后再重试！");
        // 2、验证手机号
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().select(User::getPhone).eq(User::getPhone, phone).last("LIMIT 1"));

        switch (type) {
            case LOGIN:// 1）登录
            case RESET_BIND_PHONE:// 2）换绑
                if (user == null) {
                    throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "手机号未注册！");
                } else {
                    userPhoneCache.setPhoneMapping(phone, phone);// 对数据库缓存
                }
                break;
            case REGISTER:// 3）注册
            case RESET_PWD:// 4）重置密码
            case UPDATE_PWD:// 5）修改密码
            case BIND_PHONE:// 6）绑定手机号
                if (user != null) {
                    throw new BusinessException(ResultStatus.LINK_NULL_ERR.getCode(), "该手机号已经被使用！");
                } else {
                    userPhoneCache.setPhoneMapping(phone, phone);// 对数据库缓存
                }
                break;
        }
        // 3、生成随机数
        SecureRandom secureRandom = new SecureRandom();
        // 生成一个介于100000和999999之间（含）的随机数字
        String code = String.valueOf(secureRandom.nextInt(899999) + 100000);
        threadPoolTaskExecutor.execute(() -> {
            if (smsUtil.autoSendByType(phone, code, PHONE_CODE_TTL, type)) {
                if (isLoginCode) {
                    userPhoneCache.setLoginCode(phone, code, 60 * PHONE_CODE_TTL, TimeUnit.SECONDS);
                } else {
                    userPhoneCache.setCheckCode(phone, code, 60 * PHONE_CODE_TTL, TimeUnit.SECONDS);
                }
            } else {
                throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "发送消息失败，请稍后重试！");
            }
        });
        return true;
    }

    // 忽略常见验证码 set
    private static final Set<String> IGNORE_CODE_SET = new HashSet<>();

    static {
        String[] IGNORE_CODE = {"123456", "111111", "000000", "666666", "888888", "999999", "123123", "321321", "654321"};
        IGNORE_CODE_SET.addAll(Arrays.asList(IGNORE_CODE));
    }

    /**
     * 2) 获取邮箱验证码，存储至不同的key
     *
     * @param email 邮箱
     * @param key   缓存值
     * @param type  类型 0查询，1添加
     * @return Result
     */
    private Boolean getCodeByEmail(String email, String key, EmailType type) {// type 登录0 注册1 校验2
        AssertUtil.isTrue(CheckValidUtil.checkEmail(email), "邮箱不合法！");
        // 判断是登录验证码还是其他验证码
        boolean isLoginCode = UserConstant.EMAIL_CODE_KEY.equals(key);
        // 1、获取缓存 - 注意：这里原来还检查了过期时间，但cache类没有getExpire方法，简化为只检查是否存在
        String existingCode = isLoginCode ? userEmailCache.getLoginCode(email) : userEmailCache.getCheckCode(email);
        AssertUtil.isFalse(existingCode != null, "验证码已发送，请60s后再重试！");
        // 2、验证邮箱是否已经被使用
        final User selectOne = userMapper.selectOne(new QueryWrapper<User>().lambda().select(User::getEmail).eq(User::getEmail, email));
        if (type.equals(EmailType.LOGIN)) {// 登录
            AssertUtil.isTrue(selectOne != null, "该邮箱未注册！");
            userEmailCache.setEmailMapping(email, email);// 对数据库缓存邮箱号
        }
        if (type.equals(EmailType.REGISTER)) {// 注册
            AssertUtil.isTrue(selectOne == null, "该邮箱已被使用！");
            userEmailCache.setEmailMapping(email, email);// 对数据库缓存邮箱号
        }
        // 3、生成随机数 并存储在redis设置有效期61s
        String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        if (IGNORE_CODE_SET.contains(code)) {
            code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        }
        String finalCode = code;
        threadPoolTaskExecutor.execute(() -> {
            try {
                mailService.sendCodeMail(email, "验证码", type.getName(), finalCode);
                if (isLoginCode) {
                    userEmailCache.setLoginCode(email, finalCode, 300, TimeUnit.SECONDS);
                } else {
                    userEmailCache.setCheckCode(email, finalCode, 300, TimeUnit.SECONDS);
                }
                log.info("邮件发送成功，email={}", email);
            } catch (MessagingException | UnsupportedEncodingException | jakarta.mail.MessagingException e) {
                log.error("邮件发送失败， {}", e.getMessage());
                throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "邮件发送失败！");
            }
        });
        return true;// success成功
    }

    // 验证验证码是否有效
    private boolean checkCodeEffective(String code, String redisKey) {
        String data = redisUtil.get(redisKey);
        return StringUtils.isNotBlank(data) && data.equals(code);
    }

    // 验证验证码是否有效
    private boolean checkAndDelPhone(String phone, String code) {
        String data = userPhoneCache.getLoginCode(phone);
        return StringUtils.isNotBlank(data) && data.equals(code);
    }

    private boolean checkAndDelEmail(String email, String code) {
        String data = userEmailCache.getLoginCode(email);
        return StringUtils.isNotBlank(data) && data.equals(code);
    }

    /**
     * 退出登录（单个）
     *
     * @param userId 用户id
     * @return Result
     */
    @Override
    public Boolean logoutOne(String userId, HttpServletRequest request) {
        // 删除缓存
        redisUtil.hDelete(UserConstant.USER_REFRESH_TOKEN_KEY + userId, request.getHeader(UserConstant.USER_AGENT_KEY));
        // 更新最后登录时间
        return saveLoginTime(userId, IPUtil.getIpAddress(request), ChatActiveStatusEnum.OFFLINE);
    }

    /**
     * 退出登录（所有）
     *
     * @param userId 用户id
     * @param ip     ip
     * @return Result
     */
    @Override
    public Result<Object> logoutAll(String userId, String ip) {
        // 删除缓存
        redisUtil.delete(UserConstant.USER_REFRESH_TOKEN_KEY + userId);
        // 更新最后登录时间
        return Result.ok("退出成功！", saveLoginTime(userId, ip, ChatActiveStatusEnum.OFFLINE));
    }

    /**
     * 用户信息相关
     */
    // 1、获取用户所有信息
    @Override
    public UserVO getUserInfoById(String userId) {
        // 使用统一缓存获取用户信息
        User user = userCache.getUserInfo(userId);
        if (user == null) {
            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "获取信息失败，用户不存在！");
        }
        // 转换为 UserVO
        return UserVO.toUserVo(user);
    }

    public boolean checkUserExist(String userId) {
        // 使用统一缓存检查用户是否存在
        User user = userCache.getUserInfo(userId);
        return user != null;
    }


    /**
     * 修改用户头像
     *
     * @param file   头像文件
     * @param userId 用户id
     * @return Result
     */
    @Override
    public String updateUserAvatar(MultipartFile file, String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                        .select(User::getId, User::getAvatar) // select
                        .eq(User::getId, userId)
//                .eq(User::getUserType, UserType.CUSTOMER.getCode()) // 都允许该接口修改
                        .last("LIMIT 1")
        );
        AssertUtil.isNotEmpty(user, "用户不存在！");
        // 文件压缩
        String fileName = file.getOriginalFilename();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.of(file.getInputStream())
                    .scale(0.5f)
                    .outputQuality(0.4)
                    .toOutputStream(outputStream);
            long outputSize = outputStream.toByteArray().length;
            log.info(
                    "【图片压缩】imageId= 图片原大小={}kb | 压缩后大小={}kb",
                    file.getSize() / 1024,
                    outputSize / 1024);
            AssertUtil.isFalse(outputSize > OssFileType.IMAGE.getFileSize(), "图片最大为3mb！");

        } catch (IOException e) {
            throw new BusinessException(ResultStatus.PARAM_ERR.getCode(), "图片处理失败，请稍后再试！");
        }
        // 图片格式
        AssertUtil.isTrue(FileUtil.isImage(file.getOriginalFilename()), "图片格式错误！");
        // 上传
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = OssFileType.IMAGE.getPath() + formattedTime + "/" + UUID.randomUUID();
        if (StringUtil.isNullOrEmpty(user.getAvatar())) {
            key = ossFileUtil.uploadFile(key, fileName, outputStream.toByteArray());
            // 第一次上传
        } else {
            // 更新
            key = ossFileUtil.uploadImage(key, file);
            ossFileUtil.deleteFile(user.getAvatar());
        }
        AssertUtil.isNotEmpty(key, "图片文件上传失败！");
        // 3、更新头像
        user.setAvatar(key).setId(userId);
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "修改头像失败！");
        // 清除缓存
        userCache.delUserInfo(userId);
        userCache.refreshUserModifyTime(userId);
        return key;
    }

    /**
     * 修改密码
     *
     * @param updatePwdDto 参数
     * @param userId       用户id
     * @return 用户密码
     */
    @Override
    public Boolean updatePwdByOldNewPwd(UpdatePwdDTO updatePwdDto, String userId) {
        UserCheckDTO u = userSaltService.getUserSaltById(userId);
        // 1、验证旧密码
        String oldPassword = updatePwdDto.getOldPassword();
        // 1.1 本来就没有旧密码则跳过
        if (StringUtils.isNotBlank(u.getPassword())) {
            AssertUtil.isTrue(BcryptPwdUtil.matches(oldPassword, u.getPassword(), u.getSalt()), "修改密码失败，可能旧密码有误！");
        }
        // 新密码加密
        String enPwd = BcryptPwdUtil.encodeBySalt(updatePwdDto.getNewPassword(), u.getSalt());
        User user = new User().setId(userId).setPassword(enPwd);
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "修改密码失败！");
        // 清除缓存
        userSaltCache.delUserSalt(userId);// 用户盐密码dto信息
        userCache.delUserInfo(userId);// 用户信息
        userCache.refreshUserModifyTime(userId);
        return true;
    }


    /**
     * 修改用户密码
     *
     * @param type 类型
     * @param dto  参数
     * @return Result
     */
    @Override
    public Integer updatePwdByCode(Integer type, UpdateSecondPwdDTO dto) {
        String userId = RequestHolderUtil.get().getId();
        UserCheckDTO u = userSaltService.getUserSaltById(userId);
        // 1、验证
        boolean isValid = false;
        if (Objects.equals(type, DeviceType.EMAIL.getValue())) {
            User user = userMapper.selectById(userId);
            isValid = this.checkAndDelEmail(user.getEmail(), dto.getCode());
        } else if (Objects.equals(type, DeviceType.PHONE.getValue())) {
            User user = userMapper.selectById(userId);
            isValid = this.checkAndDelPhone(user.getPhone(), dto.getCode());
        }
        AssertUtil.isTrue(isValid, "验证码错误！");
        // 新密码加密
        String enPwd = BcryptPwdUtil.encodeBySalt(dto.getNewPassword(), u.getSalt());
        User updateUser = new User().setId(userId).setPassword(enPwd);
        AssertUtil.isTrue(userMapper.updateById(updateUser) == 1, "修改密码失败！");
        // 清除缓存
        userSaltCache.delUserSalt(userId);// 用户盐密码dto信息
        userCache.delUserInfo(userId);// 用户信息
        userCache.refreshUserModifyTime(userId);
        return 1;
    }

    /**
     * 修改用户基本信息
     *
     * @param updateUserInfoDTO updateUserInfoDTO
     * @param userId            用户id
     * @return Result
     */
    @Override
    public Boolean updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO, String userId) {
        User user = UpdateUserInfoDTO.toUser(updateUserInfoDTO);
        user.setId(userId);
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "修改失败，请稍后重试！");
        // 清除缓存
        userCache.delUserInfo(userId);// 用户信息
        userCache.refreshUserModifyTime(userId);
        return true;
    }

    /**
     * 修改用户信息
     *
     * @param updateUserInfoDTO 参数
     * @param userId            用户id
     * @return 是否修改成功
     */
    @Override
    public Integer updateUserAllInfo(UpdateUserAllInfoDTO updateUserInfoDTO, String userId) {
        User user = UpdateUserAllInfoDTO.toUser(updateUserInfoDTO);
        user.setId(userId);
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "修改失败，请稍后重试！");
        // 2、oss头像
        updateAvatar(userId, updateUserInfoDTO.getAvatar());
        // 清除缓存
        userCache.delUserInfo(userId);// 用户信息
        userCache.refreshUserModifyTime(userId);
        return 1;
    }

    /**
     * 管理员修改用户密码
     *
     * @param userId 用户id
     * @param dto    参数
     * @return Result 是否成功
     */
    @Override
    public Integer updateUserPwdByAdmin(String userId, UpdateNewPwdDTO dto) {
        UserCheckDTO user = userSaltService.getUserSaltById(userId);
        AssertUtil.isNotEmpty(user, "用户不存在，请刷新重试！");
        // 新密码加密
        String enPwd = BcryptPwdUtil.encodeBySalt(dto.getNewPassword(), user.getSalt());
        AssertUtil.isTrue(userMapper.updateById(new User()
                .setId(userId)
                .setPassword(enPwd)) == 1, "修改密码失败，请稍后重试！");
        // 清除缓存
        userSaltCache.delUserSalt(userId);// 用户盐密码dto信息
        userCache.delUserInfo(userId);// 用户信息
        userCache.refreshUserModifyTime(userId);
        //  清空用户登录
        logoutAll(userId, null);
        redisUtil.delete(UserConstant.USER_REFRESH_TOKEN_KEY + userId);
        return 1;
    }

    /**
     * 更新头像
     *
     * @param userId 用户id
     * @param avatar 头像地址
     * @return 是否成功
     */
    private boolean updateAvatar(String userId, String avatar) {
        if (StringUtils.isNotBlank(avatar)) {
            try {
                // 消费新头像
                ossFileUtil.deleteRedisKey(userId, avatar);
                // 删除旧头像
                User oldUser = userMapper.selectOne(new LambdaQueryWrapper<User>().select(User::getAvatar).eq(User::getId, userId).last("LIMIT 1"));
                if (StringUtil.isNullOrEmpty(oldUser.getAvatar())) {
                    ossFileUtil.deleteFile(oldUser.getAvatar());
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 修改手机号
     *
     * @param updatePhoneDTO 参数
     * @param userId         用户id
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserPhone(UpdatePhoneDTO updatePhoneDTO, String userId) {
        // 1、获取验证码
        String code = userPhoneCache.getCheckCode(updatePhoneDTO.getNewPhone());
        AssertUtil.isTrue(!StringUtil.isNullOrEmpty(code), "验证码已过期，请重新获取！");
        // 2、验证
        AssertUtil.isTrue(updatePhoneDTO.getCode().equals(code), "验证码错误！");
        // 3、更新数据库-手机号
        User user = new User().setId(userId).setPhone(updatePhoneDTO.getNewPhone());
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "更换手机号失败！");
        // 4、获取用户信息
        User userInfo = userMapper.selectById(userId);
        // 5、删除旧手机、验证码、用户信息缓存
        userPhoneCache.delCheckCode(updatePhoneDTO.getNewPhone());// 验证码
        userPhoneCache.delPhoneMapping(userInfo.getPhone());// 旧手机号
        userPhoneCache.setPhoneMapping(updatePhoneDTO.getNewPhone(), userId);// 新手机号
        userCache.delUserInfo(userInfo.getId());// 用户信息
        userCache.refreshUserModifyTime(userId);
        return true;
    }

    /**
     * 修改邮箱
     *
     * @param updateEmailDTO 参数
     * @param userId         用户id
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserEmail(UpdateEmailDTO updateEmailDTO, String userId) {
        // 1、获取验证码
        String code = userEmailCache.getCheckCode(updateEmailDTO.getNewEmail());
        AssertUtil.isTrue(!StringUtil.isNullOrEmpty(code), "验证码已过期，请重新获取！");
        // 2、验证
        AssertUtil.isTrue(updateEmailDTO.getCode().equals(code), "验证码错误！");
        // 3、更新数据库-邮箱
        User user = new User().setId(userId).setEmail(updateEmailDTO.getNewEmail());
        AssertUtil.isTrue(userMapper.updateById(user) == 1, "更换邮箱失败！");
        // 4、获取用户信息
        User userInfo = userMapper.selectById(userId);
        // 5、删除旧缓存 验证码缓存
        userEmailCache.delCheckCode(updateEmailDTO.getNewEmail());// 删除验证码
        userEmailCache.delEmailMapping(userInfo.getEmail());// 旧邮箱缓存
        userEmailCache.setEmailMapping(updateEmailDTO.getNewEmail(), userId);// 新邮箱缓存
        userCache.delUserInfo(userInfo.getId());// 用户信息
        userCache.refreshUserModifyTime(userId);
        return true;
    }


    /**
     * 获取新手机/邮箱验证码
     *
     * @param key 手机号、邮箱
     * @return 是否成功
     */
    @Override
    public Boolean sendUpdateCode(String key, Integer type) {
        if (type == 0) {// 手机号
            return getCodeByPhone(key, UserConstant.PHONE_CHECK_CODE_KEY, SmsUtil.MsgType.REGISTER);// 1注册验证码
        } else {// 邮箱
            return getCodeByEmail(key, UserConstant.EMAIL_CHECK_CODE_KEY, EmailType.REGISTER);// 1注册验证码
        }
    }
    // --------------------------验证用户本人--------------------------------------------

    /**
     * 发送验证手机号|邮箱验证码
     *
     * @param key  手机号|邮箱
     * @param type 0：手机号，1：邮箱
     * @return Result
     */
    @Override
    public Boolean sendCheckCode(String key, Integer type) {
        if (type == 0) {// 手机号
            return getCodeByPhone(key, UserConstant.PHONE_CODE_KEY, SmsUtil.MsgType.RESET_BIND_PHONE);
        } else if (type == 1) {// 邮箱
            return getCodeByEmail(key, UserConstant.EMAIL_CODE_KEY, EmailType.CHECK);
        } else {
            throw new BusinessException(ResultStatus.PARAM_ERR.getCode(), "参数错误！");
        }
    }

    /**
     * 验证手机号
     *
     * @param userId 用户id
     * @param type   标识 0:手机号，1:邮箱
     * @param key    手机号|邮箱
     * @param code   验证码
     * @return Result
     */
    @Override
    public Boolean onCheckCode(String userId, Integer type, String key, String code) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .select(User::getPhone, User::getEmail)
                .eq(User::getId, userId) // userId
                .eq(type == 0, User::getIsPhoneVerified, 1)
                .eq(type == 1, User::getIsEmailVerified, 1)
                .last("LIMIT 1");
        User user = userMapper.selectOne(qw);
        AssertUtil.isNotEmpty(user, "验证失败，用户不存在！");
        if (type == 0 && checkCodeEffective(code, UserConstant.PHONE_CODE_KEY)) {// 手机号
            return true;
        } else if (type == 1 && checkCodeEffective(code, UserConstant.EMAIL_CODE_KEY)) {// 邮箱
            return true;
        } else {
            throw new BusinessException(ResultStatus.SELECT_ERR.getCode(), "验证失败，验证码不存在！");
        }
    }


    /**
     * 管理员密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Result
     */
    @Override
    public String toAdminLoginByPwd(String username, String password, HttpServletRequest request) {
        // 1、获取用户的盐值
        UserCheckDTO userCheckDTO = userMapper.selectUserCheckByUname(username);
        AssertUtil.isNotEmpty(userCheckDTO, "用户不存在，请确认注册信息！");
        // 2、用户验证密码(密码验证)
        boolean flag = BcryptPwdUtil.matches(password, // 密码和数据库密码比对校验
                userCheckDTO.getPassword(),// 数据库
                userCheckDTO.getSalt());
        AssertUtil.isTrue(flag, "登录失败，用户名或密码错误！");
        // 验证通过
        // 4、获取角色权限信息
        UserTokenDTO userTokenDTO = new UserTokenDTO()
                .setId(userCheckDTO.getId());
        // 5、获取用户token
        String token = saveUserToken(userTokenDTO, request);
        saveLoginTime(userTokenDTO.getId(), IPUtil.getIpAddress(request), ChatActiveStatusEnum.ONLINE);
        return token;
    }
}
