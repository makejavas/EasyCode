package com.sjhy.plugin.ui.base;

import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.sjhy.plugin.tool.CollectionUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author william
 * @version 1.0.0
 * @since 2021/02/05 17:12
 */
public class ListRadioPanel extends JPanel {
    /**
     * 标题
     */
    private String title;
    /**
     * 原属列表
     */
    private Collection<String> items;
    /**
     * 复选框列表
     */
    private List<JBRadioButton> radioButtonList;

    /**
     * 默认构造方法
     */
    public ListRadioPanel(String title, Collection<String> items) {
        // 使用水平流式布局
        super(new HorizontalLayout(16));
        this.title = title;
        this.items = items;
        this.init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        if (title != null && title.length() > 0) {
            JLabel label = new JLabel();
            label.setText(title);
            add(label);
        }
        if (CollectionUtil.isEmpty(items)) {
            return;
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        radioButtonList = new ArrayList<>(items.size());
        for (String item : items) {
            JBRadioButton radioButton = new JBRadioButton(item);
            radioButtonList.add(radioButton);
            buttonGroup.add(radioButton);
            add(radioButton);
        }
        // 默认选定第一个
        radioButtonList.get(0).setSelected(true);
    }

    public void setSwitchListener(RadioSwitchListener switchListener) {
        for (JBRadioButton radioButton : this.radioButtonList) {
            if (switchListener != null) {
                radioButton.addActionListener(switchListener);
            }
        }
    }

    /**
     * 获取已选中的元素
     *
     * @return 已选中的元素
     */
    public String getSelected() {
        if (CollectionUtil.isEmpty(radioButtonList)) {
            return "";
        }
        for (JBRadioButton radioButton : radioButtonList) {
            if (radioButton.isSelected()) {
                return radioButton.getText();
            }
        }
        return "";
    }

    /**
     * 切换监听
     */
    public interface RadioSwitchListener extends ActionListener {

        @Override
        default void actionPerformed(ActionEvent e) {
            final JBRadioButton source = (JBRadioButton) e.getSource();
            radioSwitch(source.getText(), source.isSelected());
        }

        void radioSwitch(String radio, boolean selected);
    }
}
