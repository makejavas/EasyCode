package com.sjhy.plugin.entity;

import com.sjhy.plugin.comm.CommClone;

import java.util.List;

/**
 * 抽象分组类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public abstract class AbstractGroup<T extends CommClone, E extends CommClone> extends CommClone<T> {
    /**
     * 组名
     */
    private String name;
    /**
     * 组元素
     */
    private List<E> elementList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<E> getElementList() {
        return elementList;
    }

    public void setElementList(List<E> elementList) {
        this.elementList = elementList;
    }


    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        AbstractGroup group = (AbstractGroup) super.clone();
        group.elementList = cloneUtils.cloneList(this.elementList);
        return (T) group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractGroup that = (AbstractGroup) o;

        return name.equals(that.name) && elementList.equals(that.elementList);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + elementList.hashCode();
        return result;
    }
}
