package com.sjhy.plugin.dict;

/**
 * 全局字典
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 11:41
 */
public interface GlobalDict {
    /**
     * 提示信息
     */
    String TITLE_INFO = "EasyCode Title Info";
    /**
     * 版本号
     */
    String VERSION = "1.2.8";
    /**
     * 作者名称
     */
    String AUTHOR = "makejava";
    /**
     * 默认分组名称
     */
    String DEFAULT_GROUP_NAME = "Default";
    /**
     * 默认的Java类型列表
     */
    String[] DEFAULT_JAVA_TYPE_LIST = new String[]{
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.util.Boolean",
            "java.util.Date",
            "java.time.LocalDateTime",
            "java.time.LocalDate",
            "java.time.LocalTime",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Character",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.String[]",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
    };
}
