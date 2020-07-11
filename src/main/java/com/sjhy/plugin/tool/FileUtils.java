package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.SaveFile;
import lombok.NonNull;

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
     * 读取文件内容（文本文件）
     *
     * @param jsonFile JSON配置文件
     * @return 文件内容
     */
    public String read(@NonNull PsiFile jsonFile) {
        return jsonFile.getText();
    }

    /**
     * 写入文件
     *
     * @param saveFile 需要保存的文件对象
     */
    public void write(SaveFile saveFile) {
        // 校验目录是否存在
        PsiManager psiManager = PsiManager.getInstance(saveFile.getProject());
        PsiDirectory psiDirectory;
        VirtualFile directory = LocalFileSystem.getInstance().findFileByPath(saveFile.getPath());
        if (directory == null) {
            // 尝试创建目录
            if (saveFile.isOperateTitle() && !MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "Directory " + saveFile.getPath() + " Not Found, Confirm Create?").isYes()) {
                return;
            }
            psiDirectory = WriteCommandAction.runWriteCommandAction(saveFile.getProject(), (Computable<PsiDirectory>) () -> {
                try {
                    VirtualFile dir = VfsUtil.createDirectoryIfMissing(saveFile.getPath());
                    LOG.assertTrue(dir != null);
                    // 重载文件，防止发生IndexNotReadyException异常
                    FileDocumentManager.getInstance().reloadFiles(dir);
                    return psiManager.findDirectory(dir);
                } catch (IOException e) {
                    LOG.error("path " + saveFile.getPath() + " error");
                    ExceptionUtil.rethrow(e);
                    return null;
                }
            });
        } else {
            psiDirectory = psiManager.findDirectory(directory);
        }
        if (psiDirectory == null) {
            return;
        }
        // 保存或替换文件
        PsiFile oldFile = psiDirectory.findFile(saveFile.getFile().getName());
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(saveFile.getProject());
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        if (saveFile.isOperateTitle() && oldFile != null) {
            MessageDialogBuilder.YesNoCancel yesNoCancel = MessageDialogBuilder.yesNoCancel(MsgValue.TITLE_INFO, "File " + saveFile.getFile().getName() + " Exists, Select Operate Mode?");
            yesNoCancel.yesText("Cover");
            yesNoCancel.noText("Compare");
            yesNoCancel.cancelText("Cancel");
            int result = yesNoCancel.show();
            switch (result) {
                case Messages.YES:
                    break;
                case Messages.NO:
                    // 对比代码时也格式化代码
                    if (saveFile.isReformat()) {
                        // 保留旧文件内容，用新文件覆盖旧文件执行格式化，然后再还原旧文件内容
                        String oldText = oldFile.getText();
                        WriteCommandAction.runWriteCommandAction(saveFile.getProject(), () -> psiDocumentManager.getDocument(oldFile).setText(saveFile.getFile().getText()));
                        // 提交所有改动，并非VCS中的提交文件
                        PsiDocumentManager.getInstance(saveFile.getProject()).commitAllDocuments();
                        reformatFile(saveFile.getProject(), Collections.singletonList(oldFile));
                        // 提交所有改动，并非VCS中的提交文件
                        PsiDocumentManager.getInstance(saveFile.getProject()).commitAllDocuments();
                        String newText = oldFile.getText();
                        WriteCommandAction.runWriteCommandAction(saveFile.getProject(), () -> psiDocumentManager.getDocument(oldFile).setText(oldText));
                        // 提交所有改动，并非VCS中的提交文件
                        PsiDocumentManager.getInstance(saveFile.getProject()).commitAllDocuments();
                        saveFile.setVirtualFile(new LightVirtualFile(saveFile.getFile().getName(), saveFile.getFile().getFileType(), newText));
                    }
                    CompareFileUtils.showCompareWindow(saveFile.getProject(), fileDocumentManager.getFile(psiDocumentManager.getDocument(oldFile)), saveFile.getVirtualFile());
                    return;
                case Messages.CANCEL:
                default:
                    return;
            }
        }
        PsiDirectory finalPsiDirectory = psiDirectory;
        PsiFile finalFile = WriteCommandAction.runWriteCommandAction(saveFile.getProject(), (Computable<PsiFile>) () -> {
            if (oldFile == null) {
                // 提交所有改动，并非VCS中的提交文件
                PsiDocumentManager.getInstance(saveFile.getProject()).commitAllDocuments();
                return (PsiFile) finalPsiDirectory.add(saveFile.getFile());
            } else {
                // 对旧文件进行替换操作
                Document document = psiDocumentManager.getDocument(oldFile);
                LOG.assertTrue(document != null);
                document.setText(saveFile.getFile().getText());
                return oldFile;
            }
        });
        // 判断是否需要进行代码格式化操作
        if (saveFile.isReformat()) {
            reformatFile(saveFile.getProject(), Collections.singletonList(finalFile));
        }
        // 提交所有改动，并非VCS中的提交文件
        PsiDocumentManager.getInstance(saveFile.getProject()).commitAllDocuments();
    }

    /**
     * 执行格式化
     *
     * @param project     项目对象
     * @param psiFileList 文件列表
     */
    @SuppressWarnings("unchecked")
    private void reformatFile(Project project, List<PsiFile> psiFileList) {
        if (CollectionUtil.isEmpty(psiFileList)) {
            return;
        }
        // 提交所有改动，并非VCS中的提交文件
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        // 尝试对文件进行格式化处理
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, psiFileList.toArray(new PsiFile[0]), null, false);
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
    }
}
