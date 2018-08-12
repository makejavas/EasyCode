package com.sjhy.plugin.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import com.sjhy.plugin.core.BaseGroupPanel;
import com.sjhy.plugin.core.BaseItemSelectPanel;
import com.sjhy.plugin.core.TemplateEditor;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * 模板编辑主面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
public class TemplateSettingPanel extends AbstractGroupPanel<TemplateGroup, Template> implements Configurable {
    /**
     * 配置信息
     */
    private ConfigInfo configInfo = ConfigInfo.getInstance();
    /**
     * 编辑框面板
     */
    private TemplateEditor templateEditor;

    /**
     * 默认构造方法
     */
    public TemplateSettingPanel() {
        super(CloneUtils.getInstance().cloneMap(ConfigInfo.getInstance().getTemplateGroupMap()), ConfigInfo.getInstance().getCurrTemplateGroupName());
    }

    private Template item;

    /**
     * 切换模板编辑时
     *
     * @param itemPanel 面板对象
     * @param item      模板对象
     */
    @Override
    protected void initItemPanel(JPanel itemPanel, Template item) {
        // 如果编辑面板已经实例化，需要选释放后再初始化
        this.item = item;
    }

    @Override
    protected String getItemName(Template item) {
        return item.getName();
    }

    @Override
    protected void setItemName(Template item, String itemName) {
        item.setName(itemName);
    }

    @Override
    protected Template createItem(String name) {
        return new Template(name, "Demo!");
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
        // 如果编辑面板已经实例化，需要选释放后再初始化
        if (templateEditor == null) {
            FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
            templateEditor = new TemplateEditor(ProjectManager.getInstance().getDefaultProject(), item.getName() + ".vm", item.getCode(), "描述", velocityFileType);
        }
//        com.sjhy.plugin.core.AbstractGroupPanel groupPanel = new com.sjhy.plugin.core.AbstractGroupPanel();
//        return groupPanel.createComponent(templateEditor.createComponent());
        BaseItemSelectPanel<String> baseItemSelectPanel = new BaseItemSelectPanel<String>(Arrays.asList("item1", "item2", "item3")) {

            @Override
            protected void addItem(String name) {

            }

            @Override
            protected void copyItem(String item) {

            }

            @Override
            protected void deleteItem(List<String> itemList) {

            }
        };
        baseItemSelectPanel.getRightPanel().add(templateEditor.createComponent(), BorderLayout.CENTER);

        BaseGroupPanel groupPanel = new BaseGroupPanel(Arrays.asList("Default", "Mybatis Plus")) {
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
            protected void chageGroup(String name) {

            }
        };

        JPanel result = new JPanel(new BorderLayout());
        result.add(groupPanel, BorderLayout.NORTH);
        result.add(baseItemSelectPanel, BorderLayout.CENTER);
        return result;
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
        // 防止对象篡改，需要进行克隆
        super.group = cloneUtils.cloneMap(configInfo.getTemplateGroupMap());
        super.currGroupName = configInfo.getCurrTemplateGroupName();
        super.init();
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
