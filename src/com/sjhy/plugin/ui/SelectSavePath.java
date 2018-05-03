package com.sjhy.plugin.ui;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiPackage;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.VelocityUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectSavePath extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox moduleComboBox;
    private JTextField packageField;
    private JTextField pathField;
    private JButton packageChooseButton;
    private JButton pathChooseButton;
    private JCheckBox allCheckBox;
    private JPanel templatePanel;

    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    private List<JCheckBox> checkBoxList = new ArrayList<>();
    private TemplateGroup templateGroup;

    public SelectSavePath() {
        ConfigService configService = ConfigService.getInstance();
        this.templateGroup = configService.getTemplateGroupMap().get(configService.getCurrTemplateGroupName());
        init();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private List<Template> getSelectTemplate() {
        List<String> selectTemplateNameList = new ArrayList<>();
        checkBoxList.forEach(jCheckBox -> {
            if(jCheckBox.isSelected()){
                selectTemplateNameList.add(jCheckBox.getText());
            }
        });
        List<Template> selectTemplateList = new ArrayList<>();
        if (selectTemplateNameList.isEmpty()){
            return selectTemplateList;
        }
        templateGroup.getElementList().forEach(template -> {
            if (selectTemplateNameList.contains(template.getName())) {
                selectTemplateList.add(template);
            }
        });
        return selectTemplateList;
    }

    private void onOK() {
        List<Template> selectTemplateList = getSelectTemplate();
        if (selectTemplateList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Can't Select Template!");
            return;
        }
        String savePath = pathField.getText();
        if (savePath.isEmpty()){
            JOptionPane.showMessageDialog(null, "Can't Select Save Path!");
            return;
        }
        cacheDataUtils.setSavePath(savePath);
        cacheDataUtils.setSelectTemplate(selectTemplateList);
        cacheDataUtils.setPackageName(packageField.getText());
        cacheDataUtils.setSelectModule(getSelectModule());
        VelocityUtils.getInstance().handler();
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        //添加模板组
        checkBoxList.clear();
        templatePanel.setLayout(new GridLayout(6, 2));
        templateGroup.getElementList().forEach(template -> {
            JCheckBox checkBox = new JCheckBox(template.getName());
            checkBoxList.add(checkBox);
            templatePanel.add(checkBox);
        });
        //设置全选事件
        allCheckBox.addActionListener(e -> checkBoxList.forEach(jCheckBox -> jCheckBox.setSelected(allCheckBox.isSelected())));

        //初始化Module选择
        for (Module module : cacheDataUtils.getModules()) {
            moduleComboBox.addItem(module.getName());
        }

        //选择包
        packageChooseButton.addActionListener(e -> {
            PackageChooserDialog dialog = new PackageChooserDialog("Package Chooser", cacheDataUtils.getProject());
            dialog.show();
            PsiPackage psiPackage = dialog.getSelectedPackage();
            if (psiPackage!=null) {
                packageField.setText(psiPackage.getQualifiedName());
            }
            refreshPath();
        });

        //初始化路径
        refreshPath();

        //选择路径
        pathChooseButton.addActionListener(e -> {
            @SuppressWarnings("ConstantConditions") VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), cacheDataUtils.getProject(), getSelectModule().getModuleFile().getParent());
            if (virtualFile!=null) {
                pathField.setText(virtualFile.getPath());
            }
        });
    }

    private Module getSelectModule() {
        String name = (String) moduleComboBox.getSelectedItem();
        for (Module module : cacheDataUtils.getModules()) {
            if(module.getName().equals(name)){
                return module;
            }
        }
        return cacheDataUtils.getModules()[0];
    }

    @SuppressWarnings("ConstantConditions")
    private String getBasePath() {
        Module module = getSelectModule();
        if (module==null || module.getModuleFilePath()==null) {
            return null;
        }
//        String baseDir = module.getModuleFile().getParent().getPath();
        String baseDir = new File(module.getModuleFilePath()).getParent();
        File file = new File(baseDir+"/src/main/java");
        if (file.exists()){
            return file.getAbsolutePath();
        }
        file = new File(baseDir+"/src");
        if (file.exists()){
            return file.getAbsolutePath();
        }
        return baseDir;
    }

    private void refreshPath() {
        String packageName = packageField.getText();
        String path = getBasePath();
        if (!packageName.isEmpty()){
            path += "\\" + packageName.replaceAll("\\.", "\\\\");
        }
        pathField.setText(path);
    }

    public void open() {
        this.pack();
        setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
