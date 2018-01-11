package com.sjhy.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.ui.SelectSavePath;
import org.jetbrains.annotations.Nullable;

public class MainAction extends AnAction {
    MainAction(@Nullable String text) {
        super(text);
    }

    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    @Override
    public void actionPerformed(AnActionEvent e) {
        //开始处理
        new SelectSavePath().open();
    }
}
