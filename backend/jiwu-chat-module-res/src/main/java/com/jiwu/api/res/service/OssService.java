package com.jiwu.api.res.service;

import com.jiwu.api.common.main.enums.res.OssFileType;

/**
 * oss业务
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: oss业务
 * @date: 2023/4/30 15:49
 */
public interface OssService {

    /**
     * 添加延迟删除消息
     *
     * @param userId   用户名
     * @param key      文件路径
     * @param fileType 文件类型
     */
    void addOssFileMq(String userId, String key, OssFileType fileType);

}
