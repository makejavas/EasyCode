package com.sjhy.plugin.service.impl;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.util.TextRange;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.tool.HttpUtils;
import com.sjhy.plugin.tool.JSON;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络导出导入设置服务实现
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/12 14:37
 */
public class NetworkExportImportSettingsServiceImpl implements ExportImportSettingsService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9A-Z]{20,}+");

    /**
     * 导出设置
     *
     * @param settingsStorage 要导出的设置
     */
    @Override
    public void exportConfig(SettingsStorageDTO settingsStorage) {
        // 上传数据
        String result = HttpUtils.postJson("/template", settingsStorage);
        if (result != null) {
            // 利用正则提取token值
            String token = "error";
            Matcher matcher = TOKEN_PATTERN.matcher(result);
            if (matcher.find()) {
                token = matcher.group();
            }
            // 显示token
            try {
                Method method = Messages.class.getMethod("showInputDialog", Project.class, String.class, String.class, Icon.class, String.class, InputValidator.class, TextRange.class, String.class);
                method.invoke(null, ProjectUtils.getCurrProject(), result, GlobalDict.TITLE_INFO, AllIcons.General.InformationDialog, token, new NonEmptyInputValidator(), null, "Easy Code官网地址：<a href='http://www.ieasycode.com:9527'>www.ieasycode.com:9527</a>");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // 兼容旧版本
                Messages.showInputDialog(ProjectUtils.getCurrProject(), result, GlobalDict.TITLE_INFO, AllIcons.General.InformationDialog, token, new NonEmptyInputValidator(), null);
            }
        }
    }

    /**
     * 导入设置
     *
     * @return 设置信息
     */
    @Override
    public SettingsStorageDTO importConfig() {
        String token = Messages.showInputDialog("Token:", GlobalDict.TITLE_INFO, AllIcons.General.Tip, "", new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
                return !StringUtils.isEmpty(inputString);
            }

            @Override
            public boolean canClose(String inputString) {
                return this.checkInput(inputString);
            }
        });
        if (token == null) {
            return null;
        }
        String result = HttpUtils.get(String.format("/template?token=%s", token));
        if (result == null) {
            return null;
        }
        // 解析数据
        return JSON.parse(result, SettingsStorageDTO.class);
    }
}
