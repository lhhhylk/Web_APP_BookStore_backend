# n8n 工作流使用说明

## 📋 概述

本文档说明如何使用n8n工作流来实现E-BookStore聊天机器人Agent。

## 🎯 两种实现方式

### 方式1: 使用n8n工作流（推荐用于作业提交）

- **文件**: `n8n_workflow_v2.json`
- **特点**: 使用标准n8n节点，易于理解和配置
- **适用**: 作业提交、演示、学习

### 方式2: 使用Python FastAPI服务（当前运行方式）

- **文件**: `app.py`
- **特点**: 性能更好，更灵活
- **适用**: 生产环境

## 📦 文件说明

### n8n工作流文件

1. **n8n_workflow.json** - 使用AI Agent节点的版本（需要langchain节点包）
2. **n8n_workflow_v2.json** - 使用标准HTTP Request节点的版本（推荐）

### 说明文档

1. **n8n_workflow_说明.md** - 详细的流程和节点说明
2. **n8n_配置指南.md** - 配置步骤指南
3. **n8n_截图说明.md** - 截图要求和说明

## 🚀 快速开始

### 1. 导入工作流

1. 打开n8n界面
2. 点击 "Workflows" → "Import from File"
3. 选择 `n8n_workflow_v2.json`
4. 工作流将被导入

### 2. 配置DeepSeek API凭证

1. 在n8n中创建HTTP Header Auth凭证：
   - **Name**: `DeepSeek API Header Auth`
   - **Header Name**: `Authorization`
   - **Header Value**: `Bearer YOUR_DEEPSEEK_API_KEY`

2. 在工作流的以下节点中选择此凭证：
   - "调用DeepSeek API" 节点
   - "生成最终回复" 节点

### 3. 确保MCP工具服务运行

确保Python Agent服务正在运行（提供 `/mcp-tool` 端点）：

```bash
cd bookstore_Agent_Server
python app.py
```

服务将在 `http://localhost:8000` 启动。

### 4. 激活工作流

1. 点击工作流右上角的 "Active" 开关
2. 工作流将开始监听Webhook请求

### 5. 获取Webhook URL

1. 点击 "Webhook Trigger" 节点
2. 复制显示的Webhook URL
3. 格式: `http://your-n8n-instance.com/webhook/chat`

### 6. 更新前端配置

修改前端代码中的Agent API URL为n8n的Webhook URL。

## 📸 作业提交要求

### 需要提交的文件

1. ✅ **n8n_workflow_v2.json** - 工作流JSON文件
2. ✅ **n8n_workflow_说明.md** - 详细的流程说明
3. ✅ **截图文件** - 按照 `n8n_截图说明.md` 的要求截图

### 截图清单

- [ ] 工作流全貌截图
- [ ] Webhook Trigger节点配置
- [ ] 准备消息节点配置
- [ ] 调用DeepSeek API节点配置（包括Tools定义）
- [ ] 解析响应节点配置
- [ ] 检查工具调用节点配置
- [ ] 提取工具调用节点配置
- [ ] 调用MCP工具节点配置
- [ ] 合并工具结果节点配置
- [ ] 生成最终回复节点配置
- [ ] 返回响应节点配置
- [ ] 执行记录截图

## 🔧 技术说明

### 工作流架构

```
Webhook Trigger → 准备消息 → 调用DeepSeek API → 解析响应 → 检查工具调用
                                                              ├─→ 提取工具调用 → 调用MCP工具 → 合并工具结果 → 生成最终回复 → 返回响应
                                                              └─→ 直接返回 → 返回响应
```

### 关键节点说明

1. **Webhook Trigger**: 接收前端请求
2. **准备消息**: 添加系统提示
3. **调用DeepSeek API**: 第一次调用，可能返回工具调用
4. **解析响应**: 解析AI响应，判断是否需要工具
5. **检查工具调用**: 条件判断
6. **提取工具调用**: 提取工具调用信息
7. **调用MCP工具**: 执行工具调用
8. **合并工具结果**: 合并工具结果到消息历史
9. **生成最终回复**: 使用完整消息历史生成最终回复
10. **返回响应**: 返回结果给前端

### MCP工具集成

工作流通过HTTP请求调用MCP工具服务：
- **端点**: `http://localhost:8000/mcp-tool`
- **方法**: POST
- **请求体**: 
  ```json
  {
    "tool_name": "search_books",
    "arguments": {"keyword": "Python"}
  }
  ```

## 📝 注意事项

1. **API Key安全**: 使用n8n的凭证管理，不要硬编码
2. **MCP服务地址**: 确保MCP工具服务地址正确
3. **网络连接**: 确保n8n可以访问DeepSeek API和MCP服务
4. **错误处理**: 建议在关键节点添加错误处理

## 🎓 作业说明要点

在说明文档中，需要包含：

1. **流程架构说明**
   - 整体设计思路
   - 各个节点的作用
   - 数据流向

2. **节点配置说明**
   - 每个节点的关键配置参数
   - 配置的原因和目的
   - 参数值的设置依据

3. **工具集成说明**
   - 如何将MCP服务集成到工作流中
   - 工具调用的流程
   - 工具结果的处理方式

4. **技术实现说明**
   - 使用的技术栈
   - 关键技术点的实现方式
   - 遇到的问题和解决方案

---

**完成以上步骤后，你的n8n工作流就可以正常工作了！**

