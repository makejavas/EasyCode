package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ExceptionUtil;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
     * 设置文件内容
     *
     * @param project 项目对象
     * @param psiFile 文件
     * @param text    文件内容
     * @return 覆盖后的文档对象
     */
    public Document writeFileContent(Project project, PsiFile psiFile, String fileName, String text) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiFile);
        if (document == null) {
            throw new IllegalStateException("获取文档对象失败，fileName：" + fileName);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> document.setText(text));
        // 提交改动，并非VCS中的提交文件
        psiDocumentManager.commitDocument(document);
        return document;
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
