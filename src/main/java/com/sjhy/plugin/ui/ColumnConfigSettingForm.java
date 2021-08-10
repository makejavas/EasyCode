package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ToolbarDecorator;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.enums.ColumnConfigType;
import com.sjhy.plugin.factory.CellEditorFactory;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.ui.component.GroupNameComponent;
import com.sjhy.plugin.ui.component.TableComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 13:27
 */
public class ColumnConfigSettingForm implements Configurable, BaseSettings {
    private JPanel mainPanel;
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
    /**
     * 分组操作组件
     */
    private GroupNameComponent groupNameComponent;

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
        this.tableComponent = new TableComponent<>(columns, ColumnConfig::defaultVal);
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(this.tableComponent.getTable());
        // 表格初始化
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
    }

    private void initGroupName() {
        BiConsumer<String, String> copyOperator = (groupName, oldGroupName) -> {
            // 克隆对象
            ColumnConfigGroup columnConfigGroup = CloneUtils.cloneByJson(columnConfigGroupMap.get(oldGroupName));
            columnConfigGroup.setName(groupName);
            columnConfigGroupMap.put(groupName, columnConfigGroup);
            currColumnConfigGroup = columnConfigGroup;
            refreshUiVal();
        };

        Consumer<String> addOperator = groupName -> {
            ColumnConfigGroup columnConfigGroup = new ColumnConfigGroup();
            columnConfigGroup.setName(groupName);
            columnConfigGroup.setElementList(new ArrayList<>());
            columnConfigGroup.getElementList().add(ColumnConfig.defaultVal());
            columnConfigGroupMap.put(groupName, columnConfigGroup);
            currColumnConfigGroup = columnConfigGroup;
            refreshUiVal();
        };

        Consumer<String> switchGroupOperator = groupName -> {
            currColumnConfigGroup = columnConfigGroupMap.get(groupName);
            refreshUiVal();
        };

        Consumer<String> deleteOperator = groupName -> {
            columnConfigGroupMap.remove(currColumnConfigGroup.getName());
            currColumnConfigGroup = columnConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
            refreshUiVal();
        };

        this.groupNameComponent = new GroupNameComponent(copyOperator, addOperator, deleteOperator, switchGroupOperator);
        this.groupOperatorPanel.add(groupNameComponent.getPanel());
    }

    private void initPanel() {
        // 初始化表格
        this.initTable();
        this.initGroupName();
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
        this.initPanel();
        this.loadSettingsStore(getSettingsStorage());
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
        if (this.currColumnConfigGroup == null) {
            this.currColumnConfigGroup = this.columnConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        if (this.tableComponent != null) {
            this.tableComponent.setDataList(this.currColumnConfigGroup.getElementList());
        }
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setAllGroupNames(this.columnConfigGroupMap.keySet());
            this.groupNameComponent.setCurrGroupName(this.currColumnConfigGroup.getName());
        }
    }
}
