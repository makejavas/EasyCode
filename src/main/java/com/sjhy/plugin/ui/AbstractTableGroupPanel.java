package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.util.ui.ComboBoxCellEditor;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.tool.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.*;

/**
 * 抽象的表分组面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public abstract class AbstractTableGroupPanel<T extends AbstractGroup<E>, E> {
    /**
     * 主面板
     */
    private JPanel mainPanel;
    /**
     * 分组下拉选择框
     */
    private JComboBox<String> groupComboBox;
    /**
     * 表格
     */
    private JTable table;
    /**
     * 分组按钮
     */
    private JButton copyGroupButton;
    /**
     * 删除按钮
     */
    private JButton deleteGroupButton;
    /**
     * 新增元素按钮
     */
    private JButton addItemButton;
    /**
     * 新增元素按钮
     */
    private JButton deleteItemButton;

    /**
     * 列配置信息
     */
    private ColumnConfig[] columnConfigInfo;

    /**
     * 表模型
     */
    private DefaultTableModel tableModel;

    /**
     * 分组对象
     */
    protected Map<String, T> group;
    /**
     * 当前分组名称
     */
    protected String currGroupName;

    /**
     * 初始化标记
     */
    private boolean initFlag;
    /**
     * 克隆工具类
     */
    protected CloneUtils cloneUtils = CloneUtils.getInstance();


    /**
     * 构造方法
     *
     * @param group         分组对象
     * @param currGroupName 分组名称
     */
    public AbstractTableGroupPanel(Map<String, T> group, String currGroupName) {
        this.group = group;
        this.currGroupName = currGroupName;
        init();
        initEvent();
    }

    /**
     * 获取当前分组对象
     *
     * @return 当前分组对象
     */
    protected T getCurrGroup() {
        return this.group.get(this.currGroupName);
    }

    /**
     * 刷新类类型配置
     */
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
                default:
                    break;
            }
        }
    }

    /**
     * 初始化方法
     */
    protected void init() {
        initFlag = false;
        //初始化分组
        initGroup();
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

    /**
     * 初始化事件
     */
    @SuppressWarnings("Duplicates")
    private void initEvent() {
        //切换分组事件
        groupComboBox.addActionListener(e -> {
            // 未初始化完成禁止切换分组
            if (!initFlag) {
                return;
            }
            String groupName = (String) groupComboBox.getSelectedItem();
            if (StringUtils.isEmpty(groupName)) {
                return;
            }
            if (currGroupName.equals(groupName)) {
                return;
            }
            this.currGroupName = groupName;
            init();
        });
        //复制分组事件
        copyGroupButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            //输入分组名称
            String value = Messages.showInputDialog("Group Name:", "Input Group Name:", Messages.getQuestionIcon(), currGroupName + " Copy", new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    //非空并且不存在分组名称
                    return !StringUtils.isEmpty(inputString) && !group.containsKey(inputString);
                }

                @Override
                public boolean canClose(String inputString) {
                    return this.checkInput(inputString);
                }
            });

            // 取消复制，不需要提示信息
            if (value == null) {
                return;
            }
            // 克隆对象
            T groupItem = cloneUtils.clone(group.get(currGroupName));
            groupItem.setName(value);
            group.put(value, groupItem);
            currGroupName = value;
            init();
        });
        //删除分组事件
        deleteGroupButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "Confirm Delete Group " + currGroupName + "?").isYes()) {
                if (ConfigInfo.DEFAULT_NAME.equals(currGroupName)) {
                    Messages.showWarningDialog("Can't Delete Default Group!", MsgValue.TITLE_INFO);
                    return;
                }
                group.remove(currGroupName);
                currGroupName = ConfigInfo.DEFAULT_NAME;
                init();
            }
        });
        //添加元素事件
        addItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            List<E> itemList = group.get(currGroupName).getElementList();
            // 输入名称
            String value = Messages.showInputDialog("Item Name:", "Input Item Name", Messages.getQuestionIcon(), "Demo", new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    if (StringUtils.isEmpty(inputString)) {
                        return false;
                    }
                    for (E item : itemList) {
                        if (getItemName(item).equals(inputString)) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean canClose(String inputString) {
                    return this.checkInput(inputString);
                }
            });
            // 取消添加，不需要提示信息
            if (value == null) {
                return;
            }

            itemList.add(createItem(value));
            init();
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
            if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "Confirm Delete Selected Item?").isYes()) {
                int[] rows = table.getSelectedRows();
                for (int i = rows.length - 1; i >= 0; i--) {
                    tableModel.removeRow(rows[i]);
                    getCurrGroup().getElementList().remove(rows[i]);
                }
            }
        });
    }

    /**
     * 初始化分组
     */
    private void initGroup() {
        groupComboBox.removeAllItems();
        Set<String> groupNameSet = group.keySet();
        for (String groupName : groupNameSet) {
            groupComboBox.addItem(groupName);
        }
        groupComboBox.setSelectedItem(currGroupName);
    }

    /**
     * 刷新，用于数据回绑定
     */
    protected void refresh() {
        if (tableModel == null) {
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
                    if (obj2 != null && obj2 instanceof String) {
                        String str = (String) obj2;
                        str = str.trim();
                        obj2 = str;
                        if (str.isEmpty()) {
                            obj2 = null;
                        }
                    }
                    itemArr[i++] = obj2;
                }
                getCurrGroup().getElementList().add(toItem(itemArr));
            }
        }
    }

    /**
     * 元素转行数据
     *
     * @param item 元素对象
     * @return 行数据
     */
    protected abstract Object[] toRow(E item);

    /**
     * 行数据转元素
     *
     * @param rowData 行数据
     * @return 元素对象
     */
    protected abstract E toItem(Object[] rowData);

    /**
     * 获取元素名称
     *
     * @param item 元素
     * @return 元素名称
     */
    protected abstract String getItemName(E item);

    /**
     * 创建元素
     *
     * @param name 元素名称
     * @return 元素对象
     */
    protected abstract E createItem(String name);

    /**
     * 初始化列配置
     *
     * @return 列配置数组
     */
    protected abstract ColumnConfig[] initColumn();

    /**
     * 获取主面板对象
     *
     * @return 主面板对象
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
