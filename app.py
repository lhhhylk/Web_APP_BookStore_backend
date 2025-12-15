#!/usr/bin/env python3
"""
E-BookStore 聊天机器人 Agent 服务
基于 deepseek API 和 MCP 服务
"""

import os
import json
import asyncio
import subprocess
import logging
import traceback
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import httpx

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 配置
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
MCP_SERVER_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), "bookstore_MCP_Server", "server.py")

app = FastAPI(title="E-BookStore Chatbot Agent")

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 开发环境允许所有来源，生产环境应限制
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 请求和响应模型
class ChatMessage(BaseModel):
    role: str  # "user" or "assistant"
    content: str


class ChatRequest(BaseModel):
    messages: List[ChatMessage]
    stream: bool = False


class ChatResponse(BaseModel):
    message: str
    tool_calls: Optional[List[Dict[str, Any]]] = None


# MCP工具定义（与MCP服务器中的工具对应）
MCP_TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "search_books",
            "description": "根据关键词搜索图书。可以按书名、作者进行模糊搜索，也可以按标签筛选。",
            "parameters": {
                "type": "object",
                "properties": {
                    "keyword": {
                        "type": "string",
                        "description": "搜索关键词，可以是书名或作者名（可选）"
                    },
                    "tag": {
                        "type": "string",
                        "description": "图书标签，用于筛选特定类型的图书（可选）"
                    },
                    "limit": {
                        "type": "integer",
                        "description": "返回结果的最大数量，默认20",
                        "default": 20
                    }
                }
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "get_book_by_id",
            "description": "根据图书ID获取图书的详细信息，包括库存信息。",
            "parameters": {
                "type": "object",
                "properties": {
                    "book_id": {
                        "type": "integer",
                        "description": "图书的唯一标识ID"
                    }
                },
                "required": ["book_id"]
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "get_all_tags",
            "description": "获取数据库中所有可用的图书标签列表。",
            "parameters": {
                "type": "object",
                "properties": {}
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "get_books_by_sales",
            "description": "获取按销量排序的图书列表，用于查看热门图书。",
            "parameters": {
                "type": "object",
                "properties": {
                    "limit": {
                        "type": "integer",
                        "description": "返回结果的最大数量，默认10",
                        "default": 10
                    }
                }
            }
        }
    }
]


async def call_mcp_tool(tool_name: str, arguments: Dict[str, Any]) -> Dict[str, Any]:
    """
    调用MCP工具
    直接导入并调用MCP服务器的函数
    """
    try:
        # 设置工作目录
        mcp_server_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "bookstore_MCP_Server")
        logger.info(f"调用MCP工具: {tool_name}, 参数: {arguments}, MCP目录: {mcp_server_dir}")
        
        # 检查目录是否存在
        if not os.path.exists(mcp_server_dir):
            error_msg = f"MCP服务器目录不存在: {mcp_server_dir}"
            logger.error(error_msg)
            return {"success": False, "error": error_msg}
        
        # 导入MCP服务器的函数
        import sys
        if mcp_server_dir not in sys.path:
            sys.path.insert(0, mcp_server_dir)
        
        # 动态导入MCP服务器的函数
        try:
            from server import (
                search_books,
                get_book_by_id,
                get_all_tags,
                get_books_by_sales
            )
            logger.info("成功导入MCP服务器函数")
        except ImportError as e:
            error_msg = f"无法导入MCP服务器函数: {str(e)}"
            logger.error(error_msg)
            logger.error(traceback.format_exc())
            return {"success": False, "error": error_msg}
        
        # 调用对应的函数
        if tool_name == "search_books":
            result = await search_books(
                arguments.get("keyword"),
                arguments.get("tag"),
                arguments.get("limit", 20)
            )
        elif tool_name == "get_book_by_id":
            result = await get_book_by_id(arguments.get("book_id"))
        elif tool_name == "get_all_tags":
            result = await get_all_tags()
        elif tool_name == "get_books_by_sales":
            result = await get_books_by_sales(arguments.get("limit", 10))
        else:
            result = {"success": False, "error": f"未知的工具: {tool_name}"}
        
        logger.info(f"MCP工具调用成功: {tool_name}")
        return result
        
    except Exception as e:
        error_msg = f"调用MCP工具失败: {str(e)}"
        logger.error(error_msg)
        logger.error(traceback.format_exc())
        return {
            "success": False,
            "error": error_msg,
            "traceback": traceback.format_exc() if os.getenv("DEBUG") == "true" else None
        }


async def call_deepseek_api(messages: List[Dict[str, str]], tools: List[Dict] = None) -> Dict[str, Any]:
    """
    调用DeepSeek API
    """
    if not DEEPSEEK_API_KEY:
        logger.error("DEEPSEEK_API_KEY未配置")
        raise HTTPException(status_code=500, detail="DEEPSEEK_API_KEY未配置")
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}"
    }
    
    payload = {
        "model": "deepseek-chat",
        "messages": messages,
        "temperature": 0.7,
    }
    
    if tools:
        payload["tools"] = tools
    
    try:
        logger.info(f"调用DeepSeek API，消息数量: {len(messages)}")
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(DEEPSEEK_API_URL, headers=headers, json=payload)
            logger.info(f"DeepSeek API响应状态码: {response.status_code}")
            
            if response.status_code != 200:
                error_text = response.text
                logger.error(f"DeepSeek API错误: {response.status_code} - {error_text}")
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"DeepSeek API错误: {error_text}"
                )
            
            return response.json()
    except httpx.TimeoutException:
        logger.error("DeepSeek API请求超时")
        raise HTTPException(status_code=504, detail="DeepSeek API请求超时")
    except httpx.RequestError as e:
        logger.error(f"DeepSeek API请求错误: {str(e)}")
        raise HTTPException(status_code=500, detail=f"DeepSeek API请求错误: {str(e)}")


@app.get("/")
async def root():
    """健康检查"""
    return {
        "status": "ok",
        "service": "E-BookStore Chatbot Agent",
        "mcp_tools": len(MCP_TOOLS)
    }


@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    聊天接口
    处理用户消息，调用deepseek API，并在需要时调用MCP工具
    """
    try:
        logger.info(f"收到聊天请求，消息数量: {len(request.messages)}")
        
        # 转换消息格式
        messages = [{"role": msg.role, "content": msg.content} for msg in request.messages]
        logger.info(f"转换后的消息: {messages}")
        
        # 添加系统提示
        system_message = {
            "role": "system",
            "content": """你是一个专业的图书推荐助手，可以帮助用户搜索图书、查看图书详情、获取热门图书等。
你可以使用以下工具来帮助用户：
1. search_books - 搜索图书（按书名、作者或标签）
2. get_book_by_id - 根据ID获取图书详情
3. get_all_tags - 获取所有可用的图书标签
4. get_books_by_sales - 获取销量排行榜

当用户询问图书相关信息时，你应该主动使用这些工具来获取准确的信息。
回答要友好、专业，并且要基于工具返回的实际数据。"""
        }
        
        # 将系统消息插入到消息列表的开头（如果还没有系统消息）
        if not messages or messages[0]["role"] != "system":
            messages.insert(0, system_message)
        else:
            messages[0] = system_message
        
        # 调用deepseek API
        logger.info("开始调用DeepSeek API")
        response = await call_deepseek_api(messages, tools=MCP_TOOLS)
        logger.info(f"DeepSeek API响应: {json.dumps(response, ensure_ascii=False, indent=2)[:500]}")
        
        # 处理响应
        if "choices" not in response or len(response["choices"]) == 0:
            logger.error(f"DeepSeek API响应格式错误: {response}")
            raise HTTPException(status_code=500, detail="DeepSeek API响应格式错误")
        
        assistant_message = response["choices"][0]["message"]
        tool_calls = []
        final_response = assistant_message.get("content", "")
        
        # 检查是否有工具调用
        if "tool_calls" in assistant_message and assistant_message["tool_calls"]:
            tool_calls = assistant_message["tool_calls"]
            logger.info(f"检测到工具调用: {len(tool_calls)}个")
            
            # 执行工具调用
            for tool_call in tool_calls:
                tool_name = tool_call["function"]["name"]
                try:
                    tool_arguments = json.loads(tool_call["function"]["arguments"])
                except Exception as e:
                    logger.warning(f"解析工具参数失败: {e}")
                    tool_arguments = {}
                
                logger.info(f"执行工具调用: {tool_name}, 参数: {tool_arguments}")
                # 调用MCP工具
                tool_result = await call_mcp_tool(tool_name, tool_arguments)
                logger.info(f"工具调用结果: {json.dumps(tool_result, ensure_ascii=False)[:200]}")
                
                # 将工具结果添加到消息中
                messages.append({
                    "role": "assistant",
                    "content": None,
                    "tool_calls": [tool_call]
                })
                messages.append({
                    "role": "tool",
                    "content": json.dumps(tool_result, ensure_ascii=False),
                    "tool_call_id": tool_call["id"]
                })
            
            # 再次调用API获取最终回复
            try:
                logger.info("调用DeepSeek API生成最终回复")
                final_response_obj = await call_deepseek_api(messages, tools=MCP_TOOLS)
                if "choices" in final_response_obj and len(final_response_obj["choices"]) > 0:
                    final_response = final_response_obj["choices"][0]["message"].get("content", "抱歉，我无法生成回复。")
                else:
                    logger.error(f"最终回复格式错误: {final_response_obj}")
                    final_response = "抱歉，我无法生成回复。"
            except Exception as e:
                logger.error(f"生成最终回复时出错: {str(e)}")
                logger.error(traceback.format_exc())
                # 如果第二次调用失败，使用工具结果作为回复
                final_response = f"已查询到相关信息，但生成回复时出错: {str(e)}"
        
        logger.info(f"返回最终回复: {final_response[:100]}")
        return ChatResponse(
            message=final_response,
            tool_calls=[{
                "name": tc["function"]["name"],
                "arguments": tc["function"]["arguments"]
            } for tc in tool_calls] if tool_calls else None
        )
        
    except HTTPException:
        # 重新抛出HTTP异常
        raise
    except httpx.HTTPStatusError as e:
        logger.error(f"DeepSeek API HTTP错误: {e.response.status_code} - {e.response.text}")
        raise HTTPException(
            status_code=e.response.status_code,
            detail=f"DeepSeek API错误: {e.response.text}"
        )
    except Exception as e:
        error_msg = f"处理请求时出错: {str(e)}"
        logger.error(error_msg)
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=error_msg)


@app.get("/tools")
async def list_tools():
    """列出所有可用的工具"""
    return {
        "tools": MCP_TOOLS
    }


@app.post("/mcp-tool")
async def mcp_tool_endpoint(request: dict):
    """
    MCP工具调用端点（供n8n工作流使用）
    接收工具名称和参数，调用MCP工具并返回结果
    """
    try:
        tool_name = request.get("tool_name")
        arguments = request.get("arguments", {})
        
        if not tool_name:
            raise HTTPException(status_code=400, detail="tool_name参数是必需的")
        
        # 如果arguments是字符串，解析为字典
        if isinstance(arguments, str):
            try:
                arguments = json.loads(arguments)
            except:
                arguments = {}
        
        logger.info(f"收到MCP工具调用请求: {tool_name}, 参数: {arguments}")
        
        # 调用MCP工具
        result = await call_mcp_tool(tool_name, arguments)
        
        logger.info(f"MCP工具调用结果: {json.dumps(result, ensure_ascii=False)[:200]}")
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"MCP工具端点错误: {str(e)}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"调用MCP工具失败: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)

