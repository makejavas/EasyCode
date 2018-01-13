package com.sjhy.plugin.ui;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.util.ui.ComboBoxCellEditor;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.TableInfoUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
        initEvent();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void init() {
        initFlag = false;
        ConfigService configService = ConfigService.getInstance();
        ColumnConfigGroup columnConfigGroup = configService.getColumnConfigGroupMap().get(configService.getCurrColumnConfigGroupName());
        columnConfigList = getInitColumn(columnConfigGroup.getElementList());
        //绑定数据
        tableInfo = tableInfoUtils.handler(Collections.singletonList(cacheDataUtils.getSelectDbTable())).get(0);

        refresh();

        initFlag = true;
    }

    private void refresh() {
        tableModel = new DefaultTableModel();
        columnConfigList.forEach(columnConfig -> {
            tableModel.addColumn(columnConfig.getTitle());
        });
        //追加数据
        tableInfo.getFullColumn().forEach(columnInfo -> {
            tableModel.addRow(new Object[]{columnInfo.getName(), columnInfo.getType(), columnInfo.getComment()});
        });
        table.setModel(tableModel);
        //刷新列编辑器
        refreshColumnEditor(columnConfigList);
    }

    private void initEvent() {
        //添加元素事件
        addButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Column Name:", "Demo");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Column Name Can't Is Empty!");
                return;
            }

            for (ColumnInfo columnInfo : tableInfo.getFullColumn()) {
                if (columnInfo.getName().equals(value)){
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
                    for (int row=0; row< tableModel.getRowCount(); row++) {
                        if (tableModel.getValueAt(row, index)==null){
                            tableModel.setValueAt(false, row, index);
                        }
                    }
                    tableColumn.setCellEditor(new BooleanTableCellEditor());
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
        setTitle("Config Table "+cacheDataUtils.getSelectDbTable().getName());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
