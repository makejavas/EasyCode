package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.entity.TypeMapperModel;
import com.sjhy.plugin.enums.MatchType;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Map;
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
     * 类型映射配置
     */
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    /**
     * 当前分组名
     */
    private TypeMapperGroup currTypeMapperGroup;
    /**
     * 表格
     */
    private JBTable table;

    private TypeMapperModel typeMapperModel;

    private void initPanel(SettingsStorageDTO settingsStorage) {
        this.typeMapperModel = new TypeMapperModel();
        this.loadSettingsStore(settingsStorage);
        table = new JBTable(typeMapperModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 第一列仅适用下拉框
        ComboBox<String> matchTypeField = new ComboBox<>(Stream.of(MatchType.values()).map(Enum::name).toArray(value -> new String[2]));
        if (matchTypeField.getPopup() != null) {
            matchTypeField.getPopup().getList().setBackground(JBColor.WHITE);
            matchTypeField.getPopup().getList().setForeground(JBColor.GREEN);
        }
        DefaultCellEditor matchTypeCellEditor = new DefaultCellEditor(matchTypeField);
        table.getColumn(typeMapperModel.initColumnName()[0]).setCellEditor(matchTypeCellEditor);
        // 第二列监听输入状态，及时修改属性值
        JBTextField textField = new JBTextField();
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 失去焦点时向上层发起事件通知，使table的值能够正常回写
                textField.getActionListeners()[0].actionPerformed(null);
            }
        });
        DefaultCellEditor textFieldCellEditor = new DefaultCellEditor(textField);
        table.getColumn(typeMapperModel.initColumnName()[1]).setCellEditor(textFieldCellEditor);

        // 第三列支持下拉框
        ComboBox<String> javaTypeField = new ComboBox<>(GlobalDict.DEFAULT_JAVA_TYPE_LIST);
        if (javaTypeField.getPopup() != null) {
            javaTypeField.getPopup().getList().setBackground(JBColor.WHITE);
            javaTypeField.getPopup().getList().setForeground(JBColor.GREEN);
        }
        javaTypeField.setEditable(true);
        DefaultCellEditor javaTypeCellEditor = new DefaultCellEditor(javaTypeField);
        table.getColumn(typeMapperModel.initColumnName()[2]).setCellEditor(javaTypeCellEditor);
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        // 表格初始化
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        // 分组操作
        DefaultActionGroup groupAction = new DefaultActionGroup(Arrays.asList(new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String value = Messages.showInputDialog("Group Name:", "Input Group Name:", Messages.getQuestionIcon(), currTypeMapperGroup.getName() + " Copy", new InputValidator() {
                    @Override
                    public boolean checkInput(String inputString) {
                        return !StringUtils.isEmpty(inputString) && !typeMapperGroupMap.containsKey(inputString);
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
                TypeMapperGroup typeMapperGroup = CloneUtils.cloneByJson(typeMapperGroupMap.get(currTypeMapperGroup.getName()));
                typeMapperGroup.setName(value);
                settingsStorage.getTypeMapperGroupMap().put(value, typeMapperGroup);
                currTypeMapperGroup = typeMapperGroup;
                refreshUiVal();
            }
        }, new AnAction(AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        }));
        ActionToolbar groupActionToolbar = ActionManager.getInstance().createActionToolbar("Group Toolbar", groupAction, true);
        this.groupOperatorPanel.add(groupActionToolbar.getComponent());
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
        this.initPanel(getSettingsStorage());
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.typeMapperGroupMap.equals(getSettingsStorage().getTypeMapperGroupMap());
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
        this.refreshUiVal();
    }

    private void refreshUiVal() {
        if (this.typeMapperModel != null) {
            this.typeMapperModel.init(this.currTypeMapperGroup.getElementList());
        }
        this.groupComboBox.removeAllItems();
        for (String key : this.typeMapperGroupMap.keySet()) {
            this.groupComboBox.addItem(key);
        }
        this.groupComboBox.setSelectedItem(this.currTypeMapperGroup.getName());
    }
}
