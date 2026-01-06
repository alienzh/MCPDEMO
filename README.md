# Kotlin MCP 时间服务

使用 Kotlin 编写的 MCP (Model Context Protocol) 服务，提供获取当前时间的功能。

## 功能特性

- 基于 JSON-RPC 2.0 协议
- 支持三种时间格式：ISO、本地时区、UTC
- 完整的 MCP 协议实现

## 快速开始

### 1. 构建项目

```bash
./gradlew jar
```

构建完成后，JAR 文件位于 `libs/mcpdemo-1.0.0.jar`

### 2. 测试服务

```bash
./test.sh
```

或手动测试：

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"initialize"}' | java -jar libs/mcpdemo-1.0.0.jar
```

## 在 Cursor 中使用

### 配置步骤

1. **打开 Cursor 的 MCP 配置文件**：
   - macOS: `~/Library/Application Support/Cursor/User/globalStorage/rooveterinaryinc.roo-cline/settings/cline_mcp_settings.json`
   - 或通过命令面板：`Cmd+Shift+P` → 输入 "MCP"

2. **添加以下配置**（将路径替换为你的实际项目路径）：

```json
{
  "mcpServers": {
    "now-server": {
      "command": "/usr/bin/java",
      "args": [
        "-jar",
        "/Users/admin/Documents/mcpdemo/libs/mcpdemo-1.0.0.jar"
      ],
      "env": {}
    }
  }
}
```

3. **重启 Cursor**

4. **测试**：在 AI 聊天中询问"现在是什么时间？"

## 时间格式

- `iso`: ISO 8601 格式，例如：`2026-01-06T17:40:56`
- `local`: 本地时区格式，例如：`2026-01-06 17:40:56 CST`
- `utc`: UTC 时区格式，例如：`2026-01-06T09:40:56Z`

## 项目结构

```
mcpdemo/
├── src/main/kotlin/
│   └── com/mcp/server/
│       └── Main.kt           # MCP 服务器实现
├── libs/
│   └── mcpdemo-1.0.0.jar    # 构建产物
├── build.gradle.kts          # Gradle 配置
├── test.sh                   # 测试脚本
└── README.md
```

## 常见问题

### 找不到 Java

确保 Java 已安装：

```bash
java -version
```

如果未安装，请访问 [Azul Zulu](https://www.azul.com/downloads/) 或 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 下载。

配置中使用完整路径：

```bash
# 查找 Java 路径
which java
# 或
/usr/libexec/java_home
```

### Cursor 显示 "No tools"

1. 确认配置文件路径正确
2. 确认 JAR 文件存在：`ls libs/mcpdemo-1.0.0.jar`
3. 重启 Cursor
4. 查看 Cursor 的 MCP 日志排查问题

### 手动验证服务

```bash
# 初始化
echo '{"jsonrpc":"2.0","id":1,"method":"initialize"}' | java -jar libs/mcpdemo-1.0.0.jar

# 获取工具列表
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' | java -jar libs/mcpdemo-1.0.0.jar

# 调用获取时间
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_current_time","arguments":{"format":"local"}}}' | java -jar libs/mcpdemo-1.0.0.jar
```

## 技术栈

- Kotlin 1.9.20
- kotlinx-serialization-json
- Java 时间 API
- Gradle 8.5

## 参考

本项目实现参考了文章：[如何用 Dart 写个自己的MCP服务](https://juejin.cn/post/7591350620189114374)

## 许可

MIT License
