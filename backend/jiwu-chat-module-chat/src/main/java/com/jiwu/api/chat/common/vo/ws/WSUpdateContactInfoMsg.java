package com.jiwu.api.chat.common.vo.ws;

import com.jiwu.api.chat.common.enums.WsRespTypeEnum;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSUpdateContactInfoMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "通知状态")
    private Integer noticeStatus;

    @Schema(description = "免打扰状态")
    private Integer shieldStatus;

    public static WsBaseVO<WSUpdateContactInfoMsg> buildWsBaseVO(WSUpdateContactInfoMsg contactMsg) {
        WsBaseVO<WSUpdateContactInfoMsg> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.UPDATE_CONTACT_INFO.getType());
        vo.setData(contactMsg);
        return vo;
    }

    public static WsBaseVO<WSUpdateContactInfoMsg> buildWsShieldBaseVO(Long roomId, Integer shieldStatus) {
        WsBaseVO<WSUpdateContactInfoMsg> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.UPDATE_CONTACT_INFO.getType());
        vo
                .setData(WSUpdateContactInfoMsg
                        .builder()
                        .roomId(roomId)
                        .shieldStatus(shieldStatus)
                        .build());
        return vo;
    }

    public static WsBaseVO<WSUpdateContactInfoMsg> buildWsNoticeBaseVO(Long roomId, Integer noticeStatus) {
        WsBaseVO<WSUpdateContactInfoMsg> vo = new WsBaseVO<>();
        vo.setType(WsRespTypeEnum.UPDATE_CONTACT_INFO.getType());
        vo
                .setData(WSUpdateContactInfoMsg
                        .builder()
                        .roomId(roomId)
                        .noticeStatus(noticeStatus)
                        .build());
        return vo;
    }
}




