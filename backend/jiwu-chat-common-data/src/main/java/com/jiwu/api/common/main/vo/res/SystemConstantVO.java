package com.jiwu.api.common.main.vo.res;

import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.dto.chat.cursor.MsgContentConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 系统常量VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemConstantVO {

    @Schema(description = "OSS信息")
    Map<String, OssFileType.OssFileTypeInfo> ossInfo;

    @Schema(description = "消息常量")
    Map<Integer, MsgContentConstant.MsgConstantInfo> msgInfo;

}
