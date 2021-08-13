package com.sjhy.plugin.entity;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Template implements AbstractEditorItem<Template> {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板代码
     */
    private String code;

    @Override
    public Template defaultVal() {
        return new Template("demo", "template");
    }

    @Override
    public void changeFileName(String name) {
        this.name = name;
    }

    @Override
    public String fileName() {
        return this.name;
    }

    @Override
    public void changeFileContent(String content) {
        this.code = content;
    }

    @Override
    public String fileContent() {
        return this.code;
    }
}
