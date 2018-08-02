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
     * 编辑框面板
     */
    private EditTemplatePanel editTemplatePanel;

    /**
     * 默认构造方法
     */
    public TemplateSettingPanel() {
        super(CloneUtils.getInstance().cloneMap(ConfigInfo.getInstance().getTemplateGroupMap()), ConfigInfo.getInstance().getCurrTemplateGroupName());
    }

    /**
     * 切换模板编辑时
     *
     * @param itemPanel 面板对象
     * @param item      模板对象
     */
    @Override
    protected void initItemPanel(JPanel itemPanel, Template item) {
        // 如果编辑面板已经实例化，需要选释放后再初始化
        if (editTemplatePanel != null) {
            editTemplatePanel.disposeEditor();
        }
        itemPanel.removeAll();
        editTemplatePanel = new EditTemplatePanel(item.getCode(), item::setCode);
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
        return super.mainPanel;
    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        // 修复BUG，当初始未完成时，插件进行修改判断
        if (editTemplatePanel != null) {
            editTemplatePanel.refresh();
        }
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
        if (editTemplatePanel != null) {
            editTemplatePanel.disposeEditor();
        }
    }
}
