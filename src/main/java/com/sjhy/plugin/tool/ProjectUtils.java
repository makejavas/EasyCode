package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

/**
 * IDEA项目相关工具
 *
 * @author tangcent
 * @version 1.0.0
 * @since 2020/02/14 18:35
 */
public class ProjectUtils {

    /**
     * 获取当前项目对象
     *
     * @return 当前项目对象
     */
    public static Project getCurrProject() {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();
        if (openProjects.length == 0) {
            // 在未打开任何项目，进入到设置页面时会出现
            return projectManager.getDefaultProject();
        } else if (openProjects.length == 1) {
            // 只存在一个打开的项目则使用打开的项目
            return openProjects[0];
        }

        //如果有项目窗口处于激活状态
        try {
            WindowManager wm = WindowManager.getInstance();
            for (Project project : openProjects) {
                Window window = wm.suggestParentWindow(project);
                if (window != null && window.isActive()) {
                    return project;
                }
            }
        } catch (Exception ignored) {
        }

        //否则使用默认项目
        return projectManager.getDefaultProject();
    }

    /**
     * 进行旧版本兼容，该方法已经存在 @see {@link com.intellij.openapi.project.ProjectUtil#guessProjectDir(Project)}
     *
     * @param project 项目对象
     * @return 基本目录
     */
    public static VirtualFile getBaseDir(Project project) {
        if (project.isDefault()) {
            return null;
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Module module = null;
        if (modules.length == 1) {
            module = modules[0];
        } else {
            for (Module item : modules) {
                if (item.getName().equals(project.getName())) {
                    module = item;
                    break;
                }
            }
        }
        if (module != null) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            for (VirtualFile contentRoot : moduleRootManager.getContentRoots()) {
                if (contentRoot.isDirectory() && contentRoot.getName().equals(module.getName())) {
                    return contentRoot;
                }
            }
        }
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new NullPointerException();
        }
        return LocalFileSystem.getInstance().findFileByPath(basePath);
    }
}
