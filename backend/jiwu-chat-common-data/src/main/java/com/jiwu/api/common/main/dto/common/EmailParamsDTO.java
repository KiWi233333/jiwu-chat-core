package com.jiwu.api.common.main.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 描述
 *
 * @className: EmailParamsDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/4/28 17:30
 */
@Data
@AllArgsConstructor
public class EmailParamsDTO {
    String to;// 接收方
    String theme;// 主题
    String type;// 方式
    String code;// 验证码
 }
