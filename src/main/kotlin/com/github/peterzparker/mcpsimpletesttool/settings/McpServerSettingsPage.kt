package com.github.peterzparker.mcpsimpletesttool.settings

import com.github.peterzparker.mcpsimpletesttool.model.McpServerConfig
import com.github.peterzparker.mcpsimpletesttool.services.McpServerSettingsState
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import org.jetbrains.annotations.Nls
import javax.swing.table.AbstractTableModel

/**
 * MCP Server 配置设置页面.
 *
 * 提供添加、编辑、删除 MCP Server 连接配置的界面.
 * 使用 JetBrains Kotlin UI DSL 构建声明式 UI.
 */
class McpServerSettingsPage(private val settingsState: McpServerSettingsState) :
    Configurable {

    private var configList: MutableList<McpServerConfig> = mutableListOf()
    private lateinit var tableModel: McpServerTableModel
    private lateinit var table: JBTable
    private lateinit var panel: DialogPanel

    override fun isModified(): Boolean {
        return configList != settingsState.getConfigList()
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String = "MCP Server"

    override fun createComponent(): DialogPanel {
        configList = settingsState.getConfigList().toMutableList()
        tableModel = McpServerTableModel(configList)
        table = JBTable(tableModel)

        panel = panel {
            row("MCP Server 配置:") {
                val decorator = ToolbarDecorator.createDecorator(table)
                    .setAddAction { onAdd() }
                    .setEditAction { onEdit() }
                    .setRemoveAction { onRemove() }
                cell(decorator.createPanel())
                    .align(AlignX.FILL)
                    .comment("添加、编辑或删除 MCP Server 连接配置。V1.0 仅支持 SSE 传输。")
            }
        }
        return panel
    }

    override fun apply() {
        settingsState.apply {
            val current = getConfigList()
            current.indices.reversed().forEach { removeConfig(it) }
            configList.forEach { addConfig(it) }
        }
    }

    override fun reset() {
        configList = settingsState.getConfigList().toMutableList()
        tableModel.updateData(configList)
    }

    private fun onAdd() {
        val dialog = McpServerConfigDialog(null)
        if (dialog.showAndGet()) {
            val config = McpServerConfig(dialog.configName, dialog.sseUrl)
            configList.add(config)
            tableModel.fireTableDataChanged()
        }
    }

    private fun onEdit() {
        val selectedRow = table.selectedRow
        if (selectedRow < 0 || selectedRow >= configList.size) return

        val existing = configList[selectedRow]
        val dialog = McpServerConfigDialog(existing)
        if (dialog.showAndGet()) {
            val config = McpServerConfig(dialog.configName, dialog.sseUrl)
            configList[selectedRow] = config
            tableModel.fireTableDataChanged()
        }
    }

    private fun onRemove() {
        val selectedRow = table.selectedRow
        if (selectedRow < 0 || selectedRow >= configList.size) return
        configList.removeAt(selectedRow)
        tableModel.fireTableDataChanged()
    }

    private class McpServerTableModel(private var data: List<McpServerConfig>) :
        AbstractTableModel() {

        private val columns = arrayOf("名称", "SSE URL")

        fun updateData(newData: List<McpServerConfig>) {
            data = newData
            fireTableDataChanged()
        }

        override fun getRowCount(): Int = data.size
        override fun getColumnCount(): Int = columns.size
        override fun getColumnName(column: Int): String = columns[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val config = data[rowIndex]
            return when (columnIndex) {
                0 -> config.name
                1 -> config.sseUrl
                else -> ""
            }
        }
    }
}
