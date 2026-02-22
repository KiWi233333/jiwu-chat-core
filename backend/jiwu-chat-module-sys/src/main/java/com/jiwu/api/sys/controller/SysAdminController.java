package com.jiwu.api.sys.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.sys.common.vo.RedisInfoVO;
import com.jiwu.api.common.constant.JwtConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员模块/系统管理
 * # 系统管理
 *
 * @className: SysAdminController
 * @author: Kiwi23333
 * @description: 系统管理
 * @date: 2023/8/25 21:13
 */
@Slf4j
@Tag(name = "系统管理", description = "管理员模块/系统管理")
@RestController
@RequestMapping("/admin/sys")
public class SysAdminController {

    RedisUtil<String, Integer> redisUtil;

    @Autowired
    public SysAdminController(RedisUtil<String, Integer> redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Operation(summary = "Redis清空缓存", tags = {"系统模块"})
    @DeleteMapping(value = "/redis/all")
    @ReqPermission(name = "Redis清空缓存(管理员)", intro = "Redis清空缓存(管理员),谨慎使用", expression = "admin:sys:redis:all:del")
    Result<Long> clearRedisCache(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        return Result.ok("删除成功!", redisUtil.flushDbNoAuth());
    }

    @Operation(summary = "Redis键集合", tags = {"系统模块"})
    @GetMapping(value = "/redis/list")
    @ReqPermission(name = "Redis键集合(管理员)", intro = "Redis键集合(管理员),谨慎使用", expression = "admin:sys:redis:keys:view")
    Result<List<RedisInfoVO<Object>>> getRedisKeys(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @RequestParam(required = false, name = "pattern") String pattern) {
        ArrayList<String> list = new ArrayList<>(redisUtil.keys(StringUtils.isNotBlank(pattern) ? pattern : "*"));
        list.sort(String::compareTo);
        return Result.ok("查询成功!", list.stream().map(key -> {
                    RedisInfoVO<Object> res = new RedisInfoVO<Object>()
                            .setKey(key)
                            .setExpire(redisUtil.getExpire(key));
                    try {
                        Object obj = redisUtil.get(key);
                        res.setValue(obj);
                    } catch (Exception e) {
                        return res;
                    }
                    return res;
                }
        ).collect(Collectors.toList()));
    }

    @Operation(summary = "Redis删除key", tags = {"系统模块"})
    @DeleteMapping(value = "/redis/keys")
    @ReqPermission(name = "Redis删除key(管理员)", intro = "Redis删除key缓存(管理员),谨慎使用", expression = "admin:sys:redis:keys:del")
    Result<Long> delRedisByKeys(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, @RequestParam Set<String> keys) {
        long len = redisUtil.delete(keys);
        return Result.ok(keys.size() != len ? "部分未能删除!" : "删除成功!", len);
    }
}
