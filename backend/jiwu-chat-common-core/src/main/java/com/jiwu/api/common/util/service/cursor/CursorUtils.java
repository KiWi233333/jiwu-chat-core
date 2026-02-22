package com.jiwu.api.common.util.service.cursor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.jiwu.api.common.util.service.RedisStaticUtil;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 游标分页工具类
 */
public class CursorUtils {

    public static <T> CursorPageBaseVO<Pair<T, Double>> getCursorPageByRedis(CursorPageBaseDTO cursorPageBaseDTO, String redisKey, Function<String, T> typeConvert) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples;
        if (CharSequenceUtil.isBlank(cursorPageBaseDTO.getCursor())) {//第一次
            typedTuples = RedisStaticUtil.zReverseRangeWithScores(redisKey, cursorPageBaseDTO.getPageSize());
        } else {
            typedTuples = RedisStaticUtil.zReverseRangeByScoreWithScores(redisKey, Double.parseDouble(cursorPageBaseDTO.getCursor()), cursorPageBaseDTO.getPageSize());
        }
        List<Pair<T, Double>> result = typedTuples
                .stream()
                .map(t -> Pair.of(typeConvert.apply(t.getValue()), t.getScore()))
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .collect(Collectors.toList());
        String cursor = Optional.ofNullable(CollUtil.getLast(result))
                .map(Pair::getValue)
                .map(String::valueOf)
                .orElse(null);
        Boolean isLast = result.size() != cursorPageBaseDTO.getPageSize();
        return new CursorPageBaseVO<>(cursor, isLast, result);
    }

    public static <T> CursorPageBaseVO<T> getCursorPageByMysql(IService<T> mapper, CursorPageBaseDTO request, Consumer<LambdaQueryWrapper<T>> initWrapper, SFunction<T, ?> cursorColumn, Class<?> cursorType) {
        //游标字段类型
//        Class<?> cursorType = LambdaUtil.getReturnType(cursorColumn);
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        //额外条件
        initWrapper.accept(wrapper);
        //游标条件
        if (CharSequenceUtil.isNotBlank(request.getCursor())) {
            wrapper.lt(cursorColumn, parseCursor(request.getCursor(), cursorType));
        }
        //游标方向
        wrapper.orderByDesc(cursorColumn);

        Page<T> page = mapper.page(request.plusPage(), wrapper);
        //取出游标
        String cursor = Optional.ofNullable(CollUtil.getLast(page.getRecords()))
                .map(cursorColumn)
                .map(CursorUtils::toCursor)
                .orElse(null);
        //判断是否最后一页
        Boolean isLast = page.getRecords().size() != request.getPageSize() || cursor == null;
        return new CursorPageBaseVO<>(cursor, isLast, page.getRecords());
    }


    public static <T> CursorPageBaseVO<T> getCursorJoinPageByMysql(IService<T> mapper, CursorPageBaseDTO request, Consumer<MPJLambdaWrapper<T>> initWrapper, SFunction<T, ?> cursorColumn, Class<?> cursorType) {
        // 参数校验
        if (cursorType == null) {
            throw new IllegalArgumentException("cursorType 不能为空");
        }
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>();
        // 添加额外条件
        initWrapper.accept(wrapper);

        // 构建游标条件
        buildCursorCondition(wrapper, request.getCursor(), cursorColumn, cursorType);

        // 排序方式（可扩展）
        wrapper.orderByDesc(cursorColumn);

        // 查询数据
        Page<T> page = mapper.page(request.plusPage(), wrapper);

        // 提取下一页游标
        String nextCursor = Optional.ofNullable(CollUtil.getLast(page.getRecords()))
                .flatMap(record -> Optional.ofNullable(cursorColumn.apply(record)))
                .map(CursorUtils::toCursor)
                .orElse(null);

        // 判断是否最后一页（更精确的方式：查多一条）
        Boolean isLast = page.getRecords().size() != request.getPageSize() || nextCursor == null;
        return new CursorPageBaseVO<>(nextCursor, isLast, page.getRecords());
    }

    // 封装游标条件构建逻辑
    private static <T> void buildCursorCondition(MPJLambdaWrapper<T> wrapper, String cursorStr, SFunction<T, ?> cursorColumn, Class<?> cursorType) {
        if (CharSequenceUtil.isNotBlank(cursorStr)) {
            Object cursorValue = parseCursor(cursorStr, cursorType);
            wrapper.lt(cursorColumn, cursorValue);
        }
    }


    private static String toCursor(Object o) {
        if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());
        } else {
            return o.toString();
        }
    }

    private static Object parseCursor(String cursor, Class<?> cursorClass) {
        if (Date.class.isAssignableFrom(cursorClass)) {
            return new Date(Long.parseLong(cursor));
        } else {
            return cursor;
        }
    }
}
