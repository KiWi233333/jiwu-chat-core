package com.jiwu.api.chat.common.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import com.jiwu.api.chat.service.adapter.ChatMessageAdapter;
import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.main.mapper.chat.ChatMessageMapper;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.main.dto.chat.cursor.MsgContentConstant;
import com.jiwu.api.common.main.dto.chat.msg.ChatMessageDTO;
import com.jiwu.api.common.util.common.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * Description: 消息处理器抽象类
 * Date: 2023-06-04
 */
public abstract class AbstractMsgHandler<DTO> {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    private Class<DTO> bodyClass;

    @PostConstruct
    private void init() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.bodyClass = (Class<DTO>) genericSuperclass.getActualTypeArguments()[0];
        MsgHandlerFactory.register(getMsgTypeEnum(), this);
    }

    /**
     * 消息类型
     */
    protected abstract MessageTypeEnum getMsgTypeEnum();

    @Transactional
    public Long checkAndSaveMsg(ChatMessageDTO dto, String uid) {
        DTO body = this.toBean(dto.getBody());
        // 0、参数筛选和过滤
        sharkingAndCheck(dto);
        // 1、统一校验
        AssertUtil.allCheckValidateThrow(body);
        // 2、子类扩展信息校验（可扩展校验）
        checkMsg(body, dto.getRoomId(), uid);
        // 3、构建消息扩展体
        ChatMessage insert = ChatMessageAdapter.buildMsgSave(dto, uid);// 构建实体类
        // 4、统一保存
        chatMessageMapper.insert(insert);
        // 5、子类扩展保存（主信息）(策略)
        saveMsg(insert, body);
        return insert.getId();
    }

    /**
     * 处理消息
     *
     * @param dto 消息体
     * @return 返回处理后的消息体
     */
    private ChatMessageDTO sharkingAndCheck(ChatMessageDTO dto) {
        // 内容去空
        dto.setContent(Optional.ofNullable(dto.getContent())
                .map(String::trim)
                .orElse(null));
        MsgContentConstant.checkContentLen(dto);
        return dto;
    }

    // 转化为bean
    private DTO toBean(Object body) {
        if (bodyClass.isAssignableFrom(body.getClass())) {
            return (DTO) body;
        }
        return BeanUtil.toBean(body, bodyClass);
    }

    /**
     * 校验消息
     *
     * @param body   参数
     * @param roomId 房间号
     * @param uid    用户id
     */
    protected void checkMsg(DTO body, Long roomId, String uid) {

    }

    /**
     * 保存消息
     *
     * @param body    参数
     * @param message 消息
     */
    protected abstract void saveMsg(ChatMessage message, DTO body);


    /**
     * 展示消息
     */
    public abstract Object showMsg(ChatMessage msg);

    /**
     * 被回复时——展示的消息
     */
    public abstract Object showReplyMsg(ChatMessage msg);

    /**
     * 会话列表——展示的消息
     */
    public abstract String showContactMsg(ChatMessage msg);

}
