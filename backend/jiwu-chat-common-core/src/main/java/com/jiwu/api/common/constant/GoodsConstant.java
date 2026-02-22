package com.jiwu.api.common.constant;

/**
 * 商品相关常量
 * @className: GoodsConstant
 * @author: Kiwi23333
 * @description: 商品相关常量
 * @date: 2023/5/1 17:22
 */
public class GoodsConstant {
    /** 商品信息 **/
    public static final String GOODS_INFO_MAPS = "goods:info:maps:";// 商品详细信息
    public static final String GOODS_VIEWS_MAPS = "goods:views:maps:";// 商品浏览量
    public static final String GOODS_VIEWS_USER_MAPS = "goods:views:users:";// 商品浏览量

    /** 商品分类 **/
    public static final String GOODS_CATEGORY_LIST = "goods:category:list:";// 全部分类信息
    public static final String GOODS_CATEGORY_TREE = "goods:category:tree:";// 全部分类信息
    public static final String GOODS_CATEGORY_GOODS = "goods:category:goods:";// 商品的分类信息

    /** 商品点赞/收藏 **/
    public static final String GOODS_ACTION_COLLECT_LIST = "goods:action:collect:list:";//
    public static final String GOODS_ACTION_TIP_LIST = "goods:action:collect:list:";//
    public static final Integer GOODS_ACTION_COLLECT_TYPE = 1;//
    public static final Integer GOODS_ACTION_TIP_TYPE = 0;//
}
