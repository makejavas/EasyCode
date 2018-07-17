package com.sjhy.plugin.tool;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.sjhy.plugin.entity.Template;

import java.util.List;

public class CacheDataUtils {
    //单例模式
    private static class Instance {
        private static final CacheDataUtils ME = new CacheDataUtils();
    }
    public static CacheDataUtils getInstance() {
        return Instance.ME;
    }
    private CacheDataUtils(){}

    private List<DbTable> dbTableList;
    private Module[] modules;
    private Project project;
    private DbTable selectDbTable;
    private String savePath;
    private List<Template> selectTemplate;
    private String packageName;
    private Module selectModule;

    public Module getSelectModule() {
        return selectModule;
    }

    public void setSelectModule(Module selectModule) {
        this.selectModule = selectModule;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Template> getSelectTemplate() {
        return selectTemplate;
    }

    public void setSelectTemplate(List<Template> selectTemplate) {
        this.selectTemplate = selectTemplate;
    }

    public List<DbTable> getDbTableList() {
        return dbTableList;
    }

    public void setDbTableList(List<DbTable> dbTableList) {
        this.dbTableList = dbTableList;
    }

    public Module[] getModules() {
        return modules;
    }

    public void setModules(Module[] modules) {
        this.modules = modules;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public DbTable getSelectDbTable() {
        return selectDbTable;
    }

    public void setSelectDbTable(DbTable selectDbTable) {
        this.selectDbTable = selectDbTable;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
