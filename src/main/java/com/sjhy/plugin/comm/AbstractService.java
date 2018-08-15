package com.sjhy.plugin.comm;

import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.config.Settings;

/**
 * 抽象的服务
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public abstract class AbstractService {
    /**
     * 配置信息对象
     */
    protected Settings settings = Settings.getInstance();

    /**
     * 获取当前的类型映射Mapper
     *
     * @return 类型映射Mapper
     */
    protected TypeMapperGroup getCurrMapper() {
        return settings.getTypeMapperGroupMap().get(settings.getCurrTypeMapperGroupName());
    }

    /**
     * 设置当前的类型隐射Mapper
     *
     * @param typeMapper 类型映射Mapper
     */
    protected void setCurrMapper(TypeMapperGroup typeMapper) {
        settings.getTypeMapperGroupMap().put(settings.getCurrTypeMapperGroupName(), typeMapper);
    }
}
