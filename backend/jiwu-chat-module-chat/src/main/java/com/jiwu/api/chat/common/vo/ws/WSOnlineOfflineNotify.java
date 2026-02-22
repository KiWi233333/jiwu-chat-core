package com.jiwu.api.chat.common.vo.ws;

import com.jiwu.api.chat.common.vo.ChatMemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:用户上下线变动的推送类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSOnlineOfflineNotify {
    
    private List<ChatMemberVO> changeList = new ArrayList<>();//新的上下线用户

    private Long onlineNum;//在线人数
}
