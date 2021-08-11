package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 16:14
 */
public class TemplateSettingForm implements Configurable, BaseSettings {
    /**
     * 模板描述信息，说明文档
     */
    private static final String TEMPLATE_DESCRIPTION_INFO;

    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(TemplateSettingPanel.class.getResource("/description/templateDescription.html"));
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
    private Map<String, TemplateGroup> templateGroupMap;
    /**
     * 当前分组名
     */
    private TemplateGroup currTemplateGroup;
    /**
     * 编辑框组件
     */
    private EditorComponent<Template> editorComponent;
    /**
     * 分组操作组件
     */
    private GroupNameComponent<Template, TemplateGroup> groupNameComponent;
    /**
     * 编辑列表框
     */
    private EditListComponent editListComponent;


    public TemplateSettingForm() {
        this.mainPanel = new JPanel(new BorderLayout());
    }


    private void initGroupName() {
        Consumer<TemplateGroup> switchGroupOperator = templateGroup -> {
            this.currTemplateGroup = templateGroup;
            refreshUiVal();
        };

        this.groupNameComponent = new GroupNameComponent<>(switchGroupOperator, this.templateGroupMap);
        this.mainPanel.add(groupNameComponent.getPanel(), BorderLayout.NORTH);
    }

    private void initEditList() {
        BiConsumer<String, String> copyItemFun = (newName, oldName) -> {
            Template template = currTemplateGroup.getElementList().stream().filter(item -> item.getName().equals(oldName)).findFirst().orElse(null);
            template = CloneUtils.cloneByJson(template);
            if (template != null) {
                template.setName(newName);
                currTemplateGroup.getElementList().add(template);
            }
            refreshUiVal();
        };
        Consumer<String> createItemFun = name -> {
            Template template = new Template(name, "");
            currTemplateGroup.getElementList().add(template);
            refreshUiVal();
        };

        Consumer<String> deleteItemFun = name -> {
            Template template = currTemplateGroup.getElementList().stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
            currTemplateGroup.getElementList().remove(template);
            refreshUiVal();
        };
        Consumer<String> switchItemFun = name -> {
            editorComponent.setFile(currTemplateGroup.getElementList().stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null));
        };
        this.editListComponent = new EditListComponent(copyItemFun, createItemFun, deleteItemFun, switchItemFun, "Template Name:");
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
        return "Template";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        // 复制配置，防止篡改
        this.templateGroupMap = CloneUtils.cloneByJson(settingsStorage.getTemplateGroupMap(), new TypeReference<Map<String, TemplateGroup>>() {
        });
        this.currTemplateGroup = this.templateGroupMap.get(settingsStorage.getCurrTypeMapperGroupName());
        if (this.currTemplateGroup == null) {
            this.currTemplateGroup = this.templateGroupMap.get(GlobalDict.DEFAULT_GROUP_NAME);
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
        return !this.templateGroupMap.equals(getSettingsStorage().getTemplateGroupMap())
                || !getSettingsStorage().getCurrTemplateGroupName().equals(this.currTemplateGroup.getName());
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettingsStorage().setTemplateGroupMap(this.templateGroupMap);
        getSettingsStorage().setCurrTypeMapperGroupName(this.currTemplateGroup.getName());
        // 保存包后重新加载配置
        this.loadSettingsStore(getSettingsStorage());
    }

    private void refreshUiVal() {
        if (this.groupNameComponent != null) {
            this.groupNameComponent.setGroupMap(this.templateGroupMap);
            this.groupNameComponent.setCurrGroupName(this.currTemplateGroup.getName());
        }
        if (this.editListComponent != null) {
            this.editListComponent.setItemList(this.currTemplateGroup.getElementList().stream().map(Template::getName).collect(Collectors.toList()));
        }
        if (this.editorComponent != null) {
            this.editorComponent.setFile(null);
        }
    }
}
