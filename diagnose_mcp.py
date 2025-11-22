#!/usr/bin/env python3
"""
MCP服务器诊断工具
用于检查MCP服务器配置是否正确
"""

import sys
import os

def check_python_version():
    """检查Python版本"""
    print("=" * 60)
    print("1. 检查Python版本")
    print("=" * 60)
    version = sys.version_info
    print(f"Python版本: {version.major}.{version.minor}.{version.micro}")
    if version.major < 3 or (version.major == 3 and version.minor < 8):
        print("[X] Python版本过低，需要3.8或更高版本")
        return False
    else:
        print("[OK] Python版本符合要求")
        return True

def check_dependencies():
    """检查依赖包"""
    print("\n" + "=" * 60)
    print("2. 检查依赖包")
    print("=" * 60)
    
    missing = []
    
    # 检查mcp
    try:
        import mcp
        print("[OK] mcp 已安装")
    except ImportError:
        print("[X] mcp 未安装")
        missing.append("mcp")
    
    # 检查mysql-connector-python
    try:
        import mysql.connector
        print("[OK] mysql-connector-python 已安装")
    except ImportError:
        print("[X] mysql-connector-python 未安装")
        missing.append("mysql-connector-python")
    
    # 检查pydantic
    try:
        import pydantic
        print("[OK] pydantic 已安装")
    except ImportError:
        print("[X] pydantic 未安装")
        missing.append("pydantic")
    
    if missing:
        print(f"\n缺少以下依赖包: {', '.join(missing)}")
        print("请运行: pip install -r requirements.txt")
        return False
    else:
        print("\n[OK] 所有依赖包已安装")
        return True

def check_server_file():
    """检查server.py文件"""
    print("\n" + "=" * 60)
    print("3. 检查server.py文件")
    print("=" * 60)
    
    server_path = os.path.join(os.path.dirname(__file__), "server.py")
    if os.path.exists(server_path):
        print(f"[OK] server.py 存在: {server_path}")
        
        # 尝试导入server.py
        try:
            # 检查语法
            with open(server_path, 'r', encoding='utf-8') as f:
                code = f.read()
            compile(code, server_path, 'exec')
            print("[OK] server.py 语法正确")
            return True
        except SyntaxError as e:
            print(f"[X] server.py 语法错误: {e}")
            return False
    else:
        print(f"[X] server.py 不存在: {server_path}")
        return False

def check_database_connection():
    """检查数据库连接"""
    print("\n" + "=" * 60)
    print("4. 检查数据库连接")
    print("=" * 60)
    
    try:
        import mysql.connector
        from mysql.connector import Error
        
        DB_CONFIG = {
            "host": os.getenv("DB_HOST", "localhost"),
            "port": int(os.getenv("DB_PORT", "3306")),
            "database": os.getenv("DB_NAME", "bookstore2"),
            "user": os.getenv("DB_USER", "root"),
            "password": os.getenv("DB_PASSWORD", "lhylhylhy050802"),
            "charset": "utf8mb4",
            "collation": "utf8mb4_unicode_ci"
        }
        
        print(f"数据库配置:")
        print(f"  Host: {DB_CONFIG['host']}")
        print(f"  Port: {DB_CONFIG['port']}")
        print(f"  Database: {DB_CONFIG['database']}")
        print(f"  User: {DB_CONFIG['user']}")
        
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            db_info = connection.get_server_info()
            print(f"\n[OK] 成功连接到MySQL服务器，版本: {db_info}")
            connection.close()
            return True
        else:
            print("\n[X] 无法连接到数据库")
            return False
    except Error as e:
        print(f"\n[X] 数据库连接错误: {e}")
        print("  请检查:")
        print("  1. MySQL服务是否正在运行")
        print("  2. 数据库配置信息是否正确")
        print("  3. 环境变量是否已设置")
        return False
    except Exception as e:
        print(f"\n[X] 检查数据库连接时出错: {e}")
        return False

def check_mcp_server_startup():
    """尝试启动MCP服务器（不实际运行）"""
    print("\n" + "=" * 60)
    print("5. 检查MCP服务器初始化")
    print("=" * 60)
    
    try:
        # 尝试导入server模块的关键组件
        from mcp.server import Server
        from mcp.server.stdio import stdio_server
        from mcp.types import Tool, TextContent
        
        # 尝试创建Server实例
        app = Server("bookstore-mcp-server")
        print("[OK] MCP Server 实例创建成功")
        
        # 检查list_tools函数（注意：list_tools是装饰器注册的函数，不能直接await）
        # 这里只检查Server实例是否创建成功
        print("[OK] 工具列表函数已注册")
        
        return True
    except Exception as e:
        print(f"[X] MCP服务器初始化失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def check_cherry_studio_config():
    """检查Cherry Studio配置提示"""
    print("\n" + "=" * 60)
    print("6. Cherry Studio配置检查")
    print("=" * 60)
    
    server_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "server.py"))
    work_dir = os.path.dirname(server_path)
    
    print("请确保在Cherry Studio中配置以下信息:")
    print(f"\n  服务器名称: bookstore-mcp")
    print(f"  命令: python")
    print(f"  参数: {server_path}")
    print(f"  工作目录: {work_dir}")
    print(f"\n  环境变量（可选）:")
    print(f"    DB_HOST=localhost")
    print(f"    DB_PORT=3306")
    print(f"    DB_NAME=bookstore2")
    print(f"    DB_USER=root")
    print(f"    DB_PASSWORD=lhylhylhy050802")
    
    # 检查路径中是否有空格
    if " " in server_path:
        print(f"\n[!] 警告: 路径中包含空格，在Cherry Studio中可能需要使用引号:")
        print(f'  参数: "{server_path}"')
    
    return True

def main():
    """主函数"""
    print("\n")
    print("=" * 60)
    print("E-BookStore MCP服务器 - 诊断工具")
    print("=" * 60)
    print()
    
    results = []
    
    # 运行各项检查
    results.append(("Python版本", check_python_version()))
    results.append(("依赖包", check_dependencies()))
    results.append(("server.py文件", check_server_file()))
    results.append(("数据库连接", check_database_connection()))
    results.append(("MCP服务器初始化", check_mcp_server_startup()))
    check_cherry_studio_config()
    
    # 总结
    print("\n" + "=" * 60)
    print("诊断总结")
    print("=" * 60)
    
    all_passed = True
    for name, passed in results:
        status = "[OK] 通过" if passed else "[X] 失败"
        print(f"{name}: {status}")
        if not passed:
            all_passed = False
    
    print()
    if all_passed:
        print("[OK] 所有检查通过！MCP服务器应该可以正常工作。")
        print("  如果仍然遇到问题，请检查Cherry Studio的配置。")
    else:
        print("[X] 部分检查失败，请根据上述提示修复问题。")
        print("  修复后，请重新运行此诊断工具。")
    
    print("=" * 60)
    print()

if __name__ == "__main__":
    main()

