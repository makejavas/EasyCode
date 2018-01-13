package com.sjhy.plugin.tool;

import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    //单例模式
    private static class Instance {
        private static final FileUtils ME = new FileUtils();
    }
    public static FileUtils getInstance() {
        return Instance.ME;
    }
    private FileUtils(){}

    public String read(File file) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(FileUtil.loadFileText(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public String read(InputStream in) {
        try {
            byte[] temp = FileUtil.loadBytes(in);
            return new String(temp, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void write(File file, String content) {
        write(file, content, false);
    }
    
    public void write(File file, String content, boolean append) {
        try {
            FileUtil.writeToFile(file, content, append);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
