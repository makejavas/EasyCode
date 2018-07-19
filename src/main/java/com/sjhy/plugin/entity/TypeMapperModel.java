package com.sjhy.plugin.entity;

import com.sjhy.plugin.comm.AbstractTableModel;

/**
 * 类型隐射模型
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class TypeMapperModel extends AbstractTableModel<TypeMapper> {
    @Override
    protected String[] initColumnName() {
        return new String[]{"columnType", "javaType"};
    }

    @Override
    protected Object[] toObj(TypeMapper entity) {
        return new Object[]{entity.getColumnType(), entity.getJavaType()};
    }

    @Override
    protected void setVal(TypeMapper obj, int columnIndex, Object val) {
        if (columnIndex==0){
            obj.setColumnType((String) val);
        }else{
            obj.setJavaType((String) val);
        }
    }
}
