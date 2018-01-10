package com.sjhy.plugin.entity;

import java.io.Serializable;

public class TypeMapper implements Cloneable, Serializable {
    //列类型
    private String columnType;
    //java类型
    private String javaType;

    public TypeMapper() {
    }

    public TypeMapper(String columnType, String javaType) {
        this.columnType = columnType;
        this.javaType = javaType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeMapper that = (TypeMapper) o;

        return columnType.equals(that.columnType) && javaType.equals(that.javaType);
    }

    @Override
    public int hashCode() {
        int result = columnType.hashCode();
        result = 31 * result + javaType.hashCode();
        return result;
    }
}
