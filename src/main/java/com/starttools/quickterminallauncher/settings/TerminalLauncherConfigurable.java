package com.starttools.quickterminallauncher.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 终端启动器设置界面
 * 在 IDEA 设置-工具 中显示配置选项
 *
 * @author StartTools
 * @date 2026-01-18
 */
public class TerminalLauncherConfigurable implements Configurable {

    /**
     * 命令输入框
     */
    private JTextField commandTextField;

    /**
     * 设置面板
     */
    private JPanel mainPanel;

    /**
     * 获取设置页面显示名称
     */
    @NlsContexts.ConfigurableName
    @Override
    public String getDisplayName() {
        return "Quick Terminal Launcher";
    }

    /**
     * 创建设置界面组件
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("执行命令:"), gbc);

        // 输入框
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        commandTextField = new JTextField(40);
        formPanel.add(commandTextField, gbc);

        // 说明文本 - 默认行为
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        JLabel hintLabel = new JLabel("<html><i>默认: Start-Process pwsh (将在项目根目录启动)</i></html>");
        hintLabel.setForeground(Color.GRAY);
        formPanel.add(hintLabel, gbc);

        // 变量说明标题
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel variablesTitle = new JLabel("<html><b>可用变量:</b></html>");
        formPanel.add(variablesTitle, gbc);

        // 变量说明内容
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        JLabel variablesLabel = new JLabel("<html>" +
                "<i>{PROJECT_DIR}</i> - 项目根目录路径 (例如: E:\\Projects\\MyApp)<br>" +
                "<i>{PROJECT_PATH}</i> - 项目路径 (与 PROJECT_DIR 相同)<br>" +
                "<i>{PROJECT_NAME}</i> - 项目名称 (例如: MyApp)<br>" +
                "</html>");
        variablesLabel.setForeground(Color.GRAY);
        formPanel.add(variablesLabel, gbc);

        // 示例说明标题
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel examplesTitle = new JLabel("<html><b>示例命令:</b></html>");
        formPanel.add(examplesTitle, gbc);

        // 示例内容
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        JLabel examplesLabel = new JLabel("<html>" +
                "• <code>Start-Process pwsh</code> - 启动 PowerShell (默认在项目目录)<br>" +
                "• <code>Start-Process wt</code> - 启动 Windows Terminal<br>" +
                "• <code>Start-Process pwsh -ArgumentList '-NoExit', '-Command', 'echo {PROJECT_NAME}'</code> - 显示项目名称<br>" +
                "• <code>Start-Process cmd -ArgumentList '/K', 'cd /d {PROJECT_DIR}'</code> - 启动 CMD 并切换到项目目录<br>" +
                "</html>");
        examplesLabel.setForeground(Color.GRAY);
        formPanel.add(examplesLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        return mainPanel;
    }

    /**
     * 检查设置是否被修改
     */
    @Override
    public boolean isModified() {
        TerminalLauncherSettings settings = TerminalLauncherSettings.getInstance();
        TerminalLauncherState state = settings.getState();
        if (state == null) {
            return false;
        }
        return !commandTextField.getText().equals(state.getCommand());
    }

    /**
     * 应用设置更改
     */
    @Override
    public void apply() throws ConfigurationException {
        TerminalLauncherSettings settings = TerminalLauncherSettings.getInstance();
        TerminalLauncherState state = settings.getState();
        if (state != null) {
            state.setCommand(commandTextField.getText());
        }
    }

    /**
     * 重置设置到当前保存的值
     */
    @Override
    public void reset() {
        TerminalLauncherSettings settings = TerminalLauncherSettings.getInstance();
        TerminalLauncherState state = settings.getState();
        if (state != null) {
            commandTextField.setText(state.getCommand());
        }
    }
}
