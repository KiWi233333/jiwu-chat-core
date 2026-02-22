package com.jiwu.api.common.util.common;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Slf4j
public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS); // 全部字段输出
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); // 设置可用单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); // 设置字段可以不用双引号包括
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 时间格式化

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 去掉默认的时间戳格式
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置为东八区
        objectMapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // 反序列化时，属性不存在的兼容处理
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 序列化日期时以timestamps输出，默认true
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true); // 序列化枚举是以toString()来输出，默认false，即默认以name()来输出
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false); //  序列化枚举是以ordinal()来输出，默认false
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 类为空时，不要抛异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 反序列化时,遇到未知属性时是否引起结果失败
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true); // 解析器支持解析结束符
    }

    /**
     * 将 Java 对象转为 JSON 字符串
     */
    public static <T> String toJSON(T obj) {
        String jsonStr;
        try {
            jsonStr = objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Java 转 JSON 出错！", e);
            throw new RuntimeException(e);
        }
        return jsonStr;
    }

    /**
     * 将 JSON 字符串转为 Java 对象
     */
    public static <T> T parseJSON(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        T obj;
        try {
            obj = objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("JSON 转 Java 出错！", e);
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * 将 JSON 字符串转为 Java 对象
     */
    public static <T> T parseJSON(byte[] json, Class<T> type) {
        T obj;
        try {
            obj = objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("JSON 转 Java 出错！", e);
            throw new RuntimeException(e);
        }
        return obj;
    }
}

