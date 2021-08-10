package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.enums.ColumnConfigType;
import com.sjhy.plugin.factory.CellEditorFactory;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 13:27
 */
public class ColumnConfigSettingForm implements Configurable, BaseSettings {
    private JPanel mainPanel;
    private JComboBox<String> groupComboBox;
    private JPanel groupOperatorPanel;
    /**
     * 列配置
     */
    private Map<String, ColumnConfigGroup> columnConfigGroupMap;
    /**
     * 当前分组名
     */
    private ColumnConfigGroup currColumnConfigGroup;
    /**
     * 表格组件
     */
    private TableComponent<ColumnConfig> tableComponent;

    private boolean refresh;

    private void initTable() {
        // 第一列，类型
        TableCellEditor typeEditor = CellEditorFactory.createComboBoxEditor(false, ColumnConfigType.class);
        TableComponent.Column<ColumnConfig> typeColumn = new TableComponent.Column<>("type", item -> item.getType().name(), (entity, val) -> entity.setType(ColumnConfigType.valueOf(val)), typeEditor);
        // 第二列标题
        TableCellEditor titleEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<ColumnConfig> titleColumn = new TableComponent.Column<>("title", ColumnConfig::getTitle, ColumnConfig::setTitle, titleEditor);
        // 第三列选项
        TableCellEditor selectValueEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<ColumnConfig> selectValueColumn = new TableComponent.Column<>("selectValue", ColumnConfig::getSelectValue, ColumnConfig::setSelectValue, selectValueEditor);
        List<TableComponent.Column<ColumnConfig>> columns = Arrays.asList(typeColumn, titleColumn, selectValueColumn);
        this.tableComponent = new TableComponent<>(columns, Collections.emptyList(), () -> new ColumnConfig("demo", ColumnConfigType.TEXT));
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(this.tableComponent.getTable());
        // 表格初始化
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
    }

    private void initPanel(SettingsStorageDTO settingsStorage) {
        // 初始化表格
        this.initTable();
        // 分组操作
        DefaultActionGroup groupAction = new DefaultActionGroup(Arrays.asList(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String value = Messages.showInputDialog("Group Name:", "Input Group Name:", Messages.getQuestionIcon(), currColumnConfigGroup.getName() + " Copy", new InputValidator() {
                    @Override
                    public boolean checkInput(String inputString) {
                        return !StringUtils.isEmpty(inputString) && !columnConfigGroupMap.containsKey(inputString);
                    }

                    @Override
                    public boolean canClose(String inputString) {
                        return this.checkInput(inputString);
                    }
                });
                if (value == null) {
                    return;
                }
                // 克隆对象
                ColumnConfigGroup columnConfigGroup = CloneUtils.cloneByJson(columnConfigGroupMap.get(currColumnConfigGroup.getName()));
                columnConfigGroup.setName(value);
                settingsStorage.getColumnConfigGroupMap().put(value, columnConfigGroup);
                columnConfigGroupMap.put(value, columnConfigGroup);
                currColumnConfigGroup = columnConfigGroup;
                refreshUiVal();
            }
        }, new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                columnConfigGroupMap.remove(currColumnConfigGroup.getName());
                currColumnConfigGroup = columnConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
                refreshUiVal();
            }
        }));
        ActionToolbar groupActionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", groupAction, true);
        this.groupOperatorPanel.add(groupActionToolbar.getComponent());
        this.groupComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (refresh) {
                    return;
                }
                String groupName = (String) groupComboBox.getSelectedItem();
                if (StringUtils.isEmpty(groupName)) {
                    return;
                }
                currColumnConfigGroup = columnConfigGroupMap.get(groupName);
                refreshUiVal();
            }
        });
        this.loadSettingsStore(settingsStorage);
    }

    @Override
    public String getDisplayName() {
        return "Column Config";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel(getSettingsStorage());
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.columnConfigGroupMap.equals(getSettingsStorage().getColumnConfigGroupMap())
                || !getSettingsStorage().getCurrColumnConfigGroupName().equals(this.currColumnConfigGroup.getName());
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettingsStorage().setColumnConfigGroupMap(this.columnConfigGroupMap);
        getSettingsStorage().setCurrColumnConfigGroupName(this.currColumnConfigGroup.getName());
        // 保存包后重新加载配置
        this.loadSettingsStore(getSettingsStorage());
    }

    /**
     * 加载配置信息
     *
     * @param settingsStorage 配置信息
     */
    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // 复制配置，防止篡改
        this.columnConfigGroupMap = CloneUtils.cloneByJson(settingsStorage.getColumnConfigGroupMap(), new TypeReference<Map<String, ColumnConfigGroup>>() {
        });
        this.currColumnConfigGroup = this.columnConfigGroupMap.get(settingsStorage.getCurrColumnConfigGroupName());
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        this.refresh = true;
        if (this.tableComponent != null) {
            this.tableComponent.setDataList(this.currColumnConfigGroup.getElementList());
        }
        this.groupComboBox.removeAllItems();
        for (String key : this.columnConfigGroupMap.keySet()) {
            this.groupComboBox.addItem(key);
        }
        this.groupComboBox.setSelectedItem(this.currColumnConfigGroup.getName());
        this.refresh = false;
    }
}
