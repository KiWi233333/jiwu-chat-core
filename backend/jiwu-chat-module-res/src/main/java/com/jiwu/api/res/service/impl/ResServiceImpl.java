package com.jiwu.api.res.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.config.oss.FileOSSConfig;
import com.jiwu.api.common.config.thread.ThreadPoolConfig;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.common.JsonUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.util.service.translation.TranslationUtil;
import com.jiwu.api.common.util.service.OSS.ResConstant;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.res.common.config.SseEmitterUTF8;
import com.jiwu.api.res.common.dto.TranslationDTO;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.vo.res.FileOssVO;
import com.jiwu.api.common.main.vo.res.TranslationVO;
import com.jiwu.api.res.service.OssService;
import com.jiwu.api.res.service.ResService;
import com.qiniu.util.StringMap;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 七牛云服务业务实现
 *
 * @className: ResServiceImpl
 * @author: Kiwi23333
 * @description: TODO 七牛云服务业务实现
 * @date: 2023/8/14 11:18
 */
@Service
@Slf4j
public class ResServiceImpl implements ResService {

    /* *************************** AI 翻译（SiliconFlow） *************************** */
    @Value("${res.translation.api-url}")
    private String translationApiUrl;
    @Value("${res.translation.api-key}")
    private String translationApiKey;
    @Value("${res.translation.model}")
    private String translationModel;
    @Value("${res.translation.max-tokens}")
    private Integer translationMaxTokens;

    @Resource
    private RedisUtil<String, String> redisUtil;
    @Resource
    private OssFileUtil ossFileUtil;
    @Resource
    private FileOSSConfig fileOSSConfig;
    @Resource
    private OssService ossService;
    @Resource
    private TranslationUtil translationUtil;
    @Autowired(required = false)
    private com.jiwu.api.common.util.service.translation.AiTranslationService aiTranslationService;
    @Resource
    @Qualifier(value = ThreadPoolConfig.JIWU_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 获取上传oss文件权限
     *
     * @param dirUrl   目录(images/下)
     * @param fileName 文件
     * @param userId   用户id
     * @return FileOssVO
     * @see <a href="https://developer.qiniu.com/kodo/1206/put-policy">上传策略</a>
     */
    @Override
    public Result<FileOssVO> getUploadToken(OssFileType type, String dirUrl, String fileName, String userId) {
        // 1、文件路径
        String key = type.getPath() + dirUrl + userId + "/" + fileName;
        long timeout = System.currentTimeMillis() + type.getTimeOut() * 1000;
        log.info("文件上传{}，key:{}，有效期：{}", userId, key, timeout);
        // 2、限制图片上传（策略）
        // https://developer.qiniu.com/kodo/6323/prevent-malicious-file-upload-best-practices
        // 恶意上传
        // https://developer.qiniu.com/kodo/1206/put-policy 上传策略
        StringMap policy = new StringMap();
        policy.put("scope", fileOSSConfig.bucketName + ":" + type.getPath() + key)
                .put("fileType", 0)// 文件存储类型 0、常规存储； 1、低频存储
                .put("deadline", timeout); // 有效期截至日期
        // 文件类型限制策略
        policy.put("mimeLimit", type.getFileType())// 文件类型
                .put("fsizeMin", 1024L)// 文件最小值（字节）
                .put("fsizeLimit", type.getFileSize());// 文件最大值（字节）
        // 3、获取token
        String token = ossFileUtil.getUploadToken(key, type.getTimeOut(), policy);
        // 4、缓存redis 当前谁操作文件夹 等待消费
        redisUtil.set(ResConstant.UPLOAD_NAME + userId + key, key, type.getTimeOut() + 300, TimeUnit.SECONDS);// 延长5分钟
        // 5、发送延迟消息 延迟删除
        ossService.addOssFileMq(userId, key, type);
        // 6、成功
        return Result.ok(new FileOssVO()
                .setUploadToken(token)
                .setEndDateTime(timeout)
                .setUrl(fileOSSConfig.hostName)
                .setKey(key));
    }

    /**
     * 删除oss文件
     *
     * @param key       路径文件名
     * @param userId    用户id
     * @param roleCheck 用户id
     * @return Res
     */
    @Override
    public Result<Object> deleteOssFile(String key, String userId, boolean roleCheck) {
        boolean flag = false;
        // 1、redis是否存在
        if (redisUtil.get(ResConstant.UPLOAD_NAME + userId + key) != null) {
            // 删除缓存加删除oss文件
            redisUtil.delete(ResConstant.UPLOAD_NAME + userId + key);
            log.info("{} Redis删除图片：{}", userId, key);
        }
        List<String> keyArr = Arrays.asList(key.split("/"));
        if (roleCheck && keyArr.size() > 2) {
            if (StringUtils.isNotBlank(keyArr.get(keyArr.size() - 2))
                    && (keyArr.get(keyArr.size() - 2).equals(userId))) {
                flag = ossFileUtil.deleteFile(key);
            } else {
                log.warn("Oss七牛云，用户：{}越权删除文件：{}", userId, key);
            }
        } else {
            flag = ossFileUtil.deleteFile(key);
        }
        // 2、结果
        return flag ? Result.ok("删除上传文件成功！", key) : Result.fail(ResultStatus.DELETE_ERR.getCode(), "删除失败，文件不存在！");
    }

    /**
     * 翻译
     *
     * @param dto 参数
     * @return TranslationVO
     */
    @Override
    public TranslationVO translationText(TranslationDTO dto) {
        TranslationVO vo;
        switch (dto.getType()) {
            case 1:
                final TextTranslateResponse res = translationUtil.translateText(dto.getText(),
                        dto.getSourceLang().toString(), dto.getTargetLang().toString());
                vo = TranslationVO.builder()
                        .result(res.getTargetText())
                        .sourceLang(res.getSource())
                        .targetLang(res.getTarget())
                        .tool(TranslationUtil.TranslationToolEnums.getLabelByValue(dto.getType()))
                        .build();
                break;
            case 2:
                AssertUtil.isTrue(aiTranslationService != null, "AI翻译服务未启用，请检查ai模块依赖！");
                final String content = aiTranslationService.translateSync(
                        JsonUtil.toStr(dto),
                        translationApiUrl,
                        translationApiKey,
                        translationModel,
                        translationMaxTokens);
                vo = TranslationVO.builder()
                        .result(content)
                        .sourceLang(dto.getSourceLang().toString())
                        .targetLang(dto.getTargetLang().toString())
                        .tool(TranslationUtil.TranslationToolEnums.getLabelByValue(dto.getType()))
                        .build();
                break;
            default:
                throw new BusinessException(ResultStatus.PARAM_ERR.getCode(), "翻译类型错误！");
        }
        return vo;
    }

    private final Map<String, Long> emitters = new ConcurrentHashMap<>(10);
    private static final int MAX_CONNECTIONS = 50;

    /**
     * 翻译sse
     *
     * @param dto 参数
     * @return TranslationVO
     */
    @Override
    public SseEmitterUTF8 translationTextSSE(TranslationDTO dto) {
        SseEmitterUTF8 emitter = new SseEmitterUTF8(60_000L); // 超时60秒
        final String userId = RequestHolderUtil.get().getId();
        // 限制连接数 emitters.size() <= MAX_CONNECTIONS
        AssertUtil.isTrue(emitters.size() <= MAX_CONNECTIONS, "当前连接数过多，请稍后再试！");
        final Long aLong = emitters.get(userId);
        AssertUtil.isFalse(aLong != null && System.currentTimeMillis() - aLong < 60_000, "正在翻译其他文本，请稍后再试！");
        emitters.put(userId, System.currentTimeMillis());
        emitter.onCompletion(() -> {
            log.info("SSE连接已关闭: {}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.error("SSE连接超时: {}", userId);
            emitters.remove(userId);
        });

        emitter.onError(e -> {
            log.error("SSE连接异常: {}", e.getMessage());
            emitters.remove(userId);
        });

        switch (dto.getType()) {
            case 1:
                final TextTranslateResponse res = translationUtil.translateText(dto.getText(),
                        dto.getSourceLang().toString(), dto.getTargetLang().toString());
                try {
                    emitter.send(res.getTargetText());
                    emitter.complete();
                    emitters.remove(userId);
                } catch (IOException e) {
                    emitters.remove(userId);
                    emitter.completeWithError(new BusinessException(ResultStatus.DEFAULT_ERR, "流式翻译出错，请稍后重试！"));
                }
                break;
            case 2:
                AssertUtil.isTrue(aiTranslationService != null, "AI翻译服务未启用，请检查ai模块依赖！");
                threadPoolTaskExecutor.execute(() -> {
                    try {
                        aiTranslationService.translateStream(
                                JsonUtil.toStr(dto),
                                translationApiUrl,
                                translationApiKey,
                                translationModel,
                                translationMaxTokens,
                                (content, isEnd) -> {
                                    try {
                                        if (isEnd) {
                                            emitter.complete();
                                            emitters.remove(userId);
                                        } else {
                                            emitter.send(content);
                                        }
                                    } catch (IOException e) {
                                        emitter.completeWithError(new BusinessException("AI流式翻译出错，请稍后重试！"));
                                        emitters.remove(userId);
                                    }
                                });
                    } catch (Exception e) {
                        log.warn("AI流式翻译出错，请稍后重试！", e);
                        emitter.completeWithError(new BusinessException("AI流式翻译出错，请稍后重试！"));
                        emitters.remove(userId);
                    }
                });
                break;
            default:
                emitters.remove(userId);
                throw new BusinessException(ResultStatus.PARAM_ERR.getCode(), "翻译类型错误！");
        }
        return emitter;
    }

    // 开源版已移除商品模块，相关 TODO 已移除

    // private List<Goods> goodsList;
    // private AiBaseConfigDTO aiServiceConfig;
    //
    // @PostConstruct
    // public void initConfig() {
    // goodsList = goodsMapper.selectGoodsAvaliableList();
    // aiServiceConfig = AiBaseConfigDTO.builder()
    // .systemPrompt(AIConstant.SYSTEM_PROMPT_SHOP_SERVICE + "，以下是商品数据：" +
    // JsonUtil.toStr(goodsList))
    // .apiUrl(translationApiUrl)
    // .apiKey(translationApiKey)
    // .model(translationModel.getCode())
    // .maxTokens(translationModel.getMaxTokens())
    // .build();
    // }

    //
    // /**
    // * 系统客服聊天
    // *
    // * @param dto 参数
    // * @return TranslationVO
    // */
    // @Override
    // public SseEmitterUTF8 systemServiceChat(AiBaseMessage dto) {
    // SseEmitterUTF8 emitter = new SseEmitterUTF8(120_000L); // 超时120秒
    // final String userId = RequestHolderUtil.get().getId();
    // // 限制连接数 emitters.size() <= MAX_CONNECTIONS
    // AssertUtil.isTrue(emitters.size() <= MAX_CONNECTIONS, "当前连接数过多，请稍后再试！");
    // final Long aLong = emitters.get(userId);
    // AssertUtil.isFalse(aLong != null && System.currentTimeMillis() - aLong <
    // 60_000, "正在翻译其他文本，请稍后再试！");
    // emitters.put(userId, System.currentTimeMillis());
    // emitter.onCompletion(() -> {
    // log.info("SSE连接已关闭: {}", userId);
    // emitters.remove(userId);
    // });
    //
    // emitter.onTimeout(() -> {
    // log.error("SSE连接超时: {}", userId);
    // emitters.remove(userId);
    // });
    //
    // emitter.onError(e -> {
    // log.error("SSE连接异常: {}", e.getMessage());
    // emitters.remove(userId);
    // });
    // threadPoolTaskExecutor.execute(() -> {
    // try {
    // aiChatService.callAiStream(AiModelCode.SiliconFlow, dto.getContent(), result
    // -> {
    // try {
    // if (result.isEnd()) {
    // emitter.complete();
    // emitters.remove(userId);
    // } else {
    // emitter.send(result.getContent());
    // }
    // } catch (IOException e) {
    // log.error("AI流式翻译出错: {}", e.getMessage());
    // emitter.completeWithError(new BusinessException("AI流式翻译出错，请稍后重试！"));
    // emitters.remove(userId);
    // }
    // }, aiServiceConfig);
    // } catch (Exception e) {
    // log.error("AI流式翻译出错: {}", e.getMessage());
    // emitter.completeWithError(new BusinessException("AI流式翻译出错，请稍后重试！"));
    // emitters.remove(userId);
    // }
    // });
    // return emitter;
    // }
}
