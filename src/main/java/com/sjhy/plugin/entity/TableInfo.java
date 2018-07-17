package com.sjhy.plugin.entity;

import com.intellij.database.psi.DbTable;

import java.util.List;

public class TableInfo {
    //原对象
    private DbTable obj;
    //表名
    private String name;
    //注释
    private String comment;
    //所有列
    private List<ColumnInfo> fullColumn;
    //主键列
    private List<ColumnInfo> pkColumn;
    //其他列
    private List<ColumnInfo> otherColumn;

    public DbTable getObj() {
        return obj;
    }

    public void setObj(DbTable obj) {
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

    public List<ColumnInfo> getFullColumn() {
        return fullColumn;
    }

    public void setFullColumn(List<ColumnInfo> fullColumn) {
        this.fullColumn = fullColumn;
    }

    public List<ColumnInfo> getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(List<ColumnInfo> pkColumn) {
        this.pkColumn = pkColumn;
    }

    public List<ColumnInfo> getOtherColumn() {
        return otherColumn;
    }

    public void setOtherColumn(List<ColumnInfo> otherColumn) {
        this.otherColumn = otherColumn;
    }
}
