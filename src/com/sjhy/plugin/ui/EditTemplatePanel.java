package com.sjhy.plugin.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import java.awt.*;

public class EditTemplatePanel {
    private JTextArea textArea;
    private JPanel mainPanel;
    private Editor editor;

    public EditTemplatePanel() {
        addVmEditor("test");
    }

    private void addVmEditor(String template) {
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        GridConstraints constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, 300), null, 0, true);
        mainPanel.add(editor.getComponent(), constraints);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
