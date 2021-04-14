package com.sjhy.plugin.ui;

import a.f.R;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.constants.StrState;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.tool.*;
import com.sjhy.plugin.ui.base.ImOrExportWayConfirmPanel;
import com.sjhy.plugin.ui.base.Item;
import com.sjhy.plugin.ui.base.ListCheckboxPanel;
import com.sjhy.plugin.ui.base.ListRadioPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 主设置面板
 *
 * @author makejava
 * @version 1.2.5
 * @since 2018/07/17 13:10
 */
public class MainSetting implements Configurable, Configurable.Composite {
    /**
     * 主面板
     */
    private JPanel mainPanel;
    /**
     * 作者编辑框
     */
    private JTextField authorTextField;
    /**
     * 重置默认设置按钮
     */
    private JButton resetBtn;
    /**
     * 模板导入按钮
     */
    private JButton importBtn;
    /**
     * 模板导出按钮
     */
    private JButton exportBtn;
    /**
     * 当前版本号
     */
    private JLabel versionLabel;
    /**
     * 同步地址，默认
     */
    private JTextField syncHost;

    /**
     * 重置列表
     */
    private List<Configurable> resetList;

    /**
     * 需要保存的列表
     */
    private List<Configurable> saveList;

    /**
     * 所有列表
     */
    private List<Configurable> allList;

    /**
     * 设置对象
     */
    private Settings settings = Settings.getInstance();

    /**
     * 默认构造方法
     */
    public MainSetting() {
        // 获取当前项目
        Project project = ProjectUtils.getCurrProject();
        init();

        //初始化事件
        Settings settings = Settings.getInstance();
        //重置配置信息
        resetBtn.addActionListener(e -> {
            if (MessageDialogUtils.yesNo(project, MsgValue.RESET_DEFAULT_SETTING_MSG)) {
                if (CollectionUtil.isEmpty(resetList)) {
                    return;
                }
                // 初始化默认配置
                settings.initDefault();
                // 重置
                resetList.forEach(UnnamedConfigurable::reset);
                if (CollectionUtil.isEmpty(saveList)) {
                    return;
                }
                // 保存
                saveList.forEach(configurable -> {
                    try {
                        configurable.apply();
                    } catch (ConfigurationException e1) {
                        e1.printStackTrace();
                    }
                });
            }
        });
        // 模板导入事件
        importBtn.addActionListener(e -> {
            // 创建两行一列的主面板
            ImOrExportWayConfirmPanel importPanel = new ImOrExportWayConfirmPanel();
            // 构建dialog
            DialogBuilder dialogBuilder = new DialogBuilder(project);
            dialogBuilder.setTitle(MsgValue.TITLE_INFO);
            dialogBuilder.setCenterPanel(importPanel);
            dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final String importWay = importPanel.getSelected();
                    final String importValue = importPanel.getValue();
                    if (importOrExportWayError(project, "导入", importWay, importValue)) {
                        return;
                    }
                    // 关闭并退出
                    dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                    switch (importWay) {
                        case ImOrExportWayConfirmPanel.WAY_TOKEN:
                            tokenImport(importValue);
                            break;
                        case ImOrExportWayConfirmPanel.WAY_LOCAL:
                            localImport(importValue);
                            break;
                        default:
                            break;
                    }
                }
            });
            dialogBuilder.show();
        });

        // 模板导出事件
        exportBtn.addActionListener(e -> {
            // 创建一行四列的主面板
            JPanel mainPanel = new JPanel(new GridLayout(1, 4));
            // Type Mapper
            ListCheckboxPanel typeMapperPanel = new ListCheckboxPanel("Type Mapper", settings.getTypeMapperGroupMap().keySet());
            mainPanel.add(typeMapperPanel);
            // Template
            ListCheckboxPanel templatePanel = new ListCheckboxPanel("Template", settings.getTemplateGroupMap().keySet());
            mainPanel.add(templatePanel);
            // Column Config
            ListCheckboxPanel columnConfigPanel = new ListCheckboxPanel("Column Config", settings.getColumnConfigGroupMap().keySet());
            mainPanel.add(columnConfigPanel);
            // GlobalConfig
            ListCheckboxPanel globalConfigPanel = new ListCheckboxPanel("Global Config", settings.getGlobalConfigGroupMap().keySet());
            mainPanel.add(globalConfigPanel);
            // 创建导出方式主面板
            ImOrExportWayConfirmPanel exportPanel = new ImOrExportWayConfirmPanel(false);

            JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
            centerPanel.add(mainPanel, BorderLayout.CENTER);
            centerPanel.add(exportPanel, BorderLayout.SOUTH);
            // 构建dialog
            DialogBuilder dialogBuilder = new DialogBuilder(project);
            dialogBuilder.setTitle(MsgValue.TITLE_INFO);
            dialogBuilder.setNorthPanel(new MultiLineLabel("请选择要导出的配置分组："));
            dialogBuilder.setCenterPanel(centerPanel);
            dialogBuilder.addActionDescriptor(dialogWrapper -> new AbstractAction("OK") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!MainSetting.this.isSelected(typeMapperPanel, templatePanel, columnConfigPanel, globalConfigPanel)) {
                        Messages.showWarningDialog("至少选择一个模板组！", MsgValue.TITLE_INFO);
                        return;
                    }
                    final String selected = exportPanel.getSelected();
                    final String text = exportPanel.getValue();
                    if (importOrExportWayError(project, "导出", selected, text)) {
                        return;
                    }
                    // 打包数据
                    Map<String, Object> param = new HashMap<>(4);

                    Map<String, TypeMapperGroup> typeMapper = new LinkedHashMap<>();
                    for (String selectedItem : typeMapperPanel.getSelectedItems()) {
                        typeMapper.put(selectedItem, settings.getTypeMapperGroupMap().get(selectedItem));
                    }
                    param.put(StrState.TYPE_MAPPER, typeMapper);

                    Map<String, TemplateGroup> template = new LinkedHashMap<>();
                    for (String selectedItem : templatePanel.getSelectedItems()) {
                        template.put(selectedItem, settings.getTemplateGroupMap().get(selectedItem));
                    }
                    param.put(StrState.TEMPLATE, template);

                    Map<String, ColumnConfigGroup> columnConfig = new LinkedHashMap<>();
                    for (String selectedItem : columnConfigPanel.getSelectedItems()) {
                        columnConfig.put(selectedItem, settings.getColumnConfigGroupMap().get(selectedItem));
                    }
                    param.put(StrState.COLUMN_CONFIG, columnConfig);

                    Map<String, GlobalConfigGroup> globalConfig = new LinkedHashMap<>();
                    for (String selectedItem : globalConfigPanel.getSelectedItems()) {
                        globalConfig.put(selectedItem, settings.getGlobalConfigGroupMap().get(selectedItem));
                    }
                    param.put(StrState.GLOBAL_CONFIG, globalConfig);
                    // 关闭并退出
                    dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
                    switch (selected) {
                        case ImOrExportWayConfirmPanel.WAY_TOKEN:
                            tokenExport(project, param);
                            break;
                        case ImOrExportWayConfirmPanel.WAY_LOCAL:
                            localExport(project, param, text);
                            break;
                        default:
                            break;
                    }

                }
            });
            dialogBuilder.show();
        });
    }


    private boolean importOrExportWayError(Project project, String action, String way, String value) {
        // 导入导出方式错误检查
        if (StringUtils.isEmpty(way)) {
            Messages.showWarningDialog(project, String.format("请选择%s方式", action), MsgValue.TITLE_INFO);
            return true;
        } else if (StringUtils.isEmpty(value)) {
            Messages.showWarningDialog(project, String.format("请填写%s", "Token".equals(way) ? "Token" : "本地路径"), MsgValue.TITLE_INFO);
            return true;
        }
        return false;
    }

    /**
     * 网络Token导入
     *
     * @param token
     */
    private void tokenImport(String token) {
        if (token == null) {
            return;
        }
        String url = String.format("%s/template?token=%s", settings.getSyncHost(), token);
        String result = HttpUtils.get(url);
        if (result == null) {
            return;
        }
        dataImport(result);
    }

    /**
     * 本地导入
     *
     * @param local
     */
    private void localImport(String local) {
        File localGroup = new File(local);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> param = new HashMap<>();

        Map<String, TypeMapperGroup> typeMapper = new LinkedHashMap<>();
        param.put(StrState.TYPE_MAPPER, typeMapper);
        final File typeMapperFile = new File(local + File.separatorChar + StrState.TYPE_MAPPER + ".json");
        if (typeMapperFile.exists()) {
            try {
                String content = new FileReader(typeMapperFile).readString().replace("\r", "");
                final TypeMapperGroup typeMapperGroup = objectMapper.readValue(content, TypeMapperGroup.class);
                typeMapper.put(typeMapperGroup.getName(), typeMapperGroup);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, ColumnConfigGroup> columnConfig = new LinkedHashMap<>();
        param.put(StrState.COLUMN_CONFIG, columnConfig);
        final File columnConfigFile = new File(local + File.separatorChar + StrState.COLUMN_CONFIG + ".json");
        if (columnConfigFile.exists()) {
            try {
                String content = new FileReader(columnConfigFile).readString().replace("\r", "");
                final ColumnConfigGroup columnConfigGroup = objectMapper.readValue(content, ColumnConfigGroup.class);
                columnConfig.put(columnConfigGroup.getName(), columnConfigGroup);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, TemplateGroup> template = new LinkedHashMap<>();
        param.put(StrState.TEMPLATE, template);
        final String templateDirectories = local + File.separatorChar + StrState.TEMPLATE;
        final File templateDirectoriesFile = new File(templateDirectories);
        if (templateDirectoriesFile.exists()) {
            final TemplateGroup templateGroup = (TemplateGroup) readConfig(templateDirectoriesFile, TemplateGroup.class, f -> {
                Template t = new Template();
                final String name = f.getName();
                t.setName(name.substring(0, name.indexOf(".vm")));
                t.setCode(new FileReader(f).readString().replace("\r", ""));
                return t;
            });
            templateGroup.setName(localGroup.getName());
            if (!templateGroup.getElementList().isEmpty()) {
                template.put(templateGroup.getName(), templateGroup);
            }
        }

        Map<String, GlobalConfigGroup> globalConfig = new LinkedHashMap<>();
        param.put(StrState.GLOBAL_CONFIG, globalConfig);
        final String globalConfigDirectories = local + File.separatorChar + StrState.GLOBAL_CONFIG;
        final File globalConfigDirectoriesFile = new File(globalConfigDirectories);
        if (globalConfigDirectoriesFile.exists()) {
            final GlobalConfigGroup globalConfigGroup = (GlobalConfigGroup) readConfig(globalConfigDirectoriesFile, GlobalConfigGroup.class, f -> {
                final GlobalConfig config = new GlobalConfig();
                final String name = f.getName();
                config.setName(name.substring(0, name.indexOf(".vm")));
                config.setValue(new FileReader(f).readString().replace("\r", ""));
                return config;
            });
            globalConfigGroup.setName(localGroup.getName());
            if (!globalConfigGroup.getElementList().isEmpty()) {
                globalConfig.put(globalConfigGroup.getName(), globalConfigGroup);
            }
        }

        try {
            dataImport(objectMapper.writeValueAsString(param));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private <T> AbstractGroup<T> readConfig(File directories, Class<? extends AbstractGroup<T>> clazz, Function<File, T> supplier) {
        final AbstractGroup<T> group;
        try {
            group = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(clazz.getName() + "new instance error!");
        }
        List<T> collect = Arrays.stream(directories.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".vm");
            }
        })).map(supplier).collect(Collectors.toList());
        group.setElementList(collect);
        return group;
    }

    private void dataImport(String result) {
        // 解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            if (jsonNode == null) {
                return;
            }
            // 配置覆盖
            coverConfig(jsonNode, StrState.TYPE_MAPPER, TypeMapperGroup.class, settings.getTypeMapperGroupMap());
            coverConfig(jsonNode, StrState.TEMPLATE, TemplateGroup.class, settings.getTemplateGroupMap());
            coverConfig(jsonNode, StrState.COLUMN_CONFIG, ColumnConfigGroup.class, settings.getColumnConfigGroupMap());
            coverConfig(jsonNode, StrState.GLOBAL_CONFIG, GlobalConfigGroup.class, settings.getGlobalConfigGroupMap());
            // 重置配置
            allList.forEach(UnnamedConfigurable::reset);
            if (CollectionUtil.isEmpty(saveList)) {
                return;
            }
            // 保存
            allList.forEach(configurable -> {
                try {
                    configurable.apply();
                } catch (ConfigurationException e1) {
                    e1.printStackTrace();
                }
            });
            // 覆盖提示
            Messages.showInfoMessage("导入完成", MsgValue.TITLE_INFO);
        } catch (IOException e1) {
            ExceptionUtil.rethrow(e1);
        }
    }

    /**
     * Token导出
     *
     * @param project
     * @param param
     */
    private void tokenExport(Project project, Map<String, Object> param) {
        // 上传数据
        String result = HttpUtils.postJson(settings.getSyncHost() + "/template", param);
        if (result != null) {
            // 提取token
            String token = "error";
            if (result.contains("token")) {
                int startLocation = result.indexOf("token") + 6;
                token = result.substring(startLocation, result.indexOf("，", startLocation));
            }
            // 显示token
            Messages.showInputDialog(project, result, MsgValue.TITLE_INFO, AllIcons.General.InformationDialog, token, new NonEmptyInputValidator());
        }
    }

    /**
     * 本地导出
     *
     * @param project
     * @param local
     * @param param
     */
    private void localExport(Project project, Map<String, Object> param, String local) {
        // 导出文件夹结构
        // local
        //   |-- Default
        //      |-- typeMapper.json
        //      |-- columnConfig.json
        //      |-- globalConfig
        //          |-- init.vm
        //      |-- template
        //          |-- entity.java.vm

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, TypeMapperGroup> typeMapper = (LinkedHashMap<String, TypeMapperGroup>) param.get(StrState.TYPE_MAPPER);
        for (Map.Entry<String, TypeMapperGroup> entry : typeMapper.entrySet()) {
            final TypeMapperGroup typeMapperGroup = entry.getValue();
            final String name = typeMapperGroup.getName();
            final String groupFileName = local + File.separatorChar + name + File.separatorChar + StrState.TYPE_MAPPER + ".json";
            try {
                final String string = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(typeMapperGroup);
                FileWriter writer = new FileWriter(groupFileName);
                writer.write(string);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Map<String, ColumnConfigGroup> columnConfig = (LinkedHashMap<String, ColumnConfigGroup>) param.get(StrState.COLUMN_CONFIG);
        for (Map.Entry<String, ColumnConfigGroup> entry : columnConfig.entrySet()) {
            final ColumnConfigGroup columnConfigGroup = entry.getValue();
            final String name = columnConfigGroup.getName();
            final String groupFileName = local + File.separatorChar + name + File.separatorChar + StrState.COLUMN_CONFIG + ".json";
            try {
                final String string = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(columnConfigGroup);
                FileWriter writer = new FileWriter(groupFileName);
                writer.write(string);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Map<String, TemplateGroup> template = (Map<String, TemplateGroup>) param.get(StrState.TEMPLATE);
        for (Map.Entry<String, TemplateGroup> entry : template.entrySet()) {
            final TemplateGroup templateGroup = entry.getValue();
            final String name = templateGroup.getName();
            final String templateGroupDirectories = local + File.separatorChar + name + File.separatorChar + StrState.TEMPLATE;
            for (Template t : templateGroup.getElementList()) {
                final String fileName = templateGroupDirectories + File.separatorChar + t.getName() + ".vm";
                FileWriter writer = new FileWriter(fileName);
                writer.write(t.getCode());
            }
        }


        Map<String, GlobalConfigGroup> globalConfig = (Map<String, GlobalConfigGroup>) param.get(StrState.GLOBAL_CONFIG);
        for (Map.Entry<String, GlobalConfigGroup> entry : globalConfig.entrySet()) {
            final GlobalConfigGroup globalConfigGroup = entry.getValue();
            final String name = globalConfigGroup.getName();
            final String globalConfigGroupDirectories = local + File.separatorChar + name + File.separatorChar + StrState.GLOBAL_CONFIG;
            for (GlobalConfig config : globalConfigGroup.getElementList()) {
                final String fileName = globalConfigGroupDirectories + File.separatorChar + config.getName() + ".vm";
                FileWriter writer = new FileWriter(fileName);
                writer.write(config.getValue());
            }
        }

        Messages.showInfoMessage("导出完成，请到选定的本地文件夹中查看！", MsgValue.TITLE_INFO);
    }

    /**
     * 判断是否选中
     *
     * @param checkboxPanels 复选框面板
     * @return 是否选中
     */
    private boolean isSelected(@NotNull ListCheckboxPanel... checkboxPanels) {
        for (ListCheckboxPanel checkboxPanel : checkboxPanels) {
            if (!CollectionUtil.isEmpty(checkboxPanel.getSelectedItems())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 覆盖配置
     *
     * @param jsonNode json节点对象
     * @param name     配置组名称
     * @param cls      配置组类
     * @param srcGroup 源分组
     */
    private <T extends AbstractGroup> void coverConfig(@NotNull JsonNode jsonNode, @NotNull String name, Class<T> cls, @NotNull Map<String, T> srcGroup) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (!jsonNode.has(name)) {
            return;
        }
        try {
            JsonNode node = jsonNode.get(name);
            if (node.size() == 0) {
                return;
            }
            // 覆盖配置
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String key = names.next();
                String value = node.get(key).toString();
                T group = objectMapper.readValue(value, cls);
                if (srcGroup.containsKey(key)) {
                    if (!MessageDialogUtils.yesNo(String.format("是否覆盖%s配置中的%s分组？", name, key))) {
                        continue;
                    }
                }
                srcGroup.put(key, group);
            }
        } catch (IOException e) {
            Messages.showWarningDialog("JSON解析错误！", MsgValue.TITLE_INFO);
            ExceptionUtil.rethrow(e);
        }
    }

    /**
     * 初始化方法
     */
    private void init() {
        //初始化数据
        versionLabel.setText(settings.getVersion());
        authorTextField.setText(settings.getAuthor());
        syncHost.setText(settings.getSyncHost());
    }

    /**
     * 设置显示名称
     *
     * @return 显示名称
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Easy Code";
    }

    /**
     * Returns the topic in the help file which is shown when help for the configurable is requested.
     *
     * @return the help topic, or {@code null} if no help is available
     */
    @Nullable
    @Override
    public String getHelpTopic() {
        return getDisplayName();
    }

    /**
     * 更多配置
     *
     * @return 配置选项
     */
    @NotNull
    @Override
    public Configurable[] getConfigurables() {
        Configurable[] result = new Configurable[4];
        result[0] = new TypeMapperSetting(settings);
        result[1] = new TemplateSettingPanel();
        result[2] = new TableSettingPanel();
        result[3] = new GlobalConfigSettingPanel();
        // 所有列表
        allList = new ArrayList<>();
        allList.add(result[0]);
        allList.add(result[1]);
        allList.add(result[2]);
        allList.add(result[3]);
        // 需要重置的列表
        resetList = new ArrayList<>();
        resetList.add(result[0]);
        resetList.add(result[1]);
        resetList.add(result[3]);
        // 不需要重置的列表
        saveList = new ArrayList<>();
        saveList.add(this);
        saveList.add(result[2]);
        return result;
    }

    /**
     * 获取主面板信息
     *
     * @return 主面板
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    /**
     * 判断是否修改
     *
     * @return 是否修改
     */
    @Override
    public boolean isModified() {
        return !settings.getAuthor().equals(authorTextField.getText()) ||
                !settings.getSyncHost().equals(syncHost.getText());
    }

    /**
     * 应用修改
     */
    @Override
    public void apply() {
        //保存数据
        settings.setAuthor(authorTextField.getText());
        settings.setSyncHost(syncHost.getText());
    }

    /**
     * 重置
     */
    @Override
    public void reset() {
        init();
    }
}
