# E-BookStore 聊天机器人 Agent 服务

这是一个基于 deepseek API 和 MCP 服务的聊天机器人 Agent，提供 HTTP API 接口供前端调用。

## 功能特性

- 🤖 基于 deepseek 大模型的智能对话
- 🔧 集成 MCP 服务作为工具（Tool）
- 🌐 HTTP API 接口
- 🔄 支持工具调用和结果处理
- 🌍 CORS 支持，解决跨域问题

## 安装步骤

### 1. 安装依赖

```bash
cd bookstore_Agent_Server
pip install -r requirements.txt
```

### 2. 配置环境变量

创建 `.env` 文件（参考 `.env.example`）：

```bash
# DeepSeek API配置
DEEPSEEK_API_KEY=your_deepseek_api_key_here

# 服务器配置
PORT=8000
```

或者直接设置环境变量：

**Windows PowerShell:**
```powershell
$env:DEEPSEEK_API_KEY="your_deepseek_api_key_here"
$env:PORT="8000"
```

**Windows CMD:**
```cmd
set DEEPSEEK_API_KEY=your_deepseek_api_key_here
set PORT=8000
```

**Linux/Mac:**
```bash
export DEEPSEEK_API_KEY=your_deepseek_api_key_here
export PORT=8000
```

### 3. 启动服务

```bash
python app.py
```

或者使用 uvicorn：

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

服务将在 `http://localhost:8000` 启动。

## API 接口

### 1. 健康检查

```
GET /
```

返回服务状态。

### 2. 聊天接口

```
POST /chat
```

**请求体：**
```json
{
  "messages": [
    {
      "role": "user",
      "content": "帮我找一下关于Python的图书"
    }
  ],
  "stream": false
}
```

**响应：**
```json
{
  "message": "我为您找到了以下关于Python的图书...",
  "tool_calls": [
    {
      "name": "search_books",
      "arguments": "{\"keyword\":\"Python\"}"
    }
  ]
}
```

### 3. 列出工具

```
GET /tools
```

返回所有可用的 MCP 工具。

## 使用示例

### Python 示例

```python
import requests

url = "http://localhost:8000/chat"
data = {
    "messages": [
        {"role": "user", "content": "帮我找一下关于Python的图书"}
    ]
}

response = requests.post(url, json=data)
print(response.json())
```

### JavaScript 示例

```javascript
fetch('http://localhost:8000/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    messages: [
      { role: 'user', content: '帮我找一下关于Python的图书' }
    ]
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

## 集成的 MCP 工具

1. **search_books** - 搜索图书
2. **get_book_by_id** - 获取图书详情
3. **get_all_tags** - 获取所有标签
4. **get_books_by_sales** - 获取销量排行

## 注意事项

1. 确保 MCP 服务器（bookstore_MCP_Server）的数据库配置正确
2. 需要有效的 DeepSeek API Key
3. 生产环境应限制 CORS 来源
4. 建议使用环境变量管理敏感信息

