package com.sjhy.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBList;
import com.sjhy.plugin.entity.AbstractEditorItem;
import com.sjhy.plugin.factory.AbstractItemFactory;
import com.sjhy.plugin.tool.CollectionUtil;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.base.InputExistsValidator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 编辑列表组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 16:57
 */
public class EditListComponent<E extends AbstractEditorItem<E>> {
    @Getter
    private JPanel mainPanel;

    private Consumer<E> switchItemFun;
    /**
     * 当前选中项
     */
    private String currentItem;

    private String label;

    private JBList<String> jbList;

    private Class<E> cls;

    /**
     * 分组Map
     */
    private List<E> elementList;

    private boolean refresh;

    public EditListComponent(Consumer<E> switchItemFun, String label, Class<E> cls, List<E> elementList) {
        this.switchItemFun = switchItemFun;
        this.label = label;
        this.cls = cls;
        this.elementList = elementList;
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
        String value = Messages.showInputDialog(label, "Input " + label, Messages.getQuestionIcon(), initValue, new InputExistsValidator(getAllItemName()));
        if (StringUtils.isEmpty(value)) {
            return;
        }
        consumer.accept(value);
    }

    private AnAction createCopyAction() {
        return new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputItemName(jbList.getSelectedValue() + "Copy", itemName -> elementList.stream()
                        .filter(item -> Objects.equals(item.fileName(), jbList.getSelectedValue()))
                        .findFirst()
                        .ifPresent(item -> {
                            E cloneObj = item.cloneObj();
                            cloneObj.changeFileName(itemName);
                            elementList.add(cloneObj);
                            switchItemFun.accept(cloneObj);
                        }));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!CollectionUtil.isEmpty(elementList) && !StringUtils.isEmpty(currentItem));
            }
        };
    }

    private AnAction createAddAction() {
        return new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputItemName("demo", itemName -> {
                    E defaultVal = AbstractItemFactory.createDefaultVal(cls);
                    defaultVal.changeFileName(itemName);
                    elementList.add(defaultVal);
                    switchItemFun.accept(defaultVal);
                });
            }
        };
    }

    private AnAction createRemoveAction() {
        return new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                elementList.removeIf(item -> Objects.equals(item.fileName(), jbList.getSelectedValue()));
                switchItemFun.accept(elementList.stream().findFirst().orElse(null));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!CollectionUtil.isEmpty(elementList) && !StringUtils.isEmpty(currentItem));
            }
        };
    }

    private AnAction createMoveUpAction() {
        return new AnAction(AllIcons.Actions.MoveUp) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                E selectItem = findByName(currentItem);
                int index = elementList.indexOf(selectItem);
                if (index <= 0) {
                    return;
                }
                E target = elementList.remove(index);
                elementList.add(index - 1, target);
                switchItemFun.accept(target);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                E selectItem = findByName(currentItem);
                boolean enabled = selectItem != null && elementList.indexOf(selectItem) > 0;
                e.getPresentation().setEnabled(enabled);
            }
        };
    }

    private AnAction createMoveDownAction() {
        return new AnAction(AllIcons.Actions.MoveDown) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                E selectItem = findByName(currentItem);
                int index = elementList.indexOf(selectItem);
                if (index < 0 || index >= elementList.size() - 1) {
                    return;
                }
                E target = elementList.remove(index);
                elementList.add(index + 1, target);
                switchItemFun.accept(target);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                E selectItem = findByName(currentItem);
                boolean enabled = selectItem != null && elementList.indexOf(selectItem) < elementList.size() - 1;
                e.getPresentation().setEnabled(enabled);
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
        this.jbList = new JBList<>(getAllItemName());
        this.mainPanel.add(this.jbList, BorderLayout.CENTER);
        this.jbList.addListSelectionListener(e -> {
            if (this.refresh) {
                return;
            }
            String selectedValue = jbList.getSelectedValue();
            if (StringUtils.isEmpty(selectedValue)) {
                return;
            }
            this.currentItem = selectedValue;
            switchItemFun.accept(findByName(selectedValue));
        });
    }

    private E findByName(String name) {
        return this.elementList.stream().filter(item -> Objects.equals(item.fileName(), name)).findFirst().orElse(null);
    }

    private List<String> getAllItemName() {
        if (CollectionUtil.isEmpty(elementList)) {
            return Collections.emptyList();
        }
        return elementList.stream().map(AbstractEditorItem::fileName).collect(Collectors.toList());
    }

    public void setElementList(List<E> elementList) {
        this.elementList = elementList;
        try {
            this.refresh = true;
            this.jbList.setModel(new CollectionListModel<>(getAllItemName()));
        } finally {
            this.refresh = false;
        }
        if (StringUtils.isEmpty(this.currentItem) && elementList != null && elementList.size() > 0) {
            setCurrentItem(elementList.get(0).fileName());
            switchItemFun.accept(findByName(this.currentItem));
        }
    }

    public void setCurrentItem(String currentItem) {
        this.currentItem = currentItem;
        E element = findByName(this.currentItem);
        int index = this.elementList.indexOf(element);
        if (index >= 0) {
            try {
                this.refresh = true;
                this.jbList.setSelectedIndex(index);
            } finally {
                this.refresh = false;
            }
        }
    }
}
