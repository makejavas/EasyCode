package com.sjhy.plugin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ui.TextTransferable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.ExportImportSettingsService;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;

/**
 * 剪切板导入导出配置服务实现
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/12 14:57
 */
public class ClipboardExportImportSettingsServiceImpl implements ExportImportSettingsService {
    /**
     * 导出设置
     *
     * @param settingsStorage 要导出的设置
     */
    @Override
    public void exportConfig(SettingsStorageDTO settingsStorage) {
        try {
            String json = new ObjectMapper().writeValueAsString(settingsStorage);
            CopyPasteManager.getInstance().setContents(new TextTransferable(json));
            Messages.showInfoMessage("Config info success write to clipboard！", GlobalDict.TITLE_INFO);
        } catch (JsonProcessingException e) {
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
        String json = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
        try {
            return new ObjectMapper().readValue(json, SettingsStorageDTO.class);
        } catch (IOException e) {
            // 导入失败
            Messages.showWarningDialog("Config info error by clipboard！", GlobalDict.TITLE_INFO);
            return null;
        }
    }
}
