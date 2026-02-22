package com.jiwu.api.res.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 描述
 *
 * @className: OssMqDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/8/16 19:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OssMqDTO implements Serializable {
    // 用户id
    String userId;

    // 文件路径和名 
    String key;
}
