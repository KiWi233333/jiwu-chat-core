package com.jiwu.api.common.constant;

import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.util.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 社区相关常量
 *
 * @className: GoodsConstant
 * @author: Kiwi23333
 * @description: 社区相关常量
 * @date: 2023/5/1 17:22
 */
@Component
public class CommConstant {

    @Autowired
    RedisUtil redisUtil;
    /**
     * 社区帖子
     **/
    public static final String COMM_POST_PAGE_MAPS = "comment:post:page:maps:";// 社区帖子分页 p+z+dto
    public static final String COMM_POST_INFO_MAPS = "comment:post:info:maps:";// 社区帖子信息


    public boolean delAllPostPageMaps() {
        return redisUtil.delete(COMM_POST_PAGE_MAPS);
    }

    /**
     * 社区分类
     **/
    public static final String COMM_CATEGORY_LIST = "comment:category:list:";//
    public static final String COMM_CATEGORY_TREE= "comment:category:tree:";//

    public boolean delCategoryList() {
        redisUtil.delete(COMM_CATEGORY_TREE + UserType.ADMIN.getCode());
        return redisUtil.delete(COMM_CATEGORY_TREE + UserType.CUSTOMER.getCode());
    }

    public boolean delCategoryList(UserType userType) {
        return redisUtil.delete(COMM_CATEGORY_LIST + userType.getCode());
    }

    /**
     * 社区收藏
     **/
    public static final String COMM_ACTION_LIST = "comment:action:list:";//

    public static final String COMM_POST_COMMENT_MAPS= "comment:post:comment:maps:";// 社区帖子评论+帖子id

}
