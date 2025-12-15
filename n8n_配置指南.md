# n8n 工作流配置指南

## 📋 快速开始

### 步骤1: 安装n8n

#### 使用Docker（推荐）

```bash
docker run -it --rm \
  --name n8n \
  -p 5678:5678 \
  -v ~/.n8n:/home/node/.n8n \
  n8nio/n8n
```

#### 使用npm

```bash
npm install n8n -g
n8n start
```

访问 `http://localhost:5678` 打开n8n界面。

### 步骤2: 导入工作流

1. 在n8n界面中，点击左侧菜单的 **"Workflows"**
2. 点击右上角的 **"Import from File"** 按钮
3. 选择 `n8n_workflow.json` 文件
4. 工作流将被导入并显示在列表中

### 步骤3: 配置DeepSeek API凭证

1. 点击左侧菜单的 **"Credentials"**
2. 点击 **"Add Credential"**
3. 搜索并选择 **"DeepSeek API"**（如果没有，可能需要安装相关节点包）
4. 填写以下信息：
   - **Name**: `DeepSeek API Key`
   - **API Key**: 输入你的DeepSeek API Key
5. 点击 **"Save"**

### 步骤4: 在工作流中配置凭证

1. 打开导入的工作流
2. 点击 **"DeepSeek AI Agent"** 节点
3. 在 **"Credential"** 下拉菜单中选择刚才创建的凭证
4. 同样配置 **"生成最终回复"** 节点

### 步骤5: 配置MCP工具服务地址

1. 点击 **"调用MCP工具"** 节点
2. 检查 **"URL"** 字段：
   - 默认: `http://localhost:8000/mcp-tool`
   - 如果MCP服务运行在不同地址，修改此URL

### 步骤6: 激活工作流

1. 点击工作流右上角的 **"Active"** 开关
2. 工作流将变为激活状态（绿色）

### 步骤7: 获取Webhook URL

1. 点击 **"Webhook Trigger"** 节点
2. 在节点详情中，复制显示的 **"Webhook URL"**
3. 格式类似: `http://your-n8n-instance.com/webhook/chat`

### 步骤8: 更新前端配置

修改前端代码中的Agent API URL：

**文件**: `bookstore_frontend/src/components/chatbot.jsx`

```javascript
// 修改这一行
const AGENT_API_URL = 'http://your-n8n-instance.com/webhook';  // 使用n8n的Webhook URL
```

---

## 🔧 节点配置详解

### Webhook Trigger节点

**配置位置**: 工作流第一个节点

**关键设置**:
- **HTTP Method**: `POST`
- **Path**: `chat`
- **Response Mode**: `responseNode`

**获取URL**:
- 激活工作流后，点击节点查看Webhook URL
- URL格式: `http://your-n8n-instance.com/webhook/chat`

---

### DeepSeek AI Agent节点

**配置位置**: 工作流第三个节点

**关键设置**:
- **Model**: `deepseek-chat`
- **Temperature**: `0.7`
- **Tools**: 4个工具定义（已在JSON中配置）

**凭证配置**:
- 选择之前创建的DeepSeek API凭证

**工具定义**:
工具定义已在工作流JSON中配置，包括：
- `search_books`
- `get_book_by_id`
- `get_all_tags`
- `get_books_by_sales`

---

### 调用MCP工具节点

**配置位置**: 工具调用分支中的HTTP Request节点

**关键设置**:
- **Method**: `POST`
- **URL**: `http://localhost:8000/mcp-tool`
- **Body Parameters**:
  - `tool_name`: `={{ $json.tool_name }}`
  - `arguments`: `={{ JSON.stringify($json.tool_arguments) }}`

**注意**: 
- 如果MCP服务运行在不同地址，需要修改URL
- 确保MCP服务已实现 `/mcp-tool` 端点

---

## 🛠️ MCP工具服务端点

如果使用现有的Python Agent服务，需要添加一个MCP工具调用端点：

**文件**: `bookstore_Agent_Server/app.py`

添加以下端点：

```python
@app.post("/mcp-tool")
async def mcp_tool(request: dict):
    """MCP工具调用端点（供n8n使用）"""
    tool_name = request.get("tool_name")
    arguments = request.get("arguments", {})
    
    if isinstance(arguments, str):
        arguments = json.loads(arguments)
    
    result = await call_mcp_tool(tool_name, arguments)
    return {"result": result}
```

---

## 📸 截图说明

### 1. 工作流全貌截图

在n8n界面中：
1. 打开工作流
2. 使用浏览器的缩放功能调整视图
3. 截图整个工作流画布

**截图要点**:
- 显示所有节点
- 显示节点之间的连接关系
- 显示条件分支的流向

### 2. 各个节点配置截图

为每个关键节点截图：

#### Webhook Trigger节点
- 显示HTTP Method和Path配置
- 显示Webhook URL

#### DeepSeek AI Agent节点
- 显示Model配置
- 显示Tools配置（展开一个工具定义）
- 显示凭证选择

#### 检查工具调用节点
- 显示条件判断逻辑

#### 调用MCP工具节点
- 显示URL配置
- 显示Body Parameters配置

#### 返回响应节点
- 显示Response Body配置

---

## 🧪 测试工作流

### 1. 测试Webhook

使用curl或Postman测试：

```bash
curl -X POST http://your-n8n-instance.com/webhook/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "你好"}
    ]
  }'
```

### 2. 查看执行日志

1. 在n8n界面中，点击左侧菜单的 **"Executions"**
2. 查看最近的工作流执行记录
3. 点击执行记录查看详细信息
4. 可以查看每个节点的输入输出数据

### 3. 调试节点

1. 点击任意节点
2. 点击 **"Execute Node"** 按钮
3. 查看节点的输出数据
4. 检查数据格式是否正确

---

## ⚠️ 常见问题

### 问题1: DeepSeek API节点不可用

**解决方案**:
- 确保安装了 `@n8n/n8n-nodes-langchain` 节点包
- 或者使用HTTP Request节点直接调用DeepSeek API

### 问题2: 工具调用失败

**解决方案**:
- 检查MCP服务是否运行
- 检查MCP工具端点URL是否正确
- 查看MCP服务的日志

### 问题3: Webhook无法访问

**解决方案**:
- 确保工作流已激活
- 检查n8n的网络配置
- 如果使用Docker，确保端口映射正确

### 问题4: 凭证配置错误

**解决方案**:
- 检查API Key是否正确
- 确保凭证已正确关联到节点
- 重新创建凭证并重新配置节点

---

## 📝 作业提交要求

### 需要提交的文件

1. **n8n_workflow.json** - 工作流JSON文件
2. **n8n_workflow_说明.md** - 详细的流程说明文档
3. **截图文件**:
   - 工作流全貌截图
   - 各个关键节点的配置截图

### 截图要求

1. **工作流全貌**:
   - 清晰显示所有节点
   - 显示节点连接关系
   - 标注主要流程路径

2. **节点配置细节**:
   - Webhook Trigger节点配置
   - DeepSeek AI Agent节点配置（包括工具定义）
   - 条件判断节点配置
   - MCP工具调用节点配置
   - 响应节点配置

3. **执行示例**:
   - 一次完整的执行记录
   - 显示各个节点的输入输出数据

---

## 🎓 作业说明要点

在说明文档中，需要包含：

1. **流程架构说明**:
   - 整体流程设计思路
   - 各个节点的作用
   - 数据流向

2. **节点配置说明**:
   - 每个节点的关键配置参数
   - 配置的原因和目的
   - 参数值的设置依据

3. **工具集成说明**:
   - 如何将MCP服务集成到工作流中
   - 工具调用的流程
   - 工具结果的处理方式

4. **技术实现说明**:
   - 使用的技术栈
   - 关键技术点的实现方式
   - 遇到的问题和解决方案

---

**完成以上步骤后，你的n8n工作流就可以正常工作了！**

