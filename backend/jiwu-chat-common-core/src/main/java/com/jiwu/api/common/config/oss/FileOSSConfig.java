package com.jiwu.api.common.config.oss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OSS 配置读取类
 */
@Component
public class FileOSSConfig {

    @Value("${qi-niu-cloud.access-key}")
    public String accessKey;
    @Value("${qi-niu-cloud.secret-key}")
    public String secretKey;
    @Value("${qi-niu-cloud.bucket-name}")
    public String bucketName;
    @Value("${qi-niu-cloud.host-name}")
    public String hostName;
    @Value("${qi-niu-cloud.sign-time-url-key}")
    public String signTimeUrlKey;
    @Value("${qi-niu-cloud.sign-time-url-key2}")
    public String signTimeUrlKey2;

    public String buildUrl(String key) {
        return hostName + "/" + key;
    }
}
