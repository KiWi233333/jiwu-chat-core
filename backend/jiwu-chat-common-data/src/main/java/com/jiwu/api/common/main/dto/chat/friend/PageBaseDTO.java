package com.jiwu.api.common.main.dto.chat.friend;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 描述
 *
 * @className: PageBaseDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2024/1/5 13:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PageBaseDTO {
    @Schema(description = "页面大小")
    @Min(0)
    @Max(50)
    private Integer size = 10;

    @Schema(description = "页面索引（从1开始）")
    private Integer page = 1;

    /**
     * 获取mybatisPlus的page
     *
     * @return
     */
    public Page plusPage() {
        return new Page(page, size);
    }
}
