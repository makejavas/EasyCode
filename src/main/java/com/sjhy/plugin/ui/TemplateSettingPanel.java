package com.sjhy.plugin.ui;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.ui.base.BaseGroupPanel;
import com.sjhy.plugin.ui.base.BaseItemSelectPanel;
import com.sjhy.plugin.ui.base.TemplateEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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
     * 配置信息
     */
    private ConfigInfo configInfo;

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

    public TemplateSettingPanel() {
        // 配置服务实例化
        this.configInfo = ConfigInfo.getInstance();
        // 克隆工具实例化
        this.cloneUtils = CloneUtils.getInstance();
        // 克隆对象
        this.currGroupName = this.configInfo.getCurrTemplateGroupName();
        this.group = this.cloneUtils.cloneMap(this.configInfo.getTemplateGroupMap());
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
        this.baseGroupPanel = new BaseGroupPanel(new ArrayList<>(group.keySet())) {
            @Override
            protected void createGroup(String name) {

            }

            @Override
            protected void deleteGroup(String name) {

            }

            @Override
            protected void copyGroup(String name) {

            }

            @Override
            protected void changeGroup(String name) {

            }
        };

        // 创建元素选择面板
        this.baseItemSelectPanel = new BaseItemSelectPanel<Template>(group.get(currGroupName).getElementList()) {
            @Override
            protected void addItem(String name) {

            }

            @Override
            protected void copyItem(Template item) {

            }

            @Override
            protected void deleteItem(Template item) {

            }

            @Override
            protected void selectedItem(Template item) {
                // 如果编辑面板已经实例化，需要选释放后再初始化
                if (templateEditor == null) {
                    FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
                    templateEditor = new TemplateEditor(ProjectManager.getInstance().getDefaultProject(), item.getName() + ".vm", item.getCode(), "描述", velocityFileType);
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    baseItemSelectPanel.getRightPanel().add(templateEditor.createComponent(), BorderLayout.CENTER);
                } else {
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    WriteAction.run(() -> templateEditor.getEditor().getDocument().setText(item.getCode()));
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

    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        return !configInfo.getTemplateGroupMap().equals(group) || !configInfo.getCurrTemplateGroupName().equals(currGroupName);
    }

    /**
     * 保存方法
     */
    @Override
    public void apply() {
        configInfo.setTemplateGroupMap(group);
        configInfo.setCurrTemplateGroupName(currGroupName);
    }

    /**
     * 重置方法
     */
    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        // 防止对象篡改，需要进行克隆
        this.group = cloneUtils.cloneMap(configInfo.getTemplateGroupMap());
        this.currGroupName = configInfo.getCurrTemplateGroupName();
    }

    /**
     * 关闭回调方法
     */
    @Override
    public void disposeUIResources() {
        // 修复兼容性问题
        if (templateEditor != null) {
            templateEditor.onClose();
        }
    }
}
