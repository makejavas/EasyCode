package com.sjhy.plugin.ui.component;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SeparatorFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.sjhy.plugin.entity.AbstractEditorItem;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.ui.base.EditorSettingsInit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 编辑器组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/11 13:16
 */
public class EditorComponent<T extends AbstractEditorItem> {
    /**
     * 主面板
     */
    @Getter
    private JPanel mainPanel;
    /**
     * 被编辑的文件
     */
    @Getter
    private T file;
    /**
     * 描述信息
     */
    private String remark;

    /**
     * 编辑器组件
     */
    private Editor editor;

    public EditorComponent(T file, String remark) {
        this.file = file;
        this.remark = remark;
        this.init();
    }

    public void init() {
        this.mainPanel = new JPanel(new BorderLayout());
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument("");
        this.editor = editorFactory.createEditor(document);
        this.refreshUI();
        // 初始默认设置
        EditorSettingsInit.init(this.editor);
        // 添加监控事件
        this.editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {

            }

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (file != null) {
                    file.changeFileContent(editor.getDocument().getText());
                }
            }
        });
        // 初始化描述面板
        this.initRemarkPanel();
    }

    private void initRemarkPanel() {
        // 描述信息
        JEditorPane editorPane = new JEditorPane();
        // html形式展示
        editorPane.setEditorKit(UIUtil.getHTMLEditorKit());
        // 仅查看
        editorPane.setEditable(false);
        editorPane.setText(remark);
        // 添加浏览器链接监听事件
        editorPane.addHyperlinkListener(new BrowserHyperlinkListener());

        // 描述面板
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.add(SeparatorFactory.createSeparator(IdeBundle.message("label.description"), null),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insetsBottom(2), 0, 0));
        descriptionPanel.add(ScrollPaneFactory.createScrollPane(editorPane),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        JBUI.insetsTop(2), 0, 0));

        // 分割器
        Splitter splitter = new Splitter(true, 0.6F);
        splitter.setFirstComponent(editor.getComponent());
        splitter.setSecondComponent(descriptionPanel);

        this.mainPanel.add(splitter, BorderLayout.CENTER);
        this.mainPanel.setPreferredSize(JBUI.size(400, 300));
    }

    public void setFile(T file) {
        this.file = file;
        this.refreshUI();
    }

    private void refreshUI() {
        if (this.file == null) {
            ((EditorImpl)this.editor).setViewer(true);
            // 重置文本内容
            WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> this.editor.getDocument().setText(""));
            ((EditorEx)editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(null, "demo.java.vm"));
        } else {
            ((EditorImpl)this.editor).setViewer(false);
            // 重置文本内容
            WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> this.editor.getDocument().setText(this.file.fileContent()));
            ((EditorEx)editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), this.file.fileName()));
        }
    }
}
