package com.jiwu.api.common.util.service.OSS;

import com.google.gson.Gson;
import com.jiwu.api.common.config.oss.FileOSSConfig;
import com.jiwu.api.common.config.web.OkHttpConfig;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.common.ArrayUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 文件上传工具类OSS对象存储
 */
@Slf4j
@Component
public class OssFileUtil {
    @Autowired
    private FileOSSConfig fileOSSConfig;

    /**
     * 身份
     */
    private Auth auth;
    /**
     * 配置
     */
    private Configuration config;
    /**
     * 空间管理器
     */
    private BucketManager bucketManager;

    @Resource
    private OkHttpConfig okHttpConfig;

    @PostConstruct // @PostConstruct注解标记该方法，确保在依赖注入完成后执行。
    public void init() {
        if (StringUtils.isNullOrEmpty(fileOSSConfig.accessKey) || StringUtils.isNullOrEmpty(fileOSSConfig.secretKey) || StringUtils.isNullOrEmpty(fileOSSConfig.bucketName)) {
            log.error("Oss 未配置，请进行配置");
            return;
        }
        // 在初始化方法中进行实例化
        auth = Auth.create(fileOSSConfig.accessKey, fileOSSConfig.secretKey);
        config = new Configuration(Region.autoRegion());
        bucketManager = new BucketManager(auth, config);
    }

    public BucketManager getBucketManager() {
        return bucketManager;
    }


    /**
     * 上传图片
     *
     * @param key  目录
     * @param file 文件
     * @return 资源地址
     */
    public String uploadImage(String key, MultipartFile file) {
        return uploadFile(key, file);
    }

    // 上传
    private String uploadFile(String key, MultipartFile file) {
        log.info("Uploading {}", key);
        //构造一个带指定 Region 对象的配置类
        UploadManager uploadManager = new UploadManager(config);
        // 1、生成上传凭证，然后准备上传
        // 默认不指定key的情况下，以文件内容的hash值作为文件名
        // 以什么为文件名
        log.info("File:{} ", key);
        // 2、身份信息
        try {
            if (file.getOriginalFilename() == null) return null;
            // 3、上传文件
            String upToken = auth.uploadToken(fileOSSConfig.bucketName);
            Response res = uploadManager.put(file.getBytes(), key, upToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(res.bodyString(), DefaultPutRet.class);
            // 4、打印返回的信息
            if (res.isOK() && res.isJson()) {
                log.warn("Upload OK {}", putRet.toString());
                res.close();
                return key;
            } else {
                log.error("七牛异常 fail:" + res.bodyString());
                res.close();
                return key;
            }
        } catch (IOException e) {
            log.warn("Upload fail {}", e.getMessage());
        }
        return null;
    }

    // 上传
    public String uploadFile(String key, String fileName, byte[] data) {
        log.info("Uploading {}", key);
        //构造一个带指定 Region 对象的配置类
        UploadManager uploadManager = new UploadManager(config);
        // 1、生成上传凭证，然后准备上传
        // 默认不指定key的情况下，以文件内容的hash值作为文件名
        // 以什么为文件名
        log.info("File:{} ", key);
        // 2、身份信息
        try {
            if (fileName == null) return null;
            // 3、上传文件
            String upToken = auth.uploadToken(fileOSSConfig.bucketName);
            Response res = uploadManager.put(data, key, upToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(res.bodyString(), DefaultPutRet.class);
            // 4、打印返回的信息
            if (res.isOK() && res.isJson()) {
                log.warn("Upload OK {}", putRet.toString());
                res.close();
                return key;
            } else {
                log.error("七牛异常 fail:" + res.bodyString());
                res.close();
                return key;
            }
        } catch (IOException e) {
            log.warn("Upload fail {}", e.getMessage());
        }
        return null;
    }


    /**
     * 获取上传凭证
     *
     * @param key      旧文件路径（是否覆盖上传）
     * @param duration 有效期 （秒）（default:3600s）
     * @param policy   policy
     * @return token
     */
    public String getUploadToken(String key, long duration, StringMap policy) {
        return auth.uploadToken(fileOSSConfig.bucketName, key, duration, policy);
    }

    /**
     * 获取下载凭证
     *
     * @param fullPath 文件全路径
     * @param duration 有效期 （秒）（default:3600s）
     * @return token
     */
    public String getDownLoadToken(String fullPath, long duration) {
        return auth.privateDownloadUrl(fileOSSConfig.bucketName, duration);
    }

    /**
     * 获取凭证
     *
     * @return Auth
     */
    @PostConstruct
    private Auth getAuth() {
        if (auth == null) {
            if (StringUtils.isNullOrEmpty(fileOSSConfig.accessKey) || StringUtils.isNullOrEmpty(fileOSSConfig.secretKey)) {
                log.error("Oss 未配置，请进行配置");
                return null;
            }
            auth = Auth.create(fileOSSConfig.accessKey, fileOSSConfig.secretKey);
        }
        return auth;
    }

    /**
     * oss文件删除
     *
     * @param key 文件路径名
     */
    public boolean deleteFile(String key) {
        if (StringUtils.isNullOrEmpty(key)) {
            return true;
        }
        try {
            // 解析上传成功的结果
            Response res = bucketManager.delete(fileOSSConfig.bucketName, key);
            log.info("七牛云：删除文件{}", res);
            return res.isOK() && res.isJson();
        } catch (Exception e) {
            log.warn("七牛云：删除文件失败{}", e.getMessage());
            return false;
        }
    }

    /**
     * oss文件删除
     */
    public List<String> deleteFile(List<String> keys) {
        if (keys.isEmpty()) {
            return keys;
        }
        List<String> del = new ArrayList<>();
        for (String key : keys) {
            try {
                // 解析上传成功的结果
                Response res = bucketManager.delete(fileOSSConfig.bucketName, key);
                log.info("七牛云：删除文件{}", res);
                if (res.isOK() && res.isJson()) {
                    del.add(key);
                }
            } catch (Exception e) {
                log.warn("七牛云：删除文件失败");
            }
        }
        return del;
    }


    @Autowired
    private RedisUtil<String, Object> redisUtil;

    public boolean deleteRedisKey(String userId, String key) {
        final boolean deleted = redisUtil.delete(ResConstant.UPLOAD_NAME + userId + key);
        if (deleted) {
            log.info("七牛云oss: {} 消费图片：{}", userId, key);
        } else {
            log.error("七牛云oss: {} 消费图片失败：{}", userId, key);
        }
        return deleted;
    }


    public long deleteRedisKey(String userId, Collection<String> keys) {
        // 1、删除
        List<String> redisKeys = keys.stream().map(key -> ResConstant.UPLOAD_NAME + userId + key).collect(Collectors.toList());
        // 恢复消费资格
        List<String> list = (List<String>) redisUtil.getRedisTemplate().opsForValue().multiGet(redisKeys);
        if (list != null && list.size() != keys.size()) {
            throw new BusinessException(ResultStatus.PARAM_ERR.getCode(), "部分文件不存在！");
        } else {
            // 文件存在则消费
            final long deleted = redisUtil.delete(redisKeys);
            if (deleted == keys.size()) {
                log.info("七牛云oss: {} 消费图片集合：{}", userId, keys);
            } else {
                log.error("七牛云oss: {} 消费图片集合失败：{}", userId, keys);
            }
            return deleted;
        }
    }

    /**
     * 比较差异增删文件
     *
     * @param adminId     管理员id
     * @param oldImageStr ,拼接的图片集
     * @param newImageStr ,拼接的图片集
     * @return 是否成功
     */
    public boolean diffDeleteFile(String adminId, String oldImageStr, String newImageStr) {
        Set<String> diffRemove;
        Set<String> newAdd;
        try {

            // 删除移除文件
            if (oldImageStr != null && newImageStr != null) {
                newAdd = ArrayUtil.diffAddString(oldImageStr.split(","), newImageStr.split(","));
                diffRemove = ArrayUtil.diffRemoveString(oldImageStr.split(","), newImageStr.split(","));
            } else {
                return true;
            }
            log.info("七牛云删除图片：{}", diffRemove);
            for (String key : diffRemove) {
                this.deleteFile(key);
            }
            // 消费新增图片
            log.info("七牛云消费图片：{}", newAdd);
            this.deleteRedisKey(adminId, ArrayUtil.diffAddString(oldImageStr.split(","), newImageStr.split(",")));
            return true;
        } catch (Exception e) {
            log.error("七牛云图片操作失败：{}", adminId);
            return false;
        }
    }

    public String getBucketName() {
        return fileOSSConfig.bucketName;
    }

    /**
     * 时间戳防盗链
     *
     * @param originUrl 原始URL
     * @return 时间戳防盗链URL
     */
    public String generateTimeUrl(String originUrl, long durationSecond) {
        OssURLSignerUtil.generateSignedUrl(fileOSSConfig.signTimeUrlKey, originUrl, System.currentTimeMillis() + durationSecond);
        return null;
    }

    public String generateTimeUrlOneHours(String originUrl) {
        OssURLSignerUtil.generateSignedUrl(fileOSSConfig.signTimeUrlKey, originUrl, System.currentTimeMillis() + 3600000);
        return null;
    }


    public FileInfo getFileInfo(String key) {
        try {
            FileInfo fileInfo = bucketManager.stat(fileOSSConfig.bucketName, key);
            log.info("七牛云：图片信息{}", fileInfo);
            return fileInfo;
        } catch (Exception e) {
            log.warn("七牛云：图片信息失败{}", e.getMessage());
            return null;
        }
    }


    /**
     * 获取图片信息
     *
     * @param key 图片路径
     * @return 图片信息
     */
    public OssImgInfo getImgInfo(String key) {
        try {
            Request request = new Request.Builder().url(fileOSSConfig.buildUrl(key) + "?imageInfo").build();
            return new Gson().fromJson(Objects.requireNonNull(okHttpConfig.okHttpClient().newCall(request).execute().body()).string(), OssImgInfo.class);
        } catch (Exception e) {
            log.warn("七牛云：图片信息失败{}", e.getMessage());
            return null;
        }
    }


    //    图片信息
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OssImgInfo {

        @Schema(description = "图片宽度（像素）")
        private Integer width;
        @Schema(description = "图片高度（像素）")
        private Integer height;
        @Schema(description = "图片大小（字节）")
        private Long size;
        @Schema(description = "图片格式")
        private String format;
        @Schema(description = "图片色彩模型")
        private String colorModel;
        @Schema(description = "GIF帧数")
        private Long frameCount;
    }
}
