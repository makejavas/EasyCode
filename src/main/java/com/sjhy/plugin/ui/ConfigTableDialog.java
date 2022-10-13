package com.sjhy.plugin.ui;

import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.factory.CellEditorFactory;
import com.sjhy.plugin.service.TableInfoSettingsService;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.tool.CurrGroupUtils;
import com.sjhy.plugin.tool.ProjectUtils;
import com.sjhy.plugin.tool.StringUtils;
import com.sjhy.plugin.ui.base.ConfigTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 表配置窗口
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class ConfigTableDialog extends DialogWrapper {
    /**
     * 主面板
     */
    private final JPanel mainPanel;
    /**
     * 表信息对象
     */
    private TableInfo tableInfo;

    public ConfigTableDialog() {
        super(ProjectUtils.getCurrProject());
        this.mainPanel = new JPanel(new BorderLayout());
        this.initPanel();
    }

    private void initPanel() {
        init();
        this.tableInfo = TableInfoSettingsService.getInstance().getTableInfo(CacheDataUtils.getInstance().getSelectDbTable());
        setTitle("Config Table " + this.tableInfo.getObj().getName());
        ConfigTableModel model = new ConfigTableModel(this.tableInfo);
        JBTable table = new JBTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int totalWidth = 0;

        // 配置列编辑器
        TableColumn nameColumn = table.getColumn("name");
        nameColumn.setCellEditor(CellEditorFactory.createTextFieldEditor());
        nameColumn.setMinWidth(100);
        totalWidth+=100;
        TableColumn typeColumn = table.getColumn("type");
        typeColumn.setCellRenderer(new ComboBoxTableRenderer<>(GlobalDict.DEFAULT_JAVA_TYPE_LIST));
        typeColumn.setCellEditor(CellEditorFactory.createComboBoxEditor(true, GlobalDict.DEFAULT_JAVA_TYPE_LIST));
        typeColumn.setMinWidth(120);
        totalWidth+=120;
        TableColumn commentColumn = table.getColumn("comment");
        commentColumn.setCellEditor(CellEditorFactory.createTextFieldEditor());
        commentColumn.setMinWidth(140);
        totalWidth+=140;
        // 其他附加列
        for (ColumnConfig columnConfig : CurrGroupUtils.getCurrColumnConfigGroup().getElementList()) {
            TableColumn column = table.getColumn(columnConfig.getTitle());
            switch (columnConfig.getType()) {
                case TEXT:
                    column.setCellEditor(CellEditorFactory.createTextFieldEditor());
                    column.setMinWidth(120);
                    totalWidth+=120;
                    break;
                case SELECT:
                    if (StringUtils.isEmpty(columnConfig.getSelectValue())) {
                        column.setCellEditor(CellEditorFactory.createTextFieldEditor());
                    } else {
                        String[] split = columnConfig.getSelectValue().split(",");
                        ArrayList<String> list = new ArrayList<>(Arrays.asList(split));
                        // 添加一个空值作为默认值
                        list.add(0, "");
                        split = list.toArray(new String[0]);
                        column.setCellRenderer(new ComboBoxTableRenderer<>(split));
                        column.setCellEditor(CellEditorFactory.createComboBoxEditor(false, split));
                    }
                    column.setMinWidth(100);
                    totalWidth+=100;
                    break;
                case BOOLEAN:
                    column.setCellRenderer(new BooleanTableCellRenderer());
                    column.setCellEditor(new BooleanTableCellEditor());
                    column.setMinWidth(60);
                    totalWidth+=60;
                    break;
                default:
                    break;
            }
        }

        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        this.mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        this.mainPanel.setMinimumSize(new Dimension(totalWidth, Math.max(300, totalWidth / 3)));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }

    @Override
    protected void doOKAction() {
        // 保存信息
        TableInfoSettingsService.getInstance().saveTableInfo(tableInfo);
        super.doOKAction();
    }

}
