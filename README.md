# E-BookStore MCP服务器

这是一个用于从E-BookStore系统的MySQL数据库查找图书的MCP（Model Context Protocol）服务器。它可以在Cherry Studio或其他支持MCP的Desktop App中使用。

## 功能特性

- 🔍 **搜索图书**：根据书名、作者关键词进行模糊搜索
- 🏷️ **标签筛选**：按图书标签筛选结果
- 📖 **图书详情**：根据ID获取图书的完整信息（包括库存）
- 📊 **销量排行**：获取按销量排序的热门图书
- 🏷️ **标签列表**：获取所有可用的图书标签

## 安装步骤

### 1. 安装Python依赖

确保你的系统已安装Python 3.8或更高版本，然后安装依赖：

```bash
cd mcp-server
pip install -r requirements.txt
```

### 2. 配置数据库连接

服务器会从环境变量读取数据库配置，如果没有设置，将使用默认值。你可以通过以下方式配置：

#### 方式1：设置环境变量（推荐）

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="bookstore2"
$env:DB_USER="root"
$env:DB_PASSWORD="lhylhylhy050802"
```

**Windows (CMD):**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=bookstore2
set DB_USER=root
set DB_PASSWORD=lhylhylhy050802
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=bookstore2
export DB_USER=root
export DB_PASSWORD=lhylhylhy050802
```

#### 方式2：直接修改server.py中的DB_CONFIG

编辑 `mcp-server/server.py` 文件，修改 `DB_CONFIG` 字典中的值。

### 3. 测试服务器

运行以下命令测试服务器是否正常工作：

```bash
python server.py
```

如果看到没有错误输出，说明服务器已启动（MCP服务器通过stdio通信，所以不会有常规输出）。

## 在Cherry Studio中使用

详细的配置步骤请参考 [CHERRY_STUDIO_CONFIG.md](./CHERRY_STUDIO_CONFIG.md)

### 快速配置

1. **打开Cherry Studio设置**
   - 进入设置（Settings）→ MCP服务器（MCP Servers）

2. **添加新的MCP服务器**
   - 点击"添加服务器"或"+"按钮
   - 填写以下信息：

   **服务器名称：** `bookstore-mcp`

   **命令：** `python`

   **参数：** `C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server\server.py`
   （请根据你的实际路径修改）

   **工作目录：** `C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server`

   **环境变量（可选，如果已在系统环境变量中设置则不需要）：**
   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=bookstore2
   DB_USER=root
   DB_PASSWORD=lhylhylhy050802
   ```

3. **保存并重启Cherry Studio**

4. **使用工具**
   - 在对话中，你可以直接要求AI助手查找图书
   - 例如："帮我找一下关于Python的图书"或"查找作者是鲁迅的图书"

## 在其他Desktop App中使用

### Claude Desktop

编辑配置文件（位置取决于操作系统）：

**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

**Mac:** `~/Library/Application Support/Claude/claude_desktop_config.json`

**Linux:** `~/.config/Claude/claude_desktop_config.json`

添加以下配置：

```json
{
  "mcpServers": {
    "bookstore": {
      "command": "python",
      "args": [
        "C:\\Users\\96317\\Desktop\\homework\\DA_ER\\Web APP\\bookstore_backend\\mcp-server\\server.py"
      ],
      "env": {
        "DB_HOST": "localhost",
        "DB_PORT": "3306",
        "DB_NAME": "bookstore2",
        "DB_USER": "root",
        "DB_PASSWORD": "lhylhylhy050802"
      }
    }
  }
}
```

**注意：** 请将路径中的反斜杠改为双反斜杠（Windows）或使用正斜杠。

### Cursor

在Cursor的设置中，找到MCP服务器配置部分，添加类似的配置。

## 可用的工具

### 1. search_books - 搜索图书

根据关键词和标签搜索图书。

**参数：**
- `keyword` (可选): 搜索关键词，可以是书名或作者名
- `tag` (可选): 图书标签
- `limit` (可选): 返回结果的最大数量，默认20

**示例：**
```json
{
  "keyword": "Python",
  "tag": "编程",
  "limit": 10
}
```

### 2. get_book_by_id - 获取图书详情

根据图书ID获取详细信息。

**参数：**
- `book_id` (必需): 图书的唯一标识ID

**示例：**
```json
{
  "book_id": 1
}
```

### 3. get_all_tags - 获取所有标签

获取数据库中所有可用的图书标签。

**参数：** 无

### 4. get_books_by_sales - 获取销量排行

获取按销量排序的图书列表。

**参数：**
- `limit` (可选): 返回结果的最大数量，默认10

**示例：**
```json
{
  "limit": 20
}
```

## 故障排除

### 问题1：无法连接到数据库

**解决方案：**
- 检查MySQL服务是否正在运行
- 验证数据库配置信息是否正确
- 确认数据库用户有足够的权限
- 检查防火墙设置

### 问题2：找不到Python模块

**解决方案：**
```bash
pip install -r requirements.txt
```

### 问题3：MCP服务器无法启动

**解决方案：**
- 检查Python版本（需要3.8+）
- 确认server.py文件有执行权限
- 查看错误日志以获取详细信息

### 问题4：路径问题（Windows）

**解决方案：**
- 使用绝对路径
- 确保路径中的反斜杠正确转义
- 或者使用正斜杠代替反斜杠

## 数据库表结构

服务器假设数据库中存在以下表：

### books表
- `id`: 图书ID（主键）
- `title`: 书名
- `author`: 作者
- `tags`: 标签（JSON数组格式）
- `cover`: 封面URL
- `price`: 价格
- `description`: 描述
- `deleted`: 是否已删除（0=未删除，1=已删除）
- `sales`: 销量

### book_stock表
- `id`: 库存记录ID（主键）
- `book_id`: 图书ID（外键）
- `inventory`: 库存数量

## 开发说明

### 添加新功能

要添加新的工具，需要：

1. 在 `list_tools()` 函数中添加新的 `Tool` 定义
2. 在 `call_tool()` 函数中添加处理逻辑
3. 实现对应的异步函数

### 测试

你可以使用MCP Inspector工具来测试服务器：

```bash
npx @modelcontextprotocol/inspector python server.py
```

## 许可证

本项目为E-BookStore系统的一部分。

## 支持

如有问题，请检查：
1. 数据库连接是否正常
2. 环境变量是否正确设置
3. Python依赖是否完整安装
4. MCP客户端配置是否正确

