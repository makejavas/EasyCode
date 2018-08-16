package com.sjhy.plugin.tool;

import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命名工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class NameUtils {
    private volatile static NameUtils nameUtils;

    /**
     * 单例模式
     */
    public static NameUtils getInstance() {
        if (nameUtils == null) {
            synchronized (NameUtils.class) {
                if (nameUtils == null) {
                    nameUtils = new NameUtils();
                }
            }
        }
        return nameUtils;
    }

    /**
     * 私有构造方法
     */
    NameUtils() {
    }

    /**
     * 转驼峰命名正则匹配规则
     */
    private final Pattern TO_HUMP_PATTERN = Pattern.compile("[-_]([a-z])");

    /**
     * 首字母大写方法
     *
     * @param name 名称
     * @return 结果
     */
    public String firstUpperCase(String name) {
        return StringUtils.capitalize(name);
    }

    /**
     * 首字母小写方法
     *
     * @param name 名称
     * @return 结果
     */
    public String firstLowerCase(String name) {
        return StringUtils.uncapitalize(name);
    }

    /**
     * 通过java全名获取类名
     *
     * @param fullName 全名
     * @return 类名
     */
    public String getClsNameByFullName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
    }

    /**
     * 下划线中横线命名转驼峰命名（属性名）
     *
     * @param name 名称
     * @return 结果
     */
    public String getJavaName(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        // 强转全小写
        name = name.toLowerCase();
        Matcher matcher = TO_HUMP_PATTERN.matcher(name.toLowerCase());
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 下划线中横线命名转驼峰命名（类名）
     *
     * @param name 名称
     * @return 结果
     */
    public String getClassName(String name) {
        return firstUpperCase(getJavaName(name));
    }

    /**
     * 任意对象合并工具类
     *
     * @param objects 任意对象
     * @return 合并后的字符串结果
     */
    public String append(Object... objects) {

        if (ArrayUtils.isEmpty(objects)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Object s : objects) {
            if (s != null) {
                builder.append(s);
            }
        }
        return builder.toString();
    }
}
