package com.github.peterzparker.mcpsimpletesttool.settings

import com.github.peterzparker.mcpsimpletesttool.model.McpServerConfig
import com.intellij.openapi.ui.DialogWrapper
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * MCP Server 配置编辑对话框.
 */
class McpServerConfigDialog(existingConfig: McpServerConfig?) :
    DialogWrapper(true) {

    private val nameField = JTextField(existingConfig?.name ?: "")
    private val urlField = JTextField(existingConfig?.sseUrl ?: "")

    val configName: String get() = nameField.text
    val sseUrl: String get() = urlField.text

    init {
        title = if (existingConfig != null) "编辑 MCP Server" else "添加 MCP Server"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(0, 2, 8, 8))
        panel.add(JLabel("名称:"))
        panel.add(nameField)
        panel.add(JLabel("SSE URL:"))
        panel.add(urlField)
        return panel
    }

    override fun doOKAction() {
        val name = nameField.text.trim()
        val url = urlField.text.trim()
        if (name.isEmpty()) { setErrorText("名称不能为空"); return }
        if (url.isEmpty()) { setErrorText("SSE URL 不能为空"); return }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            setErrorText("SSE URL 必须以 http:// 或 https:// 开头")
            return
        }
        super.doOKAction()
    }
}
