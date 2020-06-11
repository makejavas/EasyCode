package com.sjhy.plugin.tool;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2020/06/11 15:47
 */
public class CompareFileUtils {

    /**
     * 显示文件对比框
     *
     * @param project   项目
     * @param leftFile  左边的文件
     * @param rightFile 右边的文件
     */
    public static void showCompareWindow(Project project, VirtualFile leftFile, VirtualFile rightFile) {
        DiffContentFactory contentFactory = DiffContentFactory.getInstance();
        DiffRequestFactory requestFactory = DiffRequestFactory.getInstance();

        DiffContent leftContent = contentFactory.create(project, leftFile);
        DiffContent rightContent = contentFactory.create(project, rightFile);

        MutableDiffRequestChain chain = new MutableDiffRequestChain(leftContent, rightContent);

        chain.setWindowTitle(requestFactory.getTitle(leftFile, rightFile));
        chain.setTitle1(requestFactory.getContentTitle(leftFile));
        chain.setTitle2(requestFactory.getContentTitle(rightFile));
        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.MODAL);
    }

}
