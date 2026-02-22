package com.jiwu.api.common.config.qrcode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeException;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.io.IOException;

@Service
@Slf4j
public class QrCodeService {
    private QrConfig config;

    // 初始化新建
    @PostConstruct
    public void qrConfig() {
        //初始宽度和高度
        config = new QrConfig(300, 300).setMargin(2)
                .setForeColor(Color.BLACK)
                .setBackColor(Color.WHITE);
    }

    public QrConfig getQrConfig() {
        return config;
    }

    //生成到文件
    public void createCodeToFile(String content, String filePath) {
        try {
            QrCodeUtil.generate(content, config, FileUtil.file(filePath));
        } catch (QrCodeException e) {
            e.printStackTrace();
        }
    }

    //生成到流
    public boolean createCodeToStream(String content, HttpServletResponse response) {
        try {
            QrCodeUtil.generate(content, config, "png", response.getOutputStream());
            return true;
        } catch (QrCodeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //生成到流
    public boolean createCodeToStream(String content, HttpServletResponse response, QrConfig qrConfig) {
        try {
            QrCodeUtil.generate(content, qrConfig, "png", response.getOutputStream());
            return true;
        } catch (QrCodeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
