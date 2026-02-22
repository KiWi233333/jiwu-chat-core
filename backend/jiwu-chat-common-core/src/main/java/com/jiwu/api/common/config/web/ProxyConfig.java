package com.jiwu.api.common.config.web;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * 网络代理配置
 * 提供代理 RestTemplate 和 OkHttpClient Bean
 *
 * @author Kiwi23333
 * @date 2025/12/14
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    /**
     * 是否启用代理
     */
    private boolean enabled = false;

    /**
     * 代理类型: HTTP, SOCKS
     */
    private String type = "HTTP";

    /**
     * 代理主机
     */
    private String host = "127.0.0.1";

    /**
     * 代理端口
     */
    private int port = 7890;

    /**
     * 代理用户名（可选）
     */
    private String username;

    /**
     * 代理密码（可选）
     */
    private String password;

    /**
     * 连接超时时间（秒）
     */
    private int connectTimeout = 10;

    /**
     * 读取超时时间（秒）
     */
    private int readTimeout = 30;

    /**
     * 写入超时时间（秒）
     */
    private int writeTimeout = 10;

    /**
     * 获取代理类型枚举
     */
    private Proxy.Type getProxyType() {
        return switch (type.toUpperCase()) {
            case "SOCKS", "SOCKS5", "SOCKS4" -> Proxy.Type.SOCKS;
            case "HTTP", "HTTPS" -> Proxy.Type.HTTP;
            default -> Proxy.Type.HTTP;
        };
    }

    /**
     * 创建 Proxy 对象
     */
    private Proxy createProxy() {
        if (!enabled) {
            return Proxy.NO_PROXY;
        }
        log.info("创建网络代理: type={}, host={}, port={}", type, host, port);
        return new Proxy(getProxyType(), new InetSocketAddress(host, port));
    }

    /**
     * 设置代理认证（如果需要）
     */
    private void setupProxyAuthentication() {
        if (enabled && username != null && !username.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password != null ? password.toCharArray() : new char[0]);
                }
            });
            log.info("已设置代理认证: username={}", username);
        }
    }

    /**
     * 代理 RestTemplate Bean
     * 当 proxy.enabled=true 时创建
     */
    @Bean("proxyRestTemplate")
    @ConditionalOnProperty(prefix = "proxy", name = "enabled", havingValue = "true")
    public RestTemplate proxyRestTemplate() {
        setupProxyAuthentication();
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setProxy(createProxy());
        factory.setConnectTimeout(connectTimeout * 1000);
        factory.setReadTimeout(readTimeout * 1000);
        
        log.info("已创建代理 RestTemplate: {}://{}:{}", type, host, port);
        return new RestTemplate(factory);
    }

    /**
     * 代理 OkHttpClient Bean
     * 当 proxy.enabled=true 时创建
     */
    @Bean("proxyOkHttpClient")
    @ConditionalOnProperty(prefix = "proxy", name = "enabled", havingValue = "true")
    public OkHttpClient proxyOkHttpClient() {
        setupProxyAuthentication();
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(createProxy())
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS);

        // 如果需要代理认证
        if (enabled && username != null && !username.isEmpty()) {
            builder.proxyAuthenticator((route, response) -> {
                String credential = okhttp3.Credentials.basic(username, password != null ? password : "");
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
        }

        log.info("已创建代理 OkHttpClient: {}://{}:{}", type, host, port);
        return builder.build();
    }

    /**
     * 默认 RestTemplate Bean（无代理）
     * 用于不需要代理的场景
     */
    @Bean("defaultRestTemplate")
    public RestTemplate defaultRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout * 1000);
        factory.setReadTimeout(readTimeout * 1000);
        return new RestTemplate(factory);
    }
}
