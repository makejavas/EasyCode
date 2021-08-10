package com.sjhy.plugin.ui;

import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.EditableModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 表格组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 09:52
 */
public class TableComponent<T> extends DefaultTableModel implements EditableModel {
    /**
     * 列信息
     */
    private List<Column<T>> columns;
    /**
     * 表数据
     */
    private List<T> dataList;
    /**
     * 默认值方法
     */
    private Supplier<T> defaultValFun;
    /**
     * 表格
     */
    @Getter
    private JBTable table;

    public TableComponent(@NonNull List<Column<T>> columns, @NonNull List<T> dataList, @NonNull Supplier<T> defaultValFun) {
        this.columns = columns;
        this.dataList = dataList;
        this.defaultValFun = defaultValFun;
        this.initColumnName();
        this.initTable();
        this.setDataList(dataList);
    }

    private void initColumnName() {
        for (Column<T> column : this.columns) {
            addColumn(column.name);
        }
    }

    private void initTable() {
        this.table = new JBTable(this);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 指定编辑器
        for (Column<T> column : this.columns) {
            if (column.editor != null) {
                this.table.getColumn(column.name).setCellEditor(column.editor);
            }
        }
    }

    private Vector<String> toObj(T e) {
        Vector<String> vector = new Vector<>();
        this.columns.stream().map(item -> item.getFun.apply(e)).forEach(vector::add);
        return vector;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
        // 清空数据
        removeAllRow();
        for (T entity : this.dataList) {
            addRow(entity);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        super.setValueAt(value, row, column);
        T obj = this.dataList.get(row);
        this.columns.get(column).getSetFun().accept(obj, (String) value);
    }

    /**
     * 移除所有行
     */
    public void removeAllRow() {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            super.removeRow(0);
        }
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        this.dataList.remove(row);
    }

    public void addRow(T entity) {
        addRow(toObj(entity));
    }

    @Override
    public void addRow() {
        T entity = defaultValFun.get();
        this.dataList.add(entity);
        addRow(entity);
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        super.moveRow(oldIndex, oldIndex, newIndex);
        T remove = this.dataList.remove(oldIndex);
        this.dataList.add(newIndex, remove);
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return true;
    }


    @Data
    @AllArgsConstructor
    public static class Column<T> {
        /**
         * 列名
         */
        private String name;
        /**
         * get方法
         */
        private Function<T, String> getFun;
        /**
         * set方法
         */
        private BiConsumer<T, String> setFun;
        /**
         * 列编辑器
         */
        private TableCellEditor editor;
    }
}
