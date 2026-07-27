package com.github.peterzparker.mcpsimpletesttool.toolWindow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.peterzparker.mcpsimpletesttool.model.ToolInfo
import com.github.peterzparker.mcpsimpletesttool.services.McpClientService
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.Dimension
import javax.swing.*

/**
 * 单个 Tool 的调试 Tab 页内容.
 *
 * 包含:
 * - 参数输入区（JSON 编辑器）
 * - Send 按钮
 * - 结果展示区（纯文本/格式化 JSON）
 * - 状态信息（请求耗时、错误信息）
 *
 * @param toolInfo 工具信息
 * @param clientService MCP 客户端服务
 * @param coroutineScope 协程作用域
 */
class ToolTabContent(
    private val toolInfo: ToolInfo,
    private val clientService: McpClientService,
    private val coroutineScope: CoroutineScope
) {
    private val mapper = ObjectMapper()
    private val resultTextArea = JTextArea().apply {
        isEditable = false
        rows = 15
        columns = 60
        lineWrap = true
        wrapStyleWord = true
    }
    private val statusLabel = JBLabel("Ready")
    private val sendButton = JButton("Send")
    private val jsonEditor = JTextArea().apply {
        rows = 8
        columns = 60
        lineWrap = true
        wrapStyleWord = true
    }

    /** Tab 页的主面板 */
    val content: JComponent

    init {
        // 生成默认参数模板
        jsonEditor.text = generateDefaultJson()

        content = panel {
            row("Tool: ${toolInfo.name}") {
                toolInfo.description?.let {
                    cell(JBLabel("<html><i>$it</i></html>"))
                }
            }
            row("参数 (JSON):") {
                cell(JBScrollPane(jsonEditor))
                    .align(AlignX.FILL)
                    .comment("根据 inputSchema 自动生成的 JSON 参数模板")
            }
            row {
                cell(sendButton)
                cell(statusLabel)
            }
            row("结果:") {
                cell(JBScrollPane(resultTextArea))
                    .align(AlignX.FILL)
            }
        }.apply {
            preferredSize = Dimension(600, 500)
        }

        setupListeners()
    }

    /**
     * 设置事件监听.
     */
    private fun setupListeners() {
        sendButton.addActionListener { onSend() }
    }

    /**
     * 发送工具调用请求.
     */
    private fun onSend() {
        val argumentsJson = jsonEditor.text

        sendButton.isEnabled = false
        statusLabel.text = "Executing..."
        statusLabel.foreground = JBColor.BLUE

        val startTime = System.currentTimeMillis()

        coroutineScope.launch {
            try {
                val result = clientService.callTool(toolInfo.name, argumentsJson)
                val elapsed = System.currentTimeMillis() - startTime

                launch(Dispatchers.Swing) {
                    // 格式化展示结果
                    val displayText = try {
                        val jsonNode = mapper.readTree(result.text)
                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
                    } catch (e: Exception) {
                        result.text
                    }
                    resultTextArea.text = displayText
                    statusLabel.text = if (result.isError) {
                        "Error (${elapsed}ms)"
                    } else {
                        "Success (${elapsed}ms)"
                    }
                    statusLabel.foreground = if (result.isError) JBColor.RED else JBColor.GREEN
                    sendButton.isEnabled = true
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                launch(Dispatchers.Swing) {
                    resultTextArea.text = "Error: ${e.message}\n\n${e.stackTraceToString()}"
                    statusLabel.text = "Failed (${elapsed}ms)"
                    statusLabel.foreground = JBColor.RED
                    sendButton.isEnabled = true
                }
            }
        }
    }

    /**
     * 根据工具的 inputSchema 生成默认 JSON 参数模板.
     */
    private fun generateDefaultJson(): String {
        val schema = toolInfo.inputSchema ?: return "{\n  \n}"
        return try {
            val properties = schema["properties"] as? Map<*, *> ?: return "{\n  \n}"
            val defaultValues = mutableMapOf<String, Any>()

            properties.forEach { (key, value) ->
                if (key is String && value is Map<*, *>) {
                    val type = value["type"] as? String
                    val defaultValue = when (type) {
                        "string" -> value["default"] as? String ?: ""
                        "number", "integer" -> value["default"] ?: 0
                        "boolean" -> value["default"] ?: false
                        "array" -> value["default"] ?: emptyList<Any>()
                        "object" -> value["default"] ?: emptyMap<String, Any>()
                        else -> null
                    }
                    if (defaultValue != null) {
                        defaultValues[key] = defaultValue
                    }
                }
            }

            if (defaultValues.isEmpty()) {
                "{\n  \n}"
            } else {
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultValues)
            }
        } catch (e: Exception) {
            "{\n  \n}"
        }
    }
}
