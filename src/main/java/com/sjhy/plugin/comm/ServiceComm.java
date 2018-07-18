package com.sjhy.plugin.comm;

import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.tool.ConfigInfo;

public abstract class ServiceComm {
    protected ConfigInfo getConfigInfo() {
        return ConfigInfo.getInstance();
    }

    protected TypeMapperGroup getCurrMapper() {
        ConfigInfo configInfo = getConfigInfo();
        return configInfo.getTypeMapperGroupMap().get(configInfo.getCurrTemplateGroupName());
    }

    protected void setCurrMapper(TypeMapperGroup typeMapper) {
        ConfigInfo configInfo = getConfigInfo();
        configInfo.getTypeMapperGroupMap().put(configInfo.getCurrTypeMapperGroupName(), typeMapper);
    }
}
