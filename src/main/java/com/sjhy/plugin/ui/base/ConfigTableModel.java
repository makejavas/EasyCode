package com.sjhy.plugin.ui.base;

import com.intellij.util.ui.EditableModel;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.enums.ColumnConfigType;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 13:41
 */
public class ConfigTableModel extends DefaultTableModel implements EditableModel {

    private TableInfo tableInfo;

    public ConfigTableModel(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        this.initColumn();
        this.initTableData();
    }

    private void initColumn() {
        addColumn("name");
        addColumn("type");
        addColumn("comment");
        for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
            addColumn(columnConfig.getTitle());
        }
    }

    private void initTableData() {
        // 删除所有列
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            super.removeRow(0);
        }
        // 渲染列数据
        for (ColumnInfo columnInfo : this.tableInfo.getFullColumn()) {
            List<Object> values = new ArrayList<>();
            values.add(columnInfo.getName());
            values.add(columnInfo.getType());
            values.add(columnInfo.getComment());
            Map<String, Object> ext = columnInfo.getExt();
            if (ext == null) {
                ext = Collections.emptyMap();
            }
            for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
                Object obj = ext.get(columnConfig.getTitle());
                if (obj == null) {
                    if (columnConfig.getType() == ColumnConfigType.BOOLEAN) {
                        values.add(false);
                    } else {
                        values.add("");
                    }
                } else {
                    values.add(obj);
                }
            }
            addRow(values.toArray());
        }
    }

    @Override
    public void addRow() {
        Map<String, ColumnInfo> map = this.tableInfo.getFullColumn().stream().collect(Collectors.toMap(ColumnInfo::getName, val -> val));
        String newName = "demo";
        for (int i = 0; map.containsKey(newName); i++) {
            newName = "demo" + i;
        }
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setCustom(true);
        columnInfo.setName(newName);
        columnInfo.setExt(new HashMap<>(16));
        columnInfo.setComment("");
        columnInfo.setShortType("String");
        columnInfo.setType("java.lang.String");
        this.tableInfo.getFullColumn().add(columnInfo);
        // 刷新表数据
        this.initTableData();
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().get(row);
        if (columnInfo == null) {
            return;
        }
        // 非自定义数据不允许修改
        if (!columnInfo.getCustom() && column <= 2) {
            return;
        }
        switch (column) {
            case 0:
                String name = (String) value;
                // 列名不允许为空
                if (StringUtils.isEmpty(name)) {
                    return;
                }
                // 已存在重名不允许修改
                boolean existsName = this.tableInfo.getFullColumn().stream().anyMatch(item -> Objects.equals(item.getName(), name));
                if (existsName) {
                    return;
                }
                columnInfo.setName(name);
                break;
            case 1:
                String type = (String) value;
                // 列名不允许为空
                if (StringUtils.isEmpty(type)) {
                    return;
                }
                columnInfo.setType(type);
                columnInfo.setShortType(type.substring(type.lastIndexOf(".") + 1));
                break;
            case 2:
                columnInfo.setComment((String) value);
                break;
            default:
                ColumnConfig columnConfig = CurrGroupUtils.getCurrColumnConfigGroup().getElementList().get(column - 3);
                if (columnInfo.getExt() == null) {
                    columnInfo.setExt(new HashMap<>(16));
                }
                columnInfo.getExt().put(columnConfig.getTitle(), value);
                break;
        }
        super.setValueAt(value, row, column);
    }

    @Override
    public void removeRow(int row) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().get(row);
        if (columnInfo == null) {
            return;
        }
        // 非自定义列不允许删除
        if (!columnInfo.getCustom()) {
            return;
        }
        this.tableInfo.getFullColumn().remove(row);
        this.initTableData();
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
        ColumnInfo columnInfo = this.tableInfo.getFullColumn().remove(oldIndex);
        this.tableInfo.getFullColumn().add(newIndex, columnInfo);
        this.initTableData();
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return false;
    }
}
