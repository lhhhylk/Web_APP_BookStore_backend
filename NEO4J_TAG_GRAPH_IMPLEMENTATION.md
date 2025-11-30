# Neo4j标签图搜索功能实现说明

## 功能概述

本功能实现了基于Neo4j图数据库的标签关系图，并提供了基于标签图的智能搜索功能。当用户按照标签搜索时，系统会：
1. 在Neo4j中查找与用户选中标签通过最多2次边连接可以关联到的所有标签
2. 在MySQL中搜索所有带有这些标签中任意一个或多个的图书
3. 将搜索结果呈现给用户

## 实现架构

### 1. 数据存储层

#### MySQL（现有）
- 存储图书基本信息
- 图书的`tags`字段存储为`List<String>`

#### Neo4j（新增）
- 存储标签节点（Tag Node）
- 存储标签之间的层次关系（SUBCATEGORY_OF关系）
- 例如：`外国文学 -[SUBCATEGORY_OF]-> 文学`

### 2. 核心组件

#### 2.1 标签实体（TagNode）
**位置**: `src/main/java/com/bookstore/bookstore_backend/model/tag/TagNode.java`

- 使用`@Node("Tag")`注解标记为Neo4j节点
- `name`字段作为唯一标识（@Id）
- `parentTags`关系表示父标签（更通用的分类）

#### 2.2 标签Repository（TagNodeRepository）
**位置**: `src/main/java/com/bookstore/bookstore_backend/repository/TagNodeRepository.java`

提供以下查询方法：
- `findRelatedTagsWithinTwoHops(String tagName)`: 查找与指定标签通过最多2跳关联的所有标签
- `findRelatedTagsWithinTwoHopsForMultiple(List<String> tagNames)`: 查找与多个标签通过最多2跳关联的所有标签

**查询逻辑**：
- 查询两个方向的关系（因为Neo4j关系是单向的）
- 使用`SUBCATEGORY_OF*1..2`匹配1到2跳的关系
- 包括标签本身

#### 2.3 标签服务（TagService）
**位置**: `src/main/java/com/bookstore/bookstore_backend/services/Impl/TagService.java`

**核心方法**：

1. **buildTagGraph()**: 构建标签图
   - 从MySQL获取所有标签
   - 分析标签之间的层次关系（基于字符串包含关系）
   - 将标签和关系保存到Neo4j
   - 关系构建策略：如果标签A是标签B的子串，且B更长，则A是B的父标签
     - 例如："文学" -> "外国文学" -> "拉丁美洲文学"

2. **findRelatedTags(String tagName)**: 查找关联标签
   - 调用Repository查询与指定标签通过2跳关联的所有标签
   - 返回标签名称集合（包括自身）

3. **findRelatedTags(List<String> tagNames)**: 查找多个标签的关联标签
   - 支持多个标签同时查询
   - 返回所有关联标签的并集

4. **clearAllTags()**: 清空所有标签节点和关系

#### 2.4 图书服务扩展（BookService）
**位置**: `src/main/java/com/bookstore/bookstore_backend/services/Impl/BookService.java`

**新增方法**：

1. **getBooksByTagGraph(String keyword, String tag, Pageable pageable)**: 
   - 根据单个标签进行标签图搜索
   - 流程：
     a. 调用`tagService.findRelatedTags(tag)`获取关联标签
     b. 调用`bookRepository.findBooksByKeywordAndTagsWithPaginationAndNotDeleted()`在MySQL中搜索
     c. 加载完整图书信息（库存、封面、描述等）

2. **getBooksByTagGraph(String keyword, List<String> tags, Pageable pageable)**: 
   - 根据多个标签进行标签图搜索

3. **countBooksByTagGraph()**: 统计搜索结果数量

#### 2.5 图书Repository扩展（BookRepository）
**位置**: `src/main/java/com/bookstore/bookstore_backend/repository/BookRepository.java`

**新增查询方法**：
- `findBooksByKeywordAndTagsWithPaginationAndNotDeleted()`: 根据关键词和多个标签搜索（标签之间是OR关系）
- `findBooksByKeywordAndTagsAndNotDeleted()`: 不分页版本
- `countBooksByKeywordAndTagsAndNotDeleted()`: 统计方法

#### 2.6 控制器扩展（BookController）
**位置**: `src/main/java/com/bookstore/bookstore_backend/controller/BookController.java`

**新增接口**：

1. **GET /books/search-by-tag-graph**
   - 参数：
     - `keyword` (可选): 关键词
     - `tag` (可选): 单个标签
     - `pageIndex` (默认0): 页码
     - `pageSize` (默认5): 每页大小
   - 返回：图书列表和总数

2. **GET /books/search-by-tag-graph-multiple**
   - 参数：
     - `keyword` (可选): 关键词
     - `tags` (可选): 多个标签（逗号分隔）
     - `pageIndex` (默认0): 页码
     - `pageSize` (默认5): 每页大小
   - 返回：图书列表和总数

3. **POST /books/build-tag-graph**
   - 构建标签图
   - 返回：操作结果

4. **DELETE /books/clear-tag-graph**
   - 清空标签图
   - 返回：操作结果

## 配置说明

### 1. Maven依赖（pom.xml）
已添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
```

### 2. 应用配置（application.yml）
已添加Neo4j配置：
```yaml
spring:
  data:
    neo4j:
      uri: bolt://localhost:7687
      authentication:
        username: neo4j
        password: neo4j123
```

**注意**：请根据实际Neo4j服务器配置修改用户名和密码。

## 使用流程

### 1. 初始化标签图

首次使用前，需要构建标签图：

```bash
POST http://localhost:8082/books/build-tag-graph
```

这个操作会：
1. 从MySQL中获取所有图书的标签
2. 分析标签之间的层次关系
3. 将标签节点和关系保存到Neo4j

### 2. 使用标签图搜索

#### 单个标签搜索：
```bash
GET http://localhost:8082/books/search-by-tag-graph?tag=文学&pageIndex=0&pageSize=10
```

#### 多个标签搜索：
```bash
GET http://localhost:8082/books/search-by-tag-graph-multiple?tags=文学,小说&pageIndex=0&pageSize=10
```

#### 带关键词的标签搜索：
```bash
GET http://localhost:8082/books/search-by-tag-graph?keyword=孤独&tag=文学&pageIndex=0&pageSize=10
```

### 3. 搜索示例

假设标签图结构如下：
```
文学
  ├─ 外国文学
  │   ├─ 拉丁美洲文学
  │   └─ 欧洲文学
  └─ 中国文学
      └─ 当代文学
```

当用户搜索标签"外国文学"时：
1. Neo4j查询会找到：
   - "外国文学"（自身）
   - "文学"（父标签，1跳）
   - "拉丁美洲文学"、"欧洲文学"（子标签，1跳）
   - 其他通过2跳可以到达的标签

2. MySQL搜索会查找所有带有这些标签中任意一个的图书

3. 返回搜索结果

## 关系构建策略（已优化）

优化后的实现采用**多策略组合**的方式构建标签图：

### 策略1：分类标签的层次关系构建

1. **标签分类识别**：
   - 自动识别**分类标签**（如"中国文学"、"外国文学"、"当代文学"等）
   - 识别**主题标签**（如"现实主义"、"爱情故事"、"家族史"等）
   - 分类标签识别规则：
     - 包含"文学"、"小说"、"散文"等关键词
     - 包含地域关键词（"中国"、"外国"、"日本"、"英国"等）
     - 包含时代关键词（"古代"、"古典"、"现代"、"当代"等）

2. **层次关系构建**：
   - 基于字符串包含关系，但更智能：
     - 只有当较短标签是较长标签的**前缀或后缀**时才建立关系
     - 避免错误关系（如"魔幻现实主义"不会以"现实主义"为父）
     - 选择最合适的父标签（最长的符合条件的父标签）
   - 顶层标签处理：
     - "文学"作为根节点
     - 没有找到合适父标签的分类标签会自动连接到"文学"

3. **示例关系**：
   ```
   文学
     ├─ 中国文学
     │   ├─ 当代文学
     │   ├─ 现代文学
     │   └─ 古典文学
     └─ 外国文学
         ├─ 日本文学
         ├─ 英国文学
         └─ 拉丁美洲文学
   ```

### 策略2：主题标签与分类标签的关联构建

1. **基于共现关系**：
   - 统计每个主题标签与分类标签在同一本书中出现的次数
   - 为主题标签关联最多3个最相关的分类标签（按共现次数排序）

2. **示例关系**：
   - "现实主义" 经常与 "中国文学"、"当代文学" 一起出现
   - "魔幻现实主义" 经常与 "外国文学"、"拉丁美洲文学" 一起出现
   - 建立关系：`现实主义 -[SUBCATEGORY_OF]-> 中国文学`
   - 建立关系：`魔幻现实主义 -[SUBCATEGORY_OF]-> 外国文学`

### 优化效果

**优点**：
- ✅ 更准确地识别标签层次关系
- ✅ 避免创建错误的关系
- ✅ 基于实际数据（共现关系）建立主题-分类关联
- ✅ 自动识别分类标签和主题标签
- ✅ 支持多层级标签结构

**改进点**：
- 从简单的字符串包含关系升级为多策略组合
- 增加了标签共现统计分析
- 更智能的父标签选择算法
- 主题标签与分类标签的智能关联

## 注意事项

1. **Neo4j服务器**：确保Neo4j服务器已启动并可以访问
2. **标签图构建**：在首次使用或标签数据更新后，需要重新构建标签图
3. **性能考虑**：标签图查询通常很快，但大量标签的关系构建可能需要一些时间
4. **数据一致性**：如果MySQL中的标签数据更新，需要重新构建标签图以保持一致性

## 技术栈

- **Spring Boot 3.3.11**
- **Spring Data Neo4j**
- **Neo4j数据库**
- **MySQL数据库**（现有）
- **JPA/Hibernate**（现有）

## 文件清单

新增文件：
- `src/main/java/com/bookstore/bookstore_backend/model/tag/TagNode.java`
- `src/main/java/com/bookstore/bookstore_backend/repository/TagNodeRepository.java`
- `src/main/java/com/bookstore/bookstore_backend/services/ITagService.java`
- `src/main/java/com/bookstore/bookstore_backend/services/Impl/TagService.java`

修改文件：
- `pom.xml` - 添加Neo4j依赖
- `src/main/resources/application.yml` - 添加Neo4j配置
- `src/main/java/com/bookstore/bookstore_backend/repository/BookRepository.java` - 添加多标签搜索方法
- `src/main/java/com/bookstore/bookstore_backend/services/IBookService.java` - 添加标签图搜索接口
- `src/main/java/com/bookstore/bookstore_backend/services/Impl/BookService.java` - 实现标签图搜索
- `src/main/java/com/bookstore/bookstore_backend/controller/BookController.java` - 添加标签图搜索接口

