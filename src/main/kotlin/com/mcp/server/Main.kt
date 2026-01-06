package com.mcp.server

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// JSON-RPC 2.0 错误代码
object JsonRpcErrorCodes {
    const val PARSE_ERROR = -32700
    const val INVALID_REQUEST = -32600
    const val METHOD_NOT_FOUND = -32601
    const val INVALID_PARAMS = -32602
    const val INTERNAL_ERROR = -32603
}

// 常量定义
object JsonRpcConstants {
    const val VERSION = "2.0"
    const val METHOD_INITIALIZE = "initialize"
    const val METHOD_TOOLS_LIST = "tools/list"
    const val METHOD_TOOLS_CALL = "tools/call"
    const val TOOL_GET_CURRENT_TIME = "get_current_time"
    const val PROTOCOL_VERSION = "2024-11-05"
    const val SERVER_NAME = "now-server"
    const val SERVER_VERSION = "1.0.0"
}

// 时间格式常量
object TimeFormats {
    const val ISO = "iso"
    const val UTC = "utc"
    const val LOCAL = "local"
    const val DEFAULT = ISO
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JsonRpcRequest(
    @EncodeDefault val jsonrpc: String = JsonRpcConstants.VERSION,
    val id: JsonElement?,
    val method: String,
    val params: JsonObject? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JsonRpcResponse(
    @EncodeDefault val jsonrpc: String = JsonRpcConstants.VERSION,
    val id: JsonElement?,
    val result: JsonElement? = null,
    val error: JsonRpcError? = null
)

@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String
)

@Serializable
data class InitializeResult(
    val protocolVersion: String,
    val capabilities: Capabilities,
    val serverInfo: ServerInfo
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Capabilities(
    @EncodeDefault val tools: ToolsCapability = ToolsCapability()
)

@Serializable  
data class ToolsCapability(
    val listChanged: Boolean? = null
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String
)

@Serializable
data class Tool(
    val name: String,
    val description: String,
    val inputSchema: InputSchema
)

@Serializable
data class InputSchema(
    val type: String,
    val properties: JsonObject,
    val additionalProperties: Boolean = false
)

@Serializable
data class ToolsListResult(
    val tools: List<Tool>
)

// 辅助函数：创建错误响应
fun createErrorResponse(id: JsonElement?, code: Int, message: String): JsonRpcResponse {
    return JsonRpcResponse(
        jsonrpc = JsonRpcConstants.VERSION,
        id = id,
        error = JsonRpcError(code, message)
    )
}

// 辅助函数：创建成功响应
fun createSuccessResponse(id: JsonElement?, result: JsonElement): JsonRpcResponse {
    return JsonRpcResponse(
        jsonrpc = JsonRpcConstants.VERSION,
        id = id,
        result = result
    )
}

// 辅助函数：尝试从原始输入中提取 ID（即使 JSON 格式错误也尝试提取）
fun extractRequestId(line: String, json: Json): JsonElement? {
    // 首先尝试正常解析
    return try {
        val jsonElement = json.parseToJsonElement(line)
        if (jsonElement is JsonObject) {
            jsonElement["id"]
        } else {
            null
        }
    } catch (e: Exception) {
        // 如果正常解析失败，尝试用正则表达式提取 id 字段
        try {
            // 尝试匹配 "id": value 的模式
            val idPattern = Regex(""""id"\s*:\s*([^,}\]]+)""")
            val match = idPattern.find(line)
            if (match != null) {
                val idValue = match.groupValues[1].trim()
                // 尝试解析为数字
                try {
                    JsonPrimitive(idValue.toInt())
                } catch (e: NumberFormatException) {
                    // 如果不是数字，尝试作为字符串
                    try {
                        // 移除引号
                        val cleanValue = idValue.removeSurrounding("\"")
                        JsonPrimitive(cleanValue)
                    } catch (e: Exception) {
                        JsonPrimitive(idValue)
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

fun main() {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    val json = Json { 
        ignoreUnknownKeys = true
        isLenient = false
    }

    while (true) {
        val line = reader.readLine() ?: break
        // 跳过空行，不发送任何响应
        if (line.trim().isEmpty()) continue
        
        try {
            val request = json.decodeFromString<JsonRpcRequest>(line)
            val response = handleRequest(request)
            
            if (response != null) {
                println(Json.encodeToString(response))
                System.out.flush()
            }
        } catch (e: Exception) {
            // 解析错误时，不发送响应（避免 unknown message ID 错误）
            // MCP 客户端会超时处理
            System.err.println("Error processing request: ${e.message}")
        }
    }
}

fun handleRequest(request: JsonRpcRequest): JsonRpcResponse? {
    return when (request.method) {
        JsonRpcConstants.METHOD_INITIALIZE -> handleInitialize(request.id)
        JsonRpcConstants.METHOD_TOOLS_LIST -> handleToolsList(request.id)
        JsonRpcConstants.METHOD_TOOLS_CALL -> handleToolsCall(request)
        else -> createErrorResponse(
            request.id,
            JsonRpcErrorCodes.METHOD_NOT_FOUND,
            "Method not found: ${request.method}"
        )
    }
}

fun handleInitialize(id: JsonElement?): JsonRpcResponse {
    val result = InitializeResult(
        protocolVersion = JsonRpcConstants.PROTOCOL_VERSION,
        capabilities = Capabilities(),
        serverInfo = ServerInfo(
            name = JsonRpcConstants.SERVER_NAME,
            version = JsonRpcConstants.SERVER_VERSION
        )
    )
    
    return createSuccessResponse(id, Json.encodeToJsonElement(result))
}

fun handleToolsList(id: JsonElement?): JsonRpcResponse {
    val tool = Tool(
        name = JsonRpcConstants.TOOL_GET_CURRENT_TIME,
        description = "Get the current date and time",
        inputSchema = InputSchema(
            type = "object",
            properties = buildJsonObject {
                putJsonObject("format") {
                    put("type", "string")
                    put("description", "Time format (iso, local, utc)")
                    put("default", TimeFormats.DEFAULT)
                }
            },
            additionalProperties = false
        )
    )
    
    val result = ToolsListResult(tools = listOf(tool))
    
    return createSuccessResponse(id, Json.encodeToJsonElement(result))
}

fun handleToolsCall(request: JsonRpcRequest): JsonRpcResponse {
    // 安全地解析 params（request.params 已经是 JsonObject? 类型）
    val params = request.params ?: JsonObject(emptyMap())
    
    // 安全地获取工具名称
    val toolName = when (val nameElement = params["name"]) {
        is JsonPrimitive -> nameElement.content
        null -> {
            return createErrorResponse(
                request.id,
                JsonRpcErrorCodes.INVALID_PARAMS,
                "Invalid params: 'name' is required"
            )
        }
        else -> {
            return createErrorResponse(
                request.id,
                JsonRpcErrorCodes.INVALID_PARAMS,
                "Invalid params: 'name' must be a string"
            )
        }
    }
    
    if (toolName.isBlank()) {
        return createErrorResponse(
            request.id,
            JsonRpcErrorCodes.INVALID_PARAMS,
            "Invalid params: 'name' cannot be empty"
        )
    }
    
    if (toolName == JsonRpcConstants.TOOL_GET_CURRENT_TIME) {
        // 安全地获取 arguments
        val arguments = when (val argsElement = params["arguments"]) {
            is JsonObject -> argsElement
            null -> JsonObject(emptyMap())
            else -> {
                return createErrorResponse(
                    request.id,
                    JsonRpcErrorCodes.INVALID_PARAMS,
                    "Invalid params: 'arguments' must be an object"
                )
            }
        }
        
        // 安全地获取 format
        val format = when (val formatElement = arguments["format"]) {
            is JsonPrimitive -> formatElement.content.takeIf { it.isNotBlank() } ?: TimeFormats.DEFAULT
            null -> TimeFormats.DEFAULT
            else -> TimeFormats.DEFAULT
        }
        
        try {
            val currentTime = getCurrentTime(format)
            
            return createSuccessResponse(
                request.id,
                buildJsonObject {
                    put("content", buildJsonArray {
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", currentTime)
                        })
                    })
                }
            )
        } catch (e: Exception) {
            return createErrorResponse(
                request.id,
                JsonRpcErrorCodes.INTERNAL_ERROR,
                "Failed to get current time: ${e.message}"
            )
        }
    }
    
    return createErrorResponse(
        request.id,
        JsonRpcErrorCodes.METHOD_NOT_FOUND,
        "Tool not found: $toolName"
    )
}

fun getCurrentTime(format: String): String {
    // 统一获取时间点，确保一致性
    val now = ZonedDateTime.now()
    
    return when (format.lowercase()) {
        TimeFormats.ISO -> {
            now.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
        TimeFormats.UTC -> {
            now.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME)
        }
        TimeFormats.LOCAL -> {
            now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
        }
        else -> {
            // 默认使用 ISO 格式
            now.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}
