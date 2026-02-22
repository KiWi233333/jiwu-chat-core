package com.jiwu.api.user.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ValidPermissionVO {

    /**
     * 权限唯一CODE代码
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限介绍
     */
    private String intro;


}
