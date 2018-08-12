package com.sjhy.plugin.core;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.ui.JBUI;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.tool.ConfigInfo;
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
     * 下拉框模型
     */
    private ComboBoxModel<String> comboBoxModel;

    /**
     * 构造方法
     *
     * @param groupNameList 分组名称
     */
    public BaseGroupPanel(@NotNull List<String> groupNameList) {
        // 使用的布局
        super(new BorderLayout());
        this.groupNameList = groupNameList;
        init();
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
     * @param name 分组名称
     */
    protected abstract void chageGroup(String name);

    /**
     * 初始化方法
     */
    private void init() {
        // 创建一个内容面板
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.comboBoxModel = new CollectionComboBoxModel<>(groupNameList);
        this.comboBox = new ComboBox<>(comboBoxModel);

        // 添加下拉框
        contentPanel.add(new Label("Group Name:"));
        contentPanel.add(this.comboBox);

        // 添加事件按钮
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // 复制事件
        actionGroup.add(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                copyGroup((String) comboBoxModel.getSelectedItem());
            }
        });
        // 新增事件
        actionGroup.add(new AnAction(AllIcons.General.Add) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                System.out.println("OK");
            }
        });
        // 删除事件
        actionGroup.add(new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                String groupName = (String) comboBoxModel.getSelectedItem();
                // 默认分组不允许删除
                if (Objects.equals(comboBoxModel.getSelectedItem(), ConfigInfo.DEFAULT_NAME)) {
                    return;
                }
                // 确认删除？
                if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, String.format(MsgValue.CONFIRM_DELETE_GROUP, groupName)).isYes()) {
                    deleteGroup(groupName);
                }
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(!Objects.equals(comboBoxModel.getSelectedItem(), ConfigInfo.DEFAULT_NAME));
            }
        });

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", actionGroup, true);

        contentPanel.add(actionToolbar.getComponent());

        contentPanel.setPreferredSize(JBUI.size(600, 40));

        // 将内容面板添加至主面板左边(西边)
        this.add(contentPanel, BorderLayout.WEST);
    }
}
