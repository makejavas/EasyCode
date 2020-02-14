package com.sjhy.plugin.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
            return projectManager.getDefaultProject();//正常情况下不会发生
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
}
