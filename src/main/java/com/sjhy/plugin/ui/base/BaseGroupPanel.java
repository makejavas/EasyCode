package com.sjhy.plugin.ui.base;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.ui.JBUI;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.tool.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * 分组面板
 * 负责抽象出创建分组，删除分组，复制分组
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/11 16:27
 */
@Getter
public abstract class BaseGroupPanel extends JPanel {
    /**
     * 分组名称
     */
    private List<String> groupNameList;

    /**
     * 分组下拉框
     */
    private ComboBox<String> comboBox;

    /**
     * 构造方法
     *
     * @param groupNameList 分组名称
     */
    public BaseGroupPanel(@NotNull List<String> groupNameList, String defaultGroupName) {
        // 使用的布局
        super(new BorderLayout());
        this.groupNameList = groupNameList;
        init(defaultGroupName);
    }

    /**
     * 创建分组
     *
     * @param name 分组名称
     */
    protected abstract void createGroup(String name);

    /**
     * 删除分组
     *
     * @param name 分组名称
     */
    protected abstract void deleteGroup(String name);

    /**
     * 复制分组
     *
     * @param name 分组名称
     */
    protected abstract void copyGroup(String name);

    /**
     * 切换分组
     *
     * @param name 分组名称
     */
    protected abstract void changeGroup(String name);

    /**
     * 初始化方法
     *
     * @param defaultGroupName 默认选中分组
     */
    private void init(String defaultGroupName) {
        // 创建一个内容面板
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ComboBoxModel<String> comboBoxModel = new CollectionComboBoxModel<>(groupNameList);
        this.comboBox = new ComboBox<>(comboBoxModel);

        // 添加下拉框
        contentPanel.add(new Label("Group Name:"));
        contentPanel.add(this.comboBox);

        // 添加事件按钮
        DefaultActionGroup actionGroup = createActionGroup();

        // 添加分组选中事件
        this.comboBox.addItemListener(e -> changeGroup((String) comboBox.getSelectedItem()));

        // 选择默认分组
        this.comboBox.setSelectedItem(defaultGroupName);


        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", actionGroup, true);

        contentPanel.add(actionToolbar.getComponent());

        contentPanel.setPreferredSize(JBUI.size(600, 40));

        // 将内容面板添加至主面板左边(西边)
        this.add(contentPanel, BorderLayout.WEST);
    }

    /**
     * 重置方法
     * @param groupNameList 分组列表
     * @param defaultGroupName 默认选中分组
     */
    public void reset(@NotNull List<String> groupNameList, String defaultGroupName) {
        this.groupNameList = groupNameList;
        ComboBoxModel<String> comboBoxModel = new CollectionComboBoxModel<>(groupNameList);
        this.comboBox.setModel(comboBoxModel);
        this.comboBox.setSelectedItem(defaultGroupName);
        // 回调一波
        changeGroup(defaultGroupName);
    }


    /**
     * 输入元素名称
     *
     * @param initValue 初始值
     * @return 获得的名称，为null表示取消输入
     */
    private String inputItemName(String initValue) {
        return Messages.showInputDialog(MsgValue.GROUP_NAME_LABEL, MsgValue.TITLE_INFO, Messages.getQuestionIcon(), initValue, new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                // 非空校验
                if (StringUtils.isEmpty(inputString)) {
                    return false;
                }
                // 不能出现同名
                for (String name : groupNameList) {
                    if (Objects.equals(name, inputString)) {
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
    }

    /**
     * 创建动作组
     *
     * @return 动作组
     */
    private DefaultActionGroup createActionGroup() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // 复制事件
        actionGroup.add(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                String groupName = (String) comboBox.getSelectedItem();
                //输入新分组名称
                String newGroupName = inputItemName(groupName + "Copy");
                copyGroup(newGroupName);
            }
        });
        // 新增事件
        actionGroup.add(new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                //输入新分组名称
                String newGroupName = inputItemName("");
                createGroup(newGroupName);
            }
        });
        // 删除事件
        actionGroup.add(new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                String groupName = (String) comboBox.getSelectedItem();
                // 默认分组不允许删除
                if (Objects.equals(groupName, ConfigInfo.DEFAULT_NAME)) {
                    return;
                }
                // 确认删除？
                if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, String.format(MsgValue.CONFIRM_DELETE_GROUP, groupName)).isYes()) {
                    deleteGroup(groupName);
                }
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(!Objects.equals(comboBox.getSelectedItem(), ConfigInfo.DEFAULT_NAME));
            }
        });

        return actionGroup;
    }
}
