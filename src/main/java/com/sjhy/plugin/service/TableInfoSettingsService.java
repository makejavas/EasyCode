package com.sjhy.plugin.service;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.sjhy.plugin.dto.TableInfoSettingsDTO;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.service.impl.TableInfoSettingsServiceImpl;
import com.sjhy.plugin.tool.ProjectUtils;

import java.io.IOException;

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
        try {
            return ServiceManager.getService(ProjectUtils.getCurrProject(), TableInfoSettingsServiceImpl.class);
        } catch (AssertionError e) {
            // 出现配置文件被错误修改，或不兼容时直接删除配置文件。
            VirtualFile workspaceFile = ProjectUtils.getCurrProject().getWorkspaceFile();
            if (workspaceFile != null) {
                VirtualFile configFile = workspaceFile.getParent().findChild("easyCodeTableSetting.xml");
                if (configFile != null && configFile.exists()) {
                    WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrProject(), () -> {
                        try {
                            configFile.delete(null);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            }
            // 重新获取配置
            return ServiceManager.getService(ProjectUtils.getCurrProject(), TableInfoSettingsServiceImpl.class);
        }
    }

    /**
     * 获取表格信息
     *
     * @param dbTable 数据库表
     * @return {@link TableInfo}
     */
    TableInfo getTableInfo(DbTable dbTable);

    /**
     * 获取表信息
     *
     * @param psiClass psi类
     * @return {@link TableInfo}
     */
    TableInfo getTableInfo(PsiClass psiClass);

    /**
     * 保存表信息
     *
     * @param tableInfo 表信息
     */
    void saveTableInfo(TableInfo tableInfo);

    /**
     * 重置表信息
     *
     * @param dbTable 数据库表
     */
    void resetTableInfo(DbTable dbTable);

    /**
     * 删除表信息
     *
     * @param dbTable 数据库表
     */
    void removeTableInfo(DbTable dbTable);
}
