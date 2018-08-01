package com.sjhy.plugin.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.sjhy.plugin.tool.CacheDataUtils;

import javax.swing.*;
import java.awt.*;

/**
 * 模板编辑框面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
public class EditTemplatePanel {
    private JPanel mainPanel;
    private JPanel editPanel;
    private Editor editor;
    /**
     * 初始值
     */
    private String value;
    /**
     * 回调函数
     */
    private Callback callback;

    /**
     * 编辑框支持的文件类型
     */
    private final static FileType FILE_TYPE = FileTypeManager.getInstance().getFileTypeByExtension("vm");

    /**
     * 编辑框大小样式配置
     */
    private final static GridConstraints GRID_CONSTRAINTS = new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
            null, new Dimension(600, 400), null,
            0, true);
    /**
     * 初始化标记
     */
    private boolean initFlag;

    /**
     * 默认构造方法
     *
     * @param value    初始值
     * @param callback 回调函数
     */
    EditTemplatePanel(String value, Callback callback) {
        this.value = value;
        this.callback = callback;
    }

    /**
     * 初始化编辑框
     */
    public void init() {
        if (initFlag) {
            return;
        }
        initFlag = true;
        //初始化系统编辑器
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(value);
        editor = factory.createEditor(velocityTemplate, CacheDataUtils.getInstance().getProject(), FILE_TYPE, false);
        editPanel.add(editor.getComponent(), GRID_CONSTRAINTS);
    }

    /**
     * 刷新编辑可内容
     */
    public void refresh() {
        if (editor == null) {
            return;
        }
        this.callback.refreshValue(editor.getDocument().getText());
    }

    /**
     * 获取主面板
     *
     * @return 主面板
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * 释放掉编辑框，防止内存占用
     */
    public void disposeEditor() {
        if (editor != null && !editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
    }

    /**
     * 回调
     */
    public interface Callback {
        /**
         * 刷新保存的值
         *
         * @param value 值
         */
        void refreshValue(String value);
    }
}
