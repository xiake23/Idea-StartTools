# Quick Terminal Launcher Plugin 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 开发一个 IntelliJ IDEA 插件,在工具栏添加快速启动终端按钮,可执行用户自定义命令

**Architecture:** 基于 IntelliJ Platform SDK 开发插件,使用 Gradle 构建系统。通过 Action 实现工具栏按钮,通过 PersistentStateComponent 实现配置持久化,通过 ProcessBuilder 执行 PowerShell 命令。

**Tech Stack:** Java 17, IntelliJ Platform SDK 2023.2+, Gradle 8.x, PowerShell

---

## Task 1: 创建项目基础结构

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `src/main/resources/META-INF/plugin.xml`

**Step 1: 创建 settings.gradle.kts**

```kotlin
rootProject.name = "quick-terminal-launcher"
```

**Step 2: 创建 gradle.properties**

```properties
# IntelliJ Platform 插件配置
pluginGroup = com.starttools
pluginName = QuickTerminalLauncher
pluginVersion = 1.0.0
pluginSinceBuild = 232
pluginUntilBuild = 241.*

# IntelliJ Platform 版本
platformType = IC
platformVersion = 2023.2.5

# Java 版本
javaVersion = 17

# Gradle 配置
org.gradle.jvmargs = -Xmx2048m
```

**Step 3: 创建 build.gradle.kts**

```kotlin
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set(providers.gradleProperty("platformVersion"))
    type.set(providers.gradleProperty("platformType"))
    updateSinceUntilBuild.set(true)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(providers.gradleProperty("pluginSinceBuild"))
        untilBuild.set(providers.gradleProperty("pluginUntilBuild"))
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
```

**Step 4: 验证 Gradle 构建文件**

Run: `./gradlew tasks` (Windows 使用 `gradlew.bat tasks`)
Expected: 成功列出所有可用任务,包括 IntelliJ Platform 相关任务

**Step 5: 创建基础 plugin.xml**

```xml
<idea-plugin>
    <id>com.starttools.quickterminallauncher</id>
    <name>Quick Terminal Launcher</name>
    <vendor email="support@starttools.com" url="https://github.com/yourusername/quick-terminal-launcher">Start Tools</vendor>

    <description><![CDATA[
    一个快速启动终端并执行命令的工具。<br>
    <br>
    <strong>功能特性:</strong><br>
    <ul>
        <li>在工具栏添加快速启动按钮</li>
        <li>可自定义执行的命令</li>
        <li>默认启动 PowerShell</li>
    </ul>
    ]]></description>

    <depends>com.intellij.modules.platform</depends>

    <extensions defaultExtensionNs="com.intellij">
        <!-- 配置扩展点将在后续添加 -->
    </extensions>

    <actions>
        <!-- 动作将在后续添加 -->
    </actions>
</idea-plugin>
```

**Step 6: 验证插件配置**

Run: `gradlew buildPlugin`
Expected: 成功构建插件,在 `build/distributions/` 生成 zip 文件

**Step 7: 提交基础项目结构**

```bash
git add .
git commit -m "feat: 初始化 IDEA 插件项目结构

- 添加 Gradle 构建配置
- 添加基础 plugin.xml
- 配置 IntelliJ Platform SDK"
```

---

## Task 2: 实现配置持久化服务

**Files:**
- Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherSettings.java`
- Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherState.java`
- Modify: `src/main/resources/META-INF/plugin.xml`

**Step 1: 创建配置状态类**

Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherState.java`

```java
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
```

**Step 2: 创建配置服务类**

Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherSettings.java`

```java
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
```

**Step 3: 在 plugin.xml 注册服务**

Modify: `src/main/resources/META-INF/plugin.xml` (在 `<extensions>` 标签内添加)

```xml
<extensions defaultExtensionNs="com.intellij">
    <!-- 应用级服务:配置持久化 -->
    <applicationService
        serviceImplementation="com.starttools.quickterminallauncher.settings.TerminalLauncherSettings"/>
</extensions>
```

**Step 4: 验证配置服务**

Run: `gradlew buildPlugin`
Expected: 成功构建,无编译错误

**Step 5: 提交配置持久化功能**

```bash
git add .
git commit -m "feat: 实现配置持久化服务

- 添加 TerminalLauncherState 状态类
- 添加 TerminalLauncherSettings 服务类
- 在 plugin.xml 注册应用级服务
- 默认命令设置为 Start-Process pwsh"
```

---

## Task 3: 实现设置界面

**Files:**
- Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherConfigurable.java`
- Modify: `src/main/resources/META-INF/plugin.xml`

**Step 1: 创建设置界面类**

Create: `src/main/java/com/starttools/quickterminallauncher/settings/TerminalLauncherConfigurable.java`

```java
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

        // 说明文本
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        JLabel hintLabel = new JLabel("<html><i>提示: 此命令将在点击工具栏按钮时执行。默认: Start-Process pwsh</i></html>");
        hintLabel.setForeground(Color.GRAY);
        formPanel.add(hintLabel, gbc);

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
```

**Step 2: 在 plugin.xml 注册设置页面**

Modify: `src/main/resources/META-INF/plugin.xml` (在 `<extensions>` 标签内添加)

```xml
<!-- 设置页面:在设置-工具中显示 -->
<applicationConfigurable
    parentId="tools"
    instance="com.starttools.quickterminallauncher.settings.TerminalLauncherConfigurable"
    id="com.starttools.quickterminallauncher.settings.TerminalLauncherConfigurable"
    displayName="Quick Terminal Launcher"/>
```

**Step 3: 验证设置界面**

Run: `gradlew runIde`
Expected:
1. IDEA 启动成功
2. 打开 File > Settings > Tools
3. 看到 "Quick Terminal Launcher" 选项
4. 界面显示命令输入框和提示文本

**Step 4: 测试设置保存**

手动测试:
1. 在设置界面修改命令为 "test command"
2. 点击 Apply
3. 关闭设置
4. 重新打开设置
Expected: 命令仍然是 "test command"

**Step 5: 提交设置界面功能**

```bash
git add .
git commit -m "feat: 实现设置界面

- 添加 TerminalLauncherConfigurable 设置页面
- 在 Settings > Tools 中注册设置页面
- 实现命令输入框和提示文本
- 支持设置的保存和重置"
```

---

## Task 4: 添加工具栏图标资源

**Files:**
- Create: `src/main/resources/icons/terminal-13x13.svg`
- Create: `src/main/resources/icons/terminal-16x16.svg`
- Create: `src/main/java/com/starttools/quickterminallauncher/TerminalLauncherIcons.java`

**Step 1: 创建 13x13 图标 (适用于工具栏)**

Create: `src/main/resources/icons/terminal-13x13.svg`

```xml
<svg width="13" height="13" viewBox="0 0 13 13" xmlns="http://www.w3.org/2000/svg">
  <rect x="1" y="2" width="11" height="9" rx="1" fill="none" stroke="currentColor" stroke-width="1"/>
  <path d="M 3 5 L 5 7 L 3 9" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"/>
  <line x1="6" y1="9" x2="9" y2="9" stroke="currentColor" stroke-width="1" stroke-linecap="round"/>
</svg>
```

**Step 2: 创建 16x16 图标 (适用于设置等其他地方)**

Create: `src/main/resources/icons/terminal-16x16.svg`

```xml
<svg width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
  <rect x="1" y="2" width="14" height="12" rx="1.5" fill="none" stroke="currentColor" stroke-width="1.5"/>
  <path d="M 3.5 6 L 6 8.5 L 3.5 11" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
  <line x1="7.5" y1="11" x2="11" y2="11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
</svg>
```

**Step 3: 创建图标加载类**

Create: `src/main/java/com/starttools/quickterminallauncher/TerminalLauncherIcons.java`

```java
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
```

**Step 4: 验证图标加载**

Run: `gradlew buildPlugin`
Expected: 成功构建,无编译错误,图标文件被打包到插件中

**Step 5: 提交图标资源**

```bash
git add .
git commit -m "feat: 添加终端启动图标

- 添加 13x13 工具栏图标
- 添加 16x16 标准图标
- 创建 TerminalLauncherIcons 图标加载类"
```

---

## Task 5: 实现工具栏启动按钮

**Files:**
- Create: `src/main/java/com/starttools/quickterminallauncher/actions/LaunchTerminalAction.java`
- Modify: `src/main/resources/META-INF/plugin.xml`

**Step 1: 创建启动终端的 Action 类**

Create: `src/main/java/com/starttools/quickterminallauncher/actions/LaunchTerminalAction.java`

```java
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
        super("Launch Terminal", "快速启动终端", TerminalLauncherIcons.TOOLBAR_ICON);
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

        // 执行命令
        try {
            executeCommand(command);
            showNotification(project, "终端启动成功", NotificationType.INFORMATION);
        } catch (IOException ex) {
            LOG.error("启动终端失败", ex);
            showNotification(project, "启动失败: " + ex.getMessage(), NotificationType.ERROR);
        }
    }

    /**
     * 执行 PowerShell 命令
     *
     * @param command 要执行的命令
     * @throws IOException 执行失败时抛出异常
     */
    private void executeCommand(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "powershell.exe",
            "-NoProfile",
            "-Command",
            command
        );
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
```

**Step 2: 在 plugin.xml 注册 Action**

Modify: `src/main/resources/META-INF/plugin.xml` (替换 `<actions>` 标签内容)

```xml
<actions>
    <!-- 在主工具栏 Run 按钮旁边添加启动终端按钮 -->
    <action id="QuickTerminalLauncher.LaunchTerminal"
            class="com.starttools.quickterminallauncher.actions.LaunchTerminalAction"
            text="Launch Terminal"
            description="快速启动终端并执行配置的命令"
            icon="TerminalLauncherIcons.TOOLBAR_ICON">
        <add-to-group group-id="ToolbarRunGroup" anchor="after" relative-to-action="RunConfiguration"/>
    </action>
</actions>
```

**Step 3: 验证 Action 注册**

Run: `gradlew buildPlugin`
Expected: 成功构建,无编译错误

**Step 4: 测试工具栏按钮**

Run: `gradlew runIde`
Expected:
1. IDEA 启动成功
2. 在主工具栏右上角 Run 按钮旁边看到终端图标按钮
3. 鼠标悬停显示 "快速启动终端并执行配置的命令"

**Step 5: 测试命令执行**

手动测试:
1. 点击工具栏的终端按钮
2. Expected: PowerShell 窗口打开
3. 看到通知 "终端启动成功"

**Step 6: 测试自定义命令**

手动测试:
1. 打开 Settings > Tools > Quick Terminal Launcher
2. 修改命令为 "Write-Host 'Hello from Quick Terminal Launcher'; Start-Process pwsh"
3. 点击 Apply
4. 点击工具栏按钮
Expected: PowerShell 窗口打开,并显示 "Hello from Quick Terminal Launcher"

**Step 7: 提交工具栏按钮功能**

```bash
git add .
git commit -m "feat: 实现工具栏启动按钮

- 添加 LaunchTerminalAction 动作类
- 实现 PowerShell 命令执行逻辑
- 在主工具栏 Run 按钮旁边添加按钮
- 添加成功/失败通知提示"
```

---

## Task 6: 添加通知组配置

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml`

**Step 1: 注册通知组**

Modify: `src/main/resources/META-INF/plugin.xml` (在 `<extensions>` 标签内添加)

```xml
<!-- 通知组:用于显示插件消息 -->
<notificationGroup id="QuickTerminalLauncher"
                   displayType="BALLOON"
                   key="Quick Terminal Launcher"/>
```

**Step 2: 验证通知组配置**

Run: `gradlew buildPlugin`
Expected: 成功构建,无编译错误

**Step 3: 测试通知显示**

Run: `gradlew runIde`
手动测试:
1. 点击工具栏终端按钮
2. Expected: 右下角显示气泡通知 "终端启动成功"

**Step 4: 提交通知组配置**

```bash
git add .
git commit -m "feat: 添加通知组配置

- 注册 QuickTerminalLauncher 通知组
- 配置为气泡通知显示类型"
```

---

## Task 7: 添加项目文档

**Files:**
- Create: `README.md`
- Create: `.gitignore`

**Step 1: 创建 README.md**

Create: `README.md`

```markdown
# Quick Terminal Launcher

一个 IntelliJ IDEA 插件,用于快速启动终端并执行自定义命令。

## 功能特性

- ⚡ 在工具栏添加快速启动按钮
- 🔧 可自定义执行的命令
- 💻 默认启动 PowerShell
- ⚙️ 简单的设置界面

## 安装

### 从源码构建

1. 克隆仓库:
```bash
git clone https://github.com/yourusername/quick-terminal-launcher.git
cd quick-terminal-launcher
```

2. 构建插件:
```bash
gradlew buildPlugin
```

3. 安装插件:
   - 打开 IntelliJ IDEA
   - File > Settings > Plugins
   - 点击齿轮图标 > Install Plugin from Disk
   - 选择 `build/distributions/quick-terminal-launcher-1.0.0.zip`

### 从 JetBrains Marketplace 安装

(待发布后更新此部分)

## 使用方法

### 基本使用

1. 在主工具栏右上角找到终端图标按钮(在 Run 按钮旁边)
2. 点击按钮即可启动终端

### 自定义命令

1. 打开 File > Settings > Tools > Quick Terminal Launcher
2. 在"执行命令"输入框中输入您想要执行的命令
3. 点击 Apply 保存设置
4. 点击工具栏按钮执行您的自定义命令

### 示例命令

- 启动 PowerShell: `Start-Process pwsh`
- 启动 Windows Terminal: `Start-Process wt`
- 启动 PowerShell 并执行脚本: `Start-Process pwsh -ArgumentList '-NoExit', '-Command', 'cd C:\Projects'`
- 启动 CMD: `Start-Process cmd`

## 开发

### 环境要求

- JDK 17+
- IntelliJ IDEA 2023.2+
- Gradle 8.x

### 构建命令

```bash
# 构建插件
gradlew buildPlugin

# 运行 IDEA 实例测试插件
gradlew runIde

# 验证插件
gradlew verifyPlugin
```

## 技术栈

- Java 17
- IntelliJ Platform SDK 2023.2+
- Gradle 8.x

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request!

## 作者

Start Tools

## 更新日志

### 1.0.0 (2026-01-18)

- ✨ 初始版本发布
- ⚡ 工具栏快速启动按钮
- ⚙️ 设置界面
- 💾 配置持久化
```

**Step 2: 创建 .gitignore**

Create: `.gitignore`

```gitignore
# Gradle
.gradle/
build/
!gradle-wrapper.jar

# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr
out/

# Java
*.class
*.jar
*.war
*.ear
hs_err_pid*

# macOS
.DS_Store

# Windows
Thumbs.db
desktop.ini

# Log files
*.log

# Temporary files
*.tmp
*.bak
*.swp
*~
```

**Step 3: 验证文档**

检查文档内容是否完整和准确

**Step 4: 提交项目文档**

```bash
git add .
git commit -m "docs: 添加项目文档

- 添加 README.md 使用说明
- 添加 .gitignore 忽略配置
- 包含安装、使用、开发指南"
```

---

## Task 8: 最终验证和测试

**Step 1: 清理构建**

Run: `gradlew clean`
Expected: 成功清理 build 目录

**Step 2: 完整构建**

Run: `gradlew buildPlugin`
Expected: 成功构建插件,生成 zip 文件

**Step 3: 验证插件**

Run: `gradlew verifyPlugin`
Expected: 通过所有验证检查

**Step 4: 运行插件测试**

Run: `gradlew runIde`

手动测试检查清单:
- [ ] IDEA 成功启动
- [ ] 工具栏显示终端图标按钮
- [ ] 按钮位置在 Run 按钮旁边
- [ ] 鼠标悬停显示正确的提示文本
- [ ] 点击按钮成功启动 PowerShell
- [ ] 显示成功通知
- [ ] Settings > Tools 中显示 Quick Terminal Launcher 设置页面
- [ ] 设置界面显示命令输入框
- [ ] 修改命令后点击 Apply 能保存
- [ ] 重新打开设置,命令保持修改后的值
- [ ] 使用自定义命令能正确执行

**Step 5: 测试错误处理**

手动测试:
1. 在设置中输入无效命令 "invalid-command-xyz"
2. 点击工具栏按钮
Expected: 显示错误通知

**Step 6: 查看插件包内容**

Run: `unzip -l build/distributions/quick-terminal-launcher-1.0.0.zip`
Expected: 包含所有必要文件(classes, icons, plugin.xml)

**Step 7: 最终提交**

```bash
git add .
git commit -m "chore: 完成插件开发并通过验证

- 所有功能测试通过
- 插件构建成功
- 准备发布 1.0.0 版本"
```

---

## 完成条件

插件开发完成并满足以下条件:

✅ **功能完整性**
- 工具栏显示启动按钮
- 按钮位置在 Run 按钮旁边
- 点击按钮执行配置的命令
- 默认命令是 `Start-Process pwsh`

✅ **配置管理**
- Settings > Tools 中有设置页面
- 可以自定义执行的命令
- 配置能够持久化保存

✅ **用户体验**
- 图标清晰美观
- 操作有通知反馈
- 错误处理友好

✅ **代码质量**
- 所有类和方法有注释
- 遵循 Java 代码规范
- 构建无警告和错误

✅ **文档完善**
- README 包含完整说明
- 代码注释完整
- 提交信息清晰

## 技术要点

### IntelliJ Platform SDK 关键概念

1. **PersistentStateComponent**: 配置持久化
2. **AnAction**: 用户操作(工具栏按钮)
3. **Configurable**: 设置界面
4. **Service**: 应用级服务
5. **Notification**: 通知系统

### 测试策略

- 手动测试覆盖所有用户场景
- 验证错误处理逻辑
- 测试配置的保存和加载
- 验证不同命令的执行

### 注意事项

1. Windows 环境使用 `gradlew.bat` 而不是 `./gradlew`
2. PowerShell 命令需要正确转义
3. 图标使用 SVG 格式以支持主题切换
4. 通知分组需要在 plugin.xml 中注册
