#!/bin/bash

echo "Starting E-BookStore Chatbot Agent Server..."
echo ""

# 检查环境变量
if [ -z "$DEEPSEEK_API_KEY" ]; then
    echo "警告: DEEPSEEK_API_KEY 环境变量未设置"
    echo "请设置 DEEPSEEK_API_KEY 环境变量或创建 .env 文件"
    echo ""
fi

# 激活虚拟环境（如果存在）
if [ -f "venv/bin/activate" ]; then
    source venv/bin/activate
fi

# 启动服务
python3 app.py

