package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.comm.ServiceComm;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MainSetting extends ServiceComm implements Configurable, Configurable.Composite {
    private JPanel mainPanel;
    private JComboBox encodeComboBox;
    private JTextField authorTextField;
    private ConfigService configService;

    public MainSetting(ConfigService configService) {
        this.configService = configService;
    }

    private void init() {
        //初始化数据
        authorTextField.setText(configService.getAuthor());
        encodeComboBox.setSelectedItem(configService.getEncode());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Easy Code";
    }

    @NotNull
    @Override
    public Configurable[] getConfigurables() {
        Configurable[] result = new Configurable[1];
        result[0] = new TypeMapperSetting(configService);
        return result;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        init();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        ConfigService configService = getConfigService();
        return !configService.getEncode().equals(encodeComboBox.getSelectedItem()) || !configService.getAuthor().equals(authorTextField.getText());
    }

    @Override
    public void apply() {
        //保存数据
        ConfigService configService = getConfigService();
        configService.setAuthor(authorTextField.getText());
        configService.setEncode((String) encodeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        init();
    }
}
