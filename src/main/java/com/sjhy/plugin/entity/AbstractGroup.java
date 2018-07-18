package com.sjhy.plugin.entity;

import java.util.List;

/**
 * 抽象分组类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public interface AbstractGroup<E> {
    /**
     * 获取分组名称
     *
     * @return 分组名称
     */
    String getName();

    /**
     * 设置分组名称
     *
     * @param name 分组名称
     */
    void setName(String name);

    /**
     * 获取元素集合
     *
     * @return 元素集合
     */
    List<E> getElementList();

    /**
     * 设置元素集合
     *
     * @param elementList 元素集合
     */
    void setElementList(List<E> elementList);
}
