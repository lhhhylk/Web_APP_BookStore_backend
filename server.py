#!/usr/bin/env python3
"""
E-BookStore MCP服务器
提供从MySQL数据库查找图书的功能
"""

import asyncio
import json
import os
from typing import Any, Sequence
import mysql.connector
from mysql.connector import Error
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Tool, TextContent

# 数据库配置（从环境变量读取，如果没有则使用默认值）
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", "3306")),
    "database": os.getenv("DB_NAME", "bookstore2"),
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASSWORD", "lhylhylhy050802"),
    "charset": "utf8mb4",
    "collation": "utf8mb4_unicode_ci"
}

# 创建MCP服务器实例
app = Server("bookstore-mcp-server")


def get_db_connection():
    """获取MySQL数据库连接"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        return connection
    except Error as e:
        raise Exception(f"数据库连接失败: {str(e)}")


def get_tags_table_info(cursor):
    """检测book_tags表的结构，返回表名和book_id列名"""
    # 可能的表名
    possible_table_names = ['book_tags', 'books_tags', 'Book_tags']
    # 可能的book_id列名
    possible_id_columns = ['books_id', 'book_id', 'Book_id', 'booksId']

    for table_name in possible_table_names:
        cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
        if cursor.fetchone():
            # 表存在，检查列结构
            cursor.execute(f"DESCRIBE {table_name}")
            columns = cursor.fetchall()

            # 处理 dictionary cursor 和普通 cursor 两种情况
            if columns and isinstance(columns[0], dict):
                column_names = [col['Field'] for col in columns]
            else:
                column_names = [col[0] for col in columns]

            # 查找book_id列
            for id_col in possible_id_columns:
                if id_col in column_names:
                    return table_name, id_col

            # 如果没找到，尝试查找包含'id'的列（排除tags列）
            for col_name in column_names:
                if 'id' in col_name.lower() and col_name.lower() != 'tags':
                    return table_name, col_name

    return None, None


@app.list_tools()
async def list_tools() -> list[Tool]:
    """列出所有可用的工具"""
    return [
        Tool(
            name="search_books",
            description="根据关键词搜索图书。可以按书名、作者进行模糊搜索，也可以按标签筛选。",
            inputSchema={
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
        ),
        Tool(
            name="get_book_by_id",
            description="根据图书ID获取图书的详细信息，包括库存信息。",
            inputSchema={
                "type": "object",
                "properties": {
                    "book_id": {
                        "type": "integer",
                        "description": "图书的唯一标识ID"
                    }
                },
                "required": ["book_id"]
            }
        ),
        Tool(
            name="get_all_tags",
            description="获取数据库中所有可用的图书标签列表。",
            inputSchema={
                "type": "object",
                "properties": {}
            }
        ),
        Tool(
            name="get_books_by_sales",
            description="获取按销量排序的图书列表，用于查看热门图书。",
            inputSchema={
                "type": "object",
                "properties": {
                    "limit": {
                        "type": "integer",
                        "description": "返回结果的最大数量，默认10",
                        "default": 10
                    }
                }
            }
        )
    ]


@app.call_tool()
async def call_tool(name: str, arguments: dict[str, Any]) -> Sequence[TextContent]:
    """处理工具调用"""
    try:
        if name == "search_books":
            keyword = arguments.get("keyword")
            tag = arguments.get("tag")
            limit = arguments.get("limit", 20)
            # 确保limit是有效的整数
            try:
                limit = int(limit) if limit is not None else 20
                if limit <= 0:
                    limit = 20
            except (ValueError, TypeError):
                limit = 20
            result = await search_books(keyword, tag, limit)
            return [TextContent(type="text", text=json.dumps(result, ensure_ascii=False, indent=2))]

        elif name == "get_book_by_id":
            book_id = arguments.get("book_id")
            if not book_id:
                raise ValueError("book_id参数是必需的")
            result = await get_book_by_id(book_id)
            return [TextContent(type="text", text=json.dumps(result, ensure_ascii=False, indent=2))]

        elif name == "get_all_tags":
            result = await get_all_tags()
            return [TextContent(type="text", text=json.dumps(result, ensure_ascii=False, indent=2))]

        elif name == "get_books_by_sales":
            limit = arguments.get("limit", 10)
            # 确保limit是有效的整数
            try:
                limit = int(limit) if limit is not None else 10
                if limit <= 0:
                    limit = 10
            except (ValueError, TypeError):
                limit = 10
            result = await get_books_by_sales(limit)
            return [TextContent(type="text", text=json.dumps(result, ensure_ascii=False, indent=2))]

        else:
            raise ValueError(f"未知的工具: {name}")

    except Exception as e:
        import traceback
        error_result = {
            "error": True,
            "message": str(e),
            "type": type(e).__name__
        }
        # 在开发环境中，可以包含更详细的错误信息
        if os.getenv("DEBUG", "false").lower() == "true":
            error_result["traceback"] = traceback.format_exc()
        return [TextContent(type="text", text=json.dumps(error_result, ensure_ascii=False, indent=2))]


async def search_books(keyword: str = None, tag: str = None, limit: int = 20) -> dict:
    """搜索图书"""
    connection = None
    try:
        connection = get_db_connection()
        cursor = connection.cursor(dictionary=True)

        # 检测tags表的实际结构
        tags_table_name, book_id_column = get_tags_table_info(cursor)

        # 构建SQL查询
        if tags_table_name and book_id_column:
            # 如果存在tags表，使用JOIN查询
            query = f"""
                SELECT DISTINCT b.*, bs.inventory 
                FROM books b 
                LEFT JOIN book_stock bs ON b.id = bs.book_id
            """
            if tag:
                query += f" INNER JOIN {tags_table_name} bt ON b.id = bt.{book_id_column}"
            query += " WHERE b.deleted = 0"
            params = []

            if keyword:
                query += " AND (LOWER(b.title) LIKE %s OR LOWER(b.author) LIKE %s)"
                keyword_pattern = f"%{keyword.lower()}%"
                params.extend([keyword_pattern, keyword_pattern])

            if tag:
                query += " AND bt.tags = %s"
                params.append(tag)
        else:
            # 如果tags存储在JSON列中
            query = "SELECT b.*, bs.inventory FROM books b LEFT JOIN book_stock bs ON b.id = bs.book_id WHERE b.deleted = 0"
            params = []

            if keyword:
                query += " AND (LOWER(b.title) LIKE %s OR LOWER(b.author) LIKE %s)"
                keyword_pattern = f"%{keyword.lower()}%"
                params.extend([keyword_pattern, keyword_pattern])

            if tag:
                # 尝试JSON_CONTAINS（MySQL 5.7+）
                try:
                    query += " AND JSON_CONTAINS(b.tags, %s)"
                    params.append(json.dumps([tag]))
                except:
                    # 如果JSON_CONTAINS不可用，使用LIKE
                    query += " AND b.tags LIKE %s"
                    params.append(f"%{tag}%")

        query += " ORDER BY b.sales DESC LIMIT %s"
        params.append(limit)

        cursor.execute(query, params)
        books = cursor.fetchall()

        # 获取tags（如果使用tags表）
        if tags_table_name and book_id_column:
            for book in books:
                book_id = book['id']
                cursor.execute(f"SELECT tags FROM {tags_table_name} WHERE {book_id_column} = %s", (book_id,))
                tag_rows = cursor.fetchall()
                book['tags'] = [row['tags'] for row in tag_rows] if tag_rows else []
        else:
            # 处理tags字段（从JSON字符串转换为列表）
            for book in books:
                if book.get('tags'):
                    try:
                        if isinstance(book['tags'], str):
                            book['tags'] = json.loads(book['tags'])
                        elif not isinstance(book['tags'], list):
                            book['tags'] = []
                    except:
                        book['tags'] = []
                else:
                    book['tags'] = []

        return {
            "success": True,
            "count": len(books),
            "books": books
        }

    except Error as e:
        return {
            "success": False,
            "error": f"数据库查询错误: {str(e)}"
        }
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()


async def get_book_by_id(book_id: int) -> dict:
    """根据ID获取图书详情"""
    connection = None
    try:
        connection = get_db_connection()
        cursor = connection.cursor(dictionary=True)

        # 检测tags表的实际结构
        tags_table_name, book_id_column = get_tags_table_info(cursor)

        query = """
            SELECT b.*, bs.inventory 
            FROM books b 
            LEFT JOIN book_stock bs ON b.id = bs.book_id 
            WHERE b.id = %s AND b.deleted = 0
        """
        cursor.execute(query, (book_id,))
        book = cursor.fetchone()

        if not book:
            return {
                "success": False,
                "error": f"未找到ID为 {book_id} 的图书"
            }

        # 获取tags
        if tags_table_name and book_id_column:
            cursor.execute(f"SELECT tags FROM {tags_table_name} WHERE {book_id_column} = %s", (book_id,))
            tag_rows = cursor.fetchall()
            book['tags'] = [row['tags'] for row in tag_rows] if tag_rows else []
        else:
            # 处理tags字段（从JSON字符串转换为列表）
            if book.get('tags'):
                try:
                    if isinstance(book['tags'], str):
                        book['tags'] = json.loads(book['tags'])
                    elif not isinstance(book['tags'], list):
                        book['tags'] = []
                except:
                    book['tags'] = []
            else:
                book['tags'] = []

        return {
            "success": True,
            "book": book
        }

    except Error as e:
        return {
            "success": False,
            "error": f"数据库查询错误: {str(e)}"
        }
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()


async def get_all_tags() -> dict:
    """获取所有标签"""
    connection = None
    try:
        connection = get_db_connection()
        cursor = connection.cursor()

        # 检测tags表的实际结构
        tags_table_name, book_id_column = get_tags_table_info(cursor)

        all_tags = set()

        if tags_table_name and book_id_column:
            # 如果使用tags表
            query = f"""
                SELECT DISTINCT bt.tags 
                FROM {tags_table_name} bt 
                INNER JOIN books b ON bt.{book_id_column} = b.id 
                WHERE b.deleted = 0
            """
            cursor.execute(query)
            results = cursor.fetchall()
            for row in results:
                if row[0]:
                    all_tags.add(row[0])
        else:
            # 如果tags存储在JSON列中
            query = "SELECT tags FROM books WHERE deleted = 0"
            cursor.execute(query)
            results = cursor.fetchall()
            for row in results:
                if row[0]:
                    try:
                        tags = json.loads(row[0]) if isinstance(row[0], str) else row[0]
                        if isinstance(tags, list):
                            all_tags.update(tags)
                    except:
                        pass

        return {
            "success": True,
            "tags": sorted(list(all_tags))
        }

    except Error as e:
        return {
            "success": False,
            "error": f"数据库查询错误: {str(e)}"
        }
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()


async def get_books_by_sales(limit: int = 10) -> dict:
    """获取按销量排序的图书"""
    connection = None
    cursor = None
    try:
        # 确保limit是有效的整数
        if not isinstance(limit, int) or limit <= 0:
            limit = 10

        connection = get_db_connection()
        cursor = connection.cursor(dictionary=True)

        # 检测tags表的实际结构
        tags_table_name, book_id_column = get_tags_table_info(cursor)

        query = """
            SELECT b.*, bs.inventory 
            FROM books b 
            LEFT JOIN book_stock bs ON b.id = bs.book_id 
            WHERE b.deleted = 0 
            ORDER BY b.sales DESC 
            LIMIT %s
        """
        cursor.execute(query, (limit,))
        books = cursor.fetchall()

        # 获取tags
        for book in books:
            book_id = book.get('id')
            if not book_id:
                continue

            if tags_table_name and book_id_column:
                try:
                    cursor.execute(f"SELECT tags FROM {tags_table_name} WHERE {book_id_column} = %s", (book_id,))
                    tag_rows = cursor.fetchall()
                    book['tags'] = [row['tags'] for row in tag_rows] if tag_rows else []
                except Exception as e:
                    # 如果查询tags失败，设置为空列表
                    book['tags'] = []
            else:
                # 处理tags字段（从JSON字符串转换为列表）
                if book.get('tags'):
                    try:
                        if isinstance(book['tags'], str):
                            book['tags'] = json.loads(book['tags'])
                        elif not isinstance(book['tags'], list):
                            book['tags'] = []
                    except:
                        book['tags'] = []
                else:
                    book['tags'] = []

        return {
            "success": True,
            "count": len(books),
            "books": books
        }

    except Error as e:
        return {
            "success": False,
            "error": f"数据库查询错误: {str(e)}"
        }
    except Exception as e:
        return {
            "success": False,
            "error": f"处理错误: {str(e)} (类型: {type(e).__name__})"
        }
    finally:
        if connection and connection.is_connected():
            if cursor:
                cursor.close()
            connection.close()


async def main():
    """主函数：启动MCP服务器"""
    try:
        # 使用stdio_server创建标准输入输出流
        async with stdio_server() as (read_stream, write_stream):
            await app.run(
                read_stream,
                write_stream,
                app.create_initialization_options()
            )
    except Exception as e:
        # 将错误输出到stderr，这样Cherry Studio可以看到错误信息
        import sys
        print(f"MCP服务器启动失败: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        # 正常退出
        pass
    except Exception as e:
        import sys

        print(f"MCP服务器错误: {e}", file=sys.stderr)
        import traceback

        traceback.print_exc(file=sys.stderr)
        sys.exit(1)

