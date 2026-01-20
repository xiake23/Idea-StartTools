package com.starttools.quickterminallauncher.settings;

/**
 * 终端启动器配置状态类
 * 用于持久化存储用户配置的命令
 *
 * @author StartTools
 * @date 2026-01-18
 */
public class TerminalLauncherState {
    /**
     * 要执行的命令,默认为启动 PowerShell
     */
    public String command = "Start-Process pwsh";

    /**
     * 获取要执行的命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 设置要执行的命令
     */
    public void setCommand(String command) {
        this.command = command;
    }
}
