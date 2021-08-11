package com.sjhy.plugin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.tool.ProjectUtils;

import java.io.File;
import java.io.IOException;

/**
 * 本地文件导入导出设置服务实现
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/11 17:28
 */
public class LocalFileExportImportSettingsServiceImpl implements ExportImportSettingsService {
    /**
     * 导出设置
     *
     * @param settingsStorage 要导出的设置
     */
    @Override
    public void exportConfig(SettingsStorageDTO settingsStorage) {
        // 1.选择储存位置
        FileSaverDialog saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(new FileSaverDescriptor("Save Config As Json", "Save to"), ProjectUtils.getCurrProject());
        VirtualFileWrapper saveFile = saveFileDialog.save(null, "EasyConfig.json");
        if (saveFile == null) {
            return;
        }
        File file = saveFile.getFile();
        // 2.执行导出
        try {
            FileUtil.writeToFile(file, new ObjectMapper().writeValueAsBytes(settingsStorage));
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        }

    }

    /**
     * 导入设置
     *
     * @return 设置信息
     */
    @Override
    public SettingsStorageDTO importConfig() {
        return null;
    }
}
