package com.starttools.quickterminallauncher.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 终端启动器设置服务
 * 负责持久化和管理用户配置
 *
 * @author StartTools
 * @date 2026-01-18
 */
@Service
@State(
    name = "TerminalLauncherSettings",
    storages = @Storage("TerminalLauncherSettings.xml")
)
public final class TerminalLauncherSettings implements PersistentStateComponent<TerminalLauncherState> {

    /**
     * 配置状态
     */
    private TerminalLauncherState state = new TerminalLauncherState();

    /**
     * 获取配置服务实例
     */
    public static TerminalLauncherSettings getInstance() {
        return ApplicationManager.getApplication().getService(TerminalLauncherSettings.class);
    }

    /**
     * 获取配置状态
     */
    @Nullable
    @Override
    public TerminalLauncherState getState() {
        return state;
    }

    /**
     * 加载配置状态
     */
    @Override
    public void loadState(@NotNull TerminalLauncherState state) {
        this.state = state;
    }
}
