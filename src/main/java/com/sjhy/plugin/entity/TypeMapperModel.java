package com.sjhy.plugin.entity;

import com.intellij.util.ui.EditableModel;
import com.sjhy.plugin.comm.AbstractTableModel;
import com.sjhy.plugin.enums.MatchType;

/**
 * 类型隐射模型
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class TypeMapperModel extends AbstractTableModel<TypeMapper> implements EditableModel {
    @Override
    protected String[] initColumnName() {
        return new String[]{"matchType", "columnType", "javaType"};
    }

    @Override
    protected Object[] toObj(TypeMapper entity) {
        return new Object[]{entity.getMatchType() != null ? entity.getMatchType().name() : MatchType.REGEX.name(), entity.getColumnType(), entity.getJavaType()};
    }

    @Override
    protected void setVal(TypeMapper obj, int columnIndex, Object val) {
        if (columnIndex == 0) {
            obj.setMatchType(MatchType.valueOf((String) val));
        } else if (columnIndex == 1) {
            obj.setColumnType((String) val);
        } else {
            obj.setJavaType((String) val);
        }
    }

    @Override
    public void addRow() {
        addRow(new TypeMapper("demo", "java.lang.String"));
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {

    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return false;
    }
}
