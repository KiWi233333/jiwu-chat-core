package com.jiwu.api.common.main.cache.chat;

import cn.hutool.core.lang.Pair;
import com.jiwu.api.common.util.service.cursor.CursorUtils;
import com.jiwu.api.common.util.service.RedisKeyUtil;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseVO;
import com.jiwu.api.common.util.service.cursor.CursorPageBaseDTO;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * Description: 全局房间
 */
@Component
public class ChatHotRoomCache {
    static final String CHAT_HOT_ROOM_ZET = "chat:hot:room:info:";

    /**
     * 获取热门群聊翻页
     *
     * @return
     */
    public CursorPageBaseVO<Pair<Long, Double>> getRoomCursorPage(CursorPageBaseDTO pageBaseReq) {
        return CursorUtils.getCursorPageByRedis(pageBaseReq, RedisKeyUtil.getKey(CHAT_HOT_ROOM_ZET), Long::parseLong);
    }
    public Set<ZSetOperations.TypedTuple<String>> getRoomRange(Double hotStart, Double hotEnd) {
        return RedisStaticUtil.zRangeByScoreWithScores(CHAT_HOT_ROOM_ZET, hotStart, hotEnd);
    }

    /**
     * 更新热门群聊的最新时间
     */
    public void refreshActiveTime(Long roomId, Date refreshTime) {
        RedisStaticUtil.zAdd(CHAT_HOT_ROOM_ZET, roomId, (double) refreshTime.getTime());
    }
}
