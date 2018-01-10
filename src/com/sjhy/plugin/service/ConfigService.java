package com.sjhy.plugin.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapperGroup;

import java.util.Map;

public interface ConfigService extends PersistentStateComponent<ConfigService> {
    //默认名称
    String DEFAULT_NAME = "Default";

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
}
