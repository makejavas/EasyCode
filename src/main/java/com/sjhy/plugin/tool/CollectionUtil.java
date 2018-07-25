package com.sjhy.plugin.tool;

import java.util.Collection;

/**
 * 集合工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/25 10:24
 */
public class CollectionUtil {

    /**
     * 判断集合是否为空的
     *
     * @param collection 集合对象
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
