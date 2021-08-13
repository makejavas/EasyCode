package com.sjhy.plugin.ui.base;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;

/**
 * 编辑器设置初始化
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/13 09:26
 */
public class EditorSettingsInit {

    public static void init(Editor editor) {
        EditorSettings editorSettings = editor.getSettings();
        // 关闭虚拟空间
        editorSettings.setVirtualSpace(false);
        // 关闭标记位置（断点位置）
        editorSettings.setLineMarkerAreaShown(false);
        // 关闭缩减指南
        editorSettings.setIndentGuidesShown(false);
        // 显示行号
        editorSettings.setLineNumbersShown(true);
        // 支持代码折叠
        editorSettings.setFoldingOutlineShown(true);
        // 附加行，附加列（提高视野）
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        // 不显示换行符号
        editorSettings.setCaretRowShown(false);
    }

}
