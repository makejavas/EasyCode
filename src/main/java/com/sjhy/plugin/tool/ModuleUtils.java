package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.List;

/**
 * 模块工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/01 17:15
 */
public final class ModuleUtils {
    /**
     * 禁用构造方法
     */
    private ModuleUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取模块的源代码文件夹，不存在
     *
     * @param module 模块对象
     * @return 文件夹路径
     */
    public static VirtualFile getSourcePath(@NotNull Module module) {
        List<VirtualFile> virtualFileList = ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.SOURCE);
        if (CollectionUtil.isEmpty(virtualFileList)) {
            return VirtualFileManager.getInstance().findFileByUrl(String.format("file://%s", ModuleUtil.getModuleDirPath(module)));
        }
        return virtualFileList.get(0);
    }
}
