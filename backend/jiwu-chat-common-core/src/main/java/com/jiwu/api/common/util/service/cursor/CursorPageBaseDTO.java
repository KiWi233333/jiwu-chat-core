package com.jiwu.api.common.util.service.cursor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * @since 2023-03-19
 */
@Data
@Schema(description = "游标翻页请求")
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseDTO {

    @Schema(description = "页面大小")
    @Min(value = 0, message = "页面大小有误！")
    @Max(value = 100, message = "页面大小有误！")
    private Integer pageSize = 10;

    @Schema(description = "游标（初始为null，后续请求附带上次翻页的游标）")
    private String cursor;

    public Page plusPage() {
        return new Page(1, this.pageSize, false);
    }

    @JsonIgnore
    public Boolean isFirstPage() {
        return StringUtils.isEmpty(cursor);
    }
}
