package com.jiwu.api.user.service.send;

import com.jiwu.api.common.annotation.FlowControl;
import com.jiwu.api.user.common.config.BillsMQConfig;
import com.jiwu.api.user.service.BillsMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 账单消息处理
 *
 * @className: BillsSender
 * @author: Kiwi23333
 * @description: 账单消息处理
 * @date: 2023/7/23 1:11
 */
@Service
@Slf4j
public class BillsMqServiceImpl implements BillsMQService {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 自动更新用户钱包信息
     * 频控（60s一次）
     * @param userId  用户id
     */
    @Override
    @FlowControl(value = "other:flow:autoWallet:") // 1/60s
    public void autoUpdateWallet(String userId) {
        // 发送消息
        rabbitTemplate.convertAndSend(BillsMQConfig.DIRECT_BILLS_EXCHANGE,
                BillsMQConfig.DIRECT_SAVE_BILLS_ROUTING_KEY,
                userId);
    }
}
