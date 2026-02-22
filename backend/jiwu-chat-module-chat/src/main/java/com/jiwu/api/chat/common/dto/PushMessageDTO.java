package com.jiwu.api.chat.common.dto;

import com.jiwu.api.chat.common.enums.WSPushTypeEnum;
import com.jiwu.api.chat.common.vo.WsBaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Description: 推送给用户的消息对象
 * Date: 2023-08-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageDTO implements Serializable {
    /**
     * 推送的ws消息
     */
    private WsBaseVO<?> wsBaseMsg;
    /**
     * 推送的uid
     */
    private List<String> uidList;

    /**
     * 推送类型 1个人 2全员
     *
     * @see WSPushTypeEnum
     */
    private Integer pushType;

    public PushMessageDTO(String uid, WsBaseVO<?> wsBaseMsg) {
        this.uidList = Collections.singletonList(uid);
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.USER.getType();
    }

    public PushMessageDTO(List<String> uidList, WsBaseVO<?> wsBaseMsg) {
        this.uidList = uidList;
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.USER.getType();
    }

    public PushMessageDTO(WsBaseVO<?> wsBaseMsg) {
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.ALL.getType();
    }
}
