package com.jiwu.api.common.main.mapper.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiwu.api.common.main.pojo.chat.ChatGroupMember;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMember>, MPJBaseMapper<ChatGroupMember> {

    int updateOrReFlush(@Param("groupId") Long groupId, @Param("userId") String userId, @Param("role") Integer role);
}