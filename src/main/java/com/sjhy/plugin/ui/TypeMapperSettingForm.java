package com.sjhy.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.TypeMapperModel;
import com.sjhy.plugin.enums.MatchType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 15:33
 */
public class TypeMapperSettingForm implements Configurable, BaseSettings {
    private JPanel mainPanel;
    private JComboBox<String> groupComboBox;
    private JPanel groupOperatorPanel;
    /**
     * 表格
     */
    private JBTable table;

    private void initPanel(SettingsStorageDTO settingsStorage) {
        TypeMapperModel typeMapperModel = new TypeMapperModel();
        typeMapperModel.init(settingsStorage.currentTypeMapperGroup().getElementList());
        table = new JBTable(typeMapperModel);
        ComboBox<String> comboField = new ComboBox<>(Stream.of(MatchType.values()).map(Enum::name).toArray(value -> new String[2]), 20);
        if (comboField.getPopup() != null) {
            comboField.getPopup().getList().setBackground(JBColor.MAGENTA);
        }
        DefaultCellEditor cellEditor = new DefaultCellEditor(comboField);
        table.getColumn("matchType").setCellEditor(cellEditor);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        // 表格初始化
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        // 分组操作
        DefaultActionGroup groupAction = new DefaultActionGroup(Arrays.asList(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        }, new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        }));
        ActionToolbar groupActionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", groupAction, true);
        this.groupOperatorPanel.add(groupActionToolbar.getComponent());
        for (String key : settingsStorage.getTypeMapperGroupMap().keySet()) {
            this.groupComboBox.addItem(key);
        }
    }

    @Override
    public String getDisplayName() {
        return "Type Mapper";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public @Nullable JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    /**
     * 加载配置信息
     *
     * @param settingsStorage 配置信息
     */
    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        this.initPanel(settingsStorage);
    }
}
