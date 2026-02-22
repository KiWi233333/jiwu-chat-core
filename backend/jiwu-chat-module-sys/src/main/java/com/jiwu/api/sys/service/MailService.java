package com.jiwu.api.sys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MailService {

    @Resource
    private JavaMailSender mailSender;
    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${emailSendUser}")
    private String emailSendUser;

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${emailTemplate}")
    private String emailTemplatePath;
    @Value("${emailCodeTemplate}")
    private String emailCodeTemplate;


    public static final String LOGO_TEXT = "极物圈";


    /**
     * 发送验证码文件
     *
     * @param to    收件人
     * @param theme 主题
     * @param type  注册、修改和 登录
     * @param code  验证码
     * @throws MessagingException 发送失败
     */
    public void sendCodeMail(String to, String theme, String type, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(from, emailSendUser);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("您的" + theme + "验证码是:" + code);
        // 利用 Thymeleaf 模板构建 html 文本
        Context ctx = new Context();
        Map<String, String> map = new HashMap<>();
        map.put("title", "您的验证码是:" + code);
        map.put("type", type);
        map.put("email", to);
        map.put("code", code);
        // 给模板的参数的上下文
        ctx.setVariable("EmailParams", map);
        // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
        String emailText = templateEngine.process(emailTemplatePath, ctx);
        // 传入模板
        mimeMessageHelper.setText(emailText, true);
        mailSender.send(mimeMessage);
    }

    /**
     * 发送普通文本邮件
     *
     * @param to      收件人
     * @param title   标题
     * @param content 正文
     * @throws Exception 失败
     */
    public void sendTextMail(String to, String title, String content) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); // 发件人
        message.setTo(to);
        message.setSubject(title);
        message.setText(content);
        mailSender.send(message);
    }
}
