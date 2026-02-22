package com.jiwu.api.chat.service;


import com.jiwu.api.chat.common.vo.WsBaseVO;

import java.util.List;

public interface PushService {

     void sendPushMsg(WsBaseVO<?> msg, List<String> uidList);
     void sendPushMsg(WsBaseVO<?> msg, String uid);

     void sendPushMsg(WsBaseVO<?> msg);

}
