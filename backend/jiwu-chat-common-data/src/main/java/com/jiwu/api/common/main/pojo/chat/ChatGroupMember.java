package com.jiwu.api.common.main.pojo.chat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "群聊用户组实体")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "chat_group_member")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群组id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 成员uid
     */
    @TableField("userId")
    private String userId;

    /**
     * 成员角色1群主(可撤回，可移除，可解散，可发系统通知) 2管理员(可撤回，可移除) 3普通成员
     */
    @TableField("role")
    private Integer role;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
