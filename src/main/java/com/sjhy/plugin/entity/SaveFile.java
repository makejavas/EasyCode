package com.sjhy.plugin.entity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.sjhy.plugin.tool.FileUtils;
import lombok.Data;

/**
 * 需要保存的文件
 *
 * @author makejava
 * @version 1.0.0
 * @since 2020/04/20 22:54
 */
@Data
public class SaveFile {
    private static final Logger LOG = Logger.getInstance(SaveFile.class);
    /**
     * 所属项目
     */
    private Project project;
    /**
     * 文件保存目录
     */
    private String path;
    /**
     * 虚拟文件
     */
    private VirtualFile virtualFile;
    /**
     * 需要保存的文件
     */
    private PsiFile file;
    /**
     * 是否需要重新格式化代码
     */
    private boolean reformat;
    /**
     * 文件
     */
    private PsiFile psiFile;
    /**
     * 是否显示操作提示
     */
    private boolean operateTitle;

    /**
     * 构建对象
     *
     * @param path     路径
     * @param fileName 文件没
     * @param reformat 是否重新格式化代码
     */
    public SaveFile(Project project, String path, String fileName, String content, boolean reformat, boolean operateTitle) {
        this.path = path;
        this.project = project;
        // 构建文件对象
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        LOG.assertTrue(content != null);
        // 换行符统一使用\n
        this.file = psiFileFactory.createFileFromText(fileName, FileTypes.UNKNOWN, content.replace("\r", ""));
        this.virtualFile = new LightVirtualFile(fileName, content.replace("\r", ""));
        this.reformat = reformat;
        this.operateTitle = operateTitle;
    }

    /**
     * 写入文件
     */
    public void write() {
        FileUtils.getInstance().write(this);
    }
}
