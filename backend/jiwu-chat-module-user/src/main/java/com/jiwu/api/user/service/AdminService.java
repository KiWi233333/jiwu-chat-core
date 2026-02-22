package com.jiwu.api.user.service;

import com.jiwu.api.user.common.dto.UpdatePwdDTO;

public interface AdminService {
    /**
     * 修改管理员头像
     *
     * @param userId 用户id
     * @param key    文件路径
     * @return Result
     */
    String updateAvatar(String userId, String key);

    /**
     * 修改密码（新旧密码）
     *
     * @param userId 用户id
     * @param dto    参数
     * @return 结果
     */
    Boolean updateUserPwd(String userId, UpdatePwdDTO dto);
}
