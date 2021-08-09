package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.tool.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 09:22
 */
public class MainSettingForm implements Configurable, Configurable.Composite, BaseSettings {
    private JLabel versionLabel;
    private JButton resetBtn;
    private JButton pushBtn;
    private JButton pullBtn;
    private JButton exportByNetBtn;
    private JButton importByNetBtn;
    private JButton exportByFileBtn;
    private JButton importByFileBtn;
    private JButton exportByClipboardBtn;
    private JButton importByClipboardBtn;
    private JPanel mainPanel;
    private JTextField userSecureEditor;
    private JTextField authorEditor;

    public MainSettingForm() {
    }

    private void initEvent() {
        this.resetBtn.addActionListener(e -> JBPopupFactory.getInstance()
                .createConfirmation("确认恢复默认设置，所有Default分组配置将被重置？", () -> {
                    // 重置默认值后重新加载配置
                    getSettingsStorage().resetDefaultVal();
                    this.loadSettingsStore();
                }, 0)
                .showInFocusCenter()
        );

        this.userSecureEditor.addCaretListener(e -> {
            String userSecure = this.userSecureEditor.getText();
            if (StringUtils.isEmpty(userSecure)) {
                this.pullBtn.setEnabled(false);
                this.pushBtn.setEnabled(false);
            } else {
                this.pullBtn.setEnabled(true);
                this.pushBtn.setEnabled(true);
            }
        });
    }

    @Override
    public String getDisplayName() {
        return "EasyCode";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    @Override
    public Configurable @NotNull [] getConfigurables() {
        Configurable[] result = new Configurable[]{
                new TypeMapperSettingForm(),
//                new TemplateSettingPanel(),
//                new TableSettingPanel(),
//                new GlobalConfigSettingPanel()
        };
        // 初始装置配置信息
        for (Configurable configurable : result) {
            if (configurable instanceof BaseSettings) {
                ((BaseSettings) configurable).loadSettingsStore();
            }
        }
        return result;
    }

    @Override
    public @Nullable JComponent createComponent() {
        // 加载储存数据
        this.loadSettingsStore();
        // 初始化事件
        this.initEvent();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        if (!Objects.equals(this.authorEditor.getText(), getSettingsStorage().getAuthor())) {
            return true;
        }
        if (!Objects.equals(this.userSecureEditor.getText(), getSettingsStorage().getUserSecure())) {
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        String author = this.authorEditor.getText();
        if (StringUtils.isEmpty(author)) {
            throw new ConfigurationException("作者名称不能为空");
        }
        getSettingsStorage().setAuthor(author);
        String userSecure = this.userSecureEditor.getText();
        getSettingsStorage().setUserSecure(userSecure);
    }

    /**
     * 加载配置信息
     *
     * @param settingsStorage 配置信息
     */
    @Override
    public void loadSettingsStore(SettingsStorageDTO settingsStorage) {
        this.versionLabel.setText(settingsStorage.getVersion());
        this.authorEditor.setText(settingsStorage.getAuthor());
        this.userSecureEditor.setText(settingsStorage.getUserSecure());
        if (StringUtils.isEmpty(settingsStorage.getUserSecure())) {
            this.pullBtn.setEnabled(false);
            this.pushBtn.setEnabled(false);
        } else {
            this.pullBtn.setEnabled(true);
            this.pushBtn.setEnabled(true);
        }
    }
}
