package com.sjhy.plugin.tool;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import com.sjhy.plugin.entity.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局配置信息
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
@Data
@State(name = "EasyCodeSetting", storages = @Storage("easy-code-setting.xml"))
public class ConfigInfo implements PersistentStateComponent<ConfigInfo> {
    /**
     * 默认名称
     */
    @Transient
    public static final String DEFAULT_NAME = "Default";

    /**
     * 版本号
     */
    private String version;
    /**
     * 当前类型映射组名
     */
    private String currTypeMapperGroupName;
    /**
     * 类型映射组
     */
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    /**
     * 当前模板组名
     */
    private String currTemplateGroupName;
    /**
     * 模板组
     */
    private Map<String, TemplateGroup> templateGroupMap;
    /**
     * 当前配置表组名
     */
    private String currColumnConfigGroupName;
    /**
     * 配置表组
     */
    private Map<String, ColumnConfigGroup> columnConfigGroupMap;
    /**
     * 当前全局配置组名
     */
    private String currGlobalConfigGroupName;
    /**
     * 全局配置组
     */
    private Map<String, GlobalConfigGroup> globalConfigGroupMap;
    /**
     * 默认编码
     */
    private String encode;
    /**
     * 作者
     */
    private String author;

    /**
     * 获取单例实例对象
     *
     * @return 实例对象
     */
    public static ConfigInfo getInstance() {
        return ServiceManager.getService(ConfigInfo.class);
    }

    /**
     * 默认构造方法
     */
    @SuppressWarnings("unused")
    public ConfigInfo() {
        initDefault();
    }

    /**
     * 初始化默认设置
     */
    public void initDefault() {
        // 版本号
        this.version = "1.1.0";
        // 默认编码
        this.encode = "UTF-8";
        // 作者名称
        this.author = "makejava";
        // 当前各项分组名称
        this.currTemplateGroupName = DEFAULT_NAME;
        this.currTypeMapperGroupName = DEFAULT_NAME;
        this.currColumnConfigGroupName = DEFAULT_NAME;
        this.currGlobalConfigGroupName = DEFAULT_NAME;
        //配置默认模板
        if (this.templateGroupMap == null) {
            this.templateGroupMap = new LinkedHashMap<>();
        }
        this.templateGroupMap.put(DEFAULT_NAME, loadTemplateGroup(DEFAULT_NAME, "entity", "dao", "service", "serviceImpl", "controller"));
        this.templateGroupMap.put("MybatisPlus", loadTemplateGroup(DEFAULT_NAME, "entity", "dao", "service", "serviceImpl", "controller"));

        //配置默认类型映射
        if (this.typeMapperGroupMap == null) {
            this.typeMapperGroupMap = new LinkedHashMap<>();
        }
        TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
        List<TypeMapper> typeMapperList = new ArrayList<>();
        typeMapperList.add(new TypeMapper("varchar(\\(\\d+\\))?", "java.lang.String"));
        typeMapperList.add(new TypeMapper("text", "java.lang.String"));
        typeMapperList.add(new TypeMapper("decimal(\\(\\d+\\))?", "java.lang.Double"));
        typeMapperList.add(new TypeMapper("integer", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int(\\(\\d+\\))?", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int4", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int8", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("bigint(\\(\\d+\\))?", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("datetime", "java.util.Date"));
        typeMapperList.add(new TypeMapper("timestamp", "java.util.Date"));
        typeMapperList.add(new TypeMapper("boolean", "java.lang.Boolean"));
        typeMapperGroup.setName(DEFAULT_NAME);
        typeMapperGroup.setElementList(typeMapperList);
        typeMapperGroupMap.put(DEFAULT_NAME, typeMapperGroup);

        //初始化表配置
        if (this.columnConfigGroupMap == null) {
            this.columnConfigGroupMap = new LinkedHashMap<>();
        }
        ColumnConfigGroup columnConfigGroup = new ColumnConfigGroup();
        List<ColumnConfig> columnConfigList = new ArrayList<>();
        columnConfigList.add(new ColumnConfig("disable", ColumnConfigType.BOOLEAN));
        columnConfigGroup.setName(DEFAULT_NAME);
        columnConfigGroup.setElementList(columnConfigList);
        columnConfigGroupMap.put(DEFAULT_NAME, columnConfigGroup);

        //初始化全局配置
        if (this.globalConfigGroupMap == null) {
            this.globalConfigGroupMap = new LinkedHashMap<>();
        }
        this.globalConfigGroupMap.put(DEFAULT_NAME, loadGlobalConfigGroup(DEFAULT_NAME, "init", "define", "autoImport"));
    }

    /**
     * 加载模板文件
     *
     * @param filePath 模板路径
     * @return 模板文件内容
     */
    private static String loadTemplate(String filePath) {
        return FileUtils.getInstance().read(ConfigInfo.class.getResourceAsStream(filePath)).replaceAll("\r", "");
    }

    /**
     * 加载模板组
     *
     * @param groupName     组名
     * @param templateNames 模板名称
     * @return 模板组
     */
    private static TemplateGroup loadTemplateGroup(String groupName, String... templateNames) {
        TemplateGroup templateGroup = new TemplateGroup();
        templateGroup.setName(groupName);
        templateGroup.setElementList(new ArrayList<>());
        for (String templateName : templateNames) {
            String path = "/template/" + groupName + "/" + templateName + ".vm";
            templateGroup.getElementList().add(new Template(templateName, loadTemplate(path)));
        }
        return templateGroup;
    }

    /**
     * 加载全局配置组
     *
     * @param groupName     组名
     * @param templateNames 模板名称
     * @return 模板组
     */
    private static GlobalConfigGroup loadGlobalConfigGroup(String groupName, String... templateNames) {
        GlobalConfigGroup globalConfigGroup = new GlobalConfigGroup();
        globalConfigGroup.setName(groupName);
        globalConfigGroup.setElementList(new ArrayList<>());
        for (String templateName : templateNames) {
            String path = "/globalConfig/" + groupName + "/" + templateName + ".vm";
            globalConfigGroup.getElementList().add(new GlobalConfig(templateName, loadTemplate(path)));
        }
        return globalConfigGroup;
    }

    @Nullable
    @Override
    public ConfigInfo getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigInfo configInfo) {
        // 备份初始配置
        Map<String, TemplateGroup> templateGroupMap = this.getTemplateGroupMap();
        String version = this.getVersion();
        // 覆盖初始配置
        XmlSerializerUtil.copyBean(configInfo, this);

        // 已经合并不再重复合并
        if (configInfo.getVersion() != null && configInfo.getVersion().equals(version)) {
            return;
        }

        // 合并配置
        templateGroupMap.forEach((name, templateGroup) -> {
            if (this.getTemplateGroupMap().containsKey(name)) {
                return;
            }
            this.getTemplateGroupMap().put(name, templateGroup);
        });
    }
}
