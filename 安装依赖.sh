#!/bin/bash

echo "正在安装Agent服务依赖..."
echo ""

# 安装Agent服务依赖
pip install -r requirements.txt

echo ""
echo "依赖安装完成！"
echo ""
echo "如果MCP服务器依赖安装失败，请确保："
echo "1. 已激活虚拟环境"
echo "2. Python版本 >= 3.8"
echo "3. 网络连接正常"
echo ""

