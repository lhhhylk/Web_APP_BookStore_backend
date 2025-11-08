# 微服务架构中Gateway和Service Registry的作用

## 一、概述

本文档以E-BookStore系统的微服务实现为例，详细解释**Service Registry（服务注册中心）**和**API Gateway（API网关）**在微服务架构中的作用，并说明服务的部署与使用方式。

## 二、我们的微服务架构

### 2.1 系统组件

我们实现的微服务架构包含以下组件：

1. **Eureka Server（服务注册中心）** - 端口8761
   - 提供服务注册与发现功能
   - 管理所有微服务的路由信息

2. **Author Service（作者查询微服务）** - 端口8083
   - 独立的微服务
   - 提供根据书名查询作者的功能
   - 接口：`GET /api/author?title={书名}`

3. **Bookstore Backend（主系统）** - 端口8082
   - 主业务系统
   - 通过Feign Client调用Author Service
   - 提供作者搜索API：`GET /books/search-author?title={书名}`

4. **API Gateway（可选，将在下文说明）** - 端口8080
   - 统一入口，路由所有外部请求
   - 提供负载均衡、限流、认证等功能

### 2.2 架构图

#### 当前架构（无Gateway）

```
┌─────────────────────────────────────────────────────────┐
│                     前端应用                              │
└───────────────┬─────────────────────────────────────────┘
                │
                │ HTTP请求
                │
        ┌───────▼────────┐
        │ Bookstore      │  (8082)
        │ Backend        │  ──────┐
        │                │        │
        └───────┬────────┘        │ Feign Client
                │                 │
                │                 │
        ┌───────▼────────┐        │
        │   Eureka       │        │
        │   Server       │◄───────┘
        │   (8761)       │
        └───────┬────────┘
                │
                │ 服务发现
                │
        ┌───────▼────────┐
        │   Author       │
        │   Service      │
        │   (8083)       │
        └────────────────┘
```

#### 理想架构（有Gateway）

```
┌─────────────────────────────────────────────────────────┐
│                     前端应用                              │
└───────────────┬─────────────────────────────────────────┘
                │
                │ 所有请求统一通过Gateway
                │
        ┌───────▼────────┐
        │  API Gateway   │  (8080)
        │  (Spring Cloud │
        │    Gateway)    │
        └───────┬────────┘
                │
        ┌───────┴────────┐
        │                │
        │                │
┌───────▼────────┐ ┌────▼─────────┐
│ Bookstore      │ │   Author     │
│ Backend        │ │   Service    │
│ (8082)         │ │   (8083)     │
└───────┬────────┘ └──────────────┘
        │
        │ 内部调用
        │
┌───────▼────────┐
│   Eureka       │
│   Server       │
│   (8761)       │
└────────────────┘
```

## 三、Service Registry（服务注册中心）的作用

### 3.1 什么是Service Registry？

**Service Registry（服务注册中心）**是微服务架构中的核心组件，它负责维护所有微服务实例的注册信息和健康状态。在我们的系统中，**Eureka Server**扮演了这个角色。

### 3.2 Service Registry的核心功能

#### 1. 服务注册（Service Registration）

**作用**：微服务启动时，向注册中心注册自己的服务信息。

**在我们的系统中**：
- Author Service启动时，向Eureka Server注册
- Bookstore Backend启动时，向Eureka Server注册
- 注册信息包括：服务名称、IP地址、端口号、健康状态等

**示例**：
```yaml
# Author Service注册到Eureka
服务名称: author-service
实例ID: author-service:8083
IP地址: localhost
端口: 8083
状态: UP
```

#### 2. 服务发现（Service Discovery）

**作用**：服务之间需要相互调用时，通过服务名称从注册中心获取目标服务的实际地址。

**在我们的系统中**：
- Bookstore Backend需要调用Author Service时
- 通过Feign Client，使用服务名称`author-service`进行调用
- Feign Client会从Eureka Server查询`author-service`的实际地址（localhost:8083）
- 然后发起HTTP请求

```

#### 3. 健康检查（Health Check）

**作用**：定期检查注册服务的健康状态，自动移除不健康的服务实例。

**在我们的系统中**：
- Eureka Server每隔一定时间（默认30秒）向注册的服务发送心跳检测
- 如果服务无响应，将其标记为DOWN
- 其他服务在调用时，会自动跳过DOWN状态的服务

#### 4. 负载均衡（Load Balancing）

**作用**：当一个服务有多个实例时，自动进行负载均衡。

**在我们的系统中**：
- 如果Author Service有多个实例（如8083和8084）
- Eureka会维护所有实例的列表
- Feign Client会自动在多个实例之间进行负载均衡
- 默认使用轮询（Round Robin）策略

**示例场景**：
```
Author Service实例1: localhost:8083 (状态: UP)
Author Service实例2: localhost:8084 (状态: UP)

当Bookstore Backend调用时：
第1次请求 → 8083
第2次请求 → 8084
第3次请求 → 8083
...
```


### 3.4 在我们的系统中的实际应用

#### 服务注册流程

```
1. Author Service启动
   ↓
2. 读取配置文件，获取Eureka Server地址
   ↓
3. 向Eureka Server发送注册请求
   ↓
4. Eureka Server保存注册信息
   ↓
5. Author Service定期发送心跳（每30秒）
```

#### 服务发现流程

```
1. Bookstore Backend需要调用Author Service
   ↓
2. Feign Client从Eureka Server查询服务列表
   ↓
3. Eureka Server返回服务实例列表
   ↓
4. Feign Client选择实例（如果有多个，进行负载均衡）
   ↓
5. 发起HTTP请求
```

## 四、API Gateway（API网关）的作用

### 4.1 什么是API Gateway？

**API Gateway（API网关）**是微服务架构中的统一入口，所有外部请求都通过网关进入系统。在我们的系统中，可以使用**Spring Cloud Gateway**来实现。

### 4.2 API Gateway的核心功能

#### 1. 统一入口（Single Entry Point）

**作用**：为所有微服务提供统一的访问入口。

**无Gateway的情况**：
```
前端需要知道所有服务的地址：
- Bookstore Backend: http://localhost:8082
- Author Service: http://localhost:8083
- 其他服务: http://localhost:8084, 8085, ...

前端代码：
const bookApi = "http://localhost:8082/books";
const authorApi = "http://localhost:8083/api/author";
```

**有Gateway的情况**：
```
所有请求都通过Gateway：
- Gateway: http://localhost:8080

前端代码：
const apiBase = "http://localhost:8080";
const bookApi = `${apiBase}/books`;
const authorApi = `${apiBase}/author`;
```

#### 2. 路由转发（Routing）

**作用**：根据请求路径，将请求转发到对应的微服务。

**请求流程**：
```
1. 前端请求: GET http://localhost:8080/books/search-author?title=测试
   ↓
2. Gateway匹配路由规则: Path=/books/**
   ↓
3. Gateway从Eureka查询 community-service 的地址
   ↓
4. Gateway转发请求: GET http://localhost:8082/books/search-author?title=测试
   ↓
5. Bookstore Backend处理请求
   ↓
6. Bookstore Backend内部调用Author Service（通过Feign）
   ↓
7. 返回结果给Gateway
   ↓
8. Gateway返回给前端
```

#### 3. 负载均衡（Load Balancing）

**作用**：在多个服务实例之间进行负载均衡。

**示例**：
```
Author Service有两个实例：
- author-service:8083
- author-service:8084

Gateway配置：
uri: lb://author-service  # lb表示负载均衡

当请求到达Gateway时：
第1次请求 → author-service:8083
第2次请求 → author-service:8084
第3次请求 → author-service:8083
...
```

#### 4. 认证和授权（Authentication & Authorization）

**作用**：在网关层面统一处理认证和授权，避免在每个微服务中重复实现。

#### 5. 限流（Rate Limiting）

**作用**：限制API的访问频率，防止系统过载。

#### 6. 熔断（Circuit Breaker）

**作用**：当服务不可用时，快速失败，避免级联故障。


#### 7. 请求日志和监控（Logging & Monitoring）

**作用**：统一记录所有请求日志，便于监控和问题排查。

### 4.4 Gateway vs Service Registry的区别

| 特性 | Service Registry (Eureka) | API Gateway (Spring Cloud Gateway) |
|------|--------------------------|-----------------------------------|
| **主要作用** | 服务发现和注册 | 统一入口和路由 |
| **使用者** | 服务之间 | 外部客户端（前端） |
| **功能** | 服务注册、发现、健康检查 | 路由、认证、限流、监控 |
| **位置** | 内网 | 公网入口 |
| **通信方式** | 服务间直接调用 | 客户端→Gateway→服务 |

**关系**：
- Gateway通常也注册到Service Registry
- Gateway通过Service Registry发现后端服务
- Gateway和服务都使用Service Registry进行服务发现

## 七、部署与使用方式

### 7.1 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**（数据库：bookstore2）
- **Redis**（可选，用于Gateway限流和缓存）



### 7.5 启动服务

#### 启动顺序

1. **启动MySQL数据库**
   ```bash
   # 确保MySQL运行，数据库bookstore2已创建
   ```

2. **启动Eureka Server**
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   **验证**：访问 http://localhost:8761

3. **启动Author Service**
   ```bash
   cd author-service
   mvn spring-boot:run
   ```
   **验证**：
   - 在Eureka控制台看到`author-service`已注册
   - 测试API：`http://localhost:8083/api/author?title=测试书名`

4. **启动Bookstore Backend**
   ```bash
   # 在项目根目录
   mvn spring-boot:run
   ```
   **验证**：
   - 在Eureka控制台看到`community-service`已注册
   - 测试API：`http://localhost:8082/books/search-author?title=测试书名`

### 7.6 API测试

#### 测试Author Service

```bash
# 直接调用Author Service
curl "http://localhost:8083/api/author?title=测试书名"

# 响应
{
  "title": "测试书名",
  "author": "作者名",
  "message": "查询成功",
  "success": true
}
```

#### 测试Bookstore Backend（通过Feign调用Author Service）

```bash
# 通过Bookstore Backend调用
curl "http://localhost:8082/books/search-author?title=测试书名"

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "title": "测试书名",
    "author": "作者名",
    "message": "查询成功",
    "success": true
  },
  "total": 0
}
```

#### 测试Eureka Server

```bash
# 访问Eureka控制台
open http://localhost:8761

# 查看注册的服务
curl http://localhost:8761/eureka/apps
```


函数式服务与无状态
无状态含义
函数式服务把一次调用当成“输入 → 输出”的纯计算：请求里包含价格和数量，响应是行小计。服务不保存用户、订单或库存等上下文信息；每次计算完全依赖请求数据。
服务器端不会把任何会影响下一次调用的状态存进内存或本地磁盘，不存在“会话粘性”。即使连续请求来自同一订单，服务也不必记住上一次结果。
出现故障或重试时，只需重新提交同样的输入，就能获得同样的输出；无需担心内部状态已经被破坏或部分更新。
无状态带来的扩展优势
水平扩展简单：任何一个实例都能独立处理任意请求，扩容时只需在容器平台或集群里复制更多实例，前端负载均衡随机分配即可。
自动化伸缩友好：因为实例之间没有共享会话/缓存，平台可以按 CPU、请求量扩缩容，不会出现“新实例必须同步历史数据”的冷启动问题。
故障隔离容易：某个实例崩溃不会导致会话丢失，流量自动转移到其他实例即可；也方便滚动发布。
缓存/CDN 效率高：纯粹按输入输出计算的结果可以安全地在上游缓存（例如 API Gateway、边缘代理），减轻后台压力。
在本例中，价格计算服务无状态，使得我们只需保证每个实例包含相同的逻辑即可，既能轻松复制扩容，也能通过服务发现/负载均衡随时扩展吞吐量，满足高并发场景。