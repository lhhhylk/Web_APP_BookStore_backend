# n8n 详细使用指南

## 📋 目录

1. [n8n简介](#n8n简介)
2. [安装n8n](#安装n8n)
3. [导入工作流](#导入工作流)
4. [配置凭证](#配置凭证)
5. [配置节点](#配置节点)
6. [激活和测试](#激活和测试)
7. [节点详细说明](#节点详细说明)
8. [常见问题](#常见问题)

---

## 🎯 n8n简介

n8n是一个开源的工作流自动化工具，类似于Zapier，但可以自托管。它允许你通过可视化界面创建复杂的工作流，连接不同的服务和API。

### 为什么使用n8n？

- **可视化界面**: 通过拖拽节点创建工作流，无需编写代码
- **强大的集成**: 支持数百种服务和API
- **自托管**: 可以部署在自己的服务器上
- **开源免费**: 完全免费使用
- **灵活扩展**: 可以创建自定义节点

---

## 💻 安装n8n

### 方式1: 使用Docker（推荐）

#### Windows

1. **安装Docker Desktop**
   - 下载: https://www.docker.com/products/docker-desktop
   - 安装并启动Docker Desktop

2. **运行n8n容器**
   ```powershell
   docker run -it --rm --name n8n -p 5678:5678 -v ${HOME}/.n8n:/home/node/.n8n n8nio/n8n
   ```

   或者使用PowerShell的路径格式：
   ```powershell
   docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" n8nio/n8n
   ```

3. **访问n8n**
   - 打开浏览器访问: `http://localhost:5678`
   - 首次访问会要求创建账号

#### Linux/Mac

1. **安装Docker**（如果还没安装）
   ```bash
   # Ubuntu/Debian
   sudo apt-get update
   sudo apt-get install docker.io
   
   # Mac (使用Homebrew)
   brew install docker
   ```

2. **运行n8n容器**
   ```bash
   docker run -it --rm --name n8n -p 5678:5678 -v ~/.n8n:/home/node/.n8n n8nio/n8n
   ```

3. **访问n8n**
   - 打开浏览器访问: `http://localhost:5678`

### 方式2: 使用npm

#### 前提条件
- 安装Node.js (版本 >= 16.0.0)
- 安装npm

#### 安装步骤

1. **全局安装n8n**
   ```bash
   npm install n8n -g
   ```

2. **启动n8n**
   ```bash
   n8n start
   ```

3. **访问n8n**
   - 打开浏览器访问: `http://localhost:5678`

### 方式3: 使用npx（无需安装）

```bash
npx n8n
```

---

## 📥 导入工作流

### 步骤1: 打开n8n界面

1. 访问 `http://localhost:5678`
2. 登录你的账号（首次使用需要注册）

### 步骤2: 导入工作流文件

1. **点击左侧菜单的 "Workflows"**
   - 或者直接访问: `http://localhost:5678/workflow`

2. **点击右上角的 "Import from File" 按钮**
   - 图标是一个文件夹加箭头的图标
   - 或者使用快捷键 `Ctrl+I` (Windows) / `Cmd+I` (Mac)

3. **选择工作流文件**
   - 浏览到 `bookstore_Agent_Server` 目录
   - 选择 `n8n_workflow_v2.json` 文件
   - 点击 "打开"

4. **工作流导入成功**
   - 工作流会显示在工作流列表中
   - 点击工作流名称打开它

### 步骤3: 查看工作流

导入后，你会看到完整的工作流，包括：
- 所有节点
- 节点之间的连接
- 节点的配置

---

## 🔐 配置凭证

### 创建DeepSeek API凭证

n8n工作流需要DeepSeek API Key来调用DeepSeek API。

#### 步骤1: 获取DeepSeek API Key

1. 访问 [DeepSeek官网](https://www.deepseek.com/)
2. 注册账号并登录
3. 进入API管理页面
4. 创建新的API Key
5. 复制API Key（格式类似: `sk-xxxxxxxxxxxxx`）

#### 步骤2: 在n8n中创建凭证

1. **打开凭证管理**
   - 点击左侧菜单的 **"Credentials"**
   - 或访问: `http://localhost:5678/credentials`

2. **创建新凭证**
   - 点击右上角的 **"Add Credential"** 按钮
   - 或点击 **"Create New Credential"**

3. **选择凭证类型**
   - 在搜索框中输入: `HTTP Header Auth`
   - 选择 **"HTTP Header Auth"** 凭证类型

4. **配置凭证**
   - **Credential Name**: `DeepSeek API Header Auth`
   - **Header Name**: `Authorization`
   - **Header Value**: `Bearer YOUR_DEEPSEEK_API_KEY`
     - 将 `YOUR_DEEPSEEK_API_KEY` 替换为你的实际API Key
     - 例如: `Bearer sk-dc265e3...d34a9`

5. **保存凭证**
   - 点击 **"Save"** 按钮
   - 凭证会出现在凭证列表中

#### 步骤3: 在工作流中使用凭证

1. **打开工作流**
   - 点击工作流名称打开

2. **配置 "调用DeepSeek API" 节点**
   - 点击 **"调用DeepSeek API"** 节点
   - 在右侧配置面板中，找到 **"Authentication"** 部分
   - 选择 **"Generic Credential Type"**
   - 在 **"Credential Type"** 下拉菜单中选择 **"HTTP Header Auth"**
   - 在 **"Credential"** 下拉菜单中选择刚才创建的 **"DeepSeek API Header Auth"**

3. **配置 "生成最终回复" 节点**
   - 同样配置 **"生成最终回复"** 节点
   - 使用相同的凭证

---

## ⚙️ 配置节点

### 节点配置概览

工作流包含以下主要节点，每个节点都需要正确配置：

1. **Webhook Trigger** - 接收请求
2. **准备消息** - 处理消息
3. **调用DeepSeek API** - 第一次AI调用
4. **解析响应** - 解析AI响应
5. **检查工具调用** - 条件判断
6. **提取工具调用** - 提取工具信息
7. **调用MCP工具** - 执行工具
8. **合并工具结果** - 合并结果
9. **生成最终回复** - 最终AI调用
10. **返回响应** - 返回结果

### 详细配置说明

#### 1. Webhook Trigger节点

**位置**: 工作流第一个节点

**配置步骤**:
1. 点击 **"Webhook Trigger"** 节点
2. 在右侧配置面板中：
   - **HTTP Method**: 选择 `POST`
   - **Path**: 输入 `chat`
   - **Response Mode**: 选择 `responseNode`

**重要**: 
- 工作流激活后，这个节点会显示一个Webhook URL
- 格式类似: `http://localhost:5678/webhook/chat`
- 复制这个URL，前端会使用它

#### 2. 准备消息节点

**位置**: 第二个节点

**配置检查**:
1. 点击 **"准备消息"** 节点
2. 确认代码编辑器中有代码
3. 代码应该包含：
   - 解析请求体的逻辑
   - 添加系统提示的逻辑

**代码说明**:
- 这个节点使用JavaScript代码
- 它会解析前端发送的消息
- 添加系统提示消息，定义AI助手的角色

#### 3. 调用DeepSeek API节点

**位置**: 第三个节点

**配置步骤**:
1. 点击 **"调用DeepSeek API"** 节点
2. **基本配置**:
   - **Method**: `POST`
   - **URL**: `https://api.deepseek.com/v1/chat/completions`

3. **认证配置**:
   - **Authentication**: 选择 `Generic Credential Type`
   - **Credential Type**: 选择 `HTTP Header Auth`
   - **Credential**: 选择 `DeepSeek API Header Auth`

4. **Headers配置**:
   - 确保有 `Content-Type: application/json`

5. **Body配置**:
   - **Specify Body**: 选择 `JSON`
   - **JSON Body**: 应该包含：
     ```json
     {
       "model": "deepseek-chat",
       "messages": {{ $json.messages }},
       "temperature": 0.7,
       "tools": [...]
     }
     ```

**重要**: 
- `{{ $json.messages }}` 是n8n的表达式语法
- 它会从上一个节点的输出中获取messages字段
- tools数组定义了4个MCP工具

#### 4. 解析响应节点

**位置**: 第四个节点

**配置检查**:
1. 点击 **"解析响应"** 节点
2. 确认代码编辑器中有代码
3. 代码会：
   - 解析DeepSeek API的响应
   - 提取消息内容和工具调用信息
   - 判断是否有工具调用

#### 5. 检查工具调用节点

**位置**: 第五个节点

**配置检查**:
1. 点击 **"检查工具调用"** 节点
2. 这是一个IF条件节点
3. 条件应该是：
   - **Condition**: `has_tool_calls`
   - **Value**: `true`

**功能**:
- 如果有工具调用 → 进入工具调用流程
- 如果没有工具调用 → 直接返回

#### 6. 提取工具调用节点

**位置**: 工具调用分支的第一个节点

**配置检查**:
1. 点击 **"提取工具调用"** 节点
2. 确认代码编辑器中有代码
3. 代码会：
   - 从AI响应中提取所有工具调用
   - 为每个工具调用创建单独的数据项

#### 7. 调用MCP工具节点

**位置**: 工具调用分支的第二个节点

**配置步骤**:
1. 点击 **"调用MCP工具"** 节点
2. **基本配置**:
   - **Method**: `POST`
   - **URL**: `http://localhost:8000/mcp-tool`
     - **重要**: 确保这个地址正确
     - 如果MCP服务运行在不同地址，需要修改

3. **Body配置**:
   - **Send Body**: 勾选
   - **Body Parameters**:
     - `tool_name`: `={{ $json.tool_name }}`
     - `arguments`: `={{ JSON.stringify($json.tool_arguments) }}`

**重要**: 
- 这个节点会调用Python Agent服务的 `/mcp-tool` 端点
- 确保Python Agent服务正在运行

#### 8. 合并工具结果节点

**位置**: 工具调用分支的第三个节点

**配置检查**:
1. 点击 **"合并工具结果"** 节点
2. 确认代码编辑器中有代码
3. 代码会：
   - 收集所有工具调用的结果
   - 按照OpenAI格式构建消息
   - 合并到消息历史中

#### 9. 生成最终回复节点

**位置**: 工具调用分支的第四个节点

**配置步骤**:
1. 点击 **"生成最终回复"** 节点
2. **基本配置**:
   - **Method**: `POST`
   - **URL**: `https://api.deepseek.com/v1/chat/completions`

3. **认证配置**:
   - 使用与"调用DeepSeek API"节点相同的凭证

4. **Body配置**:
   - **Specify Body**: 选择 `JSON`
   - **JSON Body**: 
     ```json
     {
       "model": "deepseek-chat",
       "messages": {{ $json.messages }},
       "temperature": 0.7
     }
     ```

**功能**:
- 使用包含工具结果的完整消息历史
- 生成最终的用户友好回复

#### 10. 返回响应节点

**位置**: 工作流最后一个节点

**配置步骤**:
1. 点击 **"返回响应"** 节点
2. **配置**:
   - **Respond With**: 选择 `JSON`
   - **Response Body**: `={{ $json }}`

**功能**:
- 将最终结果返回给前端
- 响应格式: `{"message": "AI的回复"}`

---

## 🚀 激活和测试

### 步骤1: 确保MCP服务运行

在激活n8n工作流之前，确保Python Agent服务正在运行：

```bash
cd bookstore_Agent_Server
python app.py
```

服务应该在 `http://localhost:8000` 运行。

### 步骤2: 激活工作流

1. **打开工作流**
   - 在n8n中打开导入的工作流

2. **激活工作流**
   - 点击工作流右上角的 **"Active"** 开关
   - 开关会变为绿色，表示工作流已激活

3. **获取Webhook URL**
   - 点击 **"Webhook Trigger"** 节点
   - 在节点详情中，你会看到 **"Webhook URL"**
   - 格式: `http://localhost:5678/webhook/chat`
   - **复制这个URL**

### 步骤3: 更新前端配置

修改前端代码，使用n8n的Webhook URL：

**文件**: `bookstore_frontend/src/components/chatbot.jsx`

找到这一行：
```javascript
const AGENT_API_URL = 'http://localhost:8000';
```

修改为：
```javascript
const AGENT_API_URL = 'http://localhost:5678/webhook';  // n8n的Webhook基础URL
```

**注意**: 
- n8n会自动添加 `/chat` 路径
- 所以只需要设置基础URL

### 步骤4: 测试工作流

#### 方法1: 在n8n中测试

1. **执行单个节点**
   - 点击任意节点
   - 点击 **"Execute Node"** 按钮
   - 查看节点的输出数据

2. **执行整个工作流**
   - 点击工作流右上角的 **"Execute Workflow"** 按钮
   - 查看执行结果

#### 方法2: 使用curl测试

```bash
curl -X POST http://localhost:5678/webhook/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "你好"}
    ]
  }'
```

#### 方法3: 在前端测试

1. 启动前端服务
2. 登录系统
3. 进入聊天界面
4. 发送测试消息

### 步骤5: 查看执行记录

1. **打开执行记录**
   - 点击左侧菜单的 **"Executions"**
   - 或访问: `http://localhost:5678/executions`

2. **查看执行详情**
   - 点击任意执行记录
   - 查看每个节点的执行状态
   - 点击节点查看输入输出数据

---

## 📊 节点详细说明

### 数据流转

工作流中的数据流转过程：

```
1. Webhook Trigger
   ↓ 接收HTTP请求
   { messages: [...] }

2. 准备消息
   ↓ 添加系统提示
   { messages: [system, ...user_messages] }

3. 调用DeepSeek API
   ↓ 第一次AI调用
   { choices: [{ message: { content: "...", tool_calls: [...] } }] }

4. 解析响应
   ↓ 解析AI响应
   { content: "...", tool_calls: [...], has_tool_calls: true/false }

5. 检查工具调用
   ↓ 条件判断
   ├─→ 有工具调用 → 工具调用流程
   └─→ 无工具调用 → 直接返回

6. 提取工具调用 (如果有工具调用)
   ↓ 提取工具信息
   [{ tool_name: "...", tool_arguments: {...} }]

7. 调用MCP工具
   ↓ 执行工具
   { success: true, data: [...] }

8. 合并工具结果
   ↓ 合并到消息历史
   { messages: [..., tool_call, tool_result] }

9. 生成最终回复
   ↓ 第二次AI调用
   { choices: [{ message: { content: "..." } }] }

10. 返回响应
    ↓ 返回给前端
    { message: "..." }
```

### 表达式语法

n8n使用 `{{ }}` 语法来引用数据：

- `{{ $json.field }}` - 引用当前节点的JSON数据
- `{{ $('节点名称').item.json.field }}` - 引用其他节点的数据
- `{{ JSON.stringify($json) }}` - 将对象转换为JSON字符串

### 节点类型说明

1. **Webhook Trigger**: 接收HTTP请求
2. **Code**: 执行JavaScript代码
3. **HTTP Request**: 发送HTTP请求
4. **IF**: 条件判断
5. **Respond to Webhook**: 返回HTTP响应

---

## 🔧 常见问题

### 问题1: 工作流无法激活

**可能原因**:
- 节点配置不完整
- 凭证未配置
- 有错误节点

**解决方案**:
1. 检查所有节点是否有红色错误标记
2. 确保所有必需的配置都已填写
3. 检查凭证是否正确配置

### 问题2: Webhook无法访问

**可能原因**:
- 工作流未激活
- 端口被占用
- 网络配置问题

**解决方案**:
1. 确保工作流已激活（开关是绿色的）
2. 检查端口5678是否被占用
3. 如果使用Docker，检查端口映射

### 问题3: DeepSeek API调用失败

**可能原因**:
- API Key错误
- 凭证未正确关联
- 网络问题

**解决方案**:
1. 检查API Key是否正确
2. 确认凭证已正确关联到节点
3. 检查网络连接

### 问题4: MCP工具调用失败

**可能原因**:
- MCP服务未运行
- URL配置错误
- 服务地址不正确

**解决方案**:
1. 确保Python Agent服务正在运行
2. 检查"调用MCP工具"节点的URL配置
3. 测试MCP服务端点是否可访问

### 问题5: 数据格式错误

**可能原因**:
- 表达式语法错误
- 数据字段不匹配
- JSON格式错误

**解决方案**:
1. 检查表达式语法
2. 查看节点的输入输出数据
3. 使用"Execute Node"功能测试单个节点

---

## 📝 调试技巧

### 1. 使用Execute Node功能

- 点击任意节点
- 点击 **"Execute Node"** 按钮
- 查看节点的输出数据
- 验证数据格式是否正确

### 2. 查看执行记录

- 在Executions页面查看执行记录
- 点击执行记录查看详细信息
- 查看每个节点的输入输出

### 3. 添加调试节点

可以在工作流中添加临时节点来查看数据：

1. 添加 **"Code"** 节点
2. 在代码中输出数据：
   ```javascript
   console.log($input.all());
   return $input.all();
   ```

### 4. 检查日志

n8n会在控制台输出日志，查看日志了解执行过程。

---

## 🎓 最佳实践

1. **测试单个节点**: 在配置完整工作流之前，先测试单个节点
2. **使用描述性名称**: 给节点起有意义的名称
3. **添加注释**: 在Code节点中添加注释说明逻辑
4. **版本控制**: 定期导出工作流JSON文件作为备份
5. **错误处理**: 在关键节点添加错误处理逻辑

---

## 📚 相关资源

- [n8n官方文档](https://docs.n8n.io/)
- [n8n表达式文档](https://docs.n8n.io/code/expressions/)
- [n8n社区论坛](https://community.n8n.io/)

---

**完成以上步骤后，你的n8n工作流就可以正常工作了！**

