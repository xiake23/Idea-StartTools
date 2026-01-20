package com.starttools.quickterminallauncher.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.starttools.quickterminallauncher.TerminalLauncherIcons;
import com.starttools.quickterminallauncher.settings.TerminalLauncherSettings;
import com.starttools.quickterminallauncher.settings.TerminalLauncherState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 启动终端的 Action
 * 执行用户配置的命令来启动终端
 *
 * @author StartTools
 * @date 2026-01-18
 */
public class LaunchTerminalAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(LaunchTerminalAction.class);

    /**
     * 构造函数
     */
    public LaunchTerminalAction() {
        super();
    }

    /**
     * 执行动作:启动终端
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        // 获取用户配置的命令
        TerminalLauncherSettings settings = TerminalLauncherSettings.getInstance();
        TerminalLauncherState state = settings.getState();

        if (state == null) {
            showNotification(project, "配置加载失败", NotificationType.ERROR);
            return;
        }

        String command = state.getCommand();
        if (command == null || command.trim().isEmpty()) {
            showNotification(project, "请先在设置中配置要执行的命令", NotificationType.WARNING);
            return;
        }

        // 替换变量
        command = replaceVariables(command, project);

        // 执行命令
        try {
            executeCommand(command, project);
            showNotification(project, "终端启动成功", NotificationType.INFORMATION);
        } catch (IOException ex) {
            LOG.error("启动终端失败", ex);
            showNotification(project, "启动失败: " + ex.getMessage(), NotificationType.ERROR);
        }
    }

    /**
     * 替换命令中的变量
     *
     * @param command 原始命令
     * @param project 项目实例
     * @return 替换后的命令
     */
    private String replaceVariables(String command, Project project) {
        if (project == null) {
            return command;
        }

        String projectPath = project.getBasePath();
        String projectName = project.getName();

        if (projectPath != null) {
            command = command.replace("{PROJECT_DIR}", projectPath);
            command = command.replace("{PROJECT_PATH}", projectPath); // 别名
        }

        if (projectName != null) {
            command = command.replace("{PROJECT_NAME}", projectName);
        }

        return command;
    }

    /**
     * 执行 PowerShell 命令
     *
     * @param command 要执行的命令
     * @param project 项目实例
     * @throws IOException 执行失败时抛出异常
     */
    private void executeCommand(String command, Project project) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "powershell.exe",
            "-NoProfile",
            "-Command",
            command
        );

        // 设置工作目录为项目根目录
        if (project != null && project.getBasePath() != null) {
            processBuilder.directory(new java.io.File(project.getBasePath()));
        }

        processBuilder.start();
    }

    /**
     * 显示通知消息
     *
     * @param project 项目实例
     * @param content 通知内容
     * @param type 通知类型
     */
    private void showNotification(Project project, String content, NotificationType type) {
        Notification notification = new Notification(
            "QuickTerminalLauncher",
            "Quick Terminal Launcher",
            content,
            type
        );
        Notifications.Bus.notify(notification, project);
    }
}
