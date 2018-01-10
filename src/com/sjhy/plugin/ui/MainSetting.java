package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.comm.ServiceComm;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MainSetting extends ServiceComm implements Configurable, Configurable.Composite {
    //主面板
    private JPanel mainPanel;
    //编码选择下拉框
    private JComboBox encodeComboBox;
    //作者编辑框
    private JTextField authorTextField;
    //全局配置服务
    private ConfigService configService;

    public MainSetting(ConfigService configService) {
        this.configService = configService;
        init();
    }

    /**
     * 初始化方法
     */
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

    /**
     * 更多配置
     * @return 配置选项
     */
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
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !configService.getEncode().equals(encodeComboBox.getSelectedItem()) || !configService.getAuthor().equals(authorTextField.getText());
    }

    @Override
    public void apply() {
        //保存数据
        configService.setAuthor(authorTextField.getText());
        configService.setEncode((String) encodeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        init();
    }
}
