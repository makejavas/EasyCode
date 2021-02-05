package com.sjhy.plugin.ui.base;

import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * 导入或导出方式确定面板
 *
 * @author william
 * @version 1.0.0
 * @since 2021 /02/05 17:12
 */
public class ImOrExportWayConfirmPanel extends JPanel {

    private final Collection<String> items;
    private ListRadioPanel radioPanel;
    private JBTextField valueField;
    /**
     * 导入模式
     */
    private boolean importMode = true;

    /**
     * The Way token.
     */
    public static final String WAY_TOKEN = "网络";
    /**
     * The Way local.
     */
    public static final String WAY_LOCAL = "本地";

    private JButton localSelectBtn;

    /**
     * 默认构造方法
     */
    public ImOrExportWayConfirmPanel() {
        // 使用水平流式布局
        super();
        final GridBagLayout layout = new GridBagLayout();
        final GridBagConstraints constraints = new GridBagConstraints();
        this.items = Arrays.asList(WAY_LOCAL, WAY_TOKEN);
        setLayout(layout);
        this.init(layout, constraints);
    }

    public ImOrExportWayConfirmPanel(boolean importMode) {
        this();
        this.importMode = importMode;
    }

    /**
     * 初始化操作
     *
     * @param layout
     * @param constraints
     */
    private void init(GridBagLayout layout, GridBagConstraints constraints) {
        // 选择面板
        this.radioPanel = new ListRadioPanel(String.format("导%s方式：", importMode ? "入" : "出"), items);
        radioPanel.setSwitchListener(new ListRadioPanel.RadioSwitchListener() {
            @Override
            public void radioSwitch(String radio, boolean selected) {
                if (selected) {
                    switch (radio) {
                        case WAY_TOKEN:
                            localSelectBtn.setVisible(false);
                            valueField.setEditable(importMode);
                            valueField.setText("");
                            break;
                        case WAY_LOCAL:
                            localSelectBtn.setVisible(true);
                            valueField.setEditable(false);
                            break;
                        default:
                    }
                }
            }
        });
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(radioPanel, constraints);
        add(radioPanel);

        // 输入框
        constraints.weightx = 0.5;
        constraints.weighty = 0.2;
        constraints.gridwidth = 1;
        valueField = new JBTextField();
        valueField.setEditable(false);
        layout.setConstraints(valueField, constraints);
        add(valueField);

        // 本地路径选择
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        localSelectBtn = new JButton();
        localSelectBtn.setText("选择");
        localSelectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        });
        layout.setConstraints(localSelectBtn, constraints);
        add(localSelectBtn);
    }

    /**
     * 本地文件路径选择
     */
    private void openFileChooser() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.showDialog(new JLabel(), "选择");
        File file = jfc.getSelectedFile();
        if (file != null) {
            valueField.setText(file.getPath());
        }
    }

    /**
     * 获取已选中的元素
     *
     * @return 已选中的元素 selected
     */
    public String getSelected() {
        return radioPanel.getSelected();
    }

    /**
     * 获取值
     *
     * @return the value
     */
    public String getValue() {
        return valueField.getText();
    }
}
