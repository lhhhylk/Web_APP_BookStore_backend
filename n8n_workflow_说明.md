# E-BookStore 聊天机器人 n8n 工作流说明

## 📋 概述

本文档详细说明了 E-BookStore 聊天机器人 Agent 的 n8n 工作流配置，包括流程全貌和各个节点的配置细节。

## 🎯 工作流架构

### 流程概览

```
[Webhook Trigger] 
    ↓
[准备消息] 
    ↓
[DeepSeek AI Agent] 
    ↓
[检查工具调用] 
    ├─→ [提取工具调用] → [调用MCP工具] → [合并工具结果] → [生成最终回复] → [返回响应]
    └─→ [直接返回] → [返回响应]
```

## 📊 节点详细配置

### 1. Webhook Trigger（Webhook触发器）

**节点类型**: `n8n-nodes-base.webhook`  
**位置**: 流程入口

**配置参数**:
- **HTTP Method**: `POST`
- **Path**: `chat`
- **Response Mode**: `responseNode`（使用响应节点）
- **Webhook ID**: `bookstore-chatbot`

**功能说明**:
- 接收来自前端的HTTP POST请求
- 路径为 `/webhook/chat`（n8n会自动添加 `/webhook` 前缀）
- 等待响应节点返回结果

**请求格式**:
```json
{
  "messages": [
    {
      "role": "user",
      "content": "帮我找一下关于Python的图书"
    }
  ]
}
```

---

### 2. 准备消息（Code节点）

**节点类型**: `n8n-nodes-base.code`  
**位置**: 消息预处理

**配置参数**:
- **Language**: JavaScript
- **Code**: 见下方代码

**代码逻辑**:
```javascript
// 解析请求体
const body = $input.item.json.body || $input.item.json;
const messages = body.messages || [];

// 构建系统提示
const systemMessage = {
  role: 'system',
  content: `你是一个专业的图书推荐助手，可以帮助用户搜索图书、查看图书详情、获取热门图书等。
你可以使用以下工具来帮助用户：
1. search_books - 搜索图书（按书名、作者或标签）
2. get_book_by_id - 根据ID获取图书详情
3. get_all_tags - 获取所有可用的图书标签
4. get_books_by_sales - 获取销量排行榜

当用户询问图书相关信息时，你应该主动使用这些工具来获取准确的信息。
回答要友好、专业，并且要基于工具返回的实际数据。`
};

// 添加系统消息
const allMessages = [systemMessage, ...messages];

return {
  json: {
    messages: allMessages,
    originalRequest: body
  }
};
```

**功能说明**:
- 解析前端发送的消息
- 添加系统提示消息，定义AI助手的角色和能力
- 准备发送给AI Agent的消息列表

---

### 3. DeepSeek AI Agent（AI Agent节点）

**节点类型**: `@n8n/n8n-nodes-langchain.agent`  
**位置**: AI处理核心

**配置参数**:
- **Model**: `deepseek-chat`
- **Temperature**: `0.7`
- **Tools**: 4个MCP工具定义
  - `search_books` - 搜索图书
  - `get_book_by_id` - 获取图书详情
  - `get_all_tags` - 获取所有标签
  - `get_books_by_sales` - 获取销量排行

**工具定义示例**（search_books）:
```json
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
}
```

**认证配置**:
- **Credential Type**: DeepSeek API
- **API Key**: 需要在n8n中配置DeepSeek API Key

**功能说明**:
- 调用DeepSeek大模型处理用户消息
- 根据消息内容决定是否需要调用工具
- 如果不需要工具，直接返回回复
- 如果需要工具，返回工具调用信息

**输出格式**:
```json
{
  "text": "AI的回复内容（如果不需要工具）",
  "tool_calls": [
    {
      "id": "call_xxx",
      "function": {
        "name": "search_books",
        "arguments": "{\"keyword\":\"Python\"}"
      }
    }
  ]
}
```

---

### 4. 检查工具调用（IF节点）

**节点类型**: `n8n-nodes-base.if`  
**位置**: 条件判断

**配置参数**:
- **Condition**: 检查 `tool_calls` 字段是否存在
- **Operation**: `exists`

**功能说明**:
- 判断AI Agent是否返回了工具调用
- 如果有工具调用 → 进入工具调用流程
- 如果没有工具调用 → 直接返回AI的回复

**分支逻辑**:
- **True分支**: 进入工具调用处理流程
- **False分支**: 直接返回响应

---

### 5. 提取工具调用（Code节点）

**节点类型**: `n8n-nodes-base.code`  
**位置**: 工具调用处理（True分支）

**代码逻辑**:
```javascript
// 处理工具调用
const toolCalls = $input.item.json.tool_calls || [];
const results = [];

for (const toolCall of toolCalls) {
  const toolName = toolCall.function.name;
  const toolArgs = JSON.parse(toolCall.function.arguments || '{}');
  
  results.push({
    tool_name: toolName,
    tool_arguments: toolArgs,
    tool_call_id: toolCall.id
  });
}

return results.map(item => ({ json: item }));
```

**功能说明**:
- 从AI Agent的输出中提取所有工具调用
- 解析每个工具调用的名称和参数
- 为每个工具调用创建一个单独的数据项（支持并行调用）

**输出格式**:
```json
[
  {
    "tool_name": "search_books",
    "tool_arguments": {"keyword": "Python"},
    "tool_call_id": "call_xxx"
  }
]
```

---

### 6. 调用MCP工具（HTTP Request节点）

**节点类型**: `n8n-nodes-base.httpRequest`  
**位置**: 执行工具调用

**配置参数**:
- **Method**: `POST`
- **URL**: `http://localhost:8000/mcp-tool`
- **Authentication**: `none`
- **Body Parameters**:
  - `tool_name`: `={{ $json.tool_name }}`
  - `arguments`: `={{ JSON.stringify($json.tool_arguments) }}`

**功能说明**:
- 向MCP工具服务发送HTTP请求
- 传递工具名称和参数
- 获取工具执行结果

**请求格式**:
```json
{
  "tool_name": "search_books",
  "arguments": {"keyword": "Python", "limit": 20}
}
```

**响应格式**:
```json
{
  "success": true,
  "count": 5,
  "books": [...]
}
```

**注意**: 这个节点需要MCP工具服务运行在 `http://localhost:8000`。如果使用不同的地址，需要修改URL。

---

### 7. 合并工具结果（Code节点）

**节点类型**: `n8n-nodes-base.code`  
**位置**: 工具结果处理

**代码逻辑**:
```javascript
// 合并工具调用结果
const toolResults = $input.all();
const originalMessages = $('准备消息').item.json.messages;
const assistantMessage = $('DeepSeek AI Agent').item.json;

// 构建工具调用消息
const toolMessages = [];
for (const result of toolResults) {
  const toolCall = assistantMessage.tool_calls.find(
    tc => tc.function.name === result.json.tool_name
  );
  
  if (toolCall) {
    toolMessages.push({
      role: 'assistant',
      content: null,
      tool_calls: [toolCall]
    });
    
    toolMessages.push({
      role: 'tool',
      content: JSON.stringify(result.json.result || result.json),
      tool_call_id: toolCall.id
    });
  }
}

// 合并所有消息
const allMessages = [...originalMessages, ...toolMessages];

return {
  json: {
    messages: allMessages
  }
};
```

**功能说明**:
- 收集所有工具调用的结果
- 按照OpenAI工具调用格式构建消息
- 将工具结果添加到消息历史中
- 准备发送给AI生成最终回复

**输出格式**:
```json
{
  "messages": [
    {"role": "system", "content": "..."},
    {"role": "user", "content": "..."},
    {"role": "assistant", "content": null, "tool_calls": [...]},
    {"role": "tool", "content": "{...}", "tool_call_id": "..."}
  ]
}
```

---

### 8. 生成最终回复（LM Chat节点）

**节点类型**: `@n8n/n8n-nodes-langchain.lmChat`  
**位置**: 生成最终回复

**配置参数**:
- **Model**: `deepseek-chat`
- **Temperature**: `0.7`
- **Messages**: 来自"合并工具结果"节点的消息列表

**功能说明**:
- 使用包含工具结果的完整消息历史
- 调用DeepSeek API生成最终回复
- AI会根据工具返回的数据生成用户友好的回复

**输出格式**:
```json
{
  "text": "我为您找到了以下关于Python的图书：...",
  "content": "..."
}
```

---

### 9. 返回响应（Respond to Webhook节点）

**节点类型**: `n8n-nodes-base.respondToWebhook`  
**位置**: 流程出口（两个分支都连接到这里）

**配置参数**:
- **Respond With**: `json`
- **Response Body**: 
  ```json
  {
    "message": "{{ $json.text || $json.content }}",
    "tool_calls": "{{ $('DeepSeek AI Agent').item.json.tool_calls || null }}"
  }
  ```

**功能说明**:
- 将最终结果返回给前端
- 包含AI的回复消息
- 可选包含工具调用信息（用于调试）

**响应格式**:
```json
{
  "message": "我为您找到了以下关于Python的图书：...",
  "tool_calls": null
}
```

---

### 10. 直接返回（Respond to Webhook节点）

**节点类型**: `n8n-nodes-base.respondToWebhook`  
**位置**: 无工具调用分支

**配置参数**:
- **Respond With**: `json`
- **Response Body**: 
  ```json
  {
    "message": "{{ $json.text || $json.content || $json.message }}"
  }
  ```

**功能说明**:
- 当AI不需要调用工具时，直接返回AI的回复
- 简化响应格式

---

## 🔧 配置步骤

### 1. 导入工作流

1. 打开n8n界面
2. 点击"Workflows" → "Import from File"
3. 选择 `n8n_workflow.json` 文件
4. 工作流将被导入

### 2. 配置DeepSeek API凭证

1. 在n8n中，进入"Credentials"
2. 创建新的DeepSeek API凭证：
   - **Name**: `DeepSeek API`
   - **API Key**: 输入你的DeepSeek API Key
3. 在工作流的"DeepSeek AI Agent"和"生成最终回复"节点中，选择这个凭证

### 3. 配置MCP工具服务

确保MCP工具服务正在运行：
- 默认地址: `http://localhost:8000`
- 如果使用不同地址，修改"调用MCP工具"节点的URL

### 4. 激活工作流

1. 点击工作流右上角的"Active"开关
2. 工作流将开始监听Webhook请求

### 5. 获取Webhook URL

1. 点击"Webhook Trigger"节点
2. 复制显示的Webhook URL
3. 格式类似: `http://your-n8n-instance.com/webhook/chat`

### 6. 配置前端

修改前端代码中的Agent API URL为n8n的Webhook URL。

---

## 📸 流程截图说明

### 流程全貌图

```
┌─────────────────┐
│ Webhook Trigger │  ← 接收前端请求
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   准备消息      │  ← 添加系统提示
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ DeepSeek AI     │  ← AI处理，决定是否调用工具
│    Agent        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 检查工具调用    │  ← 判断分支
└────┬────────┬───┘
     │        │
     │        └──────────────┐
     │                       │
     ▼                       ▼
┌─────────────┐      ┌──────────────┐
│ 提取工具调用│      │  直接返回     │
└──────┬──────┘      └──────┬───────┘
       │                     │
       ▼                     │
┌─────────────┐              │
│调用MCP工具  │              │
└──────┬──────┘              │
       │                     │
       ▼                     │
┌─────────────┐              │
│合并工具结果 │              │
└──────┬──────┘              │
       │                     │
       ▼                     │
┌─────────────┐              │
│生成最终回复 │              │
└──────┬──────┘              │
       │                     │
       └─────────┬───────────┘
                 │
                 ▼
         ┌──────────────┐
         │  返回响应     │  ← 返回给前端
         └──────────────┘
```

---

## 🎯 工作流特点

1. **模块化设计**: 每个节点职责明确，易于维护
2. **条件分支**: 根据是否需要工具调用选择不同路径
3. **工具集成**: 无缝集成MCP服务作为工具
4. **错误处理**: 可以在关键节点添加错误处理节点
5. **可扩展性**: 易于添加新的工具或处理逻辑

---

## 🔍 调试建议

1. **查看节点执行日志**: 点击每个节点查看输入输出数据
2. **测试单个节点**: 使用"Execute Node"功能测试单个节点
3. **检查数据格式**: 确保节点间的数据格式匹配
4. **验证API连接**: 确保DeepSeek API和MCP服务都正常运行

---

## 📝 注意事项

1. **API Key安全**: 不要在代码中硬编码API Key，使用n8n的凭证管理
2. **MCP服务地址**: 确保MCP工具服务地址正确
3. **网络连接**: 确保n8n可以访问DeepSeek API和MCP服务
4. **错误处理**: 建议在关键节点添加错误处理逻辑

---

## 🚀 使用示例

### 前端请求

```javascript
fetch('http://your-n8n-instance.com/webhook/chat', {
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

### 响应示例

```json
{
  "message": "我为您找到了以下关于Python的图书：\n\n1. 《Python编程从入门到实践》...",
  "tool_calls": [
    {
      "id": "call_xxx",
      "function": {
        "name": "search_books",
        "arguments": "{\"keyword\":\"Python\"}"
      }
    }
  ]
}
```

---

## 📚 相关文档

- [n8n官方文档](https://docs.n8n.io/)
- [DeepSeek API文档](https://api-docs.deepseek.com/)
- [MCP服务器文档](./使用指南.md)

---

**版本**: 1.0  
**最后更新**: 2024-01-01

