#!/usr/bin/env python3
"""
Agent服务诊断工具
用于测试Agent服务的各个组件是否正常工作
"""

import os
import sys
import asyncio
import json

def test_environment():
    """测试环境变量配置"""
    print("=" * 50)
    print("1. 环境变量检查")
    print("=" * 50)
    
    deepseek_key = os.getenv("DEEPSEEK_API_KEY", "")
    if deepseek_key:
        print(f"✓ DEEPSEEK_API_KEY: {deepseek_key[:10]}...{deepseek_key[-5:]}")
    else:
        print("✗ DEEPSEEK_API_KEY: 未设置")
        return False
    
    port = os.getenv("PORT", "8000")
    print(f"✓ PORT: {port}")
    
    return True


def test_mcp_server():
    """测试MCP服务器导入"""
    print("\n" + "=" * 50)
    print("2. MCP服务器检查")
    print("=" * 50)
    
    # 获取MCP服务器目录
    current_dir = os.path.dirname(os.path.abspath(__file__))
    parent_dir = os.path.dirname(current_dir)
    mcp_server_dir = os.path.join(parent_dir, "bookstore_MCP_Server")
    
    print(f"MCP服务器目录: {mcp_server_dir}")
    
    if not os.path.exists(mcp_server_dir):
        print(f"✗ MCP服务器目录不存在: {mcp_server_dir}")
        return False
    
    print(f"✓ MCP服务器目录存在")
    
    server_py = os.path.join(mcp_server_dir, "server.py")
    if not os.path.exists(server_py):
        print(f"✗ server.py不存在: {server_py}")
        return False
    
    print(f"✓ server.py存在")
    
    # 尝试导入
    try:
        if mcp_server_dir not in sys.path:
            sys.path.insert(0, mcp_server_dir)
        
        from server import (
            search_books,
            get_book_by_id,
            get_all_tags,
            get_books_by_sales
        )
        print("✓ 成功导入MCP服务器函数")
        return True
    except ImportError as e:
        print(f"✗ 导入MCP服务器函数失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False
    except Exception as e:
        print(f"✗ 导入时发生其他错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


async def test_mcp_functions():
    """测试MCP函数调用"""
    print("\n" + "=" * 50)
    print("3. MCP函数调用测试")
    print("=" * 50)
    
    try:
        current_dir = os.path.dirname(os.path.abspath(__file__))
        parent_dir = os.path.dirname(current_dir)
        mcp_server_dir = os.path.join(parent_dir, "bookstore_MCP_Server")
        
        if mcp_server_dir not in sys.path:
            sys.path.insert(0, mcp_server_dir)
        
        from server import get_all_tags
        
        print("测试 get_all_tags...")
        result = await get_all_tags()
        print(f"✓ get_all_tags 调用成功")
        print(f"  结果: {json.dumps(result, ensure_ascii=False, indent=2)[:200]}")
        return True
    except Exception as e:
        print(f"✗ MCP函数调用失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_deepseek_api():
    """测试DeepSeek API连接"""
    print("\n" + "=" * 50)
    print("4. DeepSeek API检查")
    print("=" * 50)
    
    import httpx
    
    deepseek_key = os.getenv("DEEPSEEK_API_KEY", "")
    if not deepseek_key:
        print("✗ DEEPSEEK_API_KEY未设置，跳过API测试")
        return False
    
    api_url = "https://api.deepseek.com/v1/chat/completions"
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {deepseek_key}"
    }
    
    payload = {
        "model": "deepseek-chat",
        "messages": [
            {"role": "user", "content": "你好"}
        ],
        "max_tokens": 10
    }
    
    try:
        print("测试DeepSeek API连接...")
        response = httpx.post(api_url, headers=headers, json=payload, timeout=30.0)
        
        if response.status_code == 200:
            print("✓ DeepSeek API连接成功")
            result = response.json()
            if "choices" in result:
                print(f"  响应: {result['choices'][0]['message']['content']}")
            return True
        else:
            print(f"✗ DeepSeek API返回错误: {response.status_code}")
            print(f"  响应: {response.text}")
            return False
    except httpx.TimeoutException:
        print("✗ DeepSeek API请求超时")
        return False
    except Exception as e:
        print(f"✗ DeepSeek API测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


async def main():
    """主函数"""
    print("\n" + "=" * 50)
    print("E-BookStore Agent 服务诊断工具")
    print("=" * 50)
    
    results = []
    
    # 1. 环境变量检查
    results.append(("环境变量", test_environment()))
    
    # 2. MCP服务器检查
    results.append(("MCP服务器", test_mcp_server()))
    
    # 3. MCP函数调用测试
    results.append(("MCP函数调用", await test_mcp_functions()))
    
    # 4. DeepSeek API检查
    results.append(("DeepSeek API", test_deepseek_api()))
    
    # 总结
    print("\n" + "=" * 50)
    print("诊断结果总结")
    print("=" * 50)
    
    all_passed = True
    for name, result in results:
        status = "✓ 通过" if result else "✗ 失败"
        print(f"{name}: {status}")
        if not result:
            all_passed = False
    
    print("\n" + "=" * 50)
    if all_passed:
        print("✓ 所有检查通过！Agent服务应该可以正常工作。")
    else:
        print("✗ 部分检查失败，请根据上述错误信息修复问题。")
    print("=" * 50)


if __name__ == "__main__":
    asyncio.run(main())

