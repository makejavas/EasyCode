package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ToolbarDecorator;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.enums.MatchType;
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
 * @date 2021/08/07 15:33
 */
public class TypeMapperSettingForm implements Configurable, BaseSettings {
    private JPanel mainPanel;
    private JPanel groupOperatorPanel;
    /**
     * 类型映射配置
     */
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    /**
     * 当前分组名
     */
    private TypeMapperGroup currTypeMapperGroup;
    /**
     * 表格组件
     */
    private TableComponent<TypeMapper> tableComponent;
    /**
     * 分组操作组件
     */
    private GroupNameComponent groupNameComponent;

    private void initTable() {
        // 第一列仅适用下拉框
        TableCellEditor matchTypeEditor = CellEditorFactory.createComboBoxEditor(false, MatchType.class);
        TableComponent.Column<TypeMapper> matchTypeColumn = new TableComponent.Column<>("matchType",
                item -> item.getMatchType().name(),
                (entity, val) -> entity.setMatchType(MatchType.valueOf(val)),
                matchTypeEditor
        );
        // 第二列监听输入状态，及时修改属性值
        TableCellEditor columnTypeEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<TypeMapper> columnTypeColumn = new TableComponent.Column<>("columnType", TypeMapper::getColumnType, TypeMapper::setColumnType, columnTypeEditor);
        // 第三列支持下拉框
        TableCellEditor javaTypeEditor = CellEditorFactory.createComboBoxEditor(true, GlobalDict.DEFAULT_JAVA_TYPE_LIST);
        TableComponent.Column<TypeMapper> javaTypeColumn = new TableComponent.Column<>("javaType", TypeMapper::getJavaType, TypeMapper::setJavaType, javaTypeEditor);
        List<TableComponent.Column<TypeMapper>> columns = Arrays.asList(matchTypeColumn, columnTypeColumn, javaTypeColumn);
        this.tableComponent = new TableComponent<>(columns, Collections.emptyList(), TypeMapper::defaultVal);
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(this.tableComponent.getTable());
        // 表格初始化
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
    }

    private void initGroupName() {
        BiConsumer<String, String> copyOperator = (groupName, oldGroupName) -> {
            // 克隆对象
            TypeMapperGroup typeMapperGroup = CloneUtils.cloneByJson(typeMapperGroupMap.get(oldGroupName));
            typeMapperGroup.setName(groupName);
            typeMapperGroupMap.put(groupName, typeMapperGroup);
            currTypeMapperGroup = typeMapperGroup;
            refreshUiVal();
        };

        Consumer<String> addOperator = groupName -> {
            TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
            typeMapperGroup.setName(groupName);
            typeMapperGroup.setElementList(new ArrayList<>());
            typeMapperGroup.getElementList().add(TypeMapper.defaultVal());
            typeMapperGroupMap.put(groupName, typeMapperGroup);
            currTypeMapperGroup = typeMapperGroup;
            refreshUiVal();
        };

        Consumer<String> deleteOperator = groupName -> {
            typeMapperGroupMap.remove(currTypeMapperGroup.getName());
            currTypeMapperGroup = typeMapperGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
            refreshUiVal();
        };

        Consumer<String> switchGroupOperator = groupName -> {
            currTypeMapperGroup = typeMapperGroupMap.get(groupName);
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
        return "Type Mapper";
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
        return !this.typeMapperGroupMap.equals(getSettingsStorage().getTypeMapperGroupMap())
                || !getSettingsStorage().getCurrTypeMapperGroupName().equals(this.currTypeMapperGroup.getName());
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettingsStorage().setTypeMapperGroupMap(this.typeMapperGroupMap);
        getSettingsStorage().setCurrTypeMapperGroupName(this.currTypeMapperGroup.getName());
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
        this.typeMapperGroupMap = CloneUtils.cloneByJson(settingsStorage.getTypeMapperGroupMap(), new TypeReference<Map<String, TypeMapperGroup>>() {
        });
        this.currTypeMapperGroup = this.typeMapperGroupMap.get(settingsStorage.getCurrTypeMapperGroupName());
        if (this.currTypeMapperGroup == null) {
            this.currTypeMapperGroup = this.typeMapperGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        if (this.tableComponent != null) {
            this.tableComponent.setDataList(this.currTypeMapperGroup.getElementList());
        }
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setAllGroupNames(this.typeMapperGroupMap.keySet());
            this.groupNameComponent.setCurrGroupName(this.currTypeMapperGroup.getName());
        }
    }
}
