package com.sjhy.plugin.entity;

import java.util.ArrayList;
import java.util.List;

public class TemplateGroup implements Cloneable {
    //模板组名称
    private String name;
    //模板集合
    private List<Template> templateList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<Template> templateList) {
        this.templateList = templateList;
    }

    public TemplateGroup cloneTemplateGroup() {
        try {
            return (TemplateGroup) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        TemplateGroup templateGroup = (TemplateGroup) super.clone();
        templateGroup.templateList = new ArrayList<>();
        this.templateList.forEach(template -> templateGroup.templateList.add(template.cloneTemplate()));
        return templateGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateGroup that = (TemplateGroup) o;

        return name.equals(that.name) && templateList.equals(that.templateList);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + templateList.hashCode();
        return result;
    }
}
