# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Quick Terminal Launcher 是一个 IntelliJ IDEA 插件，用于在工具栏添加快速启动终端按钮，可执行用户自定义命令。

## 常用构建命令

```bash
# 构建插件（生成 zip 文件到 build/distributions/）
./gradlew buildPlugin

# 运行 IDEA 实例测试插件
./gradlew runIde

# 验证插件
./gradlew verifyPlugin

# 清理构建
./gradlew clean
```

## 技术栈

- Java 17
- IntelliJ Platform SDK 2023.2+ (Gradle IntelliJ Plugin 1.16.1)
- Gradle Kotlin DSL

## 项目架构

### 核心组件

```
src/main/java/com/starttools/quickterminallauncher/
├── TerminalLauncherIcons.java          # 图标加载类
├── actions/
│   └── LaunchTerminalAction.java       # 工具栏按钮动作（执行 PowerShell 命令）
└── settings/
    ├── TerminalLauncherSettings.java   # 配置持久化服务 (PersistentStateComponent)
    ├── TerminalLauncherState.java      # 配置状态类
    └── TerminalLauncherConfigurable.java # 设置界面 (Settings > Tools)
```

### 插件配置

- `src/main/resources/META-INF/plugin.xml` - 插件描述、扩展点和动作注册
- `gradle.properties` - 插件版本、平台版本等配置

### 关键 IntelliJ Platform 概念

- **AnAction**: 用于实现工具栏按钮 (`LaunchTerminalAction`)
- **PersistentStateComponent**: 配置持久化 (`TerminalLauncherSettings`)
- **Configurable**: 设置界面实现 (`TerminalLauncherConfigurable`)
- **notificationGroup**: 通知消息分组（在 plugin.xml 中注册）

### 命令执行机制

插件使用 `ProcessBuilder` 调用 `powershell.exe` 执行用户配置的命令，默认命令为 `Start-Process pwsh`。

## 代码规范

- 所有类需要头部注释，包含作用描述、创建时间、作者
- 方法需要有注释描述其作用
- 字段需要有注释描述其用途
- 使用简体中文进行注释
