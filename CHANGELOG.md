<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Spring AI MCP Tool Tester Changelog

## [Unreleased]
### Added
- MCP Server 连接管理：Settings 页面配置 SSE 端点 URL，连接/断开/重连功能
- 代码级快捷入口 (Gutter Icon)：自动识别 `@Tool` 注解，一键跳转到调试面板
- Tool 调试面板：多 Tab 管理，JSON 参数编辑，结果展示，请求耗时显示
- MCP 协议通信：基于 MCP Java SDK 2.0.0 的 SSE 传输实现
- 异步处理：所有 MCP 网络请求在后台线程执行，UI 流畅不卡顿
- 状态持久化：MCP Server 配置持久化存储至 IDEA 配置文件
- 构建配置：升级至 JDK 21、Gradle 9.6.1、IntelliJ IDEA 2025.2
