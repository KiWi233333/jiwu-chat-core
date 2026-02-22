package com.jiwu.api.user.common.dto;

import com.jiwu.api.common.main.pojo.sys.UserAddress;
import com.jiwu.api.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 描述
 *
 * @className: UserAddressDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/16 13:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserAddressDTO {


    @Schema(description = "收件人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收件人不能为空！")
    @Length(min = 2, max = 20, message = "收件人长度2-20字符！")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空！")
    @Phone
    private String phone;

    @Schema(description = "是否默认", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否默认不能为空！")
    private Integer isDefault;

    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "省份不能为空！")
    private String province;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "城市不能为空！")
    private String city;

    @Schema(description = "区/县", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "区/县不能为空！")
    private String county;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "详细地址不能为空！")
    private String address;

    @Schema(description = "邮政编码")
    private String postalCode;

    public static UserAddress toUserAddress(UserAddressDTO p) {
        return new UserAddress()
                .setName(p.getName())
                .setPhone(p.getPhone())
                .setProvince(p.getProvince())
                .setCity(p.getCity())
                .setCounty(p.getCounty())
                .setIsDefault(p.getIsDefault())
                .setPostalCode(p.getPostalCode())
                .setAddress(p.getAddress());
    }

}

