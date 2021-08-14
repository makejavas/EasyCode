package com.sjhy.plugin.tool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 克隆工具类，实现原理通过JSON序列化反序列化实现
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@SuppressWarnings("unchecked")
public final class CloneUtils {
    /**
     * 禁用构造方法
     */
    private CloneUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过JSON序列化方式进行克隆，默认不copy忽略对象
     *
     * @param entity        实例对象
     * @param typeReference 返回类型
     * @return 克隆后的实体对象
     */
    public static <E, T extends E> E cloneByJson(E entity, TypeReference<T> typeReference) {
        return cloneByJson(entity, typeReference, false);
    }

    /**
     * 通过JSON序列化方式进行克隆
     *
     * @param entity 实例对象
     * @return 克隆后的实体对象
     */
    public static <E> E cloneByJson(E entity) {
        return cloneByJson(entity, false);
    }

    /**
     * 通过JSON序列化方式进行克隆
     *
     * @param entity 实例对象
     * @param copy   是否复制被忽略的属性
     * @return 克隆后的实体对象
     */
    public static <E> E cloneByJson(E entity, boolean copy) {
        return cloneByJson(entity, null, copy);
    }

    /**
     * 通过JSON序列化方式进行克隆
     *
     * @param entity        实例对象
     * @param copy          是否复制被忽略的属性
     * @param typeReference 返回类型
     * @return 克隆后的实体对象
     */
    public static <E, T extends E> E cloneByJson(E entity, TypeReference<T> typeReference, boolean copy) {
        if (entity == null) {
            return null;
        }
        // 进行序列化
        String json = JSON.toJson(entity);
        // 进行反序列化
        E result;
        if (typeReference == null) {
            result = (E) JSON.parse(json, entity.getClass());
        } else {
            result = JSON.parse(json, typeReference);
        }
        // 复制被忽略的属性
        if (copy) {
            copyIgnoreProp(entity, result);
            // 针对TableInfo对象做特殊处理
            if (entity instanceof TableInfo) {
                handlerTableInfo((TableInfo) entity, (TableInfo) result);
            }
        }
        return result;
    }

    /**
     * 对TableInfo做特殊处理
     *
     * @param oldEntity 旧实体对象
     * @param newEntity 新实体对象
     */
    private static void handlerTableInfo(TableInfo oldEntity, TableInfo newEntity) {
        List<ColumnInfo> oldColumnInfoList = oldEntity.getFullColumn();
        List<ColumnInfo> newColumnInfoList = newEntity.getFullColumn();
        if (CollectionUtil.isEmpty(oldColumnInfoList) || CollectionUtil.isEmpty(newColumnInfoList)) {
            return;
        }
        // 进行处理
        for (ColumnInfo oldColumnInfo : oldColumnInfoList) {
            for (ColumnInfo newColumnInfo : newColumnInfoList) {
                // 对相同的列信息忽略复制
                if (Objects.equals(oldColumnInfo.getName(), newColumnInfo.getName())) {
                    copyIgnoreProp(oldColumnInfo, newColumnInfo);
                    break;
                }
            }
        }
    }

    /**
     * 复制属性
     *
     * @param oldEntity 就实体
     * @param newEntity 新实例
     */
    private static void copyIgnoreProp(Object oldEntity, Object newEntity) {
        // 类型不一样直接返回
        if (!Objects.equals(oldEntity.getClass(), newEntity.getClass())) {
            return;
        }
        // 获取所有字段
        List<Field> fieldList = ReflectionUtil.collectFields(oldEntity.getClass());
        if (CollectionUtil.isEmpty(fieldList)) {
            return;
        }
        fieldList.forEach(field -> {
            if (field.getAnnotation(JsonIgnore.class) != null) {
                // 设置允许访问
                field.setAccessible(true);
                // 复制字段
                try {
                    field.set(newEntity, field.get(oldEntity));
                } catch (IllegalAccessException e) {
                    ExceptionUtil.rethrow(e);
                }
            }
        });
    }
}
