# n8n Docker 安装问题解决

## 🔍 问题分析

错误信息显示Docker尝试从 `docker.xuanyuan.me` 拉取镜像失败，这是Docker镜像源配置的问题。

## 🛠️ 解决方案

### 方案1: 使用官方Docker Hub（推荐）

直接使用官方源拉取镜像：

```powershell
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" n8nio/n8n:latest
```

如果还是失败，尝试指定完整镜像地址：

```powershell
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" docker.io/n8nio/n8n:latest
```

### 方案2: 先拉取镜像再运行

分两步执行：

```powershell
# 步骤1: 先拉取镜像
docker pull n8nio/n8n:latest

# 步骤2: 运行容器
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" n8nio/n8n:latest
```

### 方案3: 修改Docker镜像源配置

#### Windows (Docker Desktop)

1. **打开Docker Desktop**
2. **进入设置**:
   - 点击右上角的设置图标（齿轮）
   - 或右键系统托盘中的Docker图标 → Settings

3. **配置镜像源**:
   - 点击左侧 **"Docker Engine"**
   - 在JSON配置中添加或修改 `registry-mirrors`:

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```

4. **应用并重启**:
   - 点击 **"Apply & Restart"**
   - 等待Docker重启

5. **重新运行命令**:
```powershell
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" n8nio/n8n:latest
```

#### Linux

编辑 `/etc/docker/daemon.json`:

```bash
sudo nano /etc/docker/daemon.json
```

添加内容：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```

重启Docker服务：

```bash
sudo systemctl restart docker
```

### 方案4: 使用npm安装（无需Docker）

如果Docker问题无法解决，可以直接使用npm安装：

#### 前提条件
- 安装Node.js (版本 >= 16.0.0)
- 安装npm

#### 安装步骤

```powershell
# 全局安装n8n
npm install n8n -g

# 启动n8n
n8n start
```

或者使用npx（无需安装）：

```powershell
npx n8n
```

### 方案5: 使用国内镜像源拉取

如果必须使用镜像源，可以尝试：

```powershell
# 使用阿里云镜像源
docker pull registry.cn-hangzhou.aliyuncs.com/acs/n8n:latest

# 然后运行（需要修改镜像名称）
docker run -it --rm --name n8n -p 5678:5678 -v "$env:USERPROFILE\.n8n:/home/node/.n8n" registry.cn-hangzhou.aliyuncs.com/acs/n8n:latest
```

**注意**: 这个镜像可能不是最新的，建议使用官方源。

## 🔧 推荐的镜像源配置

### 国内可用的Docker镜像源

1. **中科大镜像**:
   ```
   https://docker.mirrors.ustc.edu.cn
   ```

2. **网易镜像**:
   ```
   https://hub-mirror.c.163.com
   ```

3. **阿里云镜像**（需要注册）:
   ```
   https://your-id.mirror.aliyuncs.com
   ```

4. **腾讯云镜像**:
   ```
   https://mirror.ccs.tencentyun.com
   ```

### 完整配置示例

**Windows Docker Desktop配置** (`Docker Engine` 部分):

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

## ✅ 验证Docker配置

配置完成后，验证镜像源是否生效：

```powershell
# 查看Docker信息
docker info

# 应该能看到 registry-mirrors 配置
```

## 🚀 快速解决步骤（推荐）

### 最简单的方法：使用npm

如果Docker配置复杂，直接使用npm：

```powershell
# 1. 检查Node.js版本（需要 >= 16.0.0）
node --version

# 2. 全局安装n8n
npm install n8n -g

# 3. 启动n8n
n8n start
```

然后访问 `http://localhost:5678`

### 或者修复Docker配置

1. **打开Docker Desktop设置**
2. **Docker Engine** → 添加镜像源配置
3. **Apply & Restart**
4. **重新运行Docker命令**

## 📝 注意事项

1. **镜像源稳定性**: 国内镜像源可能不稳定，建议配置多个镜像源
2. **版本更新**: 使用镜像源可能无法获取最新版本
3. **网络问题**: 如果网络正常，建议直接使用官方源
4. **防火墙**: 确保防火墙允许Docker访问网络

## 🔍 故障排除

### 问题: 修改配置后仍无法拉取

**解决**:
1. 检查Docker是否已重启
2. 尝试清除Docker缓存: `docker system prune -a`
3. 检查网络连接
4. 尝试使用VPN或代理

### 问题: npm安装失败

**解决**:
1. 检查Node.js版本
2. 使用国内npm镜像源:
   ```powershell
   npm config set registry https://registry.npmmirror.com
   ```
3. 清除npm缓存: `npm cache clean --force`

---

**推荐**: 如果Docker配置复杂，直接使用npm安装是最简单的方法！

