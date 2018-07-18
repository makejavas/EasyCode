package com.sjhy.plugin.ui;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.util.ui.ComboBoxCellEditor;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.tool.TableInfoUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class ConfigTableDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table;
    private JButton addButton;

    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    private TableInfoUtils tableInfoUtils = TableInfoUtils.getInstance();

    private DefaultTableModel tableModel;

    private List<ColumnConfig> columnConfigList;

    private TableInfo tableInfo;

    private boolean initFlag;

    public ConfigTableDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
        initEvent();
    }

    private void onOK() {
        tableInfoUtils.save(tableInfo);
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private void init() {
        initFlag = false;
        ConfigInfo configInfo = ConfigInfo.getInstance();
        ColumnConfigGroup columnConfigGroup = configInfo.getColumnConfigGroupMap().get(configInfo.getCurrColumnConfigGroupName());
        columnConfigList = getInitColumn(columnConfigGroup.getElementList());
        //绑定数据
        tableInfo = tableInfoUtils.handler(Collections.singletonList(cacheDataUtils.getSelectDbTable())).get(0);

        refresh();

        initFlag = true;
    }

    private void refresh() {
        tableModel = new DefaultTableModel();
        columnConfigList.forEach(columnConfig -> tableModel.addColumn(columnConfig.getTitle()));
        //追加数据
        tableInfo.getFullColumn().forEach(columnInfo -> {
            List<Object> dataList = new ArrayList<>();
            dataList.add(columnInfo.getName());
            dataList.add(columnInfo.getType());
            dataList.add(columnInfo.getComment());
            //渲染附加数据
            if (columnInfo.getExt() != null && !columnInfo.getExt().isEmpty()) {
                for (int i = 3; i < tableModel.getColumnCount(); i++) {
                    dataList.add(columnInfo.getExt().get(tableModel.getColumnName(i)));
                }
            }
            tableModel.addRow(dataList.toArray());
        });
        table.setModel(tableModel);
        //刷新列编辑器
        refreshColumnEditor(columnConfigList);

        //添加数据修改事件
        tableModel.addTableModelListener(e -> {
            if (e.getFirstRow() != e.getLastRow()) {
                return;
            }
            int row = e.getFirstRow();
            int column = e.getColumn();
            Object val = tableModel.getValueAt(row, column);
            ColumnInfo columnInfo = tableInfo.getFullColumn().get(row);
            if (column == 0) {
                for (ColumnInfo info : tableInfo.getFullColumn()) {
                    if (info.getName().equals(val) && !info.getName().equals(columnInfo.getName())) {
                        JOptionPane.showMessageDialog(null, "Column Name Already exist!");
                        tableModel.setValueAt(columnInfo.getName(), row, column);
                        return;
                    }
                }
            }
            switch (column) {
                case 0:
                    columnInfo.setName((String) val);
                    break;
                case 1:
                    columnInfo.setType((String) val);
                    break;
                case 2:
                    columnInfo.setComment((String) val);
                    break;
                default:
                    break;
            }
            if (column > 2) {
                if (columnInfo.getExt() == null) {
                    columnInfo.setExt(new HashMap<>());
                }
                String title = tableModel.getColumnName(column);
                columnInfo.getExt().put(title, val);
            }
        });
    }

    private void initEvent() {
        //添加元素事件
        addButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Column Name:", "Demo");
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Column Name Can't Is Empty!");
                return;
            }

            for (ColumnInfo columnInfo : tableInfo.getFullColumn()) {
                if (columnInfo.getName().equals(value)) {
                    JOptionPane.showMessageDialog(null, "Column Name Already exist!");
                    return;
                }
            }
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(value);
            columnInfo.setType("java.lang.String");
            tableInfo.getFullColumn().add(columnInfo);
            refresh();
        });
    }

    private void refreshColumnEditor(List<ColumnConfig> columnConfigList) {
        columnConfigList.forEach(columnConfig -> {
            TableColumn tableColumn = table.getColumn(columnConfig.getTitle());
            int index = tableColumn.getModelIndex();
            switch (columnConfig.getType()) {
                case TEXT:
                    break;
                case SELECT:
                    tableColumn.setCellEditor(new ComboBoxCellEditor() {
                        @Override
                        protected List<String> getComboBoxItems() {
                            return Arrays.asList(columnConfig.getSelectValue().split(","));
                        }
                    });
                    break;
                case BOOLEAN:
                    //给空列赋初始值
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        if (tableModel.getValueAt(row, index) == null) {
                            tableModel.setValueAt(false, row, index);
                        }
                    }
                    tableColumn.setCellEditor(new BooleanTableCellEditor());
                    break;
                default:
                    break;
            }
        });
    }

    private List<ColumnConfig> getInitColumn(List<ColumnConfig> columnConfigList) {
        List<ColumnConfig> result = new ArrayList<>();
        result.add(new ColumnConfig("name", ColumnConfigType.TEXT));
        result.add(new ColumnConfig("type", ColumnConfigType.TEXT));
        result.add(new ColumnConfig("comment", ColumnConfigType.TEXT));
        result.addAll(columnConfigList);
        return result;
    }

    public void open() {
        setTitle("Config Table " + cacheDataUtils.getSelectDbTable().getName());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
