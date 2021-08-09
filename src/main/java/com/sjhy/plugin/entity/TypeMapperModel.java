package com.sjhy.plugin.entity;

import com.sjhy.plugin.comm.AbstractTableModel;
import com.sjhy.plugin.enums.MatchType;

/**
 * 类型隐射模型
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class TypeMapperModel extends AbstractTableModel<TypeMapper> {
    @Override
    public String[] initColumnName() {
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
    protected TypeMapper defaultVal() {
        return new TypeMapper("demo", "java.lang.String");
    }
}
