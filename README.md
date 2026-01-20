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
# Idea-StartTools
