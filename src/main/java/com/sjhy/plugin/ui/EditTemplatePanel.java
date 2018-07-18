package com.sjhy.plugin.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.sjhy.plugin.entity.Template;

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
    private Template template;
    private Editor editor;

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
     * 默认构造方法
     * @param template
     */
    EditTemplatePanel(Template template) {
        this.template = template;
        init();
    }

    /**
     * 初始化编辑框
     */
    private void init() {
        //初始化系统编辑器
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template.getCode());
        //TODO 退出时，会导致无法创建编辑器。
        editor = factory.createEditor(velocityTemplate, null, FILE_TYPE, false);
        editPanel.add(editor.getComponent(), GRID_CONSTRAINTS);
    }

    /**
     * 刷新编辑可内容
     */
    public void refresh() {
        this.template.setCode(editor.getDocument().getText());
    }

    /**
     * 获取主面板
     * @return 主面板
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * 释放掉编辑框，防止内存占用
     */
    public void disposeEditor() {
        if (editor!=null && !editor.isDisposed()) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
    }
}
