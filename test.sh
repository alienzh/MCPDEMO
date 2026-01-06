#!/bin/bash

# 测试 MCP 服务的脚本

echo "=== 测试 MCP 时间服务 ==="
echo ""
echo "1. 初始化请求："
echo '{"jsonrpc":"2.0","id":1,"method":"initialize"}' | ./gradlew -q run --console=plain
echo ""
echo ""

echo "2. 获取工具列表："
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' | ./gradlew -q run --console=plain
echo ""
echo ""

echo "3. 调用获取时间工具 (ISO 格式)："
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_current_time","arguments":{"format":"iso"}}}' | ./gradlew -q run --console=plain
echo ""
echo ""

echo "4. 调用获取时间工具 (本地格式)："
echo '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"get_current_time","arguments":{"format":"local"}}}' | ./gradlew -q run --console=plain
echo ""
echo ""

echo "5. 调用获取时间工具 (UTC 格式)："
echo '{"jsonrpc":"2.0","id":5,"method":"tools/call","params":{"name":"get_current_time","arguments":{"format":"utc"}}}' | ./gradlew -q run --console=plain

