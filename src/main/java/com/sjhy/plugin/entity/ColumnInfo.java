package com.sjhy.plugin.entity;

import com.intellij.database.model.DasColumn;
import lombok.Data;

import java.util.Map;

/**
 * 列信息
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class ColumnInfo {
    /**
     * 原始对象
     */
    private DasColumn obj;
    /**
     * 名称
     */
    private String name;
    /**
     * 注释
     */
    private String comment;
    /**
     * 类型
     */
    private String type;
    /**
     * 扩展数据
     */
    private Map<String, Object> ext;
}
