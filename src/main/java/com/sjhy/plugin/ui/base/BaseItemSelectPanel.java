package com.sjhy.plugin.ui.base;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.sjhy.plugin.constants.MsgValue;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 元素选择面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/12 22:26
 */
@Getter
public abstract class BaseItemSelectPanel<T extends Item> {
    /**
     * 可选面板集合
     */
    private List<T> itemList;

    /**
     * 左边面板
     */
    private JPanel leftPanel;

    /**
     * 右边面板
     */
    private JPanel rightPanel;

    /**
     * 列表面板
     */
    private JBList<String> listPanel;

    public BaseItemSelectPanel(@NotNull List<T> itemList) {
        this.itemList = itemList;
    }

    /**
     * 新增元素
     *
     * @param name 元素名称
     */
    protected abstract void addItem(String name);

    /**
     * 复制元素
     *
     * @param item 元素对象
     */
    protected abstract void copyItem(T item);

    /**
     * 删除多个元素
     *
     * @param item 元素对象
     */
    protected abstract void deleteItem(T item);

    /**
     * 选中元素
     *
     * @param item 元素对象
     */
    protected abstract void selectedItem(T item);

    /**
     * 获取面板
     */
    public JComponent getComponent() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        // 左边的选择列表
        this.leftPanel = new JPanel(new BorderLayout());

        // 头部操作按钮
        JPanel headToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 添加事件按钮
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // 新增事件
        actionGroup.add(new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                System.out.println("OK");
            }
        });
        // 复制事件
        actionGroup.add(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                copyItem(getSelectedItem());
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(getSelectedItem() != null);
            }
        });
        // 删除事件
        actionGroup.add(new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                // 确认删除？
                if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, String.format(MsgValue.CONFIRM_DELETE_MESSAGE, "")).isYes()) {
                    deleteItem(getSelectedItem());
                }
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(getSelectedItem() != null);
            }
        });

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Item Toolbar", actionGroup, true);

        headToolbar.add(actionToolbar.getComponent());
        // 添加边框
        actionToolbar.getComponent().setBorder(new CustomLineBorder(1, 1, 1, 1));

        // 工具栏添加至左边面板的北边（上面）
        leftPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

        // 元素列表
        listPanel = new JBList<>(dataConvert());
        // 只能单选
        listPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 添加元素选中事件
        listPanel.addListSelectionListener(e -> {
            T item = getSelectedItem();
            if (item == null) {
                return;
            }
            selectedItem(item);
        });
        // 添加边框
        listPanel.setBorder(new CustomLineBorder(0, 1, 1, 1));

        // 将列表添加至左边面板的中间
        leftPanel.add(listPanel, BorderLayout.CENTER);

        // 右边面板
        this.rightPanel = new JPanel(new BorderLayout());

        // 左右分割面板并添加至主面板
        Splitter splitter = new Splitter(false, 0.2F);

        splitter.setFirstComponent(leftPanel);
        splitter.setSecondComponent(rightPanel);

        mainPanel.add(splitter, BorderLayout.CENTER);

        mainPanel.setPreferredSize(JBUI.size(400, 300));

        // 存在元素时，默认选中第一个元素
        if (!itemList.isEmpty()) {
            listPanel.setSelectedIndex(0);
        }

        return mainPanel;
    }

    /**
     * 数据转换
     *
     * @return 转换结果
     */
    private List<String> dataConvert() {
        List<String> data = new ArrayList<>();
        itemList.forEach(item -> data.add(item.getName()));
        return data;
    }

    /**
     * 获取选中元素
     *
     * @return 选中元素
     */
    private T getSelectedItem() {
        String selectedName = listPanel.getSelectedValue();
        if (selectedName == null) {
            return null;
        }
        for (T t : itemList) {
            if (Objects.equals(t.getName(), selectedName)) {
                return t;
            }
        }
        return null;
    }
}
