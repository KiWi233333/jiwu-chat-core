package com.jiwu.api.common.main.vo.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 描述
 *
 * @className: FileOssVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/8/14 13:50
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FileOssVO {
    @Schema(description = "上传路径")
    String url;

    @Schema(description = "上传文件名")
    String key;

    @Schema(description = "上传凭证")
    String uploadToken;

    @Schema(description = "有效期时间戳")
    long endDateTime;
}
