package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.dict.GlobalDict;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * 文件工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class FileUtils {
    private static final Logger LOG = Logger.getInstance(FileUtils.class);
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
     * 创建子目录
     *
     * @param project 文件对象
     * @param parent  父级目录
     * @param dirName 子目录
     * @return 目录对象
     */
    public VirtualFile createChildDirectory(Project project, VirtualFile parent, String dirName) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<VirtualFile>) () -> {
            try {
                return VfsUtil.createDirectoryIfMissing(parent, dirName);
            } catch (IOException e) {
                Messages.showWarningDialog("目录创建失败：" + dirName, GlobalDict.TITLE_INFO);
                return null;
            }
        });
    }

    /**
     * 创建子文件
     *
     * @param project  项目对象
     * @param parent   父级目录
     * @param fileName 子文件名
     * @return 文件对象
     */
    public VirtualFile createChildFile(Project project, VirtualFile parent, String fileName) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<VirtualFile>) () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            try {
                PsiDirectory directory = psiManager.findDirectory(parent);
                if (directory != null) {
                    PsiFile psiFile = directory.createFile(fileName);
                    return psiFile.getVirtualFile();
                }
                return parent.createChildData(new Object(), fileName);
            } catch (IOException e) {
                Messages.showWarningDialog("文件创建失败：" + fileName, GlobalDict.TITLE_INFO);
                return null;
            }
        });
    }

    /**
     * 设置文件内容
     *
     * @param project 项目对象
     * @param file    文件
     * @param text    文件内容
     * @return 覆盖后的文档对象
     */
    public Document writeFileContent(Project project, VirtualFile file, String fileName, String text) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(file);
        if (document == null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    file.setBinaryContent(text.getBytes());
                } catch (IOException e) {
                    throw new IllegalStateException("二进制文件写入失败，fileName：" + fileName);
                }
            });
            return fileDocumentManager.getDocument(file);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> document.setText(text));
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        // 提交改动，并非VCS中的提交文件
        psiDocumentManager.commitDocument(document);
        return document;
    }

    /**
     * 格式化虚拟文件
     *
     * @param project     项目对象
     * @param virtualFile 虚拟文件
     */
    public void reformatFile(Project project, VirtualFile virtualFile) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }
        reformatFile(project, Collections.singletonList(psiFile));
    }

    /**
     * 执行格式化
     *
     * @param project     项目对象
     * @param psiFileList 文件列表
     */
    @SuppressWarnings("unchecked")
    public void reformatFile(Project project, List<PsiFile> psiFileList) {
        if (CollectionUtil.isEmpty(psiFileList)) {
            return;
        }
        // 尝试对文件进行格式化处理
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, psiFileList.toArray(new PsiFile[0]), null, false);
        // 优化导入，有时候会出现莫名其妙的问题，暂时关闭
//        processor = new OptimizeImportsProcessor(processor);
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
    }
}
