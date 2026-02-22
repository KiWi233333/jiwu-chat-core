package com.jiwu.api.res.common.dto;

import cn.hutool.extra.qrcode.QrConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.awt.Color;
import org.hibernate.validator.constraints.Range;

/**
 * 二维码转换配置
 *
 * @className: CodeOptionDTO
 * @author: Kiwi23333
 * @description: 二维码转换配置
 * @date: 2024/11/12 15:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRCodeOptionDTO {
    @Schema(description = "宽度")
    @Range(min = 10, max = 1920, message = "高度为 10-1920！")
    private Integer w = 300;

    @Schema(description = "高度")
    @Range(min = 10, max = 1080, message = "高度为 10-1080！")
    private Integer h = 300;

    @Schema(description = "边距")
    @Range(min = 0, message = "边距不能小于0！")
    private Integer m;

    @Schema(description = "圆角")
    @Range(min = 0, message = "圆角不能小于0！")
    private Integer r;

    public static QrConfig toQrConfig(QRCodeOptionDTO dto) {
        if (dto == null) {
            return new QrConfig();
        }
        return new QrConfig()
                .setForeColor(Color.BLACK)
                .setBackColor(Color.WHITE)
                .setWidth(dto.getW())
                .setHeight(dto.getH())
                .setRatio(dto.getR())
                .setMargin(dto.getM());
    }

    // 合并config
    public static QrConfig mergeConfig(QrConfig config, QRCodeOptionDTO dto) {
        if (dto == null) {
            return config;
        }
        return new QrConfig()
                .setForeColor(Color.BLACK)
                .setBackColor(Color.WHITE)
                .setWidth(dto.getW() == null ? config.getWidth() : dto.getW())
                .setHeight(dto.getH() == null ? config.getHeight() : dto.getH())
                .setRatio(dto.getR() == null ? config.getRatio() : dto.getR())
                .setMargin(dto.getM() == null ? config.getMargin() : dto.getM());
    }
}
