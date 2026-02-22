package com.jiwu.api.common.util.service.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * UserTokenDTO token包含内容太
 *
 * @className: UserTokenDTO
 * @author: Kiwi23333
 * @description: UserTokenDTO
 * @date: 2023/5/3 1:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserTokenDTO {

    /**
     * 用户id
     */
    private String id;
    private String ua;

}
