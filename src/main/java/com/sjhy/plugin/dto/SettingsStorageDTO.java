package com.sjhy.plugin.dto;

import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.*;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 设置储存传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 11:35
 */
@Data
public class SettingsStorageDTO {
    /**
     * 返回默认值，不使用静态常量，防止默认值别篡改
     *
     * @return 储存对象
     */
    public static SettingsStorageDTO defaultVal() {
        SettingsStorageDTO storage = new SettingsStorageDTO();
        storage.author = GlobalDict.AUTHOR;
        storage.version = GlobalDict.VERSION;
        storage.userSecure = "";
        // 默认分组名称
        storage.currTypeMapperGroupName = GlobalDict.DEFAULT_GROUP_NAME;
        storage.currTemplateGroupName = GlobalDict.DEFAULT_GROUP_NAME;
        storage.currColumnConfigGroupName = GlobalDict.DEFAULT_GROUP_NAME;
        storage.currGlobalConfigGroupName = GlobalDict.DEFAULT_GROUP_NAME;
        // 默认配置信息
        storage.typeMapperGroupMap = new HashMap<>(16);
        TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
        typeMapperGroup.setName(GlobalDict.DEFAULT_GROUP_NAME);
        typeMapperGroup.setElementList(Arrays.asList(new TypeMapper("varchar", "java.lang.String"), new TypeMapper("varchar\\(\\)", "java.lang.String")));
        storage.typeMapperGroupMap.put(GlobalDict.DEFAULT_GROUP_NAME, typeMapperGroup);
        return storage;
    }

    /**
     * 重置为默认值
     */
    public void resetDefaultVal() {
        SettingsStorageDTO defaultVal = defaultVal();
        this.setAuthor(defaultVal.getAuthor());
        this.setVersion(defaultVal.getVersion());
        this.setCurrColumnConfigGroupName(defaultVal.getCurrColumnConfigGroupName());
        this.setCurrTemplateGroupName(defaultVal.getCurrTemplateGroupName());
        this.setCurrGlobalConfigGroupName(defaultVal.getCurrGlobalConfigGroupName());
        this.setCurrTypeMapperGroupName(defaultVal.getCurrTypeMapperGroupName());
        this.setGlobalConfigGroupMap(defaultVal.getGlobalConfigGroupMap());
        this.setColumnConfigGroupMap(defaultVal.getColumnConfigGroupMap());
        this.setTypeMapperGroupMap(defaultVal.getTypeMapperGroupMap());
        this.setTemplateGroupMap(defaultVal.getTemplateGroupMap());
    }

    /**
     * 作者
     */
    private String author;
    /**
     * 版本号
     */
    private String version;
    /**
     * 用户密钥
     */
    private String userSecure;
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
}
