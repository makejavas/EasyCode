package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.GlobalConfig;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.ui.component.EditListComponent;
import com.sjhy.plugin.ui.component.EditorComponent;
import com.sjhy.plugin.ui.component.GroupNameComponent;
import com.sjhy.plugin.ui.component.LeftRightComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 16:14
 */
public class GlobalConfigSettingForm implements Configurable, BaseSettings {
    /**
     * 全局变量描述信息，说明文档
     */
    private static final String TEMPLATE_DESCRIPTION_INFO;

    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(GlobalConfigSettingForm.class.getResource("/description/globalConfigDescription.html"));
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        } finally {
            TEMPLATE_DESCRIPTION_INFO = descriptionInfo;
        }
    }

    private JPanel mainPanel;
    /**
     * 类型映射配置
     */
    private Map<String, GlobalConfigGroup> globalConfigGroupMap;
    /**
     * 当前分组名
     */
    private GlobalConfigGroup currGlobalConfigGroup;
    /**
     * 编辑框组件
     */
    private EditorComponent<GlobalConfig> editorComponent;
    /**
     * 分组操作组件
     */
    private GroupNameComponent<GlobalConfig, GlobalConfigGroup> groupNameComponent;
    /**
     * 编辑列表框
     */
    private EditListComponent<GlobalConfig> editListComponent;


    public GlobalConfigSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }


    private void initGroupName() {
        Consumer<GlobalConfigGroup> switchGroupOperator = globalConfigGroup -> {
            this.currGlobalConfigGroup = globalConfigGroup;
            refreshUiVal();
            // 切换分组情况编辑框
            this.editorComponent.setFile(null);
        };

        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.globalConfigGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initEditList() {
        Consumer<GlobalConfig> switchItemFun = globalConfig -> {
            refreshUiVal();
            if (globalConfig != null) {
                this.editListComponent.setCurrentItem(globalConfig.getName());
            }
            editorComponent.setFile(globalConfig);
        };
        this.editListComponent = new EditListComponent<>(switchItemFun, "GlobalConfig Name:", GlobalConfig.class, this.currGlobalConfigGroup.getElementList());
    }

    private void initEditor() {
        this.editorComponent = new EditorComponent<>(null, TEMPLATE_DESCRIPTION_INFO);
    }

    private void initPanel() {
        this.loadSettingsStore(getSettingsStorage());
        // 初始化表格
        this.initGroupName();
        // 初始化编辑列表组件
        this.initEditList();
        // 初始化编辑框组件
        this.initEditor();
        // 左右组件
        LeftRightComponent leftRightComponent = new LeftRightComponent(editListComponent.getMainPanel(), this.editorComponent.getMainPanel());
        this.mainPanel.add(leftRightComponent.getMainPanel(), BorderLayout.CENTER);
    }

    @Override
    public String getDisplayName() {
        return "Global Config";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // 复制配置，防止篡改
        this.globalConfigGroupMap = CloneUtils.cloneByJson(settingsStorage.getGlobalConfigGroupMap(), new TypeReference<Map<String, GlobalConfigGroup>>() {
        });
        this.currGlobalConfigGroup = this.globalConfigGroupMap.get(settingsStorage.getCurrGlobalConfigGroupName());
        if (this.currGlobalConfigGroup == null) {
            this.currGlobalConfigGroup = this.globalConfigGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
        }
        // 解决reset后编辑框未清空BUG
        if (this.editorComponent != null) {
            this.editorComponent.setFile(null);
        }
        this.refreshUiVal();
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initPanel();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !this.globalConfigGroupMap.equals(getSettingsStorage().getGlobalConfigGroupMap())
                || !getSettingsStorage().getCurrGlobalConfigGroupName().equals(this.currGlobalConfigGroup.getName());
    }

    @Override
    public void apply() {
        getSettingsStorage().setGlobalConfigGroupMap(this.globalConfigGroupMap);
        getSettingsStorage().setCurrGlobalConfigGroupName(this.currGlobalConfigGroup.getName());
        // 保存包后重新加载配置
        this.loadSettingsStore(getSettingsStorage());
    }

    private void refreshUiVal() {
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.globalConfigGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currGlobalConfigGroup.getName());
        }
        if (this.editListComponent != null) {
            this.editListComponent.setElementList(this.currGlobalConfigGroup.getElementList());
        }
    }
}
