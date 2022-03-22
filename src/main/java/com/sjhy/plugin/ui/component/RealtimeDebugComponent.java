package com.sjhy.plugin.ui.component;

import com.intellij.database.model.DasNamespace;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.CollectionUtil;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.ui.base.EditorSettingsInit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

/**
 * 实时调试组件
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/12 15:54
 */
public class RealtimeDebugComponent {
    @Getter
    private JPanel mainPanel;

    private ComboBox<String> comboBox;

    /**
     * 所有表
     */
    private Map<String, DasTable> allTables;

    private EditorComponent<Template> editorComponent;

    public RealtimeDebugComponent(EditorComponent<Template> editorComponent) {
        this.editorComponent = editorComponent;
        this.mainPanel = new JPanel(new FlowLayout());
        this.init();
        this.refreshTable();
    }

    private void init() {
        this.mainPanel.add(new JBLabel("实时调试"));
        // 支持搜索的下拉框
        this.comboBox = new ComboBox<>();
        // 开启搜索支持
        this.comboBox.setSwingPopup(false);
        this.mainPanel.add(this.comboBox);
        // 提交按钮
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnAction(AllIcons.Debugger.Console) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                runDebug();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                String selectVal = (String) comboBox.getSelectedItem();
                e.getPresentation().setEnabled(allTables != null && allTables.containsKey(selectVal));
            }
        });
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Template Debug", actionGroup, true);
        this.mainPanel.add(actionToolbar.getComponent());
    }

    private void runDebug() {
        // 获取选中的表
        String name = (String) comboBox.getSelectedItem();
        DasTable dasTable = this.allTables.get(name);
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
                Messages.showWarningDialog("findElement method not found", GlobalDict.TITLE_INFO);
                return;
            }
            try {
                // 针对2017.2以上版本做兼容
                dbTable = (DbTable) method.invoke(DbPsiFacade.getInstance(ProjectUtils.getCurrProject()), dasTable);
            } catch (IllegalAccessException | InvocationTargetException e1) {
                ExceptionUtil.rethrow(e1);
            }
        }
        // 获取表信息
        TableInfo tableInfo = TableInfoSettingsService.getInstance().getTableInfo(dbTable);
        // 为未配置的表设置一个默认包名
        if (tableInfo.getSavePackageName() == null) {
            tableInfo.setSavePackageName("com.companyname.modulename");
        }
        // 生成代码
        String code = CodeGenerateService.getInstance(ProjectUtils.getCurrProject()).generate(new Template("temp", editorComponent.getFile().getCode()), tableInfo);
        String fileName = editorComponent.getFile().getName();
        // 创建编辑框
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument(code);
        // 标识为模板，让velocity跳过语法校验
        document.putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, FileTemplateManager.getInstance(ProjectUtils.getCurrProject()).getDefaultProperties());
        Editor editor = editorFactory.createViewer(document, ProjectUtils.getCurrProject());
        // 配置编辑框
        EditorSettingsInit.init(editor);
        ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(ProjectUtils.getCurrProject(), fileName));
        // 构建dialog
        DialogBuilder dialogBuilder = new DialogBuilder(ProjectUtils.getCurrProject());
        dialogBuilder.setTitle(GlobalDict.TITLE_INFO);
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

    private void refreshTable() {
        // 获取当前项目所有数据源
        List<DbDataSource> dataSources = DbPsiFacade.getInstance(ProjectUtils.getCurrProject()).getDataSources();
        this.allTables = new HashMap<>(16);
        for (DbDataSource dataSource : dataSources) {
            for (DasTable table : getTables(dataSource)) {
                this.allTables.put(table.toString(), table);
            }
        }
        this.comboBox.removeAllItems();
        for (String tableName : getAllTableNameBySort()) {
            this.comboBox.addItem(tableName);
        }
    }


    private List<String> getAllTableNameBySort() {
        if (CollectionUtil.isEmpty(this.allTables)) {
            return Collections.emptyList();
        }
        // 表排前面，视图排后面
        List<String> tableList = new ArrayList<>();
        List<String> viewList = new ArrayList<>();
        for (String name : this.allTables.keySet()) {
            if (name.endsWith("table")) {
                tableList.add(name);
            } else {
                viewList.add(name);
            }
        }
        // 排序后进行拼接
        Collections.sort(tableList);
        Collections.sort(viewList);
        tableList.addAll(viewList);
        return tableList;
    }

    private JBIterable<DasTable> getTables(DbDataSource dataSource) {
        return dataSource.getModel().traverser().expandAndSkip(Conditions.instanceOf(DasNamespace.class)).filter(DasTable.class);
    }
}
