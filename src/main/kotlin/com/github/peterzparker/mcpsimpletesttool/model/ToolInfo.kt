package com.github.peterzparker.mcpsimpletesttool.model

/**
 * 从 MCP Server 获取的工具信息.
 *
 * @property name 工具名称
 * @property description 工具描述
 * @property inputSchema 工具的 JSON Schema 参数字段定义
 */
data class ToolInfo(
    val name: String,
    val description: String?,
    val inputSchema: Map<String, Any>?
)
