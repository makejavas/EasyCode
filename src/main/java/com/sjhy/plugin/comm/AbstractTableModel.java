package com.sjhy.plugin.comm;

import com.intellij.util.ui.EditableModel;
import com.sjhy.plugin.tool.CollectionUtil;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * 抽象的表模型
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public abstract class AbstractTableModel<T> extends DefaultTableModel implements EditableModel {
    /**
     * 数据
     */
    private List<T> data;

    /**
     * 构造方法
     */
    public AbstractTableModel() {
        for (String columnName : initColumnName()) {
            super.addColumn(columnName);
        }
    }

    /**
     * 初始化方法
     *
     * @param data 数据
     */
    public void init(List<T> data) {
        if (CollectionUtil.isEmpty(data)) {
            return;
        }
        // 先移除后赋值，修复复制分组后无数据展示问题
        removeAllRow();
        this.data = data;
        data.forEach(item -> super.addRow(toObj(item)));
    }


    /**
     * 移除所有行
     */
    private void removeAllRow() {
        int rowCount = getRowCount();
        if (rowCount > 0) {
            for (int i = 0; i < rowCount; i++) {
                // 只移除行数据，不移除储存数据，修复切换分组数据自动清空BUG
                super.removeRow(0);
            }
        }
    }

    /**
     * 移除指定行数据
     *
     * @param row 行号
     */
    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        this.data.remove(row);
    }

    /**
     * 设置值到指定行指定列
     *
     * @param aValue 值
     * @param row    行号
     * @param column 列号
     */
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        T obj = data.get(row);
        setVal(obj, column, aValue);
    }

    /**
     * 添加一行数据
     *
     * @param entity 实体数据
     */
    public void addRow(T entity) {
        super.addRow(toObj(entity));
        this.data.add(entity);
    }

    /**
     * 抽象初始化列名
     *
     * @return 列名
     */
    protected abstract String[] initColumnName();

    /**
     * 实体类转数据数组
     *
     * @param entity 实体类
     * @return 数据数组
     */
    protected abstract Object[] toObj(T entity);

    /**
     * 设置实体类的值
     *
     * @param obj         实体类
     * @param columnIndex 列索引
     * @param val         值
     */
    protected abstract void setVal(T obj, int columnIndex, Object val);

    /**
     * 默认值
     *
     * @return 默认值
     */
    protected abstract T defaultVal();

    /**
     * 添加行
     */
    @Override
    public void addRow() {
        addRow(defaultVal());
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        super.moveRow(oldIndex, oldIndex, newIndex);
        T remove = this.data.remove(oldIndex);
        this.data.add(newIndex, remove);
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return true;
    }
}
