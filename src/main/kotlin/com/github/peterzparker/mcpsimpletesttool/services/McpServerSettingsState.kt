package com.github.peterzparker.mcpsimpletesttool.services

import com.github.peterzparker.mcpsimpletesttool.model.McpServerConfig
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.XCollection

/**
 * MCP Server 配置的持久化存储状态.
 *
 * 使用 IntelliJ 的 PersistentStateComponent 机制，将用户配置的 MCP Server 列表
 * 持久化存储至 IDEA 配置文件中.
 */
@State(
    name = "McpServerSettings",
    storages = [Storage("mcp-server-settings.xml")]
)
class McpServerSettingsState : SimplePersistentStateComponent<McpServerSettingsState.State>(State()) {

    /**
     * 内部状态类，存储所有 MCP Server 配置.
     */
    class State : BaseState() {
        @get:XCollection(elementTypes = [ItemState::class])
        val servers: MutableList<ItemState> by list()

        fun addServer(config: McpServerConfig) {
            servers.add(ItemState(config.name, config.sseUrl))
            incrementModificationCount()
        }

        fun removeServer(index: Int) {
            if (index in servers.indices) {
                servers.removeAt(index)
                incrementModificationCount()
            }
        }

        fun updateServer(index: Int, config: McpServerConfig) {
            if (index in servers.indices) {
                servers[index] = ItemState(config.name, config.sseUrl)
                incrementModificationCount()
            }
        }

        fun toConfigList(): List<McpServerConfig> {
            return servers.map { McpServerConfig(it.name, it.sseUrl) }
        }
    }

    /**
     * 单个 Server 配置项的状态.
     */
    data class ItemState(
        var name: String = "",
        var sseUrl: String = ""
    )

    /**
     * 获取所有配置列表.
     */
    fun getConfigList(): List<McpServerConfig> = state.toConfigList()

    /**
     * 添加新配置.
     */
    fun addConfig(config: McpServerConfig) = state.addServer(config)

    /**
     * 删除指定索引的配置.
     */
    fun removeConfig(index: Int) = state.removeServer(index)

    /**
     * 更新指定索引的配置.
     */
    fun updateConfig(index: Int, config: McpServerConfig) = state.updateServer(index, config)
}
