#!/usr/bin/env python3
"""
测试数据库连接和基本查询功能
用于验证MCP服务器配置是否正确
"""

import os
import mysql.connector
from mysql.connector import Error

# 数据库配置
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", "3306")),
    "database": os.getenv("DB_NAME", "bookstore2"),
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASSWORD", "lhylhylhy050802"),
    "charset": "utf8mb4",
    "collation": "utf8mb4_unicode_ci"
}


def test_connection():
    """测试数据库连接"""
    print("=" * 50)
    print("测试数据库连接...")
    print("=" * 50)

    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            db_info = connection.get_server_info()
            print(f"✓ 成功连接到MySQL服务器，版本: {db_info}")

            cursor = connection.cursor()
            cursor.execute("SELECT DATABASE();")
            database = cursor.fetchone()
            print(f"✓ 当前数据库: {database[0]}")

            return True, connection
        else:
            print("✗ 连接失败")
            return False, None
    except Error as e:
        print(f"✗ 连接错误: {e}")
        return False, None


def test_tables(connection):
    """测试表是否存在"""
    print("\n" + "=" * 50)
    print("检查数据库表...")
    print("=" * 50)

    try:
        cursor = connection.cursor()

        # 检查books表
        cursor.execute("SHOW TABLES LIKE 'books'")
        if cursor.fetchone():
            print("✓ books表存在")
        else:
            print("✗ books表不存在")
            return False

        # 检查book_stock表
        cursor.execute("SHOW TABLES LIKE 'book_stock'")
        if cursor.fetchone():
            print("✓ book_stock表存在")
        else:
            print("⚠ book_stock表不存在（可选）")

        # 检查books_tags表（ElementCollection可能创建）
        cursor.execute("SHOW TABLES LIKE 'book_tags'")
        if cursor.fetchone():
            print("✓ books_tags表存在（使用ElementCollection）")
        else:
            print("ℹ books_tags表不存在（tags可能存储在JSON列中）")

        return True
    except Error as e:
        print(f"✗ 检查表时出错: {e}")
        return False


def test_queries(connection):
    """测试基本查询"""
    print("\n" + "=" * 50)
    print("测试基本查询...")
    print("=" * 50)

    try:
        cursor = connection.cursor(dictionary=True)

        # 查询图书数量
        cursor.execute("SELECT COUNT(*) as count FROM books WHERE deleted = 0")
        result = cursor.fetchone()
        print(f"✓ 未删除的图书数量: {result['count']}")

        # 查询前5本图书
        cursor.execute("""
            SELECT id, title, author, sales 
            FROM books 
            WHERE deleted = 0 
            ORDER BY sales DESC 
            LIMIT 5
        """)
        books = cursor.fetchall()
        print(f"\n✓ 销量前5的图书:")
        for book in books:
            print(f"  - {book['title']} by {book['author']} (销量: {book['sales']})")

        # 检查tags - 使用动态检测
        possible_table_names = ['book_tags', 'books_tags', 'Book_tags']
        tags_table_name = None
        for table_name in possible_table_names:
            cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
            if cursor.fetchone():
                tags_table_name = table_name
                break

        if tags_table_name:
            # 检测book_id列名
            # 注意：DESCRIBE 在使用 dictionary=True 时返回字典，需要特殊处理
            cursor.execute(f"DESCRIBE {tags_table_name}")
            columns = cursor.fetchall()
            # 处理 dictionary cursor 的情况
            if columns and isinstance(columns[0], dict):
                column_names = [col['Field'] for col in columns]
            else:
                column_names = [col[0] for col in columns]
            possible_id_columns = ['books_id', 'book_id', 'Book_id', 'booksId']
            book_id_column = None
            for id_col in possible_id_columns:
                if id_col in column_names:
                    book_id_column = id_col
                    break
            if not book_id_column:
                for col_name in column_names:
                    if 'id' in col_name.lower() and col_name.lower() != 'tags':
                        book_id_column = col_name
                        break

            if book_id_column:
                cursor.execute(f"SELECT COUNT(DISTINCT tags) as count FROM {tags_table_name}")
                result = cursor.fetchone()
                print(f"\n✓ 唯一标签数量: {result['count']} (表: {tags_table_name}, ID列: {book_id_column})")
            else:
                print(f"\n⚠ 找到tags表 {tags_table_name}，但无法确定book_id列")
        else:
            print("\nℹ tags存储在JSON列中")

        return True
    except Error as e:
        print(f"✗ 查询时出错: {e}")
        return False


def main():
    """主函数"""
    print("\n")
    print("E-BookStore MCP服务器 - 连接测试工具")
    print("=" * 50)
    print(f"数据库配置:")
    print(f"  Host: {DB_CONFIG['host']}")
    print(f"  Port: {DB_CONFIG['port']}")
    print(f"  Database: {DB_CONFIG['database']}")
    print(f"  User: {DB_CONFIG['user']}")
    print("=" * 50)
    print()

    # 测试连接
    success, connection = test_connection()
    if not success:
        print("\n✗ 无法连接到数据库，请检查配置")
        return

    try:
        # 测试表
        if not test_tables(connection):
            print("\n✗ 数据库表检查失败")
            return

        # 测试查询
        if not test_queries(connection):
            print("\n✗ 查询测试失败")
            return

        print("\n" + "=" * 50)
        print("✓ 所有测试通过！MCP服务器应该可以正常工作。")
        print("=" * 50)

    finally:
        if connection and connection.is_connected():
            connection.close()
            print("\n✓ 数据库连接已关闭")


if __name__ == "__main__":
    main()

