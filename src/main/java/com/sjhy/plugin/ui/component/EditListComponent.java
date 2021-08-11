package com.sjhy.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.base.InputExistsValidator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 编辑列表组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 16:57
 */
public class EditListComponent<T extends AbstractGroup> {
    @Getter
    private JPanel mainPanel;
    /**
     * 复制分组，param1=新分组名  param2=旧分组名
     */
    private BiConsumer<String, String> copyItemFun;

    private Consumer<String> createItemFun;

    private Consumer<String> deleteItemFun;

    private Consumer<String> switchItemFun;
    /**
     * 所有列表项
     */
    @Getter
    private List<String> itemList;
    /**
     * 当前选中项
     */
    private String currentItem;

    private String label;

    private JBList<String> jbList;

    public EditListComponent(BiConsumer<String, String> copyItemFun, Consumer<String> createItemFun, Consumer<String> deleteItemFun, Consumer<String> switchItemFun, String label) {
        this.copyItemFun = copyItemFun;
        this.createItemFun = createItemFun;
        this.deleteItemFun = deleteItemFun;
        this.switchItemFun = switchItemFun;
        this.label = label;
        this.itemList = new ArrayList<>();
        this.init();
    }

    private void init() {
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBorder(new CustomLineBorder(1, 1, 1, 1));
        // 上边是操作项
        this.initAction();
        // 下边是列表
        this.initList();
    }

    private void inputItemName(String initValue, Consumer<String> consumer) {
        String value = Messages.showInputDialog(label, "Input " + label, Messages.getQuestionIcon(), initValue, new InputExistsValidator(itemList));
        if (StringUtils.isEmpty(value)) {
            return;
        }
        consumer.accept(value);
    }

    private AnAction createCopyAction() {
        return new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputItemName(currentItem + "Copy", itemName -> copyItemFun.accept(itemName, currentItem));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!StringUtils.isEmpty(jbList.getSelectedValue()));
            }
        };
    }

    private AnAction createAddAction() {
        return new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputItemName("demo", createItemFun);
            }
        };
    }

    private AnAction createRemoveAction() {
        return new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                deleteItemFun.accept(currentItem);
            }
        };
    }

    private AnAction createMoveUpAction() {
        return new AnAction(AllIcons.Actions.MoveUp) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int index = itemList.indexOf(currentItem);
                if (index <= 0) {
                    return;
                }
                String target = itemList.remove(index);
                itemList.add(index - 1, target);
                setItemList(itemList);
                setCurrentItem(currentItem);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(itemList.indexOf(currentItem) > 0);
            }
        };
    }

    private AnAction createMoveDownAction() {
        return new AnAction(AllIcons.Actions.MoveDown) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int index = itemList.indexOf(currentItem);
                if (index < 0 || index >= itemList.size() - 1) {
                    return;
                }
                String target = itemList.remove(index);
                itemList.add(index + 1, target);
                setItemList(itemList);
                setCurrentItem(currentItem);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                int index = itemList.indexOf(currentItem);
                e.getPresentation().setEnabled(index >= 0 && index < itemList.size() - 1);
            }
        };
    }

    private void initAction() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // 复制操作
        actionGroup.add(createCopyAction());
        // 新增操作
        actionGroup.add(createAddAction());
        // 删除动作
        actionGroup.add(createRemoveAction());
        // 向上移动
        actionGroup.add(createMoveUpAction());
        // 向下移动
        actionGroup.add(createMoveDownAction());
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Item Toolbar", actionGroup, true);
        this.mainPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
    }

    private void initList() {
        this.jbList = new JBList<>(itemList);
        this.mainPanel.add(this.jbList, BorderLayout.CENTER);
        this.jbList.addListSelectionListener(e -> {
            String selectedValue = jbList.getSelectedValue();
            if (StringUtils.isEmpty(selectedValue)) {
                return;
            }
            switchItemFun.accept(currentItem = selectedValue);
        });
    }

    public void setItemList(List<String> itemList) {
        this.itemList = itemList;
        if (StringUtils.isEmpty(this.currentItem) && itemList != null && itemList.size() > 0) {
            setCurrentItem(itemList.get(0));
        }
        this.jbList.setModel(new CollectionListModel<>(this.itemList));
    }

    public void setCurrentItem(String currentItem) {
        this.currentItem = currentItem;
        this.jbList.setSelectedIndex(this.itemList.indexOf(this.currentItem));
    }
}
