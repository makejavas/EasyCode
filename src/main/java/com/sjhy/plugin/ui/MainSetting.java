package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.comm.AbstractService;
import com.sjhy.plugin.tool.ConfigInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 主设置面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class MainSetting extends AbstractService implements Configurable, Configurable.Composite {
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
     * 默认构造方法
     */
    public MainSetting() {
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

    /**
     * 设置显示名称
     *
     * @return 显示名称
     */
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
        Configurable[] result = new Configurable[4];
        result[0] = new TypeMapperSetting(configInfo);
        result[1] = new TemplateSettingPanel();
        result[2] = new TableSettingPanel();
        result[3] = new GlobalConfigSettingPanel();
        return result;
    }

    /**
     * 获取主面板信息
     *
     * @return 主面板
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    /**
     * 判断是否修改
     *
     * @return 是否修改
     */
    @Override
    public boolean isModified() {
        return !configInfo.getEncode().equals(encodeComboBox.getSelectedItem()) || !configInfo.getAuthor().equals(authorTextField.getText());
    }

    /**
     * 应用修改
     */
    @Override
    public void apply() {
        //保存数据
        configInfo.setAuthor(authorTextField.getText());
        configInfo.setEncode((String) encodeComboBox.getSelectedItem());
    }

    /**
     * 重置
     */
    @Override
    public void reset() {
        init();
    }
}
