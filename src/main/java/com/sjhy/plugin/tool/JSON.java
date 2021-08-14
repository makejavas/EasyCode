package com.sjhy.plugin.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * json工具
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 10:37
 */
public class JSON {

    private static final ObjectMapper INSTANCE;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        INSTANCE = new ObjectMapper();
        // 禁止将日期序列化成时间戳
        INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 禁止属性不存在时报错
        INSTANCE.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 允许字符串转换成数组
        INSTANCE.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // 读取到未知的枚举值转换成空
        INSTANCE.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        // 禁用科学计数法
        INSTANCE.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        INSTANCE.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        // 序列化忽略空值
        INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 配置时间格式
        INSTANCE.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    /**
     * 将json字符串转换成java对象
     *
     * @param json json字符串
     * @param cls  java对象类型
     * @param <T>  对象类型
     * @return 对象
     */
    public static <T> T parse(String json, Class<T> cls) {
        try {
            return INSTANCE.readValue(json, cls);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 将json字符串转换成java对象
     *
     * @param json json字符串
     * @param type java对象类型
     * @param <T>  对象类型
     * @return 对象
     */
    public static <T> T parse(String json, TypeReference<T> type) {
        try {
            return INSTANCE.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 将json对象转换成json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        try {
            return INSTANCE.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 将json对象转换成json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String toJsonByFormat(Object obj) {
        try {
            return INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode readTree(Object obj) {
        try {
            if (obj instanceof String) {
                return INSTANCE.readTree((String) obj);
            } else if (obj instanceof byte[]) {
                return INSTANCE.readTree((byte[]) obj);
            } else if (obj instanceof InputStream) {
                return INSTANCE.readTree((InputStream) obj);
            } else if (obj instanceof URL) {
                return INSTANCE.readTree((URL) obj);
            } else if (obj instanceof File) {
                return INSTANCE.readTree((File) obj);
            } else {
                // 其他对象，转字符串再转JsonNode
                return INSTANCE.readTree(toJson(obj));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
