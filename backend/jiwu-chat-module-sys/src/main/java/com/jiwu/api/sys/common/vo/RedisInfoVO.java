package com.jiwu.api.sys.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RedisInfoVO<T> {

    @Schema(description = "键")
    private String key;

    @Schema(description = "值")
    private T value;

    @Schema(description = "过期时间")
    private Long expire;

    @Schema(description = "类型")
    private String type;

}
