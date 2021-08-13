package com.sjhy.plugin.tool;

import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.entity.GlobalConfigGroup;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.service.SettingsStorageService;

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
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrTemplateGroupName();
        return settingsStorage.getTemplateGroupMap().get(groupName);
    }


    /**
     * 获取当前全局配置组对象
     *
     * @return 全局配置组对象
     */
    public static GlobalConfigGroup getCurrGlobalConfigGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrGlobalConfigGroupName();
        return settingsStorage.getGlobalConfigGroupMap().get(groupName);
    }


    /**
     * 获取当前类型映射组对象
     *
     * @return 类型映射组对象
     */
    public static TypeMapperGroup getCurrTypeMapperGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrTypeMapperGroupName();
        return settingsStorage.getTypeMapperGroupMap().get(groupName);
    }

    /**
     * 获取当前列配置组对象
     *
     * @return 列配置组对象
     */
    public static ColumnConfigGroup getCurrColumnConfigGroup() {
        SettingsStorageDTO settingsStorage = SettingsStorageService.getSettingsStorage();
        String groupName = settingsStorage.getCurrColumnConfigGroupName();
        return settingsStorage.getColumnConfigGroupMap().get(groupName);
    }

}
