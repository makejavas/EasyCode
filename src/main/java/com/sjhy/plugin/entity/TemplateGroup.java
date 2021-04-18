package com.sjhy.plugin.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板分组类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
@Data
public class TemplateGroup implements AbstractGroup<Template> {
    /**
     * 分组名称
     */
    private String name;
    /**
     * 元素对象
     */
    private List<Template> elementList;

    /**
     * 模板 name 和 Template的映射
     */
    private Map<String, Template> templateMap;

    @Override
    public void setElementList(List<Template> elementList) {
        this.elementList = elementList;
    }

    public Template getTemplate(String name) {
        if (this.templateMap == null||this.templateMap.isEmpty()) {
            this.templateMap = elementList.stream().collect(Collectors.toMap(Template::getName, template -> template, (v1, v2) -> v2));
        }
        return this.templateMap.get(name);
    }

}
