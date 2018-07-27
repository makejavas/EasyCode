package com.sjhy.plugin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局配置实体类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/27 13:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig {
    /**
     * 名称
     */
    private String name;
    /**
     * 值
     */
    private String value;
}
