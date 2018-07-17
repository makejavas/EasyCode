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

public class EditTemplatePanel {
    private JPanel mainPanel;
    private JPanel editPanel;
    private Template template;
    private Editor editor;

    private final static FileType FILE_TYPE = FileTypeManager.getInstance().getFileTypeByExtension("vm");

    private final static GridConstraints GRID_CONSTRAINTS = new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
            null, new Dimension(600, 400), null,
            0, true);

    EditTemplatePanel(Template template) {
        this.template = template;
        init();
    }

    private void init() {
        //初始化系统编辑器
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template.getCode());
        //TODO 退出时，会导致无法创建编辑器。
        editor = factory.createEditor(velocityTemplate, null, FILE_TYPE, false);
        editPanel.add(editor.getComponent(), GRID_CONSTRAINTS);
    }

    public void refresh() {
        this.template.setCode(editor.getDocument().getText());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
