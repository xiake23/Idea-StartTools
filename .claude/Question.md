# 问题记录

## 1. 开发IDEA插件 - 快速启动终端工具

**问题简述**: 初始化项目,开发一个idea的插件:一个快速启动终端并执行命令的工具

**问题具体内容**:
开发一个 IntelliJ IDEA 插件，实现以下功能：
1. 快速启动终端并执行命令的工具
2. 执行命令的内容可以由用户在 IDEA 的设置-工具里面进行设置
3. 默认命令是：Start-Process pwsh
4. 在 IDEA 右上角的启动按钮旁边添加一个按钮
5. 按钮的图标是一个启动工具的图标

**创建时间**: 2026-01-18

## 2. 执行计划实现插件

**问题简述**: 继续执行 docs/plans 目录下的实现计划

**问题具体内容**:
使用 superpowers:executing-plans 技能执行 `docs/plans/2026-01-18-quick-terminal-launcher.md` 计划文件，完成 IntelliJ IDEA 插件的完整实现。

**创建时间**: 2026-01-19

## 3. 插件版本兼容性问题

**问题简述**: 插件安装时报错，提示版本不兼容

**问题具体内容**:
安装插件到 IDEA 中时报错：
- 错误信息：插件需要构建版本至 241.* 或更早，但 IDEA 版本是 252.28539.13
- 原因：用户的 IDEA 版本较新（252），超出了插件设置的版本上限（241.*）

**解决方案**:
- 修改 `gradle.properties` 中的 `pluginUntilBuild` 从 `241.*` 改为 `999.*`
- 支持从 IDEA 2023.2 (构建号 232) 到未来所有版本

**创建时间**: 2026-01-19

## 4. 工具栏图标不显示问题

**问题简述**: 插件安装成功但工具栏没有显示图标按钮

**问题具体内容**:
- 插件安装后重启 IDEA，工具栏右上角没有看到终端图标按钮
- 在自定义工具栏搜索 "Quick" 也找不到相关 Action
- 但在 Tools 菜单底部能看到 "Launch Terminal" 选项

**排查过程**:
1. 图标路径配置错误：`icon="TerminalLauncherIcons.TOOLBAR_ICON"` 不正确
2. Action ID 格式问题
3. 工具栏组配置问题

**解决方案**:
- 移除构造函数中的图标引用，使用空构造函数
- 修改 Action ID 为完整包名格式：`com.starttools.quickterminallauncher.LaunchTerminal`
- 使用 IntelliJ 内置图标：`AllIcons.Toolwindows.ToolWindowRun`
- 同时添加到 `ToolsMenu` 和 `MainToolBar`
- 简化配置，移除图标依赖，先确保 Action 能正常注册

**创建时间**: 2026-01-19

## 5. 终端启动目录问题及功能增强

**问题简述**: 工具正常使用，但默认启动目录是用户目录而不是项目目录

**问题具体内容**:
- 使用默认命令 `Start-Process pwsh` 启动的 PowerShell 工作目录在用户主目录
- 希望能在当前项目的根目录启动，方便直接操作项目文件
- 同时希望能支持灵活的自定义配置

**需求方案**:
实现组合方案：
1. 默认行为：自动在项目根目录启动终端
2. 变量替换：支持在命令中使用项目相关变量
3. 设置界面：添加变量说明和使用示例

**实现内容**:

### 功能 1：自动项目目录
- 使用 `ProcessBuilder.directory()` 自动设置工作目录为项目根目录
- 无需用户配置，开箱即用
- 如果没有打开项目，回退到默认行为

### 功能 2：变量替换
支持以下变量：
- `{PROJECT_DIR}` - 项目根目录路径
- `{PROJECT_PATH}` - 项目路径（别名）
- `{PROJECT_NAME}` - 项目名称

### 功能 3：增强的设置界面
添加以下内容：
- 默认行为说明
- 可用变量列表和说明
- 4个实用的命令示例：
  1. `Start-Process pwsh` - 启动 PowerShell（默认在项目目录）
  2. `Start-Process wt` - 启动 Windows Terminal
  3. `Start-Process pwsh -ArgumentList '-NoExit', '-Command', 'echo {PROJECT_NAME}'` - 显示项目名称
  4. `Start-Process cmd -ArgumentList '/K', 'cd /d {PROJECT_DIR}'` - 启动 CMD 并切换到项目目录

**实现文件**:
- 修改：`LaunchTerminalAction.java` - 添加 `replaceVariables()` 方法和项目目录设置
- 修改：`TerminalLauncherConfigurable.java` - 优化设置界面，添加详细说明

**创建时间**: 2026-01-19
