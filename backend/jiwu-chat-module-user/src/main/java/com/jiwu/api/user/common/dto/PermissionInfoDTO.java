package com.jiwu.api.user.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * BillsTimeTotalDTO
 *
 * @className: BillsTimeTotalDTO
 * @author: Kiwi23333
 * @description: BillsTimeTotalDTO
 * @date: 2023/7/18 20:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PermissionInfoDTO {


    private String name;
    /**
     * 权限码
     */
    private String code;

}
