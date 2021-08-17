package com.sjhy.plugin.ui.component;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.AbstractGroup;
import com.sjhy.plugin.service.ExportImportSettingsService;
import com.sjhy.plugin.service.SettingsStorageService;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.CollectionUtil;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 导出导入组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/12 10:35
 */
public class ExportImportComponent {
    /**
     * 导出按钮
     */
    private JButton exportBtn;
    /**
     * 导入按钮
     */
    private JButton importBtn;
    /**
     * 导出导入服务
     */
    private ExportImportSettingsService service;

    /**
     * 导入成功回调
     */
    private Runnable callback;

    public ExportImportComponent(JButton exportBtn, JButton importBtn, ExportImportSettingsService service, Runnable callback) {
        this.exportBtn = exportBtn;
        this.importBtn = importBtn;
        this.service = service;
        this.callback = callback;
        this.init();
    }

    private void init() {
        this.exportBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlerExportAction();
            }
        });
        this.importBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlerImportAction();
            }
        });
    }

    /**
     * 处理导出动作
     */
    private void handlerExportAction() {
        // 复制一份，避免篡改
        SettingsStorageDTO settingsStorage = CloneUtils.cloneByJson(SettingsStorageService.getSettingsStorage());
        // 创建一行四列的主面板
        JPanel mainPanel = new JPanel(new GridLayout(1, 4));
        // Type Mapper
        ListCheckboxComponent typeMapperPanel = new ListCheckboxComponent("Type Mapper", settingsStorage.getTypeMapperGroupMap().keySet());
        mainPanel.add(typeMapperPanel);
        // Template
        ListCheckboxComponent templatePanel = new ListCheckboxComponent("Template", settingsStorage.getTemplateGroupMap().keySet());
        mainPanel.add(templatePanel);
        // Column Config
        ListCheckboxComponent columnConfigPanel = new ListCheckboxComponent("Column Config", settingsStorage.getColumnConfigGroupMap().keySet());
        mainPanel.add(columnConfigPanel);
        // GlobalConfig
        ListCheckboxComponent globalConfigPanel = new ListCheckboxComponent("Global Config", settingsStorage.getGlobalConfigGroupMap().keySet());
        mainPanel.add(globalConfigPanel);
        // 构建dialog
        DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
        dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
        dialogBuilder.setNorthPanel(new MultiLineLabel("请选择要导出的配置分组："));
        dialogBuilder.setCenterPanel(mainPanel);
        dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSelected(typeMapperPanel, templatePanel, columnConfigPanel, globalConfigPanel)) {
                    Messages.showWarningDialog("至少选择一个模板组！", GlobalDict.TITLE_INFO);
                    return;
                }
                // 过滤数据
                filterSelected(typeMapperPanel, settingsStorage.getTypeMapperGroupMap());
                filterSelected(templatePanel, settingsStorage.getTemplateGroupMap());
                filterSelected(columnConfigPanel, settingsStorage.getColumnConfigGroupMap());
                filterSelected(globalConfigPanel, settingsStorage.getGlobalConfigGroupMap());
                // 关闭并退出
                dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                service.exportConfig(settingsStorage);
            }
        });
        // 显示窗口
        dialogBuilder.show();
    }

    /**
     * 判断是否选中
     *
     * @param checkboxPanels 复选框面板
     * @return 是否选中
     */
    private boolean isSelected(@NotNull ListCheckboxComponent... checkboxPanels) {
        for (ListCheckboxComponent checkboxPanel : checkboxPanels) {
            if (!CollectionUtil.isEmpty(checkboxPanel.getSelectedItems())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤选中数据
     *
     * @param checkboxPanel 选中面板
     * @param map           需要过滤的map
     */
    private void filterSelected(ListCheckboxComponent checkboxPanel, Map<String, ?> map) {
        List<String> selectedItems = checkboxPanel.getSelectedItems();
        map.keySet().removeIf(item -> !selectedItems.contains(item));
    }

    private void handlerImportAction() {
        SettingsStorageDTO localSettings = SettingsStorageService.getSettingsStorage();
        SettingsStorageDTO remoteSettings = service.importConfig();
        if (remoteSettings == null) {
            return;
        }
        // 对同名分组进行覆盖、放弃、改名操作
        // 创建主面板
        JPanel mainPanel = new JPanel(new VerticalFlowLayout());
        List<Handler> allHandlerList = new ArrayList<>();
        addRadioComponent(allHandlerList, "TypeMapper", localSettings.getTypeMapperGroupMap(), remoteSettings.getTypeMapperGroupMap());
        addRadioComponent(allHandlerList, "Template", localSettings.getTemplateGroupMap(), remoteSettings.getTemplateGroupMap());
        addRadioComponent(allHandlerList, "ColumnConfig", localSettings.getColumnConfigGroupMap(), remoteSettings.getColumnConfigGroupMap());
        addRadioComponent(allHandlerList, "GlobalConfig", localSettings.getGlobalConfigGroupMap(), remoteSettings.getGlobalConfigGroupMap());
        for (Handler handler : allHandlerList) {
            if (handler.getRadioComponent() != null) {
                mainPanel.add(handler.getRadioComponent());
            }
        }
        // 没有需要选择处理的分组则不构建Dialog
        boolean anyMatch = allHandlerList.stream().anyMatch(item -> item.getRadioComponent() != null);
        if (!anyMatch) {
            // 执行每个处理器
            for (Handler handler : allHandlerList) {
                handler.execute();
            }
            // 执行回调
            if (callback != null) {
                callback.run();
            }
            return;
        }
        // 构建dialog
        DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
        dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
        dialogBuilder.setNorthPanel(new MultiLineLabel("请选择重复配置的处理方式："));
        dialogBuilder.setCenterPanel(mainPanel);
        dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行每个处理器
                for (Handler handler : allHandlerList) {
                    handler.execute();
                }
                // 执行回调
                if (callback != null) {
                    callback.run();
                }
                // 关闭并退出
                dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                Messages.showInfoMessage("导入完成", GlobalDict.TITLE_INFO);
            }
        });
        // 显示窗口
        dialogBuilder.show();
    }

    private <T extends AbstractGroup> void addRadioComponent(List<Handler> allHandlerList, String groupName, Map<String, T> localMap, Map<String, T> remoteMap) {
        if (CollectionUtil.isEmpty(remoteMap)) {
            return;
        }
        for (String key : remoteMap.keySet()) {
            if (localMap.containsKey(key)) {
                ListRadioComponent listRadioComponent = new ListRadioComponent(groupName + "->" + key, Stream.of(Operator.values()).map(item -> StringUtils.capitalize(item.name())).collect(Collectors.toList()));
                allHandlerList.add(new Handler<>(listRadioComponent, localMap, remoteMap, key));
            } else {
                allHandlerList.add(new Handler<>(null, localMap, remoteMap, key));
            }
        }
    }

    private static class Handler<T extends AbstractGroup> {
        @Getter
        private ListRadioComponent radioComponent;

        private Map<String, T> localMap;

        private Map<String, T> remoteMap;

        private String name;

        Handler(ListRadioComponent radioComponent, Map<String, T> localMap, Map<String, T> remoteMap, String name) {
            this.radioComponent = radioComponent;
            this.localMap = localMap;
            this.remoteMap = remoteMap;
            this.name = name;
        }

        void execute() {
            Operator operator = Operator.COVER;
            if (radioComponent != null) {
                String selectedItem = radioComponent.getSelectedItem();
                if (selectedItem != null) {
                    operator = Operator.valueOf(selectedItem.toUpperCase());
                }
            }
            switch (operator) {
                case COVER:
                    localMap.put(name, remoteMap.get(name));
                    break;
                case RENAME:
                    String newName = name;
                    for (int i = 0; localMap.containsKey(newName); i++) {
                        newName = name + i;
                    }
                    T item = remoteMap.get(name);
                    item.setName(newName);
                    localMap.put(newName, item);
                    break;
                case DISCARD:
                    break;
                default:
                    break;
            }
        }
    }

    public enum Operator {
        /**
         * 覆盖
         */
        COVER,
        /**
         * 重命名
         */
        RENAME,
        /**
         * 丢弃
         */
        DISCARD
    }
}
