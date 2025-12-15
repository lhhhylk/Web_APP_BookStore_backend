# n8n 快速入门指南

## 🚀 5分钟快速开始

### 步骤1: 启动n8n（2分钟）

#### 方式1: 使用npm（推荐，最简单）

**Windows/Linux/Mac:**
```powershell
# 检查Node.js版本（需要 >= 16.0.0）
node --version

# 全局安装n8n
npm install n8n -g

# 启动n8n
n8n start
```

等待看到 `n8n ready on 0.0.0.0, version X.X.X` 表示启动成功。

#### 方式2: 使用Docker

**Windows PowerShell:**
```powershell
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" n8nio/n8n:latest
```

**Linux/Mac:**
```bash
docker run -it --rm --name n8n -p 5678:5678 -v ~/.n8n:/home/node/.n8n n8nio/n8n:latest
```

**如果Docker拉取镜像失败，请参考 `n8n_Docker问题解决.md`**

#### 方式3: 使用npx（无需安装）

```bash
npx n8n
```

**访问**: 打开浏览器访问 `http://localhost:5678`

---

### 步骤2: 导入工作流（1分钟）

1. 在n8n界面中，点击左侧 **"Workflows"**
2. 点击右上角 **"Import from File"** 按钮（或按 `Ctrl+I`）
3. 选择 `bookstore_Agent_Server/n8n_workflow_v2.json` 文件
4. 工作流导入成功！

---

### 步骤3: 配置DeepSeek API凭证（1分钟）

1. **创建凭证**:
   - 点击左侧 **"Credentials"**
   - 点击 **"Add Credential"**
   - 搜索并选择 **"HTTP Header Auth"**
   - 填写：
     - **Name**: `DeepSeek API Header Auth`
     - **Header Name**: `Authorization`
     - **Header Value**: `Bearer YOUR_API_KEY`（替换为你的DeepSeek API Key）
   - 点击 **"Save"**

2. **在工作流中使用凭证**:
   - 打开导入的工作流
   - 点击 **"调用DeepSeek API"** 节点
   - 在 **"Authentication"** 部分：
     - 选择 **"Generic Credential Type"**
     - **Credential Type**: 选择 **"HTTP Header Auth"**
     - **Credential**: 选择 **"DeepSeek API Header Auth"**
   - 同样配置 **"生成最终回复"** 节点

---

### 步骤4: 确保MCP服务运行（30秒）

打开新的终端窗口：

```bash
cd bookstore_Agent_Server
python app.py
```

确保服务在 `http://localhost:8000` 运行。

---

### 步骤5: 激活工作流（30秒）

1. 在n8n中，打开工作流
2. 点击右上角的 **"Active"** 开关（变为绿色）
3. 点击 **"Webhook Trigger"** 节点
4. 复制显示的 **Webhook URL**（类似: `http://localhost:5678/webhook/chat`）

---

### 步骤6: 更新前端配置（30秒）

修改 `bookstore_frontend/src/components/chatbot.jsx`:

```javascript
// 修改这一行
const AGENT_API_URL = 'http://localhost:5678/webhook';  // n8n的Webhook基础URL
```

**注意**: n8n会自动添加 `/chat` 路径。

---

### 步骤7: 测试（30秒）

1. 启动前端: `cd bookstore_frontend && npm start`
2. 登录系统
3. 点击 **"智能助手"**
4. 发送消息: "你好"

---

## 📸 关键配置截图位置

### 1. 凭证配置
- **位置**: Credentials → HTTP Header Auth
- **关键**: Header Value必须是 `Bearer YOUR_API_KEY` 格式

### 2. 节点凭证关联
- **位置**: "调用DeepSeek API" 和 "生成最终回复" 节点
- **关键**: 选择刚才创建的凭证

### 3. MCP工具URL
- **位置**: "调用MCP工具" 节点
- **关键**: URL必须是 `http://localhost:8000/mcp-tool`

### 4. Webhook URL
- **位置**: "Webhook Trigger" 节点（激活后显示）
- **关键**: 复制这个URL用于前端配置

---

## 🔍 验证清单

- [ ] n8n服务正在运行（访问 http://localhost:5678）
- [ ] 工作流已导入
- [ ] DeepSeek API凭证已创建并关联
- [ ] MCP服务正在运行（http://localhost:8000）
- [ ] 工作流已激活（开关是绿色的）
- [ ] 前端已更新API URL
- [ ] 可以发送消息并收到回复

---

## ⚠️ 常见问题快速解决

### 问题: n8n无法启动

**解决**: 
- 确保Docker已安装并运行
- 或确保Node.js版本 >= 16.0.0

### 问题: 凭证配置后仍报错

**解决**:
- 检查Header Value格式: 必须是 `Bearer YOUR_API_KEY`
- 确保API Key有效
- 重新关联凭证到节点

### 问题: MCP工具调用失败

**解决**:
- 确保Python Agent服务正在运行
- 检查"调用MCP工具"节点的URL是否正确
- 测试: `curl http://localhost:8000/mcp-tool`

### 问题: Webhook无法访问

**解决**:
- 确保工作流已激活
- 检查Webhook URL是否正确
- 确保前端使用正确的URL

---

## 📚 详细文档

如果需要更详细的说明，请查看：
- `n8n_详细使用指南.md` - 完整的使用指南
- `n8n_配置指南.md` - 配置步骤详解
- `n8n_截图说明.md` - 截图要求

---

**按照以上步骤，5分钟内就可以让n8n工作流运行起来！**

