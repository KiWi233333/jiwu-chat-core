package com.jiwu.api.common.main.dto.chat.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 房间详情VO
 *
 * @className: ChatRoomVO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatRoomSelfVO {

    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "联系人名称")
    private String name;

    @Schema(description = "联系人头像")
    private String avatar;

    @Schema(description = "对方的用户id")
    private String targetUid;

    @Schema(description = "是否被删除（0-正常，1-删除）", example = "0")
    private Integer deleteStatus;

    @Schema(description = "成为好友时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
