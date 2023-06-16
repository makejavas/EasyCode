package com.sjhy.plugin.service.impl;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.psi.PsiClass;
import com.sjhy.plugin.dto.TableInfoSettingsDTO;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.service.TableInfoSettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 15:20
 */
@State(name = "EasyCodeTableSetting", storages = @Storage("easyCodeTableSettingEncode.xml"))
public class TableInfoSettingsServiceImpl implements TableInfoSettingsService {

    private TableInfoSettingsDTO tableInfoSettings = new TableInfoSettingsDTO();

    @Nullable
    @Override
    public TableInfoSettingsDTO getState() {
        return tableInfoSettings;
    }

    @Override
    public void loadState(@NotNull TableInfoSettingsDTO state) {
        this.tableInfoSettings = state;
    }

    /**
     * 获取表格信息
     *
     * @param dbTable 数据库表
     * @return {@link TableInfo}
     */
    @Override
    public TableInfo getTableInfo(DbTable dbTable) {
        return Objects.requireNonNull(getState()).readTableInfo(dbTable);
    }

    /**
     * 获取表信息
     *
     * @param psiClass psi类
     * @return {@link TableInfo}
     */
    @Override
    public TableInfo getTableInfo(PsiClass psiClass) {
        return Objects.requireNonNull(getState()).readTableInfo(psiClass);
    }

    /**
     * 保存表信息
     *
     * @param tableInfo 表信息
     */
    @Override
    public void saveTableInfo(TableInfo tableInfo) {
        Objects.requireNonNull(getState()).saveTableInfo(tableInfo);
    }

    /**
     * 重置表信息
     *
     * @param dbTable 数据库表
     */
    @Override
    public void resetTableInfo(DbTable dbTable) {
        Objects.requireNonNull(getState()).resetTableInfo(dbTable);
    }

    /**
     * 删除表信息
     *
     * @param dbTable 数据库表
     */
    @Override
    public void removeTableInfo(DbTable dbTable) {
        Objects.requireNonNull(getState()).removeTableInfo(dbTable);
    }
}
