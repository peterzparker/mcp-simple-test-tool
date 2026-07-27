# Spring AI MCP Tool Tester

![Build](https://github.com/peterzparker/mcp-simple-test-tool/workflows/Build/badge.svg)

## 概述

**Spring AI MCP Tool Tester** 是一款 IntelliJ IDEA 插件，为 Spring AI / MCP 开发者提供类似 Postman 的 `@Tool` 方法调试能力。插件通过直连 MCP（Model Context Protocol）协议，与运行中的 Spring AI MCP Server 进行通信，实现工具方法的快速测试与验证。

## 功能特性

### MCP Server 连接管理
- 提供插件设置页面（Settings → Tools → MCP Server），允许用户添加、编辑、删除 MCP Server 连接配置
- V1.0 支持 SSE (Server-Sent Events) 传输方式
- 在插件面板顶部显示当前选中 Server 的连接状态（Connected / Disconnected），并提供手动重连按钮
- 配置持久化存储至 IDEA 配置文件中

### 代码级快捷入口 (Gutter Icon)
- 实时扫描当前项目代码，识别带有 `@Tool` 注解的 public 方法
- 在识别到的方法左侧行号旁渲染插件图标
- 点击图标直接唤起/聚焦 Tool Window 调试面板，自动打开对应 Tool 的调试 Tab

### Tool 调试面板
- 连接成功后自动获取远端 Server 提供的工具列表
- 支持多 Tab 页同时调试多个 Tool
- 根据 inputSchema 自动生成带默认值的 JSON 参数模板
- 提供 JSON 编辑器，支持语法校验
- 展示请求耗时和执行结果（纯文本/格式化 JSON）
- 调用失败时展示 MCP 返回的错误信息或异常堆栈

## 环境要求

- **IDE**: IntelliJ IDEA 2025.2 及以上版本
- **JDK**: 21
- **Gradle**: 9.6.1
- **框架**: Spring AI 2.0.0（`@Tool` 注解）

## 快速开始

1. 打开 **Settings → Tools → MCP Server**，添加你的 MCP Server SSE URL（如 `http://localhost:8080/sse`）
2. 打开 **MCP Tool Runner** 工具窗口（View → Tool Windows → MCP Tool Runner）
3. 在下拉框中选择已配置的 Server，点击 **Connect**
4. 连接成功后，自动加载工具列表
5. 点击任意工具，在 Tab 页中编辑 JSON 参数并点击 **Send** 执行

## 安装

- **IDE 插件市场**: <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > 搜索 "Spring AI MCP Tool Tester"
- **手动安装**: 下载 [latest release](https://github.com/peterzparker/mcp-simple-test-tool/releases/latest) 的插件包，通过 <kbd>Install plugin from disk...</kbd> 安装

## 构建

```bash
./gradlew buildPlugin
```

构建产物位于 `build/distributions/` 目录。

## 调试

```bash
./gradlew runIde
```

## 技术栈

- **语言**: Kotlin
- **核心框架**: IntelliJ Platform Plugin SDK
- **MCP 通信**: MCP Java SDK (io.modelcontextprotocol.sdk:mcp:2.0.0)
- **异步处理**: Kotlin Coroutines
- **UI 构建**: JetBrains Kotlin UI DSL + Swing
- **构建工具**: Gradle 9.6.1

---

Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
