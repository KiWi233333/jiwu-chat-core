package com.jiwu.api.chat.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息表情反应聚合VO
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息表情反应聚合信息")
public class ReactionVO {

    @Schema(description = "emoji编码")
    private String emojiType;

    @Schema(description = "该emoji的总反应数")
    private Integer count;

    @Schema(description = "反应的用户ID列表（截断前N个）")
    private List<String> userIds;

    @Schema(description = "当前查询用户是否已反应")
    private Boolean isCurrentUser;
}
