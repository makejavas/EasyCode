package com.sjhy.plugin.ui;

import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.ui.base.BaseGroupPanel;
import com.sjhy.plugin.ui.base.BaseItemSelectPanel;
import com.sjhy.plugin.ui.base.TemplateEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模板编辑主面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
public class TemplateSettingPanel implements Configurable {
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

    /**
     * 配置信息
     */
    private Settings settings;

    /**
     * 编辑框面板
     */
    private TemplateEditor templateEditor;

    /**
     * 基本的分组面板
     */
    private BaseGroupPanel baseGroupPanel;

    /**
     * 基本的元素选择面板
     */
    private BaseItemSelectPanel<Template> baseItemSelectPanel;

    /**
     * 当前分组
     */
    private Map<String, TemplateGroup> group;

    /**
     * 当前选中分组
     */
    private String currGroupName;

    /**
     * 克隆工具
     */
    private CloneUtils cloneUtils;

    /**
     * 项目对象
     */
    private Project project;

    TemplateSettingPanel() {
        // 存在打开的项目则使用打开的项目，否则使用默认项目
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();
        // 项目对象
        this.project = openProjects.length > 0 ? openProjects[0] : projectManager.getDefaultProject();
        // 配置服务实例化
        this.settings = Settings.getInstance();
        // 克隆工具实例化
        this.cloneUtils = CloneUtils.getInstance();
        // 克隆对象
        this.currGroupName = this.settings.getCurrTemplateGroupName();
        this.group = this.cloneUtils.cloneMap(this.settings.getTemplateGroupMap());
    }

    /**
     * 获取设置显示的名称
     *
     * @return 名称
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    /**
     * 获取主面板对象
     *
     * @return 主面板对象
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 实例化分组面板
        this.baseGroupPanel = new BaseGroupPanel(new ArrayList<>(group.keySet()), this.currGroupName) {
            @Override
            protected void createGroup(String name) {
                // 创建分组
                TemplateGroup templateGroup = new TemplateGroup();
                templateGroup.setName(name);
                templateGroup.setElementList(new ArrayList<>());
                group.put(name, templateGroup);
                currGroupName = name;
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
                // 创建空白分组，需要清空输入框
                templateEditor.reset("empty", "");
            }

            @Override
            protected void deleteGroup(String name) {
                // 删除分组
                group.remove(name);
                currGroupName = Settings.DEFAULT_NAME;
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
            }

            @Override
            protected void copyGroup(String name) {
                // 复制分组
                TemplateGroup templateGroup = cloneUtils.clone(group.get(currGroupName));
                templateGroup.setName(name);
                currGroupName = name;
                group.put(name, templateGroup);
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
            }

            @Override
            protected void changeGroup(String name) {
                currGroupName = name;
                if (baseItemSelectPanel == null) {
                    return;
                }
                // 重置模板选择
                baseItemSelectPanel.reset(group.get(currGroupName).getElementList(), 0);
                if (group.get(currGroupName).getElementList().isEmpty()) {
                    // 没有元素时，需要清空编辑框
                    templateEditor.reset("empty", "");
                }
            }
        };

        // 创建元素选择面板
        this.baseItemSelectPanel = new BaseItemSelectPanel<Template>(group.get(currGroupName).getElementList()) {
            @Override
            protected void addItem(String name) {
                List<Template> templateList = group.get(currGroupName).getElementList();
                // 新增模板
                templateList.add(new Template(name, ""));
                baseItemSelectPanel.reset(templateList, templateList.size()-1);
            }

            @Override
            protected void copyItem(String newName, Template item) {
                // 复制模板
                Template template = cloneUtils.clone(item);
                template.setName(newName);
                List<Template> templateList = group.get(currGroupName).getElementList();
                templateList.add(template);
                baseItemSelectPanel.reset(templateList, templateList.size()-1);
            }

            @Override
            protected void deleteItem(Template item) {
                // 删除模板
                group.get(currGroupName).getElementList().remove(item);
                baseItemSelectPanel.reset(group.get(currGroupName).getElementList(), 0);
                if (group.get(currGroupName).getElementList().isEmpty()) {
                    // 没有元素时，需要清空编辑框
                    templateEditor.reset("empty", "");
                }
            }

            @Override
            protected void selectedItem(Template item) {
                // 如果编辑面板已经实例化，需要选释放后再初始化
                if (templateEditor == null) {
                    FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
                    templateEditor = new TemplateEditor(project, item.getName() + ".vm", item.getCode(), TEMPLATE_DESCRIPTION_INFO, velocityFileType);
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    baseItemSelectPanel.getRightPanel().add(templateEditor.createComponent(), BorderLayout.CENTER);
                } else {
                    // 更新代码
                    templateEditor.reset(item.getName(), item.getCode());
                }
            }
        };

        mainPanel.add(baseGroupPanel, BorderLayout.NORTH);
        mainPanel.add(baseItemSelectPanel.getComponent(), BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * 数据发生修改时调用
     */
    private void onUpdate() {
        // 同步修改的代码
        Template template = baseItemSelectPanel.getSelectedItem();
        if (template != null) {
            template.setCode(templateEditor.getEditor().getDocument().getText());
        }
    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        return !settings.getTemplateGroupMap().equals(group) || !settings.getCurrTemplateGroupName().equals(currGroupName);
    }

    /**
     * 保存方法
     */
    @Override
    public void apply() {
        settings.setTemplateGroupMap(group);
        settings.setCurrTemplateGroupName(currGroupName);
    }

    /**
     * 重置方法
     */
    @Override
    public void reset() {
        // 没修改过的清空下不需要重置
        if (!isModified()) {
            return;
        }
        // 防止对象篡改，需要进行克隆
        this.group = cloneUtils.cloneMap(settings.getTemplateGroupMap());
        this.currGroupName = settings.getCurrTemplateGroupName();
        // 重置元素选择面板
        baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
    }

    /**
     * 关闭回调方法
     */
    @Override
    public void disposeUIResources() {
        // 释放编辑框
        if (templateEditor != null) {
            templateEditor.onClose();
        }
    }
}
