package com.sjhy.plugin.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.constants.StrState;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.TableInfoService;
import com.sjhy.plugin.tool.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 选择保存路径
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class SelectSavePath extends JDialog {
    /**
     * 主面板
     */
    private JPanel contentPane;
    /**
     * 确认按钮
     */
    private JButton buttonOK;
    /**
     * 取消按钮
     */
    private JButton buttonCancel;
    /**
     * 模型下拉框
     */
    private JComboBox<String> moduleComboBox;
    /**
     * 包字段
     */
    private JTextField packageField;
    /**
     * 路径字段
     */
    private JTextField pathField;
    /**
     * 前缀字段
     */
    private JTextField preField;
    /**
     * 包选择按钮
     */
    private JButton packageChooseButton;
    /**
     * 路径选择按钮
     */
    private JButton pathChooseButton;
    /**
     * 模板全选框
     */
    private JCheckBox allCheckBox;
    /**
     * 模板面板
     */
    private JPanel templatePanel;
    /**
     * 统一配置复选框
     */
    private JCheckBox unifiedConfig;
    /**
     * 禁止提示复选框
     */
    private JCheckBox titleConfig;
    /**
     * 分组选择框
     */
    private JComboBox<String> groupComboBox;
    /**
     * 所有模板复选框
     */
    private List<JCheckBox> checkBoxList = new ArrayList<>();
    /**
     * 数据缓存工具类
     */
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    /**
     * 模板组对象
     */
    private TemplateGroup templateGroup;
    /**
     * 表信息服务
     */
    private TableInfoService tableInfoService;
    /**
     * 项目对象
     */
    private Project project;
    /**
     * 代码生成服务
     */
    private CodeGenerateService codeGenerateService;
    /**
     * 当前项目中的module
     */
    private List<Module> moduleList;

    /**
     * 实体模式生成代码
     */
    private boolean entityMode;

    /**
     * 构造方法
     */
    public SelectSavePath(Project project) {
        this(project, false);
    }

    /**
     * 构造方法
     */
    public SelectSavePath(Project project, boolean entityMode) {
        this.entityMode = entityMode;
        this.project = project;
        this.tableInfoService = TableInfoService.getInstance(project);
        this.codeGenerateService = CodeGenerateService.getInstance(project);
        this.templateGroup = CurrGroupUtils.getCurrTemplateGroup();
        // 初始化module，存在资源路径的排前面
        this.moduleList = new LinkedList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            // 存在源代码文件夹放前面，否则放后面
            if (ModuleUtils.existsSourcePath(module)) {
                this.moduleList.add(0, module);
            } else {
                this.moduleList.add(module);
            }
        }
        init();
        setTitle(MsgValue.TITLE_INFO);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 获取已经选中的模板
     *
     * @return 模板对象集合
     */
    private List<Template> getSelectTemplate() {
        // 获取到已选择的复选框
        List<String> selectTemplateNameList = new ArrayList<>();
        checkBoxList.forEach(jCheckBox -> {
            if (jCheckBox.isSelected()) {
                selectTemplateNameList.add(jCheckBox.getText());
            }
        });
        List<Template> selectTemplateList = new ArrayList<>(selectTemplateNameList.size());
        if (selectTemplateNameList.isEmpty()) {
            return selectTemplateList;
        }
        // 将复选框转换成对应的模板对象
        templateGroup.getElementList().forEach(template -> {
            if (selectTemplateNameList.contains(template.getName())) {
                selectTemplateList.add(template);
            }
        });
        return selectTemplateList;
    }

    /**
     * 确认按钮回调事件
     */
    private void onOK() {
        List<Template> selectTemplateList = getSelectTemplate();
        // 如果选择的模板是空的
        if (selectTemplateList.isEmpty()) {
            Messages.showWarningDialog("Can't Select Template!", MsgValue.TITLE_INFO);
            return;
        }
        String savePath = pathField.getText();
        if (StringUtils.isEmpty(savePath)) {
            Messages.showWarningDialog("Can't Select Save Path!", MsgValue.TITLE_INFO);
            return;
        }
        // 针对Linux系统路径做处理
        savePath = savePath.replace("\\", "/");
        // 保存路径使用相对路径
        String basePath = project.getBasePath();
        if (!StringUtils.isEmpty(basePath) && savePath.startsWith(basePath)) {
            if (savePath.length() > basePath.length()) {
                if ("/".equals(savePath.substring(basePath.length(), basePath.length() + 1))) {
                    savePath = savePath.replace(basePath, ".");
                }
            } else {
                savePath = savePath.replace(basePath, ".");
            }
        }
        // 保存配置
        TableInfo tableInfo = null;
        if(!entityMode) {
            tableInfo = tableInfoService.getTableInfoAndConfig(cacheDataUtils.getSelectDbTable());
        } else {
            tableInfo = tableInfoService.getTableInfoAndConfigByPsiClass(cacheDataUtils.getSelectPsiClass());
        }
        tableInfo.setSavePath(savePath);
        tableInfo.setSavePackageName(packageField.getText());
        tableInfo.setPreName(preField.getText());
        tableInfo.setTemplateGroupName((String) groupComboBox.getSelectedItem());
        Module module = getSelectModule();
        if (module != null) {
            tableInfo.setSaveModelName(module.getName());
        }
        tableInfoService.save(tableInfo);

        // 生成代码
        codeGenerateService.generateByUnifiedConfig(getSelectTemplate(), unifiedConfig.isSelected(), !titleConfig.isSelected(), this.entityMode);
        // 关闭窗口
        dispose();
    }

    /**
     * 取消按钮回调事件
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 初始化模板组
     */
    private void initTemplateGroup() {
        //添加模板组
        checkBoxList.clear();
        templatePanel.removeAll();
        templatePanel.repaint();
        templatePanel.setLayout(new GridLayout(6, 2));
        templateGroup.getElementList().forEach(template -> {
            JCheckBox checkBox = new JCheckBox(template.getName());
            checkBoxList.add(checkBox);
            templatePanel.add(checkBox);
        });
        // 移除所有旧事件
        ActionListener[] actionListeners = allCheckBox.getActionListeners();
        if (actionListeners != null && actionListeners.length > 0) {
            for (ActionListener actionListener : actionListeners) {
                allCheckBox.removeActionListener(actionListener);
            }
        }
        //添加全选事件
        allCheckBox.addActionListener(e -> checkBoxList.forEach(jCheckBox -> jCheckBox.setSelected(allCheckBox.isSelected())));
        allCheckBox.setSelected(false);
    }

    /**
     * 初始化方法
     */
    private void init() {
        // 初始化模板组
        initTemplateGroup();

        //初始化Module选择
        for (Module module : this.moduleList) {
            moduleComboBox.addItem(module.getName());
        }

        //监听module选择事件
        moduleComboBox.addActionListener(e -> {
            // 刷新路径
            refreshPath();
        });

        try {
            Class<?> cls = Class.forName("com.intellij.ide.util.PackageChooserDialog");
            //添加包选择事件
            packageChooseButton.addActionListener(e -> {
                try {
                    Constructor<?> constructor = cls.getConstructor(String.class, Project.class);
                    Object dialog = constructor.newInstance("Package Chooser", project);
                    // 显示窗口
                    Method showMethod = cls.getMethod("show");
                    showMethod.invoke(dialog);
                    // 获取选中的包名
                    Method getSelectedPackageMethod = cls.getMethod("getSelectedPackage");
                    Object psiPackage = getSelectedPackageMethod.invoke(dialog);
                    if (psiPackage != null) {
                        Method getQualifiedNameMethod = psiPackage.getClass().getMethod("getQualifiedName");
                        String packageName = (String) getQualifiedNameMethod.invoke(psiPackage);
                        packageField.setText(packageName);
                        // 刷新路径
                        refreshPath();
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e1) {
                    ExceptionUtil.rethrow(e1);
                }
            });

            // 添加包编辑框失去焦点事件
            packageField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    // 刷新路径
                    refreshPath();
                }
            });
        } catch (ClassNotFoundException e) {
            // 没有PackageChooserDialog，并非支持Java的IDE，禁用相关UI组件
            packageField.setEnabled(false);
            packageChooseButton.setEnabled(false);
        }

        //初始化路径
        refreshPath();

        //选择路径
        pathChooseButton.addActionListener(e -> {
            //将当前选中的model设置为基础路径
            VirtualFile path = ProjectUtils.getBaseDir(project);
            Module module = getSelectModule();
            if (module != null) {
                path = ModuleUtils.getSourcePath(module);
            }
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, path);
            if (virtualFile != null) {
                pathField.setText(virtualFile.getPath());
            }
        });

        // 获取选中的表信息（鼠标右键的那张表），并提示未知类型
        TableInfo tableInfo = null;
        if(entityMode) {
            tableInfo = tableInfoService.getTableInfoAndConfigByPsiClass(cacheDataUtils.getSelectPsiClass());
        } else {
            tableInfo = tableInfoService.getTableInfoAndConfig(cacheDataUtils.getSelectDbTable());
        }

        // 设置默认配置信息
        if (!StringUtils.isEmpty(tableInfo.getSaveModelName())) {
            moduleComboBox.setSelectedItem(tableInfo.getSaveModelName());
        }
        if (!StringUtils.isEmpty(tableInfo.getSavePackageName())) {
            packageField.setText(tableInfo.getSavePackageName());
        }
        if (!StringUtils.isEmpty(tableInfo.getPreName())) {
            preField.setText(tableInfo.getPreName());
        }
        Settings settings = Settings.getInstance();
        String groupName = settings.getCurrTemplateGroupName();
        if (!StringUtils.isEmpty(tableInfo.getTemplateGroupName())) {
            if (settings.getTemplateGroupMap().containsKey(tableInfo.getTemplateGroupName())) {
                groupName = tableInfo.getTemplateGroupName();
                this.templateGroup = settings.getTemplateGroupMap().get(groupName);
                // 选中的模板组发生变化，尝试重新初始化
                initTemplateGroup();
            }
        }
        for (String key : settings.getTemplateGroupMap().keySet()) {
            groupComboBox.addItem(key);
        }
        groupComboBox.setSelectedItem(groupName);
        groupComboBox.addActionListener(e -> {
            String selectedItem = (String) groupComboBox.getSelectedItem();
            if (this.templateGroup.getName().equals(selectedItem)) {
                return;
            }
            this.templateGroup = settings.getTemplateGroupMap().get(selectedItem);
            // 选中的模板组发生变化，尝试重新初始化
            initTemplateGroup();
            this.open();
        });
        String savePath = tableInfo.getSavePath();
        if (!StringUtils.isEmpty(savePath)) {
            // 判断是否需要拼接项目路径
            if (savePath.startsWith(StrState.RELATIVE_PATH)) {
                String projectPath = project.getBasePath();
                savePath = projectPath + savePath.substring(1);
            }
            pathField.setText(savePath);
        }
    }

    /**
     * 获取选中的Module
     *
     * @return 选中的Module
     */
    private Module getSelectModule() {
        String name = (String) moduleComboBox.getSelectedItem();
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return ModuleManager.getInstance(project).findModuleByName(name);
    }

    /**
     * 获取基本路径
     *
     * @return 基本路径
     */
    private String getBasePath() {
        Module module = getSelectModule();
        VirtualFile baseVirtualFile = ProjectUtils.getBaseDir(project);
        if (baseVirtualFile == null) {
            Messages.showWarningDialog("无法获取到项目基本路径！", MsgValue.TITLE_INFO);
            return "";
        }
        String baseDir = baseVirtualFile.getPath();
        if (module != null) {
            VirtualFile virtualFile = ModuleUtils.getSourcePath(module);
            if (virtualFile != null) {
                baseDir = virtualFile.getPath();
            }
        }
        return baseDir;
    }

    /**
     * 刷新目录
     */
    private void refreshPath() {
        String packageName = packageField.getText();
        // 获取基本路径
        String path = getBasePath();
        // 兼容Linux路径
        path = path.replace("\\", "/");
        // 如果存在包路径，添加包路径
        if (!StringUtils.isEmpty(packageName)) {
            path += "/" + packageName.replace(".", "/");
        }
        pathField.setText(path);
    }

    /**
     * 打开窗口
     */
    public void open() {
        this.pack();
        setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
