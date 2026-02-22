package com.jiwu.api.user.common.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * 权限表
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RoleTreeVO {


    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 父id
     */
    private String parentId;
    /**
     * 创建人
     */
    private String creator;

    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色唯一CODE代码
     */
    private String code;

    /**
     * 角色介绍
     */
    private String intro;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 子角色
     */
    private List<RoleTreeVO> children;


    public static List<RoleTreeVO> buildTree(List<RoleTreeVO> list) {
        Map<String, RoleTreeVO> nodeMap = new HashMap<>();
        List<RoleTreeVO> roots = new ArrayList<>();

        // 构建节点映射表
        for (RoleTreeVO p : list) {
            nodeMap.put(p.getId(), p);
        }
        // 构建树结构
        for (RoleTreeVO node : list) {
            String parentId = node.getParentId();
            if (StringUtil.isNullOrEmpty(parentId)) {
                roots.add(node);
            } else {
                RoleTreeVO parent = nodeMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }

    public static RoleTreeVO toRoleTreeVO(Role p) {
        return new RoleTreeVO()
                .setId(p.getId())
                .setName(p.getName())
                .setParentId(p.getParentId())
                .setCreateTime(p.getCreateTime())
                .setCode(p.getCode())
                .setCreator(p.getCreator())
                .setUpdateTime(p.getUpdateTime());

    }
}
