package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.sjhy.plugin.tool.CollectionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 列表复选框组件
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/03 17:12
 */
public class ListCheckboxComponent extends JPanel {
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
    private List<JBCheckBox> checkBoxList;

    /**
     * 默认构造方法
     */
    public ListCheckboxComponent(String title, Collection<String> items) {
        // 使用垂直流式布局
        super(new VerticalFlowLayout());
        this.title = title;
        this.items = items;
        this.init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        JTextPane textPane = new JTextPane();
        textPane.setText(title);
        textPane.setEditable(false);
        add(textPane);
        if (CollectionUtil.isEmpty(items)) {
            return;
        }
        checkBoxList = new ArrayList<>(items.size());
        for (String item : items) {
            JBCheckBox checkBox = new JBCheckBox(item);
            checkBoxList.add(checkBox);
            add(checkBox);
        }
    }

    /**
     * 获取已选中的元素
     *
     * @return 已选中的元素
     */
    public List<String> getSelectedItems() {
        if (CollectionUtil.isEmpty(checkBoxList)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        checkBoxList.forEach(checkBox -> {
            if (checkBox.isSelected()) {
                result.add(checkBox.getText());
            }
        });
        return result;
    }
}
