package com.sjhy.plugin.tool;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射工具
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/11 10:09
 */
public class ReflectionUtils {

    public static Class<?> getGenericClass(Object obj, int index) {
        // 获取泛型接口
        Type type = obj.getClass().getGenericInterfaces()[0];
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type targetType = parameterizedType.getActualTypeArguments()[index];
            return (Class<?>) targetType;
        } else {
            // 不存在泛型
            throw new IllegalArgumentException(obj.getClass() + " not found generic");
        }
    }

}
