package com.sjhy.plugin.entity;

import com.intellij.database.model.DasColumn;

public class ColumnInfo {
    //原对象
    private DasColumn obj;
    //名称
    private String name;
    //注释
    private String comment;
    //类型
    private String type;

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
}
