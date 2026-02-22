package com.jiwu.api.res.service.impl;

import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.res.common.config.OssMqConfig;
import com.jiwu.api.res.common.dto.OssMqDTO;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.res.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
public class OssServiceImpl implements OssService {
    @Autowired
    RedisUtil<String, String> redisUtil;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void addOssFileMq(String userId, String key, OssFileType fileType) {
        rabbitTemplate.convertAndSend(OssMqConfig.OSS_DELETE_DELAY_EXCHANGE,
                OssMqConfig.OSS_DELETE_DELAY_QUEUE,
                JacksonUtil.toJSON(new OssMqDTO().setUserId(userId).setKey(key)),
                message -> {
                    message.getMessageProperties().setDelayLong(fileType.getTimeOut() * 1000L); // 分钟
                    return message;
                });
        log.info("七牛云：用户{},延迟队列文件路径：{}，time: {}", userId, key, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
