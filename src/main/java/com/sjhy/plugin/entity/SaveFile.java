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
import com.sjhy.plugin.dto.GenerateOptions;
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
     * 文件内容
     */
    private String content;
    /**
     * 文件工具类
     */
    private FileUtils fileUtils = FileUtils.getInstance();
    /**
     * 回调对象
     */
    private Callback callback;
    /**
     * 生成配置
     */
    private GenerateOptions generateOptions;

    /**
     * 保存文件
     *
     * @param project         项目
     * @param content         内容
     * @param callback        回调
     * @param generateOptions 生成选项
     */
    public SaveFile(@NonNull Project project, @NonNull String content, @NonNull Callback callback, @NonNull GenerateOptions generateOptions) {
        this.project = project;
        this.callback = callback;
        this.content = content.replace("\r", "");
        this.generateOptions = generateOptions;
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
        String tmpFilePath = handlerPath(callback.getSavePath());
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
        if (!Boolean.TRUE.equals(callback.getWriteFile())) {
            return;
        }
        // 判断目录是否存在
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        if (baseDir == null) {
            throw new IllegalStateException("项目基本路径不存在");
        }
        // 处理保存路径
        String savePath = handlerPath(callback.getSavePath(), false);
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
        VirtualFile psiFile = directory.findChild(callback.getFileName());
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
        String msg = String.format("Directory %s Not Found, Confirm Create?", callback.getSavePath());
        if (generateOptions.getTitleSure()) {
            saveDir = fileUtils.createChildDirectory(project, baseDir, savePath);
            return saveDir;
        } else if (generateOptions.getTitleRefuse()) {
            return null;
        } else {
            if (MessageDialogUtils.yesNo(project, msg)) {
                saveDir = fileUtils.createChildDirectory(project, baseDir, savePath);
                return saveDir;
            }
        }
        return null;
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
            file = fileUtils.createChildFile(project, directory, callback.getFileName());
            if (file == null) {
                return;
            }
            document = coverFile(file);
        } else {
            // 提示覆盖文件
            if (generateOptions.getTitleSure()) {
                // 默认选是
                document = coverFile(file);
            } else if (generateOptions.getTitleRefuse()) {
                // 默认选否
                return;
            } else {
                String msg = String.format("File %s Exists, Select Operate Mode?", file.getPath());
                int result = MessageDialogUtils.yesNoCancel(project, msg, "Convert", "Compare", "Cancel");
                switch (result) {
                    case Messages.YES:
                        // 覆盖文件
                        document = coverFile(file);
                        break;
                    case Messages.NO:
                        // 对比代码时也格式化代码
                        String newText = content;
                        if (Boolean.TRUE.equals(callback.getReformat())) {
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
                        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(callback.getFileName());
                        CompareFileUtils.showCompareWindow(project, file, new LightVirtualFile(callback.getFileName(), fileType, newText));
                        return;
                    case Messages.CANCEL:
                    default:
                        return;
                }
            }
        }
        // 执行代码格式化操作
        if (Boolean.TRUE.equals(callback.getReformat())) {
            FileUtils.getInstance().reformatFile(project, file);
        }
        // 提交文档改动，并非VCS中的提交文件
        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
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
        return FileUtils.getInstance().writeFileContent(project, file, callback.getFileName(), text);
    }
}
