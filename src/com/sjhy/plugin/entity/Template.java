package com.sjhy.plugin.entity;

import com.sjhy.plugin.comm.CommClone;

public class Template extends CommClone<Template> {
    //模板名称
    private String name;
    //模板代码
    private String code;

    public Template() {
    }

    public Template(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        return name.equals(template.name) && code.equals(template.code);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }
}
