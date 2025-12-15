# n8n 工作流截图说明文档

## 📸 需要截图的节点和配置

本文档说明在n8n中需要截图的关键节点和配置细节，用于作业提交。

---

## 1. 工作流全貌截图

### 截图要求
- 显示整个工作流画布
- 所有节点都清晰可见
- 节点之间的连接线清晰
- 标注主要流程路径

### 截图步骤
1. 在n8n中打开工作流
2. 使用浏览器缩放功能（Ctrl + 鼠标滚轮）调整视图，使所有节点可见
3. 使用截图工具（如Windows的Snipping Tool）截图整个画布
4. 在截图上标注：
   - 流程入口（Webhook Trigger）
   - 主要处理流程
   - 条件分支点
   - 流程出口

### 标注说明
```
[Webhook Trigger] 
    ↓
[准备消息] 
    ↓
[调用DeepSeek API] 
    ↓
[解析响应] 
    ↓
[检查工具调用] 
    ├─→ [提取工具调用] → [调用MCP工具] → [合并工具结果] → [生成最终回复] → [提取最终回复] → [返回响应]
    └─→ [直接返回] → [返回响应]
```

---

## 2. Webhook Trigger 节点配置截图

### 需要显示的配置
1. **HTTP Method**: POST
2. **Path**: chat
3. **Response Mode**: responseNode
4. **Webhook URL**: 显示完整的Webhook URL

### 截图步骤
1. 点击 **"Webhook Trigger"** 节点
2. 确保节点配置面板完全展开
3. 截图整个配置面板
4. 特别标注：
   - HTTP Method设置
   - Path设置
   - Webhook URL（激活工作流后显示）

### 配置说明文字
```
节点类型: Webhook Trigger
功能: 接收来自前端的HTTP POST请求
配置:
- HTTP Method: POST
- Path: chat
- Response Mode: responseNode（使用响应节点返回结果）
- Webhook URL: http://your-n8n-instance.com/webhook/chat
```

---

## 3. 准备消息节点配置截图

### 需要显示的配置
1. 代码编辑器的完整代码
2. 代码的主要逻辑说明

### 截图步骤
1. 点击 **"准备消息"** 节点
2. 展开代码编辑器
3. 确保代码完整可见
4. 截图代码编辑器

### 配置说明文字
```
节点类型: Code (JavaScript)
功能: 解析请求并添加系统提示消息
主要逻辑:
1. 解析前端发送的消息列表
2. 构建系统提示消息，定义AI助手的角色和能力
3. 将系统消息添加到消息列表的开头
4. 返回处理后的消息列表
```

---

## 4. 调用DeepSeek API节点配置截图

### 需要显示的配置
1. **Method**: POST
2. **URL**: https://api.deepseek.com/v1/chat/completions
3. **Authentication**: Header Auth（显示凭证名称）
4. **Headers**: Content-Type: application/json
5. **Body**: JSON格式的请求体（包括model、messages、tools等）

### 截图步骤
1. 点击 **"调用DeepSeek API"** 节点
2. 展开配置面板
3. 分别截图：
   - 基本配置（Method、URL）
   - 认证配置
   - Headers配置
   - Body配置（JSON格式，包括tools定义）

### 配置说明文字
```
节点类型: HTTP Request
功能: 调用DeepSeek API进行AI对话
配置:
- Method: POST
- URL: https://api.deepseek.com/v1/chat/completions
- Authentication: Header Auth（使用Bearer Token）
- Headers: Content-Type: application/json
- Body: 
  - model: deepseek-chat
  - messages: 来自"准备消息"节点的消息列表
  - temperature: 0.7
  - tools: 4个MCP工具定义（search_books, get_book_by_id, get_all_tags, get_books_by_sales）
```

### Tools定义截图
特别需要截图Tools部分的配置，显示：
- 每个工具的名称
- 工具的描述
- 工具的参数定义

---

## 5. 解析响应节点配置截图

### 需要显示的配置
1. 代码编辑器的完整代码
2. 代码逻辑说明

### 截图步骤
1. 点击 **"解析响应"** 节点
2. 展开代码编辑器
3. 截图代码

### 配置说明文字
```
节点类型: Code (JavaScript)
功能: 解析DeepSeek API的响应
主要逻辑:
1. 提取assistant的消息内容
2. 提取工具调用信息（如果有）
3. 判断是否有工具调用
4. 返回解析后的数据，包括content、tool_calls、has_tool_calls等字段
```

---

## 6. 检查工具调用节点配置截图

### 需要显示的配置
1. 条件判断逻辑
2. 条件表达式

### 截图步骤
1. 点击 **"检查工具调用"** 节点
2. 展开条件配置面板
3. 截图条件设置

### 配置说明文字
```
节点类型: IF (条件判断)
功能: 判断是否需要调用工具
条件:
- 检查 has_tool_calls 字段是否为 true
- True分支: 进入工具调用流程
- False分支: 直接返回AI的回复
```

---

## 7. 提取工具调用节点配置截图

### 需要显示的配置
1. 代码编辑器的完整代码

### 截图步骤
1. 点击 **"提取工具调用"** 节点
2. 展开代码编辑器
3. 截图代码

### 配置说明文字
```
节点类型: Code (JavaScript)
功能: 从AI响应中提取所有工具调用
主要逻辑:
1. 遍历所有工具调用
2. 解析每个工具调用的名称和参数
3. 为每个工具调用创建一个单独的数据项
4. 支持并行处理多个工具调用
```

---

## 8. 调用MCP工具节点配置截图

### 需要显示的配置
1. **Method**: POST
2. **URL**: http://localhost:8000/mcp-tool
3. **Body Parameters**: tool_name 和 arguments

### 截图步骤
1. 点击 **"调用MCP工具"** 节点
2. 展开配置面板
3. 分别截图：
   - 基本配置（Method、URL）
   - Body Parameters配置

### 配置说明文字
```
节点类型: HTTP Request
功能: 调用MCP工具服务执行工具
配置:
- Method: POST
- URL: http://localhost:8000/mcp-tool
- Body Parameters:
  - tool_name: 工具名称（来自"提取工具调用"节点）
  - arguments: 工具参数（JSON字符串格式）
```

---

## 9. 合并工具结果节点配置截图

### 需要显示的配置
1. 代码编辑器的完整代码

### 截图步骤
1. 点击 **"合并工具结果"** 节点
2. 展开代码编辑器
3. 截图代码

### 配置说明文字
```
节点类型: Code (JavaScript)
功能: 合并所有工具调用的结果
主要逻辑:
1. 收集所有工具调用的结果
2. 按照OpenAI工具调用格式构建消息
3. 将工具结果添加到消息历史中
4. 准备发送给AI生成最终回复
```

---

## 10. 生成最终回复节点配置截图

### 需要显示的配置
1. **Method**: POST
2. **URL**: https://api.deepseek.com/v1/chat/completions
3. **Body**: JSON格式（包含完整的消息历史）

### 截图步骤
1. 点击 **"生成最终回复"** 节点
2. 展开配置面板
3. 截图Body配置（显示消息历史结构）

### 配置说明文字
```
节点类型: HTTP Request
功能: 使用包含工具结果的完整消息历史生成最终回复
配置:
- Method: POST
- URL: https://api.deepseek.com/v1/chat/completions
- Authentication: Header Auth（与第一次调用相同）
- Body:
  - model: deepseek-chat
  - messages: 包含工具调用和工具结果的完整消息历史
  - temperature: 0.7
```

---

## 11. 返回响应节点配置截图

### 需要显示的配置
1. **Respond With**: json
2. **Response Body**: JSON格式的响应体

### 截图步骤
1. 点击 **"返回响应"** 节点
2. 展开配置面板
3. 截图Response Body配置

### 配置说明文字
```
节点类型: Respond to Webhook
功能: 将最终结果返回给前端
配置:
- Respond With: json
- Response Body: 
  {
    "message": "AI的回复内容"
  }
```

---

## 12. 执行记录截图

### 需要显示的配置
1. 一次完整的执行记录
2. 各个节点的执行状态
3. 关键节点的输入输出数据

### 截图步骤
1. 在n8n中，点击左侧菜单的 **"Executions"**
2. 选择一次成功的执行记录
3. 截图执行记录的整体视图
4. 点击几个关键节点，截图其输入输出数据：
   - "调用DeepSeek API"节点的输出
   - "调用MCP工具"节点的输入输出
   - "生成最终回复"节点的输出
   - "返回响应"节点的最终输出

### 配置说明文字
```
执行记录说明:
1. 显示工作流的完整执行流程
2. 各个节点的执行状态（成功/失败）
3. 执行时间线
4. 关键节点的输入输出数据，验证数据流转正确
```

---

## 📝 截图整理建议

### 截图文件命名
- `01_工作流全貌.png`
- `02_Webhook_Trigger节点.png`
- `03_准备消息节点.png`
- `04_调用DeepSeek_API节点.png`
- `05_解析响应节点.png`
- `06_检查工具调用节点.png`
- `07_提取工具调用节点.png`
- `08_调用MCP工具节点.png`
- `09_合并工具结果节点.png`
- `10_生成最终回复节点.png`
- `11_返回响应节点.png`
- `12_执行记录.png`

### 截图质量要求
- 清晰度：至少1920x1080分辨率
- 可读性：所有文字清晰可见
- 完整性：配置面板完全展开
- 标注：在截图上添加必要的标注和说明

### 文档组织
将所有截图整理到一个文件夹中，并创建一个说明文档，按照上述顺序组织截图，每个截图配以相应的说明文字。

---

## 🎯 作业提交清单

- [ ] n8n_workflow.json 文件
- [ ] n8n_workflow_说明.md 文档
- [ ] 工作流全貌截图
- [ ] 各个关键节点的配置截图（至少8-10个节点）
- [ ] 执行记录截图
- [ ] 截图说明文档（本文件）

---

**完成以上截图后，你的作业就完整了！**

