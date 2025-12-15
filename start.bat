@echo off
echo Starting E-BookStore Chatbot Agent Server...
echo.

REM 检查环境变量
if "%DEEPSEEK_API_KEY%"=="" (
    echo 警告: DEEPSEEK_API_KEY 环境变量未设置
    echo 请设置 DEEPSEEK_API_KEY 环境变量或创建 .env 文件
    echo.
)

REM 激活虚拟环境（如果存在）
if exist "venv\Scripts\activate.bat" (
    call venv\Scripts\activate.bat
)

REM 启动服务
python app.py

pause

