package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatContact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ChatContactMapper extends BaseMapper<ChatContact> {
    int refreshOrCreateActiveTime(@Param("roomId") Long roomId, @Param("memberUidList") List<String> memberUidList, @Param("msgId") Long msgId, @Param("activeTime") Date activeTime);

    int refreshOrCreateReadTime(@Param("roomId") Long roomId, @Param("memberUidList") List<String> memberUidList, @Param("readTime") Date readTime);
}
