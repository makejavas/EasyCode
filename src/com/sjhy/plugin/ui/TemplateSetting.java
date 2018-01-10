package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TemplateSetting implements Configurable {
    private JPanel mainPanel;
    private JButton copyGroupButton;
    private JButton deleteButton;
    private JTabbedPane templateTabbedPane;
    private JComboBox groupButton;
    private JButton addButton;
    private JButton removeButton;

    private ConfigService configService;

    TemplateSetting(ConfigService configService) {
        this.configService = configService;
        init();
    }

    private void init() {
        this.templateTabbedPane.addTab("entity", new EditTemplatePanel().getMainPanel());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.mainPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {

    }
}
