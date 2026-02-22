package com.jiwu.api.common.main.dao.sys;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jiwu.api.common.main.enums.chat.ChatActiveStatusEnum;
import com.jiwu.api.common.main.enums.common.NormalOrNoEnum;
import com.jiwu.api.common.main.mapper.sys.UserMapper;
import com.jiwu.api.common.main.pojo.sys.User;
import com.jiwu.api.common.main.pojo.sys.UserSalt;
import com.jiwu.api.common.main.vo.user.UserWithSaltVO;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Date;
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
public class UserDAO extends MPJBaseServiceImpl<UserMapper, User> {
    // 获取在线数
    public Long getOnlineCount(List<String> memberUidList) {
        return lambdaQuery()
                .eq(User::getActiveStatus, ChatActiveStatusEnum.ONLINE.getStatus())
                .in(CollUtil.isNotEmpty(memberUidList), User::getId, memberUidList)
                .count();
    }

    // 游标获取用户列表
    public CursorPageBaseVO<User> getCursorPage(List<String> memberUidList, CursorPageBaseDTO request, ChatActiveStatusEnum online) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getStatus());//筛选上线或者离线的
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
        }, User::getLastLoginTime, Date.class);
    }

    /**
     * 获取用户信息
     * getId
     * getActiveStatus
     * getUsername
     * @param ids 用户ids
     * @return 部分信息
     */
    public List<User> getFriendList(List<String> ids) {
        return lambdaQuery()
                .in(User::getId, ids)
                .select(User::getId,
                        User::getActiveStatus,
                        User::getUsername,
                        User::getNickname,
                        User::getUserType,
                        User::getAvatar)// 上线状态、头像和昵称
                .list();
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLastLoginTime)
                .last("limit 1000")
                .select(User::getId,
                        User::getNickname,
                        User::getUserType,
                        User::getUsername,
                        User::getAvatar)
                .list();
    }


    public UserWithSaltVO getAndSaltByUname(String username) {
        return selectJoinOne(UserWithSaltVO.class, new MPJLambdaWrapper<User>().eq(User::getUsername, username)
                .leftJoin(UserSalt.class,
                        UserSalt::getUserId, User::getId)
        );
    }

    public UserWithSaltVO getAndSaltById(String userId) {
        return selectJoinOne(UserWithSaltVO.class, new MPJLambdaWrapper<User>()
                .selectAll(User.class)
                .select(UserSalt::getSalt)
                .eq(User::getId, userId)
                .leftJoin(UserSalt.class,
                        UserSalt::getUserId, User::getId)
        );
    }
}
