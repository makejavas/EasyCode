package com.sjhy.plugin.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板信息类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
@NoArgsConstructor
public class Template {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板代码
     */
    private String code;

    public Template(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
