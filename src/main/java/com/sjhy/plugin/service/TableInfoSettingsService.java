package com.sjhy.plugin.service;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.sjhy.plugin.dto.TableInfoSettingsDTO;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.service.impl.TableInfoSettingsServiceImpl;
import com.sjhy.plugin.tool.ProjectUtils;

/**
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 15:16
 */
public interface TableInfoSettingsService extends PersistentStateComponent<TableInfoSettingsDTO> {
    /**
     * 获取实例
     *
     * @return {@link SettingsStorageService}
     */
    static TableInfoSettingsService getInstance() {
        return ServiceManager.getService(ProjectUtils.getCurrProject(), TableInfoSettingsServiceImpl.class);
    }

    /**
     * 获取表格信息
     *
     * @param dbTable 数据库表
     * @return {@link TableInfo}
     */
    TableInfo getTableInfo(DbTable dbTable);

    /**
     * 保存表信息
     *
     * @param tableInfo 表信息
     */
    void saveTableInfo(TableInfo tableInfo);
}
