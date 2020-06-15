package com.sjhy.plugin.tool;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
//import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ExceptionUtil;

import java.lang.reflect.InvocationTargetException;

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

        try {
            Class<?> cls = Class.forName("com.intellij.diff.actions.impl.MutableDiffRequestChain");
            // 新版支持
            DiffContentFactory contentFactory = DiffContentFactory.getInstance();
            DiffRequestFactory requestFactory = DiffRequestFactory.getInstance();

            DiffContent leftContent = contentFactory.create(project, leftFile);
            DiffContent rightContent = contentFactory.create(project, rightFile);

            DiffRequestChain chain = (DiffRequestChain) cls.getConstructor(DiffContent.class, DiffContent.class).newInstance(leftContent, rightContent);
//            MutableDiffRequestChain chain = new MutableDiffRequestChain(leftContent, rightContent);

            cls.getMethod("setWindowTitle", String.class).invoke(chain, requestFactory.getTitle(leftFile, rightFile));
            cls.getMethod("setTitle1", String.class).invoke(chain, requestFactory.getContentTitle(leftFile));
            cls.getMethod("setTitle2", String.class).invoke(chain, requestFactory.getContentTitle(rightFile));
//            chain.setWindowTitle(requestFactory.getTitle(leftFile, rightFile));
//            chain.setTitle1(requestFactory.getContentTitle(leftFile));
//            chain.setTitle2(requestFactory.getContentTitle(rightFile));
            DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.MODAL);
        } catch (ClassNotFoundException e) {
            // 旧版兼容
            DiffRequest diffRequest = DiffRequestFactory.getInstance().createFromFiles(project, leftFile, rightFile);
            DiffManager.getInstance().showDiff(project, diffRequest, DiffDialogHints.MODAL);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            ExceptionUtil.rethrow(e);
        }
    }

}
