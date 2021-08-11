package com.sjhy.plugin.entity;

import lombok.Data;

import java.util.List;

/**
 * 类型映射分组
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class TypeMapperGroup implements AbstractGroup<TypeMapperGroup, TypeMapper> {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 元素对象
     */
    private List<TypeMapper> elementList;
}
