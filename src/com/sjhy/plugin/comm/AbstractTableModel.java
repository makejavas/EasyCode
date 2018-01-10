package com.sjhy.plugin.comm;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public abstract class AbstractTableModel<T> extends DefaultTableModel {
    //数据
    private List<T> data;

    public AbstractTableModel() {
        for (String columnName : initColumnName()) {
            super.addColumn(columnName);
        }
    }

    public void init(List<T> data) {
        if (data==null){
            return;
        }
        this.data = data;
        removeAllRow();
        data.forEach(item-> super.addRow(toObj(item)));
    }


    /**
     * 移除所有行
     */
    private void removeAllRow() {
        int rowCount = getRowCount();
        if (rowCount>0){
            for (int i = 0; i< rowCount; i++) {
                super.removeRow(0);
            }
        }
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        this.data.remove(row);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        T obj = data.get(row);
        setVal(obj, column, aValue);
    }

    public void addRow(T entity) {
        super.addRow(toObj(entity));
        this.data.add(entity);
    }

    /**
     * 抽象初始化列名
     * @return 列名
     */
    protected abstract String[] initColumnName();

    protected abstract Object[] toObj(T entity);

    protected abstract void setVal(T obj, int columnIndex, Object val);
}
