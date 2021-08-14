package com.sjhy.plugin.dto;

import lombok.Data;

import java.util.Map;

/**
 * 列信息传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 17:29
 */
@Data
public class ColumnInfoDTO {
    /**
     * 名称
     */
    private String name;
    /**
     * 注释
     */
    private String comment;
    /**
     * 全类型
     */
    private String type;
    /**
     * 标记是否为自定义附加列
     */
    private Boolean custom;
    /**
     * 扩展数据
     */
    private Map<String, Object> ext;
}
