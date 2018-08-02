package com.sjhy.plugin.tool;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
     * @param file 文件对象
     * @return 文件内容
     */
    public String read(File file) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(FileUtil.loadFileText(file, "UTF-8"));
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        }
        return builder.toString();
    }

    /**
     * 读取输入流内容
     * @param in 输入流
     * @return 文件内容
     */
    public String read(InputStream in) {
        try {
            byte[] temp = FileUtil.loadBytes(in);
            return new String(temp, "UTF-8");
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ExceptionUtil.rethrow(e);
                }
            }
        }
        return null;
    }

    /**
     * 写入文件内容
     * @param file 文件
     * @param content 内容
     */
    public void write(File file, String content) {
        write(file, content, false);
    }

    /**
     * 写入文件内容
     * @param file 文件
     * @param content 文件内容
     * @param append 是否为追加模式
     */
    public void write(File file, String content, boolean append) {
        try {
            FileUtil.writeToFile(file, content, append);
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        }
    }
}
