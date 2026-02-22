package com.jiwu.api.common.util.service.OSS;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class OssURLSignerUtil {
    /**
     * 生成签名URL
     *
     * @param key      密钥
     * @param url      原始URL
     * @param deadline 过期时间戳
     * @return 签名后的URL
     */
    public static String generateSignedUrl(String key, String url, long deadline) {
        try {
            // 解析URL
            URL parsedUrl = new URL(url);
            String path = parsedUrl.getPath();
            String query = parsedUrl.getQuery();

            // 将路径进行URL编码，斜线"/"不编码
            String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
                    .replace("%2F", "/");

            // 生成时间戳的16进制字符串
            String t = Long.toHexString(deadline);

            // 生成签名
            String sign = generateSign(key, encodedPath, t);

            // 构造签名部分
            String signPart = "sign=" + sign + "&t=" + t;

            // 构造完整的签名URL
            StringBuilder signedUrlBuilder = new StringBuilder();
            signedUrlBuilder.append(parsedUrl.getProtocol())
                    .append("://")
                    .append(parsedUrl.getHost())
                    .append(encodedPath);
            if (query != null && !query.isEmpty()) {
                signedUrlBuilder.append("?").append(query).append("&").append(signPart);
            } else {
                signedUrlBuilder.append("?").append(signPart);
            }

            return signedUrlBuilder.toString();
        } catch (Exception e) {
            log.warn("OSS Failed to generate signed URL: {}" , e.getMessage());
            return null;
        }
    }

    /**
     * 生成签名
     *
     * @param key  密钥
     * @param path URL路径
     * @param time 时间戳
     * @return 签名字符串
     */
    private static String generateSign(String key, String path, String time) {
        // 拼接字符串
        String toSign = key + path + time;
        // 使用MD5生成签名
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        byte[] hash = md.digest(toSign.getBytes(StandardCharsets.UTF_8));
        // 将字节转换为16进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toLowerCase();
    }

}
