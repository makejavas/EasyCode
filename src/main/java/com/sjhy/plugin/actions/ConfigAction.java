package com.sjhy.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.core.TemplateEditor;
import org.jetbrains.annotations.Nullable;

/**
 * 表配置菜单
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class ConfigAction extends AnAction {
    /**
     * 构造方法
     *
     * @param text 菜单名称
     */
    ConfigAction(@Nullable String text) {
        super(text);
    }

    /**
     * 处理方法
     *
     * @param event 事件对象
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
//        new ConfigTableDialog().open();

        Project project = event.getProject();
        if (project == null) {
            return;
        }

//        UrlUtil.loadText()

        FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("ft");
        TemplateEditor templateEditor = new TemplateEditor(project, "test.ft", "内容", "<html><body><span style='color:red'>Test</span></body></html>", velocityFileType);

//        AbstractGroupPanel groupPanel = new AbstractGroupPanel();
//
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//                templateEditor.onClose();
//            }
//        });
//
//        frame.setContentPane(groupPanel.createComponent(new JPanel()));
//
//        frame.setSize(600, 400);
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//        frame.toFront();

        System.out.println("OK");
    }
}
