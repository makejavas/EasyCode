package com.sjhy.plugin.factory;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.stream.Stream;

/**
 * 表格编辑器构建工厂类
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 13:38
 */
public class CellEditorFactory {

    /**
     * 创建下拉框编辑器
     *
     * @param editable 可编辑的
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createComboBoxEditor(boolean editable, Class<? extends Enum> enumCls) {
        Enum[] enumConstants = enumCls.getEnumConstants();
        return createComboBoxEditor(editable, Stream.of(enumConstants).map(Enum::name).toArray(value -> new String[enumConstants.length]));
    }

    /**
     * 创建下拉框编辑器
     *
     * @param editable 可编辑的
     * @param items    选项
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createComboBoxEditor(boolean editable, String... items) {
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setEditable(editable);
        // 配色
        if (comboBox.getPopup() != null) {
            comboBox.getPopup().getList().setBackground(JBColor.WHITE);
            comboBox.getPopup().getList().setForeground(JBColor.GREEN);
        }
        if (!editable) {
            transmitFocusEvent(comboBox);
        }
        return new DefaultCellEditor(comboBox);
    }

    public static TableCellEditor createBooleanEditor() {
        return new BooleanTableCellEditor();
    }

    /**
     * 创建文本框编辑器
     *
     * @return {@link TableCellEditor}
     */
    public static TableCellEditor createTextFieldEditor() {
        JBTextField textField = new JBTextField();
        transmitFocusEvent(textField);
        return new DefaultCellEditor(textField);
    }

    /**
     * 传递失去焦点事件
     *
     * @param component 组件
     */
    private static void transmitFocusEvent(JComponent component) {
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                // 失去焦点时向上层发起事件通知，使table的值能够正常回写
                ActionListener[] actionListeners = component.getListeners(ActionListener.class);
                if (actionListeners == null) {
                    return;
                }
                for (ActionListener actionListener : actionListeners) {
                    actionListener.actionPerformed(null);
                }
            }
        });
    }

}
