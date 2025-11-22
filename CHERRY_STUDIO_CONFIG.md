# Cherry Studio配置指南

## 快速配置步骤

### 1. 在Cherry Studio中添加MCP服务器

1. 打开Cherry Studio
2. 点击左下角的**设置**（齿轮图标）
3. 选择**MCP服务器**（MCP Servers）
4. 点击**添加服务器**（Add Server）或**+**按钮

### 2. 填写服务器配置信息

#### 方式1：使用Python直接运行（推荐）

**服务器名称：**
```
bookstore-mcp
```

**命令：**
```
python
```

**参数：**
```
C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server\server.py
```
（请根据你的实际路径修改）

**工作目录：**
```
C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server
```

**环境变量（可选，如果已在系统环境变量中设置则不需要）：**
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bookstore2
DB_USER=root
DB_PASSWORD=lhylhylhy050802
```

#### 方式2：使用完整Python路径

如果系统PATH中没有python，可以使用完整路径：

**命令：**
```
C:\Python\python.exe
```
（替换为你的Python实际安装路径）

**参数：**
```
C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server\server.py
```

### 3. 保存并启用

1. 点击**保存**（Save）
2. 确保服务器旁边的开关是**启用**状态
3. 重启Cherry Studio（如果需要）

### 4. 验证配置

在Cherry Studio的对话中，尝试以下命令：

- "帮我找一下关于Python的图书"
- "查找所有标签"
- "显示销量前10的图书"
- "获取ID为1的图书详情"

如果AI助手能够使用这些工具并返回结果，说明配置成功！

## 故障排除

### 问题：服务器无法启动

**检查项：**
1. Python是否正确安装？运行 `python --version` 检查
2. 依赖是否安装？运行 `pip install -r requirements.txt`
3. 路径是否正确？确保server.py的路径存在
4. 数据库是否运行？确保MySQL服务已启动

### 问题：无法连接到数据库

**检查项：**
1. MySQL服务是否正在运行
2. 数据库配置信息是否正确（host, port, database, user, password）
3. 数据库用户是否有足够的权限
4. 防火墙是否阻止了连接

### 问题：找不到模块

**解决方案：**
```bash
pip install mcp mysql-connector-python
```

### 问题：路径中的空格或特殊字符

如果路径中包含空格或特殊字符，请使用引号：
```
"C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server\server.py"
```

## 配置示例（JSON格式）

如果你需要手动编辑配置文件，可以参考以下JSON格式：

```json
{
  "mcpServers": {
    "bookstore-mcp": {
      "command": "python",
      "args": [
        "C:\\Users\\96317\\Desktop\\homework\\DA_ER\\Web APP\\bookstore_backend\\mcp-server\\server.py"
      ],
      "cwd": "C:\\Users\\96317\\Desktop\\homework\\DA_ER\\Web APP\\bookstore_backend\\mcp-server",
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

**注意：** Windows路径中的反斜杠需要转义为双反斜杠。

## 高级配置

### 使用虚拟环境

如果你使用Python虚拟环境：

**命令：**
```
C:\path\to\venv\Scripts\python.exe
```

### 使用uv工具（如果已安装）

**命令：**
```
uv
```

**参数：**
```
run python C:\Users\96317\Desktop\homework\DA_ER\Web APP\bookstore_backend\mcp-server\server.py
```

## 测试服务器

在配置完成后，你可以在Cherry Studio中测试：

1. 创建一个新对话
2. 输入："列出所有可用的图书标签"
3. 如果AI助手能够调用工具并返回标签列表，说明配置成功

## 支持

如果遇到问题，请检查：
- Cherry Studio的日志文件
- Python的错误输出
- 数据库连接状态
- 环境变量设置

