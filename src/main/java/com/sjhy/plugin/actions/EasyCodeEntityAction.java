package com.sjhy.plugin.actions;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.tool.CacheDataUtils;
import com.sjhy.plugin.ui.SelectSavePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 从Java类生成代码菜单
 */
public class EasyCodeEntityAction extends AnAction {

    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = Arrays.stream(psiJavaFile.getClasses())
                .filter(o -> o.getModifierList() != null && o.getModifierList().hasModifierProperty("public"))
                .findFirst()
                .orElse(null);
        if (psiClass == null) {
            return;
        }
        cacheDataUtils.setSelectPsiClass(psiClass);
        cacheDataUtils.setPsiClassList(Lists.newArrayList(psiClass));
        new SelectSavePath(project, true).open();
    }



}
