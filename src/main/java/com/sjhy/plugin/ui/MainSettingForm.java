package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.SettingsStorageService;
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
public class MainSettingForm implements Configurable, Configurable.Composite {
    /**
     * 设置储存传输对象
     */
    private SettingsStorageDTO settingsStorage;
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

    private void loadStorageValue() {
        this.versionLabel.setText(this.settingsStorage.getVersion());
        this.authorEditor.setText(this.settingsStorage.getAuthor());
        this.userSecureEditor.setText(this.settingsStorage.getUserSecure());
        if (StringUtils.isEmpty(this.settingsStorage.getUserSecure())) {
            this.pullBtn.setEnabled(false);
            this.pushBtn.setEnabled(false);
        } else {
            this.pullBtn.setEnabled(true);
            this.pushBtn.setEnabled(true);
        }
    }

    private void initEvent() {
        this.resetBtn.addActionListener(e -> JBPopupFactory.getInstance()
                .createConfirmation("确认恢复默认设置，所有Default分组配置将被重置？", () -> {
                    // 重置默认值后重新加载配置
                    settingsStorage.resetDefaultVal();
                    this.loadStorageValue();
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
    public @NotNull Configurable[] getConfigurables() {
        Configurable[] result = new Configurable[]{
                new TypeMapperSettingForm(),
//                new TemplateSettingPanel(),
//                new TableSettingPanel(),
//                new GlobalConfigSettingPanel()
        };
        return result;
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.settingsStorage = SettingsStorageService.getSettingsStorage();
        // 加载储存数据
        this.loadStorageValue();
        // 初始化事件
        this.initEvent();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        if (!Objects.equals(this.authorEditor.getText(), settingsStorage.getAuthor())) {
            return true;
        }
        if (!Objects.equals(this.userSecureEditor.getText(), settingsStorage.getUserSecure())) {
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        this.loadStorageValue();
    }

    @Override
    public void apply() throws ConfigurationException {
        String author = this.authorEditor.getText();
        if (StringUtils.isEmpty(author)) {
            throw new ConfigurationException("作者名称不能为空");
        }
        this.settingsStorage.setAuthor(author);
        String userSecure = this.userSecureEditor.getText();
        this.settingsStorage.setUserSecure(userSecure);
    }
}
