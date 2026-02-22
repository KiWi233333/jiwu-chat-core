package com.jiwu.api.user;

import com.jiwu.api.JiwuApiApplication;
import com.jiwu.api.common.main.cache.user.*;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.util.common.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * 用户缓存类测试
 * <p>
 * 使用方法：
 * 1. 直接运行测试方法（推荐使用 runAllTests）
 * 2. 或单独运行各个测试方法
 *
 * @author Kiwi23333
 * @date 2025/11/19
 */
@SpringBootTest(classes = JiwuApiApplication.class)
@Slf4j
public class UserCacheTest {

    @Resource
    private UserCache userCache;

    @Resource
    private UserInfoCache userInfoCache;

    @Resource
    private UserSaltCache userSaltCache;

    @Resource
    private UserPhoneCache userPhoneCache;

    @Resource
    private UserEmailCache userEmailCache;

    @Resource
    private UserNameCache userNameCache;

    private static final String TEST_USER_ID = "test_user_123";
    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_CODE = "123456";

    /**
     * 运行所有测试
     */
    @Test
    public void runAllTests() {
        log.info("========== 开始运行所有缓存测试 ==========");
        try {
            testUserCache();
            testUserSaltCache();
            testUserPhoneCache();
            testUserEmailCache();
            testUserNameCache();
            testIntegratedScenario();
            testUserUpdateScenario();
            testUserLoginScenario();
            log.info("========== 所有测试完成 ==========");
        } catch (Exception e) {
            log.error("测试过程中出现异常", e);
        }
    }

    /**
     * 测试 UserCache
     */
    @Test
    public void testUserCache() {
        System.out.println("=== 测试 UserCache ===");

        // 测试获取用户信息
        User user = userCache.getUserInfo(TEST_USER_ID);
        System.out.println("获取用户信息: " + (user != null ? user.getUsername() : "null"));

        // 测试删除用户信息
        userCache.delUserInfo(TEST_USER_ID);
        System.out.println("删除用户信息缓存");

        // 测试刷新用户修改时间
        userCache.refreshUserModifyTime(TEST_USER_ID);
        System.out.println("刷新用户修改时间");

        // 测试在线状态
        boolean isOnline = userCache.isOnline(TEST_USER_ID);
        System.out.println("用户是否在线: " + isOnline);

        System.out.println("✓ UserCache 测试完成\n");
    }

    /**
     * 测试 UserSaltCache
     */
    @Test
    public void testUserSaltCache() {
        System.out.println("=== 测试 UserSaltCache ===");

        // 测试删除单个用户盐值缓存
        userSaltCache.delUserSalt(TEST_USER_ID);
        System.out.println("删除用户盐值缓存: " + TEST_USER_ID);

        // 测试批量删除用户盐值缓存
        userSaltCache.batchDelUserSalt(TEST_USER_ID, "user_456", "user_789");
        System.out.println("批量删除用户盐值缓存");

        System.out.println("✓ UserSaltCache 测试完成\n");
    }

    /**
     * 测试 UserPhoneCache
     */
    @Test
    public void testUserPhoneCache() {
        System.out.println("=== 测试 UserPhoneCache ===");

        // 测试设置手机号映射
        userPhoneCache.setPhoneMapping(TEST_PHONE, TEST_USER_ID);
        System.out.println("设置手机号映射: " + TEST_PHONE + " -> " + TEST_USER_ID);

        // 测试获取手机号映射
        String userId = userPhoneCache.getPhoneMapping(TEST_PHONE);
        System.out.println("获取手机号映射: " + userId);
        AssertUtil.equal(TEST_USER_ID, userId, "手机号映射不正确");

        // 测试设置登录验证码
        userPhoneCache.setLoginCode(TEST_PHONE, TEST_CODE, 5, TimeUnit.MINUTES);
        System.out.println("设置登录验证码: " + TEST_CODE);

        // 测试获取登录验证码
        String loginCode = userPhoneCache.getLoginCode(TEST_PHONE);
        System.out.println("获取登录验证码: " + loginCode);
        AssertUtil.equal(TEST_CODE, loginCode, "登录验证码不正确");

        // 测试删除登录验证码
        userPhoneCache.delLoginCode(TEST_PHONE);
        System.out.println("删除登录验证码");

        String deletedLoginCode = userPhoneCache.getLoginCode(TEST_PHONE);
        AssertUtil.isEmpty(deletedLoginCode, "登录验证码应该已被删除");

        // 测试设置注册验证码
        userPhoneCache.setCheckCode(TEST_PHONE, TEST_CODE, 10, TimeUnit.MINUTES);
        System.out.println("设置注册验证码: " + TEST_CODE);

        // 测试获取注册验证码
        String checkCode = userPhoneCache.getCheckCode(TEST_PHONE);
        System.out.println("获取注册验证码: " + checkCode);
        AssertUtil.equal(TEST_CODE, checkCode, "注册验证码不正确");

        // 测试删除注册验证码
        userPhoneCache.delCheckCode(TEST_PHONE);
        System.out.println("删除注册验证码");

        // 测试删除手机号映射
        userPhoneCache.delPhoneMapping(TEST_PHONE);
        System.out.println("删除手机号映射");

        String deletedMapping = userPhoneCache.getPhoneMapping(TEST_PHONE);
        AssertUtil.isEmpty(deletedMapping, "手机号映射应该已被删除");

        System.out.println("✓ UserPhoneCache 测试完成\n");
    }

    @Test
    public void testUserEmailCache() {
        System.out.println("=== 测试 UserEmailCache ===");

        // 测试设置邮箱映射
        userEmailCache.setEmailMapping(TEST_EMAIL, TEST_USER_ID);
        System.out.println("设置邮箱映射: " + TEST_EMAIL + " -> " + TEST_USER_ID);

        // 测试获取邮箱映射
        String userId = userEmailCache.getEmailMapping(TEST_EMAIL);
        System.out.println("获取邮箱映射: " + userId);
        AssertUtil.equal(TEST_USER_ID, userId, "邮箱映射不正确");

        // 测试设置登录验证码
        userEmailCache.setLoginCode(TEST_EMAIL, TEST_CODE, 5, TimeUnit.MINUTES);
        System.out.println("设置登录验证码: " + TEST_CODE);

        // 测试获取登录验证码
        String loginCode = userEmailCache.getLoginCode(TEST_EMAIL);
        System.out.println("获取登录验证码: " + loginCode);
        AssertUtil.equal(TEST_CODE, loginCode, "登录验证码不正确");

        // 测试删除登录验证码
        userEmailCache.delLoginCode(TEST_EMAIL);
        System.out.println("删除登录验证码");

        String deletedLoginCode = userEmailCache.getLoginCode(TEST_EMAIL);
        AssertUtil.isEmpty(deletedLoginCode, "登录验证码应该已被删除");

        // 测试设置注册验证码
        userEmailCache.setCheckCode(TEST_EMAIL, TEST_CODE, 10, TimeUnit.MINUTES);
        System.out.println("设置注册验证码: " + TEST_CODE);

        // 测试获取注册验证码
        String checkCode = userEmailCache.getCheckCode(TEST_EMAIL);
        System.out.println("获取注册验证码: " + checkCode);
        AssertUtil.equal(TEST_CODE, checkCode, "注册验证码不正确");

        // 测试删除注册验证码
        userEmailCache.delCheckCode(TEST_EMAIL);
        System.out.println("删除注册验证码");

        // 测试删除邮箱映射
        userEmailCache.delEmailMapping(TEST_EMAIL);
        System.out.println("删除邮箱映射");

        String deletedMapping = userEmailCache.getEmailMapping(TEST_EMAIL);
        AssertUtil.isEmpty(deletedMapping, "邮箱映射应该已被删除");

        System.out.println("✓ UserEmailCache 测试完成\n");
    }

    @Test
    public void testUserNameCache() {
        System.out.println("=== 测试 UserNameCache ===");

        // 测试设置用户名映射
        userNameCache.setUsernameMapping(TEST_USERNAME, TEST_USER_ID);
        System.out.println("设置用户名映射: " + TEST_USERNAME + " -> " + TEST_USER_ID);

        // 测试获取用户名映射
        String userId = userNameCache.getUsernameMapping(TEST_USERNAME);
        System.out.println("获取用户名映射: " + userId);
        AssertUtil.equal(TEST_USER_ID, userId, "用户名映射不正确");

        // 测试删除用户名映射
        userNameCache.delUsernameMapping(TEST_USERNAME);
        System.out.println("删除用户名映射");

        String deletedMapping = userNameCache.getUsernameMapping(TEST_USERNAME);
        AssertUtil.isEmpty(deletedMapping, "用户名映射应该已被删除");

        System.out.println("✓ UserNameCache 测试完成\n");
    }

    @Test
    public void testIntegratedScenario() {
        System.out.println("=== 测试集成场景：用户注册流程 ===");

        try {
            // 1. 发送手机验证码
            userPhoneCache.setCheckCode(TEST_PHONE, TEST_CODE, 5, TimeUnit.MINUTES);
            System.out.println("1. 发送手机验证码: " + TEST_CODE);

            // 2. 验证验证码
            String code = userPhoneCache.getCheckCode(TEST_PHONE);
            AssertUtil.equal(TEST_CODE, code, "验证码验证失败");
            System.out.println("2. 验证码验证成功");

            // 3. 注册成功，设置各种映射
            userPhoneCache.setPhoneMapping(TEST_PHONE, TEST_USER_ID);
            userEmailCache.setEmailMapping(TEST_EMAIL, TEST_USER_ID);
            userNameCache.setUsernameMapping(TEST_USERNAME, TEST_USER_ID);
            System.out.println("3. 设置用户映射关系");

            // 4. 删除验证码
            userPhoneCache.delCheckCode(TEST_PHONE);
            System.out.println("4. 删除已使用的验证码");

            // 5. 刷新用户信息
            userCache.refreshUserModifyTime(TEST_USER_ID);
            System.out.println("5. 刷新用户修改时间");

            System.out.println("✓ 集成场景测试完成\n");

        } finally {
            // 清理测试数据
            cleanupTestData();
        }
    }

    @Test
    public void testUserUpdateScenario() {
        System.out.println("=== 测试集成场景：用户修改手机号 ===");

        String oldPhone = TEST_PHONE;
        String newPhone = "13900139000";

        try {
            // 1. 设置旧手机号映射
            userPhoneCache.setPhoneMapping(oldPhone, TEST_USER_ID);
            System.out.println("1. 用户当前手机号: " + oldPhone);

            // 2. 发送新手机号验证码
            userPhoneCache.setCheckCode(newPhone, TEST_CODE, 10, TimeUnit.MINUTES);
            System.out.println("2. 发送新手机号验证码: " + TEST_CODE);

            // 3. 验证验证码
            String code = userPhoneCache.getCheckCode(newPhone);
            AssertUtil.equal(TEST_CODE, code, "新手机号验证码验证失败");
            System.out.println("3. 新手机号验证码验证成功");

            // 4. 更新手机号映射
            userPhoneCache.delPhoneMapping(oldPhone);
            userPhoneCache.setPhoneMapping(newPhone, TEST_USER_ID);
            System.out.println("4. 更新手机号映射");

            // 5. 删除验证码
            userPhoneCache.delCheckCode(newPhone);
            System.out.println("5. 删除已使用的验证码");

            // 6. 清除用户信息缓存
            userCache.delUserInfo(TEST_USER_ID);
            userCache.refreshUserModifyTime(TEST_USER_ID);
            System.out.println("6. 清除并刷新用户缓存");

            // 验证新映射
            String mappedUserId = userPhoneCache.getPhoneMapping(newPhone);
            AssertUtil.equal(TEST_USER_ID, mappedUserId, "新手机号映射不正确");

            // 验证旧映射已删除
            String oldMappedUserId = userPhoneCache.getPhoneMapping(oldPhone);
            AssertUtil.isEmpty(oldMappedUserId, "旧手机号映射应该已删除");

            System.out.println("✓ 用户修改手机号场景测试完成\n");

        } finally {
            // 清理测试数据
            userPhoneCache.delPhoneMapping(oldPhone);
            userPhoneCache.delPhoneMapping(newPhone);
            userPhoneCache.delCheckCode(newPhone);
        }
    }

    @Test
    public void testUserLoginScenario() {
        System.out.println("=== 测试集成场景：用户登录流程 ===");

        try {
            // 1. 设置用户名映射（假设已注册）
            userNameCache.setUsernameMapping(TEST_USERNAME, TEST_USER_ID);
            System.out.println("1. 用户已注册，用户名: " + TEST_USERNAME);

            // 2. 发送登录验证码
            userPhoneCache.setLoginCode(TEST_PHONE, TEST_CODE, 3, TimeUnit.MINUTES);
            System.out.println("2. 发送登录验证码: " + TEST_CODE);

            // 3. 验证登录验证码
            String code = userPhoneCache.getLoginCode(TEST_PHONE);
            AssertUtil.equal(TEST_CODE, code, "登录验证码验证失败");
            System.out.println("3. 登录验证码验证成功");

            // 4. 登录成功，删除验证码
            userPhoneCache.delLoginCode(TEST_PHONE);
            System.out.println("4. 登录成功，删除验证码");

            // 5. 获取用户信息（会自动加载缓存）
            User user = userCache.getUserInfo(TEST_USER_ID);
            System.out.println("5. 加载用户信息: " + (user != null ? "成功" : "用户不存在"));

            System.out.println("✓ 用户登录场景测试完成\n");

        } finally {
            // 清理测试数据
            cleanupTestData();
        }
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        System.out.println("清理测试数据...");

        // 清理手机号相关
        userPhoneCache.delPhoneMapping(TEST_PHONE);
        userPhoneCache.delLoginCode(TEST_PHONE);
        userPhoneCache.delCheckCode(TEST_PHONE);

        // 清理邮箱相关
        userEmailCache.delEmailMapping(TEST_EMAIL);
        userEmailCache.delLoginCode(TEST_EMAIL);
        userEmailCache.delCheckCode(TEST_EMAIL);

        // 清理用户名相关
        userNameCache.delUsernameMapping(TEST_USERNAME);

        // 清理用户信息
        userCache.delUserInfo(TEST_USER_ID);
        userSaltCache.delUserSalt(TEST_USER_ID);

        System.out.println("测试数据清理完成");
    }
}
