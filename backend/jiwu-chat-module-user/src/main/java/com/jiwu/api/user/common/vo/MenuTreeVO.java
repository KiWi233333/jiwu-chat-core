package com.jiwu.api.user.common.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.jiwu.api.common.main.pojo.sys.Menu;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * 菜单列表VO
 *
 * @className: Permission
 * @author: Kiwi23333
 * @description: 菜单列表VO
 * @date: 2023/5/2 13:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MenuTreeVO {


    @Schema(description = "id")
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "菜单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "父节点")
    private String parentId;

    @Schema(description = "节点类型：（1页面，2按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "用户类型（0前台，1管理...）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sysType;

    @Schema(description = "页面对应的地址")
    private String linkUrl;

    @Schema(description = "排序")
    private Integer sortOrder;



    @Schema(description = "组件地址")
    private String componentPath;

    @Schema(description = "图标样式")
    private String icon;

    @Schema(description = "激活图标样式")
    private String onIcon;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 子角色
     */
    private List<MenuTreeVO> children;


    public static List<MenuTreeVO> buildTree(List<MenuTreeVO> list) {
        Map<String, MenuTreeVO> nodeMap = new HashMap<>();
        List<MenuTreeVO> roots = new ArrayList<>();

        // 构建节点映射表
        for (MenuTreeVO p : list) {
            nodeMap.put(p.getId(), p);
        }
        // 构建树结构
        for (MenuTreeVO node : list) {
            String parentId = node.getParentId();
            if (StringUtil.isNullOrEmpty(parentId)) {
                roots.add(node);
            } else {
                MenuTreeVO parent = nodeMap.get(parentId);
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

    public static MenuTreeVO toMenuTreeVO(Menu p) {
        return new MenuTreeVO()
                .setId(p.getId())
                .setSortOrder(p.getSortOrder())
                .setSysType(p.getSysType())
                .setType(p.getType())
                .setLinkUrl(p.getLinkUrl())
                .setName(p.getName())
                .setParentId(p.getParentId())
                .setCode(p.getCode())
                .setComponentPath(p.getComponentPath())
                .setIcon(p.getIcon())
                .setOnIcon(p.getOnIcon())
                .setCreateTime(p.getCreateTime())
                .setUpdateTime(p.getUpdateTime());
    }
}
