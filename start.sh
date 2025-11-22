#!/bin/bash
# Linux/Mac启动脚本

echo "启动E-BookStore MCP服务器..."
echo ""

# 设置环境变量（如果需要，请修改这些值）
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=bookstore2
export DB_USER=root
export DB_PASSWORD=lhylhylhy050802

# 启动服务器
python3 server.py

