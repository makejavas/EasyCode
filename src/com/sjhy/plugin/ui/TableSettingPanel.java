package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.ColumnConfig;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.entity.ColumnConfigType;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.tool.CloneUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TableSettingPanel extends AbstractTableGroupPanel<ColumnConfigGroup, ColumnConfig> implements Configurable {
    private ConfigService configService = ConfigService.getInstance();
    private CloneUtils cloneUtils = CloneUtils.getInstance();
    TableSettingPanel() {
        super(CloneUtils.getInstance().cloneMap(ConfigService.getInstance().getColumnConfigGroupMap()), ConfigService.getInstance().getCurrColumnConfigGroupName());
    }

    @Override
    protected Object[] toRow(ColumnConfig item) {
        return new Object[]{item.getTitle(), item.getType().name(), item.getSelectValue()};
    }

    @Override
    protected ColumnConfig toItem(Object[] rowData) {
        return new ColumnConfig((String) rowData[0], ColumnConfigType.valueOf((String) rowData[1]), (String) rowData[2]);
    }

    @Override
    protected String getItemName(ColumnConfig item) {
        return item.getTitle();
    }

    @Override
    protected ColumnConfig createItem(String value) {
        return new ColumnConfig(value, ColumnConfigType.TEXT);
    }

    @Override
    protected ColumnConfig[] initColumn() {
        ColumnConfig[] columnConfigs = new ColumnConfig[3];
        columnConfigs[0] = new ColumnConfig("title", ColumnConfigType.TEXT);
        columnConfigs[1] = new ColumnConfig("type", ColumnConfigType.SELECT, "TEXT,SELECT,BOOLEAN");
        columnConfigs[2] = new ColumnConfig("selectValue", ColumnConfigType.TEXT);
        return columnConfigs;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Table Editor Config";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return getMainPanel();
    }

    @Override
    public boolean isModified() {
        refresh();
        return !configService.getColumnConfigGroupMap().equals(group) || !configService.getCurrColumnConfigGroupName().equals(currGroupName);
    }

    @Override
    public void apply() {
        configService.setColumnConfigGroupMap(group);
        configService.setCurrColumnConfigGroupName(currGroupName);
    }

    @Override
    public void reset() {
        init(cloneUtils.cloneMap(configService.getColumnConfigGroupMap()), configService.getCurrColumnConfigGroupName());
    }
}
