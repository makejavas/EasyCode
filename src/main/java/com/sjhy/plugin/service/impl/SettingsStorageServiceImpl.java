package com.sjhy.plugin.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sjhy.plugin.dto.SettingsStorageDTO;
import com.sjhy.plugin.service.SettingsStorageService;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 设置储存服务实现
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/07 11:32
 */
@State(name = "EasyCodeSetting", storages = @Storage("easy-code-setting.xml"))
public class SettingsStorageServiceImpl implements SettingsStorageService {

    private SettingsStorageDTO settingsStorage = SettingsStorageDTO.defaultVal();

    /**
     * 获取配置
     *
     * @return 配置对象
     */
    @Nullable
    @Override
    public SettingsStorageDTO getState() {
        return settingsStorage;
    }

    /**
     * 加载配置
     *
     * @param state 配置对象
     */
    @Override
    public void loadState(@NotNull SettingsStorageDTO state) {
        // 加载配置后填充默认值，避免版本升级导致的配置信息不完善问题
        state.fillDefaultVal();
        this.settingsStorage = state;
    }
}
