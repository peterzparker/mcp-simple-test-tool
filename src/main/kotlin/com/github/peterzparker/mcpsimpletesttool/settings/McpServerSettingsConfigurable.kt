package com.github.peterzparker.mcpsimpletesttool.settings

import com.github.peterzparker.mcpsimpletesttool.services.McpServerSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable

/**
 * MCP Server 设置配置器.
 *
 * 作为 IDEA Settings 界面中的配置入口，包装 McpServerSettingsPage.
 * 在 Settings → Tools → MCP Server 中注册.
 */
class McpServerSettingsConfigurable : Configurable {

    private val settingsState = service<McpServerSettingsState>()
    private var settingsPage: McpServerSettingsPage? = null

    override fun getDisplayName(): String = "MCP Server"

    override fun createComponent(): javax.swing.JComponent {
        if (settingsPage == null) {
            settingsPage = McpServerSettingsPage(settingsState)
        }
        return settingsPage!!.createComponent()
    }

    override fun isModified(): Boolean {
        return settingsPage?.isModified() == true
    }

    override fun apply() {
        settingsPage?.apply()
    }

    override fun reset() {
        settingsPage?.reset()
    }
}
