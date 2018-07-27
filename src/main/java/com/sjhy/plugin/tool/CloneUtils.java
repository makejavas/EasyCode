package com.sjhy.plugin.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjhy.plugin.entity.AbstractGroup;

import java.io.IOException;
import java.util.*;

/**
 * 克隆工具类，实现原理通过JSON序列化反序列化实现
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class CloneUtils {
    private volatile static CloneUtils cloneUtils;

    /**
     * 单例模式
     */
    public static CloneUtils getInstance() {
        if (cloneUtils == null) {
            synchronized (CloneUtils.class) {
                if (cloneUtils == null) {
                    cloneUtils = new CloneUtils();
                }
            }
        }
        return cloneUtils;
    }

    private CloneUtils() {
    }

    /**
     * JSON处理工具类
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 实体对象克隆方法
     *
     * @param entity 实体对象
     * @return 克隆后的实体对象
     */
    public <E> E clone(E entity) {
        if (entity == null) {
            return null;
        }
        try {
            //noinspection unchecked
            return objectMapper.readValue(objectMapper.writeValueAsString(entity), (Class<E>) entity.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * List集合克隆方法
     *
     * @param src 源数据
     * @param <E> 实体类型
     * @return 克隆结果
     */
    public <E> List<E> cloneList(List<E> src) {
        if (src == null) {
            return null;
        }
        List<E> result = new ArrayList<>(src.size());
        src.forEach(e -> result.add(this.clone(e)));
        return result;
    }

    /**
     * Map集合克隆方法
     *
     * @param src 源数据
     * @param <K> 键类型
     * @param <E> 值类型
     * @return 克隆结果
     */
    public <K, E> Map<K, E> cloneMap(Map<K, E> src) {
        if (src == null) {
            return null;
        }
        Map<K, E> result;
        if (src instanceof LinkedHashMap) {
            result = new LinkedHashMap<>(src.size());
        } else {
            result = new HashMap<>(src.size());
        }
        src.forEach((k, e) -> result.put(k, this.clone(e)));
        return result;
    }
}
