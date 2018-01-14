package com.sjhy.plugin.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameUtils {
    //单例模式
    private static class Instance {
        private static final NameUtils ME = new NameUtils();
    }
    public static NameUtils getInstance() {
        return Instance.ME;
    }
    private NameUtils(){}
    
    /**
     * 首字母大写方法
     * @param name 名称
     * @return 结果
     */
    public String firstUpperCase(String name) {
        StringBuilder builder = new StringBuilder(name);
        builder.replace(0, 1, name.substring(0, 1).toUpperCase());
        return builder.toString();
    }

    /**
     * 首字母小写方法
     * @param name 名称
     * @return 结果
     */
    public String firstLowerCase(String name) {
        StringBuilder builder = new StringBuilder(name);
        builder.replace(0, 1, name.substring(0, 1).toLowerCase());
        return builder.toString();
    }

    /**
     * 通过java全名获取类名
     * @param fullName 全名
     * @return 类名
     */
    public String getClsNameByFullName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.')+1, fullName.length());
    }

    /**
     * 下划线中横线命名转驼峰命名
     * @param name 名称
     * @return 结果
     */
    public String getJavaName(String name) {
        Pattern pattern = Pattern.compile("[-_]([a-z])");
        Matcher matcher = pattern.matcher(name.toLowerCase());
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public String append(Object ...objects) {
        StringBuilder builder = new StringBuilder();
        for (Object s : objects) {
            if (s!=null) {
                builder.append(s);
            }
        }
        return builder.toString();
    }
}
