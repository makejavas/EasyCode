package com.sjhy.plugin.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.sjhy.plugin.entity.ColumnConfigGroup;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapperGroup;

import java.util.Map;

/**
 * 配置服务接口
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public interface ConfigService extends PersistentStateComponent<ConfigService> {
    /**
     * 默认名称
     */
    String DEFAULT_NAME = "Default";

    /**
     * 获取单例实例对象
     *
     * @return 实例对象
     */
    static ConfigService getInstance() {
        return ServiceManager.getService(ConfigService.class);
    }

    String getCurrTypeMapperGroupName();

    void setCurrTypeMapperGroupName(String currTypeMapperGroupName);

    Map<String, TypeMapperGroup> getTypeMapperGroupMap();

    String getCurrTemplateGroupName();

    void setCurrTemplateGroupName(String currTemplateGroupName);

    String getEncode();

    void setEncode(String encode);

    String getAuthor();

    void setAuthor(String author);

    void setTypeMapperGroupMap(Map<String, TypeMapperGroup> typeMapperGroupMap);

    Map<String, TemplateGroup> getTemplateGroupMap();

    void setTemplateGroupMap(Map<String, TemplateGroup> templateGroupMap);

    Map<String, ColumnConfigGroup> getColumnConfigGroupMap();

    void setColumnConfigGroupMap(Map<String, ColumnConfigGroup> columnConfigGroupMap);

    String getCurrColumnConfigGroupName();

    void setCurrColumnConfigGroupName(String currColumnConfigGroupName);
}
