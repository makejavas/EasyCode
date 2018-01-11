package com.sjhy.plugin.action;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.sjhy.plugin.tool.CacheDataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainActionGroup extends ActionGroup {
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        if (anActionEvent==null) {
            return AnAction.EMPTY_ARRAY;
        }
        Project project = anActionEvent.getProject();
        if (project==null) {
            return AnAction.EMPTY_ARRAY;
        }
        //获取模型
        Module[] modules = ModuleManager.getInstance(project).getModules();
        //获取选中的单个表
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        DbTable selectDbTable = null;
        if (psiElement instanceof DbTable) {
            selectDbTable = (DbTable) psiElement;
        }
        if (selectDbTable==null) {
            return AnAction.EMPTY_ARRAY;
        }
        //获取选中的所有表
        PsiElement[] psiElements = anActionEvent.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return AnAction.EMPTY_ARRAY;
        }
        List<DbTable> dbTableList = new ArrayList<>();
        for (PsiElement element : psiElements) {
            if (!(element instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) element;
            dbTableList.add(dbTable);
        }
        if (dbTableList.isEmpty()) {
            return AnAction.EMPTY_ARRAY;
        }
        //保存数据
        cacheDataUtils.setProject(project);
        cacheDataUtils.setDbTableList(dbTableList);
        cacheDataUtils.setModules(modules);
        cacheDataUtils.setSelectDbTable(selectDbTable);
        return getMenuList();
    }

    private AnAction[] getMenuList() {
        String mainActionId = "com.sjhy.easy.code.action.generate";
        String configActionId = "com.sjhy.easy.code.action.config";
        ActionManager actionManager = ActionManager.getInstance();
        AnAction mainAction = actionManager.getAction(mainActionId);
        if (mainAction==null) {
            mainAction = new MainAction("Generate Code");
            actionManager.registerAction(mainActionId, mainAction);
        }
        AnAction configAction = actionManager.getAction(configActionId);
        if (configAction==null) {
            configAction = new ConfigAction("Config Table");
            actionManager.registerAction(configActionId, configAction);
        }
        return new AnAction[]{mainAction, configAction};
    }
}
