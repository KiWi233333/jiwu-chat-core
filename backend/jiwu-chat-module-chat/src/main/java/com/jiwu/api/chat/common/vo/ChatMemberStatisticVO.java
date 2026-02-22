package com.jiwu.api.chat.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 群成员统计信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberStatisticVO {

    @Schema(description = "在线人数")
    private Long onlineNum;//在线人数
//    @Schema(description = "总人数")
//    @Deprecated
//    private Long totalNum;//总人数
}
