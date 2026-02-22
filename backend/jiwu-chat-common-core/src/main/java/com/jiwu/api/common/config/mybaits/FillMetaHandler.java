package com.jiwu.api.common.config.mybaits;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * mybatis plus字段自动填充
 *
 * @className: FillMeteHandler
 * @author: Kiwi23333
 * @description: mybatis plus字段自动填充
 * @date: 2023/4/30 3:26
 */
@Component
public class FillMetaHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入操作时候自动填充
        Date date = new Date();
        this.setFieldValByName("updateTime", date, metaObject);
        this.setFieldValByName("createTime", date, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作时自动填充
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

}
