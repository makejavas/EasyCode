package com.sjhy.plugin.core;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * 系统图标
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/12 17:08
 */
@SuppressWarnings("unused")
public final class Icons {
    /**
     * 禁止创建实例对象
     */
    private Icons() {
    }

    public static final Icon ADD_ICON = IconLoader.findIcon("/general/add.png");

    public static final Icon DEL_ICON = IconLoader.findIcon("/general/remove.png");

    public static final Icon COPY_ICON = IconLoader.findIcon("/actions/copy.png");

    public static final Icon ROLLBACK_ICON = IconLoader.findIcon("/actions/rollback.png");
}
