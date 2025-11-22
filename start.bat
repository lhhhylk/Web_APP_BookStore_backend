@echo off
REM Windows启动脚本
echo 启动E-BookStore MCP服务器...
echo.

REM 设置环境变量（如果需要，请修改这些值）
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=bookstore2
set DB_USER=root
set DB_PASSWORD=lhylhylhy050802

REM 启动服务器
python server.py

pause

