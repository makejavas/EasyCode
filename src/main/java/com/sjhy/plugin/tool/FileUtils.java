package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.MsgValue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 文件工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class FileUtils {
    private static volatile FileUtils fileUtils;

    /**
     * 单例模式
     */
    public static FileUtils getInstance() {
        if (fileUtils == null) {
            synchronized (FileUtils.class) {
                if (fileUtils == null) {
                    fileUtils = new FileUtils();
                }
            }
        }
        return fileUtils;
    }

    private FileUtils() {
    }

    /**
     * 读取文件内容（文本文件）
     *
     * @param project  项目对象
     * @param file 文件对象
     * @return 文件内容
     */
    public String read(Project project, File file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) {
            return null;
        }
        PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null) {
            return null;
        }
        return psiFile.getText();
    }

    /**
     * 写入文件内容，覆盖模式
     *
     * @param project  项目对象
     * @param file     文件
     * @param content  文件内容
     * @param reformat 是否对代码进行重新格式化
     */
    public void write(Project project, File file, String content, boolean reformat) {
        // 替换换行符
        content = content.replace("\r\n", "\n");
        // 创建文件
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile psiFile = psiFileFactory.createFileFromText(file.getName(), FileTypes.UNKNOWN, content);
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile parentVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file.getParentFile());
        if (parentVirtualFile == null) {
            // 自动创建目录
            Messages.showWarningDialog("目录不存在", MsgValue.TITLE_INFO);
            return;
        }
        PsiDirectory psiDirectory = psiManager.findDirectory(parentVirtualFile);
        saveFileAndFormatCode(project, psiDirectory, psiFile, reformat);
    }

    /**
     * 保存并格式化文件
     *
     * @param project      项目
     * @param psiDirectory 目录
     * @param psiFile      文件
     * @param reformat 是否对代码进行重新格式化
     */
    @SuppressWarnings("unchecked")
    private void saveFileAndFormatCode(Project project, PsiDirectory psiDirectory, PsiFile psiFile, boolean reformat) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile oldFile = psiDirectory.findFile(psiFile.getName());
            if (oldFile != null) {
                oldFile.delete();
            }
            PsiFile newFile = (PsiFile) psiDirectory.add(psiFile);
            // 提交所有改动，并非CVS中的提交文件
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            // 判断是否需要格式化代码
            if (!reformat) {
                return;
            }
            // 尝试对文件进行格式化处理
            AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, new PsiFile[]{newFile}, null, false);
            // 优化导入
            processor = new OptimizeImportsProcessor(processor);
            // 重新编排代码（会将代码中的属性与方法的顺序进行重新调整）
//            processor = new RearrangeCodeProcessor(processor);

            // 清理代码，进行旧版本兼容，旧版本的IDEA尚未提供该处理器
            try {
                Class<AbstractLayoutCodeProcessor> codeCleanupCodeProcessorCls = (Class<AbstractLayoutCodeProcessor>) Class.forName("com.intellij.codeInsight.actions.CodeCleanupCodeProcessor");
                Constructor<AbstractLayoutCodeProcessor> constructor = codeCleanupCodeProcessorCls.getConstructor(AbstractLayoutCodeProcessor.class);
                processor = constructor.newInstance(processor);
            } catch (ClassNotFoundException ignored) {
                // 类不存在直接忽略
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                // 抛出未知异常
                ExceptionUtil.rethrow(e);
            }
            // 执行处理
            processor.run();
        });
    }
}
