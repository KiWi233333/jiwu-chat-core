package com.jiwu.api.res.service;

import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.res.common.config.SseEmitterUTF8;
import com.jiwu.api.res.common.dto.TranslationDTO;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.vo.res.FileOssVO;
import com.jiwu.api.common.main.vo.res.TranslationVO;


/**
 * 文件存储业务
 *
 * @className: UserWalletService
 * @author: Kiwi23333
 * @description: 文件存储业务
 * @date: 2023/4/30 15:49
 */
public interface ResService {

    /**
     * 用户上传图片权限
     *
     * @param fileType 文件类型
     * @param dirUrl   目录
     * @param fileName 文件名
     * @param userId   用户名
     * @return FileOssVo
     */
    Result<FileOssVO> getUploadToken(OssFileType fileType, String dirUrl, String fileName, String userId);

    /**
     * 删除oss文件
     *
     * @param key       路径文件名
     * @param userId    用户id
     * @param roleCheck 用户权限校验
     * @return Result
     */
    Result<Object> deleteOssFile(String key, String userId, boolean roleCheck);

    /**
     * 翻译
     *
     * @param dto 参数
     * @return TranslationVO
     */
    TranslationVO translationText(TranslationDTO dto);

    /**
     * 翻译sse
     *
     * @param dto 参数
     * @return TranslationVO
     */
    SseEmitterUTF8 translationTextSSE(TranslationDTO dto);

    /**
     * 系统客服聊天
     *
     * @param dto 参数
     * @return TranslationVO
     */
//    SseEmitterUTF8 systemServiceChat(AiBaseMessage dto);
}
