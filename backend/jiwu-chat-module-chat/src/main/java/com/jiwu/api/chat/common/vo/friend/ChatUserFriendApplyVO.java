package com.jiwu.api.chat.common.vo.friend;

import com.jiwu.api.common.main.enums.user.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;


/**
 * Description: 好友校验
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserFriendApplyVO {
    @Schema(description = "申请id")
    private Long applyId;

    @Schema(description = "申请人uid")
    private String userId;

    @Schema(description = "申请类型 1加好友")
    private Integer type;

    @Schema(description = "申请信息")
    private String msg;

    @Schema(description = "申请状态（0-待审批，1-同意）")
    private Integer status;

    @Schema(description = "发起时间", example = "2023-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "用户信息")
    private User user;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        @Schema(description = "用户uid")
        private String id;
        @Schema(description = "用户名称")
        private String nickName;
        @Schema(description = "头像")
        private String avatar;
        @Schema(description = "用户性别")
        private Gender gender;
        @Schema(description = "个性签名")
        private String slogan;
    }
}
