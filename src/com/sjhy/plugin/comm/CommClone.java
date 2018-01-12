package com.sjhy.plugin.comm;

import com.sjhy.plugin.tool.CloneUtils;

public abstract class CommClone<T> implements Cloneable {
    //克隆工具
    protected CloneUtils cloneUtils = CloneUtils.getInstance();
    @SuppressWarnings({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}