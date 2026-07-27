package com.github.peterzparker.mcpsimpletesttool.model

/**
 * MCP Server 连接配置数据类.
 *
 * @property name 配置名称，用于标识
 * @property sseUrl SSE 端点 URL（如 http://localhost:8080/sse）
 */
data class McpServerConfig(
    val name: String,
    val sseUrl: String
) {
    override fun toString(): String = name
}
