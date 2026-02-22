package com.jiwu.api.chat.common.vo.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录常规返回
 * Date: 2023-03-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSLoginSuccess {


    private String uid;

    private String avatar;

    private String name;

//    private List<Role> roleList;
}
