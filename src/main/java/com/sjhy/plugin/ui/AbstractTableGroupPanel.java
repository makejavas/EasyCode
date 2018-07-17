package com.sjhy.plugin.ui;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.util.ui.ComboBoxCellEditor;
import com.sjhy.plugin.comm.CommClone;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.service.ConfigService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.*;

public abstract class AbstractTableGroupPanel<T extends AbstractGroup<T,E>, E extends CommClone> {
    private JPanel mainPanel;
    private JComboBox groupComboBox;
    private JTable table;
    private JButton copyGroupButton;
    private JButton deleteGroupButton;
    private JButton addItemButton;
    private JButton deleteItemButton;

    private ColumnConfig[] columnConfigInfo;

    private DefaultTableModel tableModel;

    protected Map<String,T> group;
    protected String currGroupName;

    private boolean initFlag;


    AbstractTableGroupPanel(Map<String, T> group, String currGroupName) {
        init(group, currGroupName);
        initEvent();
    }

    protected AbstractGroup<T,E> getCurrGroup() {
        return this.group.get(this.currGroupName);
    }

    //刷新类类型配置
    private void refreshEditorType() {
        for (ColumnConfig column : columnConfigInfo) {
            TableColumn tableColumn = table.getColumn(column.getTitle());
            switch (column.getType()) {
                case TEXT:
                    break;
                case SELECT:
                    tableColumn.setCellEditor(new ComboBoxCellEditor() {
                        @Override
                        protected List<String> getComboBoxItems() {
                            return Arrays.asList(column.getSelectValue().split(","));
                        }
                    });
                    break;
                case BOOLEAN:
                    tableColumn.setCellEditor(new BooleanTableCellEditor());
                    break;
            }
        }
    }

    protected void init(Map<String, T> group, String currGroupName) {
        this.group = group;
        this.currGroupName = currGroupName;
        initFlag = false;
        //初始化分组
        initGroup(group.keySet(), currGroupName);
        //初始化列
        columnConfigInfo = initColumn();
        tableModel = new DefaultTableModel();
        table.setModel(tableModel);
        for (ColumnConfig column : columnConfigInfo) {
            tableModel.addColumn(column.getTitle());
        }
        //初始化数据
        getCurrGroup().getElementList().forEach(e -> {
            tableModel.addRow(toRow(e));
        });
        table.setModel(tableModel);
        refreshEditorType();
        initFlag = true;
    }

    private void initEvent() {
        //切换分组事件
        groupComboBox.addActionListener(e -> {
            if (!initFlag){
                return;
            }
            String groupName = (String) groupComboBox.getSelectedItem();
            if (groupName==null) {
                return;
            }
            if (currGroupName.equals(groupName)) {
                return;
            }
            init(group, groupName);
        });
        //复制分组事件
        copyGroupButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName+" Copy");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (group.containsKey(value)){
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
                return;
            }
            //noinspection unchecked
            T groupItem = group.get(currGroupName).clone();
            groupItem.setName(value);
            group.put(value, groupItem);
            init(group, value);
        });
        //删除分组事件
        deleteGroupButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group "+currGroupName+"?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (result==0){
                if(ConfigService.DEFAULT_NAME.equals(currGroupName)){
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                group.remove(currGroupName);
                init(group, ConfigService.DEFAULT_NAME);
            }
        });
        //添加元素事件
        addItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", "Demo");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Item Name Can't Is Empty!");
                return;
            }
            List<E> itemList = group.get(currGroupName).getElementList();
            for (E item : itemList) {
                if (getItemName(item).equals(value)){
                    JOptionPane.showMessageDialog(null, "Item Name Already exist!");
                    return;
                }
            }
            itemList.add(createItem(value));
            init(group, currGroupName);
        });
        //删除元素
        deleteItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            List<E> itemList = group.get(currGroupName).getElementList();
            if (itemList.isEmpty()) {
                return;
            }
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Selected Item?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (result==0) {
                int[] rows = table.getSelectedRows();
                for (int i = rows.length-1; i>=0; i--) {
                    tableModel.removeRow(rows[i]);
                    getCurrGroup().getElementList().remove(rows[i]);
                }
            }
        });
    }

    //初始化分组
    @SuppressWarnings("unchecked")
    private void initGroup(Set<String> groupNameSet, String selectGroupName) {
        groupComboBox.removeAllItems();
        groupNameSet.forEach(groupComboBox::addItem);
        groupComboBox.setSelectedItem(selectGroupName);
    }

    //用于数据回绑定
    protected void refresh() {
        if (tableModel==null){
            return;
        }
        Vector vector = tableModel.getDataVector();
        getCurrGroup().getElementList().clear();
        for (Object obj : vector) {
            if (obj instanceof Vector) {
                Vector item = (Vector) obj;
                Object[] itemArr = new Object[item.size()];
                int i = 0;
                for (Object obj2 : item) {
                    if (obj2!=null && obj2 instanceof String) {
                        String str = (String) obj2;
                        str = str.trim();
                        obj2 = str;
                        if (str.isEmpty()){
                            obj2 = null;
                        }
                    }
                    itemArr[i++] = obj2;
                }
                getCurrGroup().getElementList().add(toItem(itemArr));
            }
        }
    }

    protected abstract Object[] toRow(E item);

    protected abstract E toItem(Object[] rowData);

    protected abstract String getItemName(E item);

    protected abstract E createItem(String value);

    protected abstract ColumnConfig[] initColumn();

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
