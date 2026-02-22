package com.jiwu.api.chat.common.utils;

import com.jiwu.api.common.main.enums.chat.MessageTypeEnum;
import com.jiwu.api.common.main.pojo.chat.ChatMessage;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

import static com.jiwu.api.common.main.enums.chat.MessageTypeEnum.OSS_MSG_FILE_TYPES;

@Component
public class OssCheckUtil {

    @Resource
    private OssFileUtil ossFileUtil;

    /**
     * 是否能够消息删除文件
     */
    public boolean checkAndDeleteOssFile(ChatMessage msg) {
        if (!Arrays.asList(OSS_MSG_FILE_TYPES).contains(msg.getType())) {
            return true;
        }
        String deleteKey = null;
        if (Objects.equals(msg.getType(), MessageTypeEnum.IMG.getType())) {
            deleteKey = msg.getExtra().getImgMsgDTO().getUrl();
        } else if (Objects.equals(msg.getType(), MessageTypeEnum.FILE.getType())) {
            deleteKey = msg.getExtra().getFileMsgDTO().getUrl();
        } else if (Objects.equals(msg.getType(), MessageTypeEnum.SOUND.getType())) {
            deleteKey = msg.getExtra().getSoundMsgDTO().getUrl();
        } else if (Objects.equals(msg.getType(), MessageTypeEnum.VIDEO.getType())) {
            deleteKey = msg.getExtra().getVideoMsgDTO().getUrl();
        }
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(deleteKey)) {
            return ossFileUtil.deleteFile(deleteKey);
        }
        return true;
    }

}
