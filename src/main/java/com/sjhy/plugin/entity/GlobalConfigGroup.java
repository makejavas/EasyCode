package com.sjhy.plugin.entity;

import lombok.Data;

import java.util.List;

/**
 * 全局配置分组
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/27 13:10
 */
@Data
public class GlobalConfigGroup implements AbstractGroup<GlobalConfigGroup, GlobalConfig> {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 元素对象集合
     */
    private List<GlobalConfig> elementList;
}
