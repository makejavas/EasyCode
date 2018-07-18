package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
     * 对象克隆工具
     */
    private CloneUtils cloneUtils = CloneUtils.getInstance();
    /**
     * 编辑框面板
     */
    private EditTemplatePanel editTemplatePanel;

    /**
     * 默认构造方法
     */
    TemplateSettingPanel() {
        super(CloneUtils.getInstance().cloneMap(ConfigInfo.getInstance().getTemplateGroupMap()), ConfigInfo.getInstance().getCurrTemplateGroupName());
    }

    /**
     * 切换模板编辑时
     * @param itemPanel 面板对象
     * @param item 模板对象
     */
    @Override
    protected void initItemPanel(JPanel itemPanel, Template item) {
        if (editTemplatePanel!=null) {
            editTemplatePanel.disposeEditor();
        }
        itemPanel.removeAll();
        editTemplatePanel = new EditTemplatePanel(item);
        itemPanel.add(editTemplatePanel.getMainPanel());
        itemPanel.updateUI();
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

    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return getMainPanel();
    }

    @Override
    public boolean isModified() {
        editTemplatePanel.refresh();
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
        init(cloneUtils.cloneMap(configInfo.getTemplateGroupMap()), configInfo.getCurrTemplateGroupName());
    }

    /**
     * 关闭回调方法
     */
    @Override
    public void disposeUIResources() {
        editTemplatePanel.disposeEditor();
    }
}
