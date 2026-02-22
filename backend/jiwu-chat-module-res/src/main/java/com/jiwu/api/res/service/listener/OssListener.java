package com.jiwu.api.res.service.listener;

import com.jiwu.api.common.util.common.JacksonUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.util.service.OSS.ResConstant;
import com.jiwu.api.res.common.config.OssMqConfig;
import com.jiwu.api.res.common.dto.OssMqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * oss信息操作
 *
 * @className: ordersMq
 * @author: Kiwi23333
 * @description: oss信息操作
 * @date: 2023/7/14 17:05
 */
@Slf4j
@Component
public class OssListener {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OssFileUtil ossFileUtil;

    /**
     * 过期文件删除处理
     * （oss文件延迟队列监听）
     *
     * @param msg 用户id:路径
     */
    @RabbitListener(queues = OssMqConfig.OSS_DELETE_DELAY_QUEUE)
    public void delayOssFileListener(String msg) {
        OssMqDTO dto = JacksonUtil.parseJSON(msg, OssMqDTO.class);
        // 1、存在（文件未被消费）
        if (dto == null) {
            return;
        }
        if (redisUtil.delete(ResConstant.UPLOAD_NAME + dto.getUserId() + dto.getKey())) {
            log.warn("七牛云：用户{}上传文件未被使用，自动删除：{}", dto.getUserId(), dto.getKey());
            // 删除过期
            ossFileUtil.deleteFile(dto.getKey());
        }
    }
}
