package com.jiwu.api.common.main.pojo.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_salt")
public class UserSalt {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private String userId;

    @TableField("salt")
    private String salt;
}