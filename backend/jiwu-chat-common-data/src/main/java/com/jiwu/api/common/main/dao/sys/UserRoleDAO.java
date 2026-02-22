package com.jiwu.api.common.main.dao.sys;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.jiwu.api.common.main.mapper.sys.UserRoleMapper;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.main.pojo.sys.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户DAO
 *
 * @className: UserDAO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/26 17:40
 */
@Service
public class UserRoleDAO extends MPJBaseServiceImpl<UserRoleMapper, UserRole> {

    public List<Role> selectUserRoleList(String uid) {
        MPJLambdaWrapper<UserRole> qw = new MPJLambdaWrapper<UserRole>()
                .selectAll(Role.class)
                .join("JOIN", Role.class, Role::getId, UserRole::getRoleId);
        qw.eq(UserRole::getUserId, uid);
        return selectJoinList(Role.class, qw);
    }
}
