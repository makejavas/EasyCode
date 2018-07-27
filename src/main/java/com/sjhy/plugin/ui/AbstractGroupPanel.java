package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 抽象分组面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public abstract class AbstractGroupPanel<T extends AbstractGroup<E>, E> {
    /**
     * 主面板
     */
    protected JPanel mainPanel;
    /**
     * 分组下拉选择框
     */
    protected JComboBox<String> groupComboBox;
    /**
     * 复制按钮
     */
    protected JButton copyGroupButton;
    /**
     * 删除按钮
     */
    protected JButton deleteGroupButton;
    /**
     * 新增元素
     */
    protected JButton addItemButton;
    /**
     * 删除元素
     */
    protected JButton deleteItemButton;
    /**
     * 复制元素
     */
    protected JButton copyItemButton;
    /**
     * 元素分组面板
     */
    protected JPanel itemGroupPanel;
    /**
     * 元素面板
     */
    protected JPanel itemPanel;

    /**
     * 初始化标记
     */
    protected Boolean initFlag = Boolean.FALSE;
    /**
     * 当前选中的元素下标索引值
     */
    protected int selectItemIndex;

    /**
     * 当前分组名称
     */
    protected String currGroupName;

    /**
     * 分组映射对象
     */
    protected Map<String, T> group;

    /**
     * 克隆工具类
     */
    protected CloneUtils cloneUtils = CloneUtils.getInstance();

    /**
     * 构造方法
     *
     * @param group           分组映射对象
     * @param selectGroupName 当前选中的分组名称
     */
    public AbstractGroupPanel(Map<String, T> group, String selectGroupName) {
        this.group = group;
        this.currGroupName = selectGroupName;
        // 设置布局
        itemGroupPanel.setLayout(new VerticalFlowLayout());
        itemPanel.setLayout(new GridLayout());
        // 初始化事件
        initEvent();
        // 初始化
        init();
    }

    protected void init() {
        initFlag = false;

        //初始化所有组
        initGroup();

        //初始化所有元素
        initItem();

        initFlag = true;
    }

    /**
     * 初始化分组
     */
    private void initGroup() {
        groupComboBox.removeAllItems();
        for (String groupName : group.keySet()) {
            groupComboBox.addItem(groupName);
        }
        // 设置选中默认分组
        groupComboBox.setSelectedItem(currGroupName);
    }

    /**
     * 初始化所有元素
     */
    private void initItem() {
        initFlag = false;
        //获取选中组的所有元素
        List<E> elementList = group.get(currGroupName).getElementList();
        itemGroupPanel.removeAll();
        if (elementList.isEmpty()) {
            itemGroupPanel.updateUI();
            initFlag = true;
            return;
        }
        elementList.forEach(item -> {
            JButton button = new JButton();
            button.setText(getItemName(item));
            //元素选中事件
            button.addActionListener(e -> {

                if (!initFlag) {
                    return;
                }
                String itemName = button.getText();
                for (int i = 0; i < elementList.size(); i++) {
                    E element = elementList.get(i);
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
        // 修复下标越界异常
        if (selectItemIndex >= elementList.size()) {
            selectItemIndex = 0;
        }
        //初始化第一个元素面板
        initItemPanel(itemPanel, elementList.get(selectItemIndex));
        initFlag = true;
    }

    /**
     * 初始化所有事件
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
            // 未初始化禁止复制分组
            if (!initFlag) {
                return;
            }
            // 输入分组名称
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName + " Copy");
            if (StringUtils.isEmpty(value)) {
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (group.containsKey(value)) {
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
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
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group " + currGroupName + "?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            // 点击YES选项时
            if (JOptionPane.YES_OPTION == result) {
                if (ConfigInfo.DEFAULT_NAME.equals(currGroupName)) {
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
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
            String value = JOptionPane.showInputDialog(null, "Input Item Name:", "Demo");
            if (StringUtils.isEmpty(value)) {
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
            // 选中最后一个元素，即当前添加的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
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
            if (result == JOptionPane.YES_OPTION) {
                itemList.remove(selectItemIndex);
                // 移步到当前删除元素的前一个元素
                if (selectItemIndex > 0) {
                    selectItemIndex--;
                }
                initItem();
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
            item = cloneUtils.clone(item);
            // 设置元素名称
            setItemName(item, value);
            itemList.add(item);
            // 移步至当前复制的元素
            selectItemIndex = itemList.size() - 1;
            initItem();
        });
    }

    /**
     * 初始化元素面板
     *
     * @param itemPanel 父面板
     * @param item      元素对象
     */
    protected abstract void initItemPanel(JPanel itemPanel, E item);

    /**
     * 获取元素名称
     *
     * @param item 元素对象
     * @return 元素名称
     */
    protected abstract String getItemName(E item);

    /**
     * 设置元素名称
     *
     * @param item     元素对象
     * @param itemName 元素名称
     */
    protected abstract void setItemName(E item, String itemName);

    /**
     * 创建元素
     *
     * @param name 元素名称
     * @return 元素对象
     */
    protected abstract E createItem(String name);
}
