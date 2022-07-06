package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
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
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 15:33
 */
public class TypeMapperSettingForm implements BaseSettings {
    private final JPanel mainPanel;
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
    private GroupNameComponent<TypeMapper, TypeMapperGroup> groupNameComponent;

    public TypeMapperSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }

    private void initTable() {
        // 第一列仅适用下拉框
        String[] matchTypeNames = Stream.of(MatchType.values()).map(MatchType::name).toArray(String[]::new);
        TableCellEditor matchTypeEditor = CellEditorFactory.createComboBoxEditor(false, matchTypeNames);
        TableCellRenderer matchTypeRenderer = new ComboBoxTableRenderer<>(matchTypeNames);
        TableComponent.Column<TypeMapper> matchTypeColumn = new TableComponent.Column<>("matchType",
                item -> item.getMatchType() != null ? item.getMatchType().name() : MatchType.REGEX.name(),
                (entity, val) -> entity.setMatchType(MatchType.valueOf(val)),
                matchTypeEditor,
                matchTypeRenderer
        );
        // 第二列监听输入状态，及时修改属性值
        TableCellEditor columnTypeEditor = CellEditorFactory.createTextFieldEditor();
        TableComponent.Column<TypeMapper> columnTypeColumn = new TableComponent.Column<>("columnType", TypeMapper::getColumnType, TypeMapper::setColumnType, columnTypeEditor, null);
        // 第三列支持下拉框
        TableCellEditor javaTypeEditor = CellEditorFactory.createComboBoxEditor(true, GlobalDict.DEFAULT_JAVA_TYPE_LIST);
        TableCellRenderer javaTypeRenderer = new ComboBoxTableRenderer<>(GlobalDict.DEFAULT_JAVA_TYPE_LIST);
        TableComponent.Column<TypeMapper> javaTypeColumn = new TableComponent.Column<>("javaType", TypeMapper::getJavaType, TypeMapper::setJavaType, javaTypeEditor, javaTypeRenderer);
        List<TableComponent.Column<TypeMapper>> columns = Arrays.asList(matchTypeColumn, columnTypeColumn, javaTypeColumn);
        // 表格初始化
        this.tableComponent = new TableComponent<>(columns, this.currTypeMapperGroup.getElementList(), TypeMapper.class);
        this.mainPanel.add(this.tableComponent.createPanel(), BorderLayout.CENTER);
    }

    private void initGroupName() {
        // 切换分组操作
        Consumer<TypeMapperGroup> switchGroupOperator = typeMapperGroupMap -> {
            this.currTypeMapperGroup = typeMapperGroupMap;
            refreshUiVal();
        };
        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.typeMapperGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initPanel() {
        this.loadSettingsStore(getSettingsStorage());
        // 初始化表格
        this.initTable();
        this.initGroupName();
    }

    @Override
    public String getDisplayName() {
        return "Type Mapper";
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.typeMapperGroupMap.equals(getSettingsStorage().getTypeMapperGroupMap())
                || !getSettingsStorage().getCurrTypeMapperGroupName().equals(this.currTypeMapperGroup.getName());
    }

    @Override
    public void apply() {
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
            this.groupNameComponent.setGroupMap(this.typeMapperGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currTypeMapperGroup.getName());
        }
    }
}
