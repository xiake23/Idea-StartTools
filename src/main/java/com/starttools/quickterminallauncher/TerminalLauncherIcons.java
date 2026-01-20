package com.starttools.quickterminallauncher;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * 图标加载类
 * 提供插件使用的所有图标
 *
 * @author StartTools
 * @date 2026-01-18
 */
public interface TerminalLauncherIcons {
    /**
     * 工具栏图标 (13x13)
     */
    Icon TOOLBAR_ICON = IconLoader.getIcon("/icons/terminal-13x13.svg", TerminalLauncherIcons.class);

    /**
     * 标准图标 (16x16)
     */
    Icon STANDARD_ICON = IconLoader.getIcon("/icons/terminal-16x16.svg", TerminalLauncherIcons.class);
}
