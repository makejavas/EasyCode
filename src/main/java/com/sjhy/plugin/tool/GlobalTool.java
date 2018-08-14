package com.sjhy.plugin.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 全局工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/14 18:11
 */
public class GlobalTool extends NameUtils {
    private static volatile GlobalTool globalTool;

    /**
     * 私有构造方法
     */
    private GlobalTool() {
        objectMapper = new ObjectMapper();
        // 设置空对象不要抛异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * 单例模式
     */
    public static GlobalTool getInstance() {
        if (globalTool == null) {
            synchronized (GlobalTool.class) {
                if (globalTool == null) {
                    globalTool = new GlobalTool();
                }
            }
        }
        return globalTool;
    }

    /**
     * jackson格式化工具
     */
    private ObjectMapper objectMapper;

    /**
     * 创建集合
     *
     * @param items 初始元素
     * @return 集合对象
     */
    public Set<?> newHashSet(Object... items) {
        return items == null ? new HashSet<>() : new HashSet<>(Arrays.asList(items));
    }

    /**
     * 创建列表
     *
     * @param items 初始元素
     * @return 列表对象
     */
    public List<?> newArrayList(Object... items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(items));
    }

    /**
     * 创建有序Map
     *
     * @return map对象
     */
    public Map<?, ?> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * 创建无须Map
     *
     * @return map对象
     */
    public Map<?, ?> newHashMap() {
        return new HashMap<>(16);
    }

    /**
     * 获取字段，私有属性一样强制访问
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getField(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> cls = obj.getClass();
        return ReflectionUtil.getField(cls, obj, Object.class, fieldName);
    }

    /**
     * 获取某个类的所有字段
     *
     * @param cls 类
     * @return 所有字段
     */
    private List<Field> getAllFieldByClass(Class<?> cls) {
        List<Field> result = new ArrayList<>();
        do {
            result.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));
        return result;
    }

    /**
     * 调式对象
     *
     * @param obj 对象
     * @return 调式JSON结果
     */
    public String debug(Object obj) {
        if (obj == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder("\n/*调试信息：\nField Item List:\n");
        Class<?> cls = obj.getClass();
        List<Field> fieldList = getAllFieldByClass(cls);
        fieldList.forEach(field -> {
            field.setAccessible(true);
            builder.append("field=");
            builder.append(field.getName());
            builder.append(",\t\ttype=");
            builder.append(field.getType());
            builder.append(",\t\tvalue=");
            try {
                builder.append(field.get(obj));
            } catch (IllegalAccessException e) {
                ExceptionUtil.rethrow(e);
            }
            builder.append("\n");
        });
        // 方法列表
        builder.append("\nMethod List:\n");
        // 排除方法
        List<String> fillterMethodName = Arrays.asList("hashCode", "toString", "equals");
        for (Method method : cls.getDeclaredMethods()) {
            if (fillterMethodName.contains(method.getName())) {
                continue;
            }
            builder.append(method.getName());
            builder.append("=");
            builder.append(method.toGenericString());
            builder.append("\n");
        }
        builder.append("*/\n");
        return builder.toString();
    }
}
