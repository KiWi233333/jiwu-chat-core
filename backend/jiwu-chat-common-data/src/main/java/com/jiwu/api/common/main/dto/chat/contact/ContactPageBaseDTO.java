package com.jiwu.api.common.main.dto.chat.contact;

import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import com.jiwu.api.common.main.enums.chat.RoomTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * @since 2023-03-19
 */
@Data
@Schema(description = "游标翻页请求")
@AllArgsConstructor
@NoArgsConstructor
public class ContactPageBaseDTO extends CursorPageBaseDTO {

    @Schema(description = "房间类型")
    @Min(1)
    @Max(3)
    /**
     * @see RoomTypeEnum
     */
    private Integer type = null;

    public boolean isGroupType() {
        return RoomTypeEnum.GROUP.getType().equals(type);
    }
}
