package com.jiwu.api.chat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ws登录token
 *
 * @className: WSAuthorize
 * @author: Kiwi23333
 * @description: ws登录token
 * @date: 2023/12/1 16:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSAuthorize {
    private String token;
}
