package com.sjhy.plugin.entity;

import com.intellij.database.model.DasColumn;

import java.util.Map;

public class ColumnInfo {
    //原对象
    private DasColumn obj;
    //名称
    private String name;
    //注释
    private String comment;
    //类型
    private String type;
    //扩展数据
    private Map<String, Object> ext;
    //允许为空
    private Boolean notNull;

    public Boolean getNotNull() {
        return notNull;
    }

    public void setNotNull(Boolean notNull) {
        this.notNull = notNull;
    }

    public DasColumn getObj() {
        return obj;
    }

    public void setObj(DasColumn obj) {
        this.obj = obj;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }
}
