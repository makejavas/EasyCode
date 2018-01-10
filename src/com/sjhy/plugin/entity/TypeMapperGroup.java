package com.sjhy.plugin.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TypeMapperGroup implements Cloneable, Serializable {
    private String name;
    private List<TypeMapper> typeMapperList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TypeMapper> getTypeMapperList() {
        return typeMapperList;
    }

    public void setTypeMapperList(List<TypeMapper> typeMapperList) {
        this.typeMapperList = typeMapperList;
    }

    public TypeMapperGroup cloneTypeMapperGroup() {
        try {
            return (TypeMapperGroup) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        TypeMapperGroup typeMapperGroup = (TypeMapperGroup) super.clone();
        typeMapperGroup.typeMapperList = new ArrayList<>();
        this.typeMapperList.forEach(typeMapper -> typeMapperGroup.typeMapperList.add(typeMapper.cloneTypeMapper()));
        return typeMapperGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeMapperGroup that = (TypeMapperGroup) o;

        return name.equals(that.name) && typeMapperList.equals(that.typeMapperList);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeMapperList.hashCode();
        return result;
    }
}
