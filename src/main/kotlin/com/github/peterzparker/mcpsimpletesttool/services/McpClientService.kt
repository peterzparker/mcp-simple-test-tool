package com.github.peterzparker.mcpsimpletesttool.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.peterzparker.mcpsimpletesttool.model.ToolInfo
import com.intellij.openapi.diagnostic.thisLogger
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport
import io.modelcontextprotocol.spec.McpSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MCP 客户端服务.
 */
class McpClientService {

    private val logger = thisLogger()
    private val mapper = ObjectMapper()

    private var client: McpSyncClient? = null

    @Volatile
    var isConnected: Boolean = false
        private set

    private val cachedTools = mutableMapOf<String, ToolInfo>()

    /**
     * 连接到指定的 MCP Server SSE 端点.
     */
    suspend fun connect(sseUrl: String) = withContext(Dispatchers.IO) {
        logger.info("Connecting to MCP Server: $sseUrl")
        try {
            val transport = HttpClientSseClientTransport.builder(sseUrl).build()
            client = McpClient.sync(transport).build()
            client!!.initialize()
            isConnected = true
            cachedTools.clear()
            logger.info("Successfully connected to MCP Server: $sseUrl")
        } catch (e: Exception) {
            isConnected = false
            client = null
            logger.error("Failed to connect to MCP Server: $sseUrl", e)
            throw e
        }
    }

    /**
     * 断开与 MCP Server 的连接.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            client?.closeGracefully()
        } catch (e: Exception) {
            logger.warn("Error while disconnecting", e)
        } finally {
            client = null
            isConnected = false
            cachedTools.clear()
        }
    }

    /**
     * 获取 MCP Server 提供的工具列表.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun listTools(): List<ToolInfo> = withContext(Dispatchers.IO) {
        checkConnected()

        val listToolsResult = client!!.listTools()
        val tools = listToolsResult.tools()

        val result = tools.map { tool ->
            val inputSchema = tool.inputSchema()?.let { schema ->
                parseSchema(schema as Any)
            }
            ToolInfo(tool.name(), tool.description(), inputSchema)
        }

        cachedTools.clear()
        result.forEach { cachedTools[it.name] = it }
        result
    }

    /**
     * 调用指定的工具.
     */
    suspend fun callTool(toolName: String, argumentsJson: String): McpCallResult =
        withContext(Dispatchers.IO) {
            checkConnected()

            val argsMap = parseArgumentsJson(argumentsJson)
            val request = McpSchema.CallToolRequest(toolName, argsMap)
            val callResult = client!!.callTool(request)

            val textContent = callResult.content().joinToString("\n") { content ->
                when (content) {
                    is McpSchema.TextContent -> content.text()
                    else -> content.toString()
                }
            }

            McpCallResult(
                text = textContent,
                isError = callResult.isError()
            )
        }

    fun getCachedTool(name: String): ToolInfo? = cachedTools[name]

    private fun checkConnected() {
        if (!isConnected || client == null) {
            throw IllegalStateException("Not connected to any MCP Server")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseSchema(schema: Any): Map<String, Any>? {
        return try {
            if (schema is Map<*, *>) {
                val result = LinkedHashMap<String, Any>()
                for ((key, value) in schema) {
                    if (key is String) {
                        result[key] = value as Any
                    }
                }
                result
            } else {
                mapper.convertValue(schema, Map::class.java) as Map<String, Any>
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse input schema", e)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseArgumentsJson(json: String): Map<String, Any> {
        if (json.isBlank()) return emptyMap()
        return try {
            mapper.readValue(json, Map::class.java) as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            logger.warn("Failed to parse arguments JSON", e)
            emptyMap()
        }
    }
}

data class McpCallResult(
    val text: String,
    val isError: Boolean
)
