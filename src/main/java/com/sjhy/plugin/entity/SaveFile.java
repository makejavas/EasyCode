package com.sjhy.plugin.entity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.LightVirtualFile;
import com.sjhy.plugin.tool.CompareFileUtils;
import com.sjhy.plugin.tool.FileUtils;
import com.sjhy.plugin.tool.MessageDialogUtils;
import com.sjhy.plugin.tool.ProjectUtils;
import lombok.Data;
import lombok.NonNull;

import java.io.File;

/**
 * 需要保存的文件
 * <p>
 * 如果文件保存在项目路径下，则使用idea提供的psi对象操作。如果文件保存在非项目路径下，则使用java原始IO流操作。
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
     * 文件名
     */
    private String fileName;
    /**
     * 文件内容
     */
    private String content;
    /**
     * 是否需要重新格式化代码
     */
    private boolean reformat;
    /**
     * 是否显示操作提示
     */
    private boolean operateTitle;
    /**
     * 文件工具类
     */
    private FileUtils fileUtils = FileUtils.getInstance();

    /**
     * 构建对象
     *
     * @param project      项目对象
     * @param path         绝对路径
     * @param fileName     文件名
     * @param content      文件内容
     * @param reformat     是否重新格式化代码
     * @param operateTitle 操作提示
     */
    public SaveFile(@NonNull Project project, @NonNull String path, @NonNull String fileName, String content, boolean reformat, boolean operateTitle) {
        this.path = path;
        this.project = project;
        this.fileName = fileName;
        LOG.assertTrue(content != null);
        this.content = content.replace("\r", "");
        this.reformat = reformat;
        this.operateTitle = operateTitle;
    }

    /**
     * 文件是否为项目文件
     *
     * @return 是否为项目文件
     */
    private boolean isProjectFile() {
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        // 无法获取到项目基本目录，可能是Default项目，直接返回非项目文件
        if (baseDir == null) {
            return false;
        }
        // 路径对比，判断项目路径是否为文件保存路径的子路径
        String projectPath = handlerPath(baseDir.getPath());
        String tmpFilePath = handlerPath(this.path);
        if (tmpFilePath.length() > projectPath.length()) {
            if (!"/".equals(tmpFilePath.substring(projectPath.length(), projectPath.length() + 1))) {
                return false;
            }
        }
        return tmpFilePath.indexOf(projectPath) == 0;
    }

    /**
     * 处理路径，统一分割符并转小写
     *
     * @param path 路径
     * @return 处理后的路径
     */
    private String handlerPath(String path) {
        return handlerPath(path, true);
    }

    /**
     * 处理路径，统一分割符并转小写
     *
     * @param path      路径
     * @param lowerCase 是否转小写
     * @return 处理后的路径
     */
    private String handlerPath(String path, boolean lowerCase) {
        // 统一分割符
        path = path.replace("\\", "/");
        // 避免重复分割符
        path = path.replace("//", "/");
        // 统一小写
        return lowerCase ? path.toLowerCase() : path;
    }

    /**
     * 通过IDEA自带的Psi文件方式写入
     */
    public void write() {
        // 判断目录是否存在
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        if (baseDir == null) {
            throw new IllegalStateException("项目基本路径不存在");
        }
        // 处理保存路径
        String savePath = handlerPath(this.path, false);
        if (isProjectFile()) {
            // 删除保存路径的前面部分
            savePath = savePath.substring(handlerPath(baseDir.getPath()).length());
        } else {
            baseDir = null;
        }
        // 删除开头与结尾的/符号
        while (savePath.startsWith("/")) {
            savePath = savePath.substring(1);
        }
        while (savePath.endsWith("/")) {
            savePath = savePath.substring(0, savePath.length() - 1);
        }
        // 查找保存目录是否存在
        VirtualFile saveDir;
        if (baseDir == null) {
            saveDir = VfsUtil.findFileByIoFile(new File(savePath), false);
        } else {
            saveDir = VfsUtil.findRelativeFile(baseDir, savePath.split("/"));
        }
        // 提示创建目录
        VirtualFile directory = titleCreateDir(saveDir, baseDir, savePath);
        if (directory == null) {
            return;
        }
        VirtualFile psiFile = directory.findChild(this.fileName);
        // 保存或覆盖
        saveOrReplaceFile(psiFile, directory);
    }

    /**
     * 提示创建目录
     *
     * @param saveDir 保存路径
     * @return 是否放弃执行
     */
    private VirtualFile titleCreateDir(VirtualFile saveDir, VirtualFile baseDir, String savePath) {
        if (saveDir != null) {
            return saveDir;
        }
        // 尝试创建目录
        String msg = String.format("Directory %s Not Found, Confirm Create?", this.path);
        if (this.operateTitle && !MessageDialogUtils.yesNo(project, msg)) {
            return null;
        }
        saveDir = fileUtils.createChildDirectory(project, baseDir, savePath);
        return saveDir;
    }

    /**
     * 保存或替换文件
     *
     * @param file      文件
     * @param directory 目录
     */
    private void saveOrReplaceFile(VirtualFile file, VirtualFile directory) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document;
        // 文件不存在直接创建
        if (file == null) {
            file = fileUtils.createChildFile(project, directory, this.fileName);
            if (file == null) {
                return;
            }
            document = coverFile(file);
        } else {
            // 提示覆盖文件
            if (operateTitle) {
                String msg = String.format("File %s Exists, Select Operate Mode?", file.getPath());
                int result = MessageDialogUtils.yesNoCancel(project, msg, "Conver", "Compare", "Cancel");
                switch (result) {
                    case Messages.YES:
                        // 覆盖文件
                        document = coverFile(file);
                        break;
                    case Messages.NO:
                        // 对比代码时也格式化代码
                        String newText = content;
                        if (reformat) {
                            // 保留旧文件内容，用新文件覆盖旧文件执行格式化，然后再还原旧文件内容
                            String oldText = getFileText(file);
                            Document tmpDoc = coverFile(file);
                            // 格式化代码
                            FileUtils.getInstance().reformatFile(project, file);
                            // 提交文档改动，并非VCS中的提交文件
                            psiDocumentManager.commitDocument(tmpDoc);
                            // 获取新的文件内容
                            newText = getFileText(file);
                            // 还原旧文件
                            coverFile(file, oldText);
                        }
                        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
                        CompareFileUtils.showCompareWindow(project, file, new LightVirtualFile(fileName, fileType, newText));
                        return;
                    case Messages.CANCEL:
                    default:
                        return;
                }
            } else {
                // 没有操作提示的情况下直接覆盖
                document = coverFile(file);
            }
        }
        // 执行代码格式化操作
        if (reformat) {
            FileUtils.getInstance().reformatFile(project, file);
        }
        // 提交文档改动，并非VCS中的提交文件
        psiDocumentManager.commitDocument(document);
    }

    private String getFileText(VirtualFile file) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Document document = fileDocumentManager.getDocument(file);
        if (document == null) {
            throw new IllegalStateException("virtual file to document failure");
        }
        return document.getText();
    }

    /**
     * 覆盖文件
     *
     * @param file 文件
     * @return 覆盖后的文档对象
     */
    private Document coverFile(VirtualFile file) {
        return coverFile(file, content);
    }

    /**
     * 覆盖文件
     *
     * @param file 文件
     * @param text 文件内容
     * @return 覆盖后的文档对象
     */
    private Document coverFile(VirtualFile file, String text) {
        return FileUtils.getInstance().writeFileContent(project, file, fileName, text);
    }
}
