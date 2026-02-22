package com.jiwu.api.common.main.dto.chat.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑群聊房间DTO
 *
 * @className: UpdateRoomGroupDTO
 * @author: Kiwi23333
 * @description: 群聊详情
 * @date: 2024/06/26 12:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateRoomGroupDTO {

    @Schema(description = "群头像")
    @Length(max = 200, message = "群头像格式错误！")
    private String avatar;


    @Schema(description = "群名称")
    @Length(max = 30, message = "群名称过长！")
    private String name;

    @Schema(description = "群详情")
    RoomGroupExtJson detail;

}
