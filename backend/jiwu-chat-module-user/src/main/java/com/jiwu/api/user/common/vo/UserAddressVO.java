package com.jiwu.api.user.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 描述
 *
 * @className: UserAddressVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/27 15:08
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class UserAddressVO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "收货人")
    private String name;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县")
    private String county;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "邮编")
    private String postalCode;

    @Schema(description = "手机号")
    private String phone;


}
