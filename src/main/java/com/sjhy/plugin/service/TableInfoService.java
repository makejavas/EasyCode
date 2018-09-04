package com.sjhy.plugin.service;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.tool.CollectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 表信息服务，Project级Service
 * 1.提供原始对象转包装对象服务
 * 2.提供表额外配置信息持久化服务
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 11:27
 */
@SuppressWarnings("unchecked")
public interface TableInfoService {
    /**
     * 获取实例对象
     * @param project 项目对象
     * @return 实例对象
     */
    static TableInfoService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, TableInfoService.class);
    }

    /**
     * 通过DbTable获取TableInfo
     *
     * @param dbTable 原始表对象
     * @return 表信息对象
     */
    TableInfo getTableInfoByDbTable(DbTable dbTable);

    /**
     * 通过DbTable获取TableInfo
     *
     * @param dbTables 原始表对象
     * @return 表信息对象
     */
    default List<TableInfo> getTableInfoByDbTable(Collection<DbTable> dbTables) {
        if (CollectionUtil.isEmpty(dbTables)) {
            return Collections.EMPTY_LIST;
        }
        List<TableInfo> tableInfoList = new ArrayList<>(dbTables.size());
        dbTables.forEach(dbTable -> tableInfoList.add(this.getTableInfoByDbTable(dbTable)));
        return tableInfoList;
    }

    /**
     * 获取表信息并加载配置信息
     *
     * @param dbTable 原始表对象
     * @return 表信息对象
     */
    TableInfo getTableInfoAndConfig(DbTable dbTable);

    /**
     * 获取表信息并加载配置信息
     *
     * @param dbTables 原始表对象
     * @return 表信息对象
     */
    default List<TableInfo> getTableInfoAndConfig(Collection<DbTable> dbTables) {
        if (CollectionUtil.isEmpty(dbTables)) {
            return Collections.EMPTY_LIST;
        }
        List<TableInfo> tableInfoList = new ArrayList<>(dbTables.size());
        dbTables.forEach(dbTable -> tableInfoList.add(this.getTableInfoAndConfig(dbTable)));
        return tableInfoList;
    }

    /**
     * 类型校验，如果存在未知类型则引导用于去条件类型
     *
     * @param dbTable 原始表对象
     * @return 是否验证通过
     */
    boolean typeValidator(DbTable dbTable);

    /**
     * 保存数据
     *
     * @param tableInfo 表信息对象
     */
    void save(TableInfo tableInfo);
}
