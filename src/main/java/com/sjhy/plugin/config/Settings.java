package com.sjhy.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 全局设置
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/09 15:00
 */
@Data
@State(name = "EasyCodeSetting", storages = @Storage("easy-code-setting.xml"))
public class Settings implements PersistentStateComponent<Settings> {
    /**
     * 获取实例对象
     */
    public static Settings getInstance() {
        return ServiceManager.getService(Settings.class);
    }

    /**
     * 获取持久化状态信息
     *
     * @return 持久化状态信息
     */
    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    /**
     * 读取系统中保存的持久化状态信息
     *
     * @param state 读取到的持久化状态信息
     */
    @Override
    public void loadState(@NotNull Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    /**
     * 未读取到持久化状态信息前进行初始化操作
     */
    @Override
    public void noStateLoaded() {

    }
}
