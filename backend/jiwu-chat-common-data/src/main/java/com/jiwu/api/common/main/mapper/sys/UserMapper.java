package com.jiwu.api.common.main.mapper.sys;

import com.jiwu.api.common.main.dto.user.UserCheckDTO;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.pojo.sys.UserSalt;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.NonNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends MPJBaseMapper<User> {

    /**
     * 查询链表查询用户的 id、盐、密码
     *
     * @param username 用户名
     * @param userType 用户类型
     * @return UserCheckDTO
     */
    default UserCheckDTO selectUserCheckByUname(@NonNull String username, Integer userType) {
        MPJLambdaWrapper<User> qw = new MPJLambdaWrapper<>();
        qw.select(
                        User::getId,
                        User::getPassword,
                        User::getStatus,
                        User::getUserType
                ) // 用户表
                .select(UserSalt::getSalt)// 盐表
                .eq(userType != null, User::getUserType, userType)
                .and(q -> q
                        .eq(User::getUsername, username)
                        .or()
                        .eq(User::getEmail, username)
                        .or()
                        .eq(User::getPhone, username))
                .rightJoin(UserSalt.class, UserSalt::getUserId, User::getId); // 右表
        // 返回该用户对应的盐值
        return this.selectJoinOne(UserCheckDTO.class, qw);
    }

    /**
     * 查询链表查询用户的 id、盐、密码
     *
     * @param username        用户名
     * @param adminLoginTypes 用户类型
     * @return UserCheckDTO
     */
    default UserCheckDTO selectUserCheckByUnameTypes(String username, List<Integer> adminLoginTypes) {
        MPJLambdaWrapper<User> qw = new MPJLambdaWrapper<>();
        qw.select(User::getId, User::getPassword, User::getStatus) // 用户表
                .select(UserSalt::getSalt)// 盐表
                .in(User::getUserType, adminLoginTypes)
                .and(q -> q
                        .eq(User::getUsername, username)
                        .or()
                        .eq(User::getEmail, username)
                        .or()
                        .eq(User::getPhone, username))
                .rightJoin(UserSalt.class, UserSalt::getUserId, User::getId);
        return this.selectJoinOne(UserCheckDTO.class, qw);
    }


    default UserCheckDTO selectUserCheckByUname(String username) {
        MPJLambdaWrapper<User> qw = new MPJLambdaWrapper<>();
        qw.select(User::getId, User::getPassword, User::getStatus) // 用户表
                .select(UserSalt::getSalt)// 盐表
                .and(q -> q
                        .eq(User::getUsername, username)
                        .or()
                        .eq(User::getEmail, username)
                        .or()
                        .eq(User::getPhone, username))
                .rightJoin(UserSalt.class, UserSalt::getUserId, User::getId); // 右表
        // 返回该用户对应的盐值
        return this.selectJoinOne(UserCheckDTO.class, qw);
    }


    /**
     * 查询链表查询用户的 id、盐、密码
     *
     * @param userId 用户id
     * @return UserCheckDTO
     */
    default UserCheckDTO selectUserCheckById(String userId) {
        MPJLambdaWrapper<User> qw = new MPJLambdaWrapper<>();
        qw.select(User::getId, User::getPassword) // 用户表
                .select(UserSalt::getSalt)// 盐表
                .eq(User::getId, userId)
                .rightJoin(UserSalt.class, UserSalt::getUserId, User::getId); // 右表
        // 返回该用户对应的盐值
        return this.selectJoinOne(UserCheckDTO.class, qw);
    }

}
