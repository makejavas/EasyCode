package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.tool.StringUtils;

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
            String value = Messages.showInputDialog("Group Name:", "Input Group Name", Messages.getQuestionIcon(), currGroupName + "Copy", new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    // 要求非空并且不存在
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
            // 点击YES选项时
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
            String value = Messages.showInputDialog("Item Name:", "Input Item Name", Messages.getQuestionIcon(), "Demo", new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    //输入空字符串
                    if (StringUtils.isEmpty(inputString)) {
                        return false;
                    }
                    //已经存在
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
            if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "Confirm Delete Item " + itemName + "?").isYes()) {
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
            String value = Messages.showInputDialog("Item Name:", "Input Item Name", Messages.getQuestionIcon(), itemName + "Copy", new InputValidator() {
                @Override
                public boolean checkInput(String inputString) {
                    //输入空字符串
                    if (StringUtils.isEmpty(inputString)) {
                        return false;
                    }
                    //已经存在
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
            // 取消复制，不需要提示信息
            if (value == null) {
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
