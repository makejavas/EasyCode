package com.sjhy.plugin.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.TableInfoService;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.CollectionUtil;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.ui.base.BaseGroupPanel;
import com.sjhy.plugin.ui.base.BaseItemSelectPanel;
import com.sjhy.plugin.ui.base.TemplateEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 模板编辑主面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
public class TemplateSettingPanel implements Configurable {
    /**
     * 模板描述信息，说明文档
     */
    private static final String TEMPLATE_DESCRIPTION_INFO;

    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(TemplateSettingPanel.class.getResource("/description/templateDescription.html"));
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        } finally {
            TEMPLATE_DESCRIPTION_INFO = descriptionInfo;
        }
    }

    /**
     * 配置信息
     */
    private Settings settings;

    /**
     * 编辑框面板
     */
    private TemplateEditor templateEditor;

    /**
     * 基本的分组面板
     */
    private BaseGroupPanel baseGroupPanel;

    /**
     * 基本的元素选择面板
     */
    private BaseItemSelectPanel<Template> baseItemSelectPanel;

    /**
     * 当前分组
     */
    private Map<String, TemplateGroup> group;

    /**
     * 当前选中分组
     */
    private String currGroupName;

    /**
     * 项目对象
     */
    private Project project;

    TemplateSettingPanel() {
        // 项目对象
        this.project = ProjectUtils.getCurrProject();
        // 配置服务实例化
        this.settings = Settings.getInstance();
        // 克隆对象
        this.currGroupName = this.settings.getCurrTemplateGroupName();
        this.group = CloneUtils.cloneByJson(this.settings.getTemplateGroupMap(), new TypeReference<Map<String, TemplateGroup>>() {});
    }

    /**
     * 获取设置显示的名称
     *
     * @return 名称
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    /**
     * 获取主面板对象
     *
     * @return 主面板对象
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        this.currGroupName = findExistedGroupName(this.currGroupName);

        // 实例化分组面板
        this.baseGroupPanel = new BaseGroupPanel(new ArrayList<>(group.keySet()), this.currGroupName) {
            @Override
            protected void createGroup(String name) {
                // 创建分组
                TemplateGroup templateGroup = new TemplateGroup();
                templateGroup.setName(name);
                templateGroup.setElementList(new ArrayList<>());
                group.put(name, templateGroup);
                currGroupName = name;
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
                // 创建空白分组，需要清空输入框
                templateEditor.reset("empty", "");
            }

            @Override
            protected void deleteGroup(String name) {
                // 删除分组
                group.remove(name);
                currGroupName = Settings.DEFAULT_NAME;
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
            }

            @Override
            protected void copyGroup(String name) {
                // 复制分组
                TemplateGroup templateGroup = CloneUtils.cloneByJson(group.get(currGroupName));
                templateGroup.setName(name);
                currGroupName = name;
                group.put(name, templateGroup);
                baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
            }

            @Override
            protected void changeGroup(String name) {
                currGroupName = name;
                if (baseItemSelectPanel == null) {
                    return;
                }
                // 重置模板选择
                baseItemSelectPanel.reset(group.get(currGroupName).getElementList(), 0);
                if (group.get(currGroupName).getElementList().isEmpty()) {
                    // 没有元素时，需要清空编辑框
                    templateEditor.reset("empty", "");
                }
            }
        };

        // 创建元素选择面板
        this.baseItemSelectPanel = new BaseItemSelectPanel<Template>(group.get(currGroupName).getElementList(), true) {
            @Override
            protected void addItem(String name) {
                List<Template> templateList = group.get(currGroupName).getElementList();
                // 新增模板
                templateList.add(new Template(name, ""));
                baseItemSelectPanel.reset(templateList, templateList.size() - 1);
            }

            @Override
            protected void copyItem(String newName, Template item) {
                // 复制模板
                Template template = CloneUtils.cloneByJson(item);
                template.setName(newName);
                List<Template> templateList = group.get(currGroupName).getElementList();
                templateList.add(template);
                baseItemSelectPanel.reset(templateList, templateList.size() - 1);
            }

            @Override
            protected void deleteItem(Template item) {
                // 删除模板
                group.get(currGroupName).getElementList().remove(item);
                baseItemSelectPanel.reset(group.get(currGroupName).getElementList(), 0);
                if (group.get(currGroupName).getElementList().isEmpty()) {
                    // 没有元素时，需要清空编辑框
                    templateEditor.reset("empty", "");
                }
            }

            @Override
            protected void selectedItem(Template item) {
                // 如果编辑面板已经实例化，需要选释放后再初始化
                if (templateEditor == null) {
                    FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
                    templateEditor = new TemplateEditor(project, item.getName() + ".vm", item.getCode(), TEMPLATE_DESCRIPTION_INFO, velocityFileType);
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    baseItemSelectPanel.getRightPanel().add(templateEditor.createComponent(), BorderLayout.CENTER);
                } else {
                    // 更新代码
                    templateEditor.reset(item.getName(), item.getCode());
                }
            }
        };

        // 添加调试面板
        this.addDebugPanel();

        mainPanel.add(baseGroupPanel, BorderLayout.NORTH);
        mainPanel.add(baseItemSelectPanel.getComponent(), BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * 获取存在的分组名
     *
     * @param groupName 分组名
     * @return 存在的分组名
     */
    private String findExistedGroupName(String groupName) {
        //如果groupName不存在
        if (!group.containsKey(groupName)) {
            if (group.containsKey(Settings.DEFAULT_NAME)) {//尝试使用默认分组
                return Settings.DEFAULT_NAME;
            } else {
                //获取第一个分组
                return group.keySet().stream().findFirst().orElse(Settings.DEFAULT_NAME);
            }
        }
        return groupName;
    }

    private JBIterable<DasTable> getTables(DbDataSource dataSource) {
        return dataSource.getModel().traverser().expandAndSkip(Conditions.instanceOf(DasNamespace.class)).filter(DasTable.class);
    }

    /**
     * 添加调试面板
     */
    private void addDebugPanel() {
        // 主面板
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(new JLabel("实时调试"));

        // 创建下拉框
        List<String> tableList = new ArrayList<>();
        List<DbDataSource> dataSourceList = DbPsiFacade.getInstance(project).getDataSources();
        if (!CollectionUtil.isEmpty(dataSourceList)) {
            dataSourceList.forEach(dbDataSource -> getTables(dbDataSource).forEach(table -> tableList.add(table.toString())));
        }
        ComboBoxModel<String> comboBoxModel = new CollectionComboBoxModel<>(tableList);
        ComboBox<String> comboBox = new ComboBox<>(comboBoxModel);
        panel.add(comboBox);

        // 调试动作按钮
        DefaultActionGroup actionGroup = new DefaultActionGroup(new AnAction(AllIcons.Debugger.Console) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                // 获取选中的表
                String name = (String) comboBox.getSelectedItem();
                List<DbDataSource> dataSourceList = DbPsiFacade.getInstance(project).getDataSources();
                DasTable dasTable = null;
                if (!CollectionUtil.isEmpty(dataSourceList)) {
                    for (DbDataSource dbDataSource : dataSourceList) {
                        for (DasTable table : getTables(dbDataSource)) {
                            if (Objects.equals(table.toString(), name)) {
                                dasTable = table;
                            }
                        }
                    }
                }
                if (dasTable == null) {
                    return;
                }
                DbTable dbTable = null;
                if (dasTable instanceof DbTable) {
                    // 针对2017.2版本做兼容
                    dbTable = (DbTable) dasTable;
                } else {
                    Method method = ReflectionUtil.getMethod(DbPsiFacade.class, "findElement", DasObject.class);
                    if (method == null) {
                        Messages.showWarningDialog("findElement method not found", MsgValue.TITLE_INFO);
                        return;
                    }
                    try {
                        // 针对2017.2以上版本做兼容
                        dbTable = (DbTable) method.invoke(DbPsiFacade.getInstance(project), dasTable);
                    } catch (IllegalAccessException|InvocationTargetException e1) {
                        ExceptionUtil.rethrow(e1);
                    }
                }
                // 获取表信息
                TableInfo tableInfo = TableInfoService.getInstance(project).getTableInfoAndConfig(dbTable);
                // 为未配置的表设置一个默认包名
                if (tableInfo.getSavePackageName() == null) {
                    tableInfo.setSavePackageName("com.companyname.modulename");
                }
                // 生成代码
                String code = CodeGenerateService.getInstance(project).generate(new Template("temp", templateEditor.getEditor().getDocument().getText()), tableInfo);

                // 创建编辑框
                EditorFactory editorFactory = EditorFactory.getInstance();
                PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                String fileName = templateEditor.getName();
                FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
                PsiFile psiFile = psiFileFactory.createFileFromText("EasyCodeTemplateDebug.vm.ft", velocityFileType, code, 0, true);
                // 标识为模板，让velocity跳过语法校验
                psiFile.getViewProvider().putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, FileTemplateManager.getInstance(project).getDefaultProperties());
                Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                assert document != null;
                Editor editor = editorFactory.createEditor(document, project, velocityFileType, true);
                // 配置编辑框
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
                ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, new LightVirtualFile(fileName)));
                // 构建dialog
                DialogBuilder dialogBuilder = new DialogBuilder(project);
                dialogBuilder.setTitle(MsgValue.TITLE_INFO);
                JComponent component = editor.getComponent();
                component.setPreferredSize(new Dimension(800, 600));
                dialogBuilder.setCenterPanel(component);
                dialogBuilder.addCloseButton();
                dialogBuilder.addDisposable(() -> {
                    //释放掉编辑框
                    editorFactory.releaseEditor(editor);
                    dialogBuilder.dispose();
                });
                dialogBuilder.show();
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(comboBox.getSelectedItem() != null);
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Template Debug", actionGroup, true);
        panel.add(actionToolbar.getComponent());
        baseGroupPanel.add(panel, BorderLayout.EAST);
    }

    /**
     * 数据发生修改时调用
     */
    private void onUpdate() {
        // 同步修改的代码
        Template template = baseItemSelectPanel.getSelectedItem();
        if (template != null) {
            template.setCode(templateEditor.getEditor().getDocument().getText());
        }
    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        return !settings.getTemplateGroupMap().equals(group) || !settings.getCurrTemplateGroupName().equals(currGroupName);
    }

    /**
     * 保存方法
     */
    @Override
    public void apply() {
        settings.setTemplateGroupMap(group);
        settings.setCurrTemplateGroupName(currGroupName);
    }

    /**
     * 重置方法
     */
    @Override
    public void reset() {
        // 没修改过的清空下不需要重置
        if (!isModified()) {
            return;
        }
        // 防止对象篡改，需要进行克隆
        this.group = CloneUtils.cloneByJson(settings.getTemplateGroupMap(), new TypeReference<Map<String, TemplateGroup>>() {});
        this.currGroupName = settings.getCurrTemplateGroupName();
        this.currGroupName = findExistedGroupName(settings.getCurrTemplateGroupName());
        if (baseGroupPanel == null) {
            return;
        }
        // 重置元素选择面板
        baseGroupPanel.reset(new ArrayList<>(group.keySet()), currGroupName);
    }

    /**
     * 关闭回调方法
     */
    @Override
    public void disposeUIResources() {
        // 释放编辑框
        if (templateEditor != null) {
            templateEditor.onClose();
        }
    }
}
