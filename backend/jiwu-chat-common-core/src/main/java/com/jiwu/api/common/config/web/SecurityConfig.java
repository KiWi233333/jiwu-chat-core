//package com.example.back_jiwuquang_api.core.constant;
//
//import com.example.back_jiwuquang_api.core.constant.filters.AuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.constant.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.constant.annotation.web.builders.HttpSecurity;
//import org.springframework.security.constant.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.constant.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.constant.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    private AuthenticationFilter filter;
//
//
//    /**
//     * 验证请求重载
//     *
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//    /**
//     * URL请求配置
//     *
//     * @param httpSecurity
//     * @throws Exception
//     */
//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        //配置规则和参数
//        httpSecurity.csrf().disable()//由于使用的是JWT，我们这里不需要csrf
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()//基于token,所以不需要session
//                .authorizeRequests()//授权配置
//                .antMatchers(getPublicUrls()).permitAll()// 不需要权限
//                .anyRequest().authenticated();//除以上请求外的所有请求需要鉴权认证
//        //添加JWT Filter
//        httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//        //禁用缓存
//        httpSecurity.headers().cacheControl();
//        //允许跨域
//        httpSecurity.headers().frameOptions().disable();
//    }
//
//    /**
//     * 密码生成策略
//     *
//     * @return
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Value("${spring.profiles.active}")
//    private String env;
//
//    /**
//     * 请求过滤参数
//     *
//     * @return Urls
//     */
//    private String[] getPublicUrls() {
//        List<String> list = new ArrayList<>();
//        // 发布版本
//        if (!env.contains("prod")) {
//            list.add("/swagger-ui.html/**");
//            list.add("/swagger-ui/**");
//            list.add("/swagger-resources/**");
//        }
//        // 前台
//        list.add("/res/**");// 公共资源
//        list.add("/user/login/**");// 登录
//        list.add("/user/register/**");// 注册
//        list.add("/user/register/**");// 登录
//        list.add("/user/wallet/combo");// 充值套餐
//        list.add("/goods/sku/**");// 商品规格
//        list.add("/event/**");// 前台活动
//        // 后台
//        list.add("/admin/login/**");// 管理员登录
//        return list.toArray(new String[0]);
//    }
//}
