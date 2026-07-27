package com.github.peterzparker.mcpsimpletesttool.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * MCP Tool Runner ToolWindow 工厂类.
 *
 * 负责创建插件的主调试面板，包含 MCP Server 连接管理和 Tool 调试功能.
 */
class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = McpToolRunnerPanel(project, toolWindow)
        panel.initialize()
    }

    override fun shouldBeAvailable(project: Project) = true
}
