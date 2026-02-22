package com.jiwu.api.user.config.web;

import com.jiwu.api.user.config.interceptor.AuthInterceptor;
import com.jiwu.api.user.config.interceptor.PortFlowInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.*;


@Slf4j
@Configuration
@EnableWebMvc
public class SwaggerAndWebConfig implements WebMvcConfigurer { // 覆写addResourceHandlers跨域
    @Override
    public void configurePathMatch(@NotNull PathMatchConfigurer configurer) {
        // 启用尾部斜杠匹配
//        configurer.setUseTrailingSlashMatch(true);

        // 配置AntPathMatcher以处理多余的斜杠
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setTrimTokens(true);  // 去除路径中的空格
        matcher.setCachePatterns(true);
        configurer.setPathMatcher(matcher);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

    // 跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有接口
                .allowCredentials(true) // 是否发送 Cookie
                .allowedOriginPatterns("*") // 支持域
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 支持方法
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

    // 拦截器
    @Autowired
    AuthInterceptor authInterceptor;
    // 拦截器
    @Autowired
    PortFlowInterceptor portFlowInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1、身份验证拦截器
        registry.addInterceptor(authInterceptor) // 注册拦截器
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        // 公共
                        "/public/**",// 公共资源
                        "/res/oss/**",// 公共资源
                        "/res/image/**",// 公共资源
                        "/res/video/**",// 公共资源
                        "/res/file/**",// 公共资源
                        "/res/app/**",// 应用安装
                        "/res/qrcode/stream", // 二维码流生成
                        "/res/utils/webhook/**", // webhook
//                        "/res/utils/mcp/**", // TODO MCP暂时解开
                        // 前台
                        "/user/login/**",// 登录
                        "/user/register/**",// 注册
                        "/user/info/check/**",// 身份验证|码
                        "/user/exist",// 用户名是否存在
                        "/user/wallet/combo",// 充值套餐
                        "/doc.html/**", // swagger
                        "/swagger-ui.html/**",
                        // 后台
                        "/admin/login/**" // 管理员登录
                        // TODO: 测试其他
                );
        // 2、频控拦截器
        registry.addInterceptor(portFlowInterceptor);
    }
}
