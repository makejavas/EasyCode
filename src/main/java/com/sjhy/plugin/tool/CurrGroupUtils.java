package com.sjhy.plugin.tool;

import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.entity.TemplateGroup;

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
}
