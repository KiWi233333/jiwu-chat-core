package com.jiwu.api.common.main.dto.chat.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 添加群聊房间
 *
 * @className: InsertRoomGroupDTO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/12/26 12:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsertRoomGroupDTO {

    @Schema(description = "群头像")
    @Length(max = 200, message = "群头像格式错误！")
    @NotNull(message = "群头像不能为空！")
    private String avatar;


    @NotNull(message = "群成员不能为空！")
    @Size(min = 1, max = 300, message = "群成员不能超过300人！")
    @Schema(description = "邀请的uid")
    private List<String> uidList;
}
