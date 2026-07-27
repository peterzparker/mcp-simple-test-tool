package com.github.peterzparker.mcpsimpletesttool.toolWindow

import com.github.peterzparker.mcpsimpletesttool.model.McpServerConfig
import com.github.peterzparker.mcpsimpletesttool.services.McpClientService
import com.github.peterzparker.mcpsimpletesttool.services.McpServerSettingsState
import com.github.peterzparker.mcpsimpletesttool.model.ToolInfo
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

/**
 * MCP Tool 运行器主面板.
 *
 * 包含:
 * - 顶部连接栏: Server 选择器、连接/断开按钮、状态指示
 * - 工具列表区: 显示连接后获取的工具列表
 * - 多 Tab 调试区: 以 Tab 页形式管理多个 Tool 的调试
 */
class McpToolRunnerPanel(
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clientService = McpClientService()
    private val settingsState = service<McpServerSettingsState>()

    private val statusLabel = JBLabel("Disconnected")
    private val connectButton = JButton("Connect")
    private val reconnectButton = JButton("Reconnect")
    private val serverComboBox = ComboBox<McpServerConfig>()
    private val toolListModel = DefaultListModel<ToolInfo>()
    private val toolList = JList(toolListModel)

    private val mainPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    private var currentSseUrl: String? = null

    /**
     * 初始化面板内容.
     */
    fun initialize() {
        val content = buildPanel()
        val contentManager = toolWindow.contentManager
        val factory = com.intellij.ui.content.ContentFactory.getInstance()
        val tabContent = factory.createContent(content, null, false)
        contentManager.addContent(tabContent)
    }

    /**
     * 构建主面板.
     */
    private fun buildPanel(): JComponent {
        refreshServerList()
        setupListeners()

        val connectionBar = panel {
            row("Server:") {
                cell(serverComboBox)
                    .align(AlignX.FILL)
            }
            row {
                cell(connectButton)
                cell(reconnectButton)
                cell(statusLabel)
            }
        }

        val toolScrollPane = JBScrollPane(toolList).apply {
            preferredSize = Dimension(250, -1)
        }

        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            toolScrollPane,
            JPanel().apply {
                layout = BorderLayout()
                add(JBLabel("选择工具并点击 Send 开始调试"), BorderLayout.CENTER)
            }
        ).apply {
            dividerLocation = 250
            resizeWeight = 0.2
        }

        mainPanel.apply {
            add(connectionBar)
            add(splitPane)
        }

        return mainPanel
    }

    /**
     * 刷新 Server 下拉列表.
     */
    private fun refreshServerList() {
        serverComboBox.removeAllItems()
        settingsState.getConfigList().forEach { serverComboBox.addItem(it) }
        if (serverComboBox.itemCount > 0) {
            serverComboBox.selectedIndex = 0
        }
    }

    /**
     * 设置事件监听.
     */
    private fun setupListeners() {
        connectButton.addActionListener { onConnect() }
        reconnectButton.addActionListener { onReconnect() }
        toolList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                onToolSelected()
            }
        }
    }

    /**
     * 连接到选中的 Server.
     */
    private fun onConnect() {
        val selected = serverComboBox.selectedItem as? McpServerConfig ?: return
        val sseUrl = selected.sseUrl
        currentSseUrl = sseUrl

        connectButton.isEnabled = false
        statusLabel.text = "Connecting..."

        coroutineScope.launch {
            try {
                clientService.connect(sseUrl)
                launch(Dispatchers.Swing) {
                    statusLabel.text = "Connected ✓"
                    statusLabel.foreground = JBColor.GREEN
                    connectButton.isEnabled = false
                    reconnectButton.isEnabled = true
                    loadTools()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Swing) {
                    statusLabel.text = "Connection Failed: ${e.message}"
                    statusLabel.foreground = JBColor.RED
                    connectButton.isEnabled = true
                    reconnectButton.isEnabled = true
                }
            }
        }
    }

    /**
     * 重连.
     */
    private fun onReconnect() {
        coroutineScope.launch {
            clientService.disconnect()
            onConnect()
        }
    }

    /**
     * 加载工具列表.
     */
    private fun loadTools() {
        coroutineScope.launch {
            try {
                val tools = clientService.listTools()
                launch(Dispatchers.Swing) {
                    toolListModel.clear()
                    tools.forEach { toolListModel.addElement(it) }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Swing) {
                    statusLabel.text = "Failed to load tools: ${e.message}"
                }
            }
        }
    }

    /**
     * 工具选中事件 - 打开调试 Tab.
     */
    private fun onToolSelected() {
        val selectedTool = toolList.selectedValue ?: return
        createToolTab(selectedTool)
    }

    /**
     * 创建或聚焦工具调试 Tab 页.
     */
    private fun createToolTab(toolInfo: ToolInfo) {
        val contentManager = toolWindow.contentManager
        val existingContent = contentManager.contents.find {
            it.displayName == toolInfo.name
        }
        if (existingContent != null) {
            contentManager.setSelectedContent(existingContent)
            return
        }

        val tabContent = ToolTabContent(toolInfo, clientService, coroutineScope)
        val factory = com.intellij.ui.content.ContentFactory.getInstance()
        val content = factory.createContent(tabContent.content, toolInfo.name, false)
        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

    /**
     * 通过工具名称打开 Tab（由 Gutter Icon 调用）.
     */
    fun openToolByName(toolName: String) {
        val toolInfo = toolListModel.elements().asSequence().find { it.name == toolName }
            ?: clientService.getCachedTool(toolName)
        if (toolInfo != null) {
            createToolTab(toolInfo)
        }
    }
}
