package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.comm.ServiceComm;
import com.sjhy.plugin.tool.ConfigInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MainSetting extends ServiceComm implements Configurable, Configurable.Composite {
    /**
     * 主面板
     */
    private JPanel mainPanel;
    /**
     * 编码选择下拉框
     */
    private JComboBox encodeComboBox;
    /**
     * 作者编辑框
     */
    private JTextField authorTextField;
    /**
     * 全局配置服务
     */
    private ConfigInfo configInfo;

    public MainSetting(ConfigInfo configInfo) {
        this.configInfo = configInfo;
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        //初始化数据
        authorTextField.setText(configInfo.getAuthor());
        encodeComboBox.setSelectedItem(configInfo.getEncode());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Easy Code";
    }

    /**
     * 更多配置
     *
     * @return 配置选项
     */
    @NotNull
    @Override
    public Configurable[] getConfigurables() {
        Configurable[] result = new Configurable[3];
        result[0] = new TypeMapperSetting(configInfo);
        result[1] = new TemplateSettingPanel();
        result[2] = new TableSettingPanel();
        return result;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !configInfo.getEncode().equals(encodeComboBox.getSelectedItem()) || !configInfo.getAuthor().equals(authorTextField.getText());
    }

    @Override
    public void apply() {
        //保存数据
        configInfo.setAuthor(authorTextField.getText());
        configInfo.setEncode((String) encodeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        init();
    }
}
