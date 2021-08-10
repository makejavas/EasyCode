package com.sjhy.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.tool.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 分组编辑组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 14:13
 */
public class GroupNameComponent {

    /**
     * 复制分组，param1=新分组名  param2=旧分组名
     */
    private BiConsumer<String, String> copyGroupFun;

    private Consumer<String> createGroupFun;

    private Consumer<String> deleteGroupFun;

    private Consumer<String> switchGroupFun;

    /**
     * 所有分组
     */
    private Set<String> allGroupNames;

    /**
     * 当前分组
     */
    private String currGroupName;

    @Getter
    private JPanel panel;

    private ComboBox<String> groupComboBox;

    private boolean refresh;

    public GroupNameComponent(BiConsumer<String, String> copyGroupFun, Consumer<String> createGroupFun, Consumer<String> deleteGroupFun, Consumer<String> switchGroupFun) {
        this.copyGroupFun = copyGroupFun;
        this.createGroupFun = createGroupFun;
        this.deleteGroupFun = deleteGroupFun;
        this.switchGroupFun = switchGroupFun;
        this.allGroupNames = Collections.emptySet();
        this.init();
    }

    private void inputGroupName(String initValue, Consumer<String> consumer) {
        String value = Messages.showInputDialog("Group Name:", "Input Group Name:", Messages.getQuestionIcon(), initValue, new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return !StringUtils.isEmpty(inputString) && !allGroupNames.contains(inputString);
            }

            @Override
            public boolean canClose(String inputString) {
                return this.checkInput(inputString);
            }
        });
        if (StringUtils.isEmpty(value)) {
            return;
        }
        consumer.accept(value);
    }

    private AnAction copyAction() {
        return new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputGroupName(currGroupName + "Copy", groupName -> copyGroupFun.accept(groupName, currGroupName));
            }
        };
    }

    private AnAction addAction() {
        return new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                inputGroupName("GroupName", createGroupFun);
            }
        };
    }

    private AnAction removeAction() {
        return new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                deleteGroupFun.accept(currGroupName);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(!GlobalDict.DEFAULT_GROUP_NAME.equals(currGroupName));
            }
        };
    }

    private void init() {
        panel = new JPanel(new BorderLayout());
        // 分组操作
        DefaultActionGroup groupAction = new DefaultActionGroup(Arrays.asList(this.copyAction(), this.addAction(), this.removeAction()));
        ActionToolbar groupActionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", groupAction, true);
        this.panel.add(groupActionToolbar.getComponent(), BorderLayout.EAST);
        this.groupComboBox = new ComboBox<>(this.allGroupNames.toArray(new String[0]));
        this.panel.add(this.groupComboBox, BorderLayout.CENTER);
        this.groupComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (refresh) {
                    return;
                }
                String selectedItem = (String) groupComboBox.getSelectedItem();
                if (StringUtils.isEmpty(selectedItem)) {
                    return;
                }
                switchGroupFun.accept(selectedItem);
            }
        });
    }

    public void setAllGroupNames(Set<String> allGroupNames) {
        this.allGroupNames = allGroupNames;
        try {
            this.refresh = true;
            this.groupComboBox.removeAllItems();
            for (String item : this.allGroupNames) {
                this.groupComboBox.addItem(item);
            }
        } finally {
            this.refresh = false;
        }
    }

    public void setCurrGroupName(String currGroupName) {
        this.currGroupName = currGroupName;
        try {
            this.refresh = true;
            this.groupComboBox.setSelectedItem(this.currGroupName);
        } finally {
            this.refresh = false;
        }
    }
}
