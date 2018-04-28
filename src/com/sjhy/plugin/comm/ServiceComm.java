package com.sjhy.plugin.comm;

import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.service.impl.ConfigServiceImpl;

public abstract class ServiceComm {
    protected ConfigService getConfigService() {
        try {
            return ConfigService.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new ConfigServiceImpl();
        }
    }
    protected TypeMapperGroup getCurrMapper() {
        ConfigService configService = getConfigService();
        return configService.getTypeMapperGroupMap().get(configService.getCurrTemplateGroupName());
    }

    protected void setCurrMapper(TypeMapperGroup typeMapper) {
        ConfigService configService = getConfigService();
        configService.getTypeMapperGroupMap().put(configService.getCurrTypeMapperGroupName(), typeMapper);
    }
}
