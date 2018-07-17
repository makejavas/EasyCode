package com.sjhy.plugin.entity;

import com.sjhy.plugin.comm.CommClone;

public class ColumnConfig extends CommClone<ColumnConfig> {
    private String title;
    private ColumnConfigType type;
    private String selectValue;

    public ColumnConfig() {
    }

    public ColumnConfig(String title, ColumnConfigType type) {
        this.title = title;
        this.type = type;
    }

    public ColumnConfig(String title, ColumnConfigType type, String selectValue) {
        this.title = title;
        this.type = type;
        this.selectValue = selectValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ColumnConfigType getType() {
        return type;
    }

    public void setType(ColumnConfigType type) {
        this.type = type;
    }

    public String getSelectValue() {
        return selectValue;
    }

    public void setSelectValue(String selectValue) {
        this.selectValue = selectValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnConfig that = (ColumnConfig) o;

        return title.equals(that.title) && type == that.type && (selectValue != null ? selectValue.equals(that.selectValue) : that.selectValue == null);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (selectValue != null ? selectValue.hashCode() : 0);
        return result;
    }
}
