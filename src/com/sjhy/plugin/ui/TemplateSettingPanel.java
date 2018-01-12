package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.tool.CloneUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TemplateSettingPanel extends AbstractGroupPanel<TemplateGroup, Template> implements Configurable {
    private ConfigService configService = ConfigService.getInstance();
    private CloneUtils cloneUtils = CloneUtils.getInstance();
    private EditTemplatePanel editTemplatePanel;
    TemplateSettingPanel() {
        super(CloneUtils.getInstance().cloneMap(ConfigService.getInstance().getTemplateGroupMap()), ConfigService.getInstance().getCurrTemplateGroupName());
    }

    @Override
    protected void initItemPanel(JPanel itemPanel, Template item) {
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
        return !configService.getTemplateGroupMap().equals(group) || !configService.getCurrTemplateGroupName().equals(currGroupName);
    }

    @Override
    public void apply() {
        configService.setTemplateGroupMap(group);
        configService.setCurrTemplateGroupName(currGroupName);
    }

    @Override
    public void reset() {
        init(cloneUtils.cloneMap(configService.getTemplateGroupMap()), configService.getCurrTemplateGroupName());
    }
}
