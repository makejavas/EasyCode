package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.sjhy.plugin.comm.CommClone;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.tool.ConfigInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class AbstractGroupPanel<T extends AbstractGroup<T, E>, E extends CommClone> {
    private JPanel mainPanel;
    private JComboBox groupComboBox;
    private JButton copyGroupButton;
    private JButton deleteGroupButton;
    private JButton addItemButton;
    private JButton deleteItemButton;
    private JButton copyItemButton;
    private JPanel itemGroupPanel;
    private JPanel itemPanel;

    private Boolean initFlag = Boolean.FALSE;
    private int selectItemIndex;

    String currGroupName;

    Map<String, T> group;

    AbstractGroupPanel(Map<String, T> group, String selectGroupName) {
        itemGroupPanel.setLayout(new VerticalFlowLayout());
        itemPanel.setLayout(new GridLayout());
        initEvent();
        init(group, selectGroupName);
    }

    void init(Map<String, T> group, String selectGroupName) {
        this.group = group;
        this.currGroupName = selectGroupName;
        initFlag = Boolean.FALSE;
        //所有组名
        Set<String> groupNameSet = group.keySet();
        //选中组名称
        AbstractGroup<T, E> selectGroup = group.get(selectGroupName);

        //初始化所有组
        initGroup(groupNameSet, selectGroupName);

        //初始化所有元素
        initItem(selectGroup.getElementList());


        initFlag = Boolean.TRUE;
    }

    //初始化分组
    @SuppressWarnings("unchecked")
    private void initGroup(Set<String> groupNameSet, String selectGroupName) {
        groupComboBox.removeAllItems();
        groupNameSet.forEach(groupComboBox::addItem);
        groupComboBox.setSelectedItem(selectGroupName);
    }

    //初始化所有元素
    private void initItem(List<E> itemList) {
        initFlag = false;
        itemGroupPanel.removeAll();
        if (itemList.isEmpty()) {
            itemGroupPanel.updateUI();
            initFlag = true;
            return;
        }
        itemList.forEach(item -> {
            JButton button = new JButton();
            button.setText(getItemName(item));
            //元素选中事件
            button.addActionListener(e -> {

                if (!initFlag) {
                    return;
                }
                String itemName = button.getText();
                for (int i = 0; i < itemList.size(); i++) {
                    E element = itemList.get(i);
                    if (itemName.equals(getItemName(element))) {
                        selectItemIndex = i;
                        initItemPanel(itemPanel, element);
                        return;
                    }
                }
            });
            itemGroupPanel.add(button);
        });
        itemGroupPanel.updateUI();
        //初始化第一个元素面板
        initItemPanel(itemPanel, itemList.get(selectItemIndex));
        initFlag = true;
    }

    //初始化所有事件
    private void initEvent() {
        //切换分组事件
        groupComboBox.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String groupName = (String) groupComboBox.getSelectedItem();
            if (groupName == null) {
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
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName + " Copy");
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (group.containsKey(value)) {
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
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group " + currGroupName + "?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (result == 0) {
                if (ConfigInfo.DEFAULT_NAME.equals(currGroupName)) {
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                group.remove(currGroupName);
                init(group, ConfigInfo.DEFAULT_NAME);
            }
        });
        //添加元素事件
        addItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", "Demo");
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Item Name Can't Is Empty!");
                return;
            }
            List<E> itemList = group.get(currGroupName).getElementList();
            for (E item : itemList) {
                if (getItemName(item).equals(value)) {
                    JOptionPane.showMessageDialog(null, "Item Name Already exist!");
                    return;
                }
            }
            itemList.add(createItem(value));
            selectItemIndex = itemList.size() - 1;
            initItem(itemList);
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
            String itemName = getItemName(itemList.get(selectItemIndex));
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Item " + itemName + "?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if (result == 0) {
                itemList.remove(selectItemIndex);
                if (selectItemIndex >= itemList.size()) {
                    selectItemIndex = itemList.size() - 1;
                }
                initItem(itemList);
            }
        });
        //复制元素
        copyItemButton.addActionListener(e -> {
            if (!initFlag) {
                return;
            }
            List<E> itemList = group.get(currGroupName).getElementList();
            if (itemList.isEmpty()) {
                return;
            }
            E item = itemList.get(selectItemIndex);
            String itemName = getItemName(item);
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", itemName + " Copy");
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Item Name Can't Is Empty!");
                return;
            }
            //noinspection unchecked
            item = (E) item.clone();
            setItemName(item, value);
            itemList.add(item);
            selectItemIndex = itemList.size() - 1;
            initItem(itemList);
        });
    }

    //初始化元素面板
    protected abstract void initItemPanel(JPanel itemPanel, E item);

    //获取元素名称
    protected abstract String getItemName(E item);

    protected abstract void setItemName(E item, String itemName);

    //创建元素
    protected abstract E createItem(String name);

    //所有元素Get方法
    JPanel getMainPanel() {
        return mainPanel;
    }

    JComboBox getGroupComboBox() {
        return groupComboBox;
    }

    JButton getCopyGroupButton() {
        return copyGroupButton;
    }

    JButton getDeleteGroupButton() {
        return deleteGroupButton;
    }

    JButton getAddItemButton() {
        return addItemButton;
    }

    JButton getDeleteItemButton() {
        return deleteItemButton;
    }

    JButton getCopyItemButton() {
        return copyItemButton;
    }

    JPanel getItemGroupPanel() {
        return itemGroupPanel;
    }

    JPanel getItemPanel() {
        return itemPanel;
    }
}
