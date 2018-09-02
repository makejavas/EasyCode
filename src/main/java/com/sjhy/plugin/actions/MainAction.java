package com.sjhy.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.ui.SelectSavePath;
import org.jetbrains.annotations.Nullable;

/**
 * 代码生成菜单
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class MainAction extends AnAction {
    /**
     * 构造方法
     *
     * @param text 菜单名称
     */
    MainAction(@Nullable String text) {
        super(text);
    }

    /**
     * 处理方法
     *
     * @param event 事件对象
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        //开始处理
        new SelectSavePath(event.getProject()).open();
    }
}
