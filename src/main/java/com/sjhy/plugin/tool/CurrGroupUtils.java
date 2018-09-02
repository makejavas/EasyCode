package com.sjhy.plugin.tool;

import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapperGroup;

/**
 * 当前分组配置获取工具
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/01 16:51
 */
public final class CurrGroupUtils {
    /**
     * 禁用构造方法
     */
    private CurrGroupUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取当前模板组对象
     *
     * @return 模板组对象
     */
    public static TemplateGroup getCurrTemplateGroup() {
        Settings settings = Settings.getInstance();
        String groupName = settings.getCurrTemplateGroupName();
        return settings.getTemplateGroupMap().get(groupName);
    }

    /**
     * 覆盖或添加模板组
     *
     * @param groupName     组名
     * @param templateGroup 模板组对象
     */
    public static void setCurrTemplateGroup(String groupName, TemplateGroup templateGroup) {
        templateGroup.setName(groupName);
        Settings.getInstance().getTemplateGroupMap().put(groupName, templateGroup);
    }

    /**
     * 获取当前全局配置组对象
     *
     * @return 全局配置组对象
     */
    public static GlobalConfigGroup getCurrGlobalConfigGroup() {
        Settings settings = Settings.getInstance();
        String groupName = settings.getCurrGlobalConfigGroupName();
        return settings.getGlobalConfigGroupMap().get(groupName);
    }

    /**
     * 覆盖或添加全局配置组
     *
     * @param groupName         组名
     * @param globalConfigGroup 全局配置组对象
     */
    public static void setGlobalConfigGroup(String groupName, GlobalConfigGroup globalConfigGroup) {
        globalConfigGroup.setName(groupName);
        Settings.getInstance().getGlobalConfigGroupMap().put(groupName, globalConfigGroup);
    }

    /**
     * 获取当前类型映射组对象
     *
     * @return 类型映射组对象
     */
    public static TypeMapperGroup getCurrTypeMapperGroup() {
        Settings settings = Settings.getInstance();
        String groupName = settings.getCurrTypeMapperGroupName();
        return settings.getTypeMapperGroupMap().get(groupName);
    }

    /**
     * 覆盖或添加类型映射组
     *
     * @param groupName         组名
     * @param typeMapperGroup 类型映射组对象
     */
    public static void setTypeMapperGroup(String groupName, TypeMapperGroup typeMapperGroup) {
        typeMapperGroup.setName(groupName);
        Settings.getInstance().getTypeMapperGroupMap().put(groupName, typeMapperGroup);
    }

    /**
     * 获取当前列配置组对象
     *
     * @return 列配置组对象
     */
    public static ColumnConfigGroup getCurrColumnConfigGroup() {
        Settings settings = Settings.getInstance();
        String groupName = settings.getCurrColumnConfigGroupName();
        return settings.getColumnConfigGroupMap().get(groupName);
    }

    /**
     * 覆盖或添加列配置组
     *
     * @param groupName         组名
     * @param tolumnConfigGroup 列配置组对象
     */
    public static void setColumnConfigGroup(String groupName, ColumnConfigGroup tolumnConfigGroup) {
        tolumnConfigGroup.setName(groupName);
        Settings.getInstance().getColumnConfigGroupMap().put(groupName, tolumnConfigGroup);
    }
}
