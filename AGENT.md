# IrisRPC AGENT.MD

## 项目概览

**IrisRPC** 是一个基于 MQTT 协议的轻量级 RPC 框架，专为物联网 (IoT) 场景设计。灵感来自希腊神话中彩虹女神 Iris，通过 MQTT 实现跨网络的设备与服务间通信。

### 核心特性
- ✅ 基于 MQTT 协议的 RPC 调用
- ✅ Spring Boot Starter 自动配置
- ✅ MCP (Model Context Protocol) 支持
- ✅ 服务发现与动态注册
- ✅ 支持保留消息机制

---

## 🚀 OpenSpec 开发模式

本项目已启用 **OpenSpec (opsx)** 开发模式，使用规范驱动的工作流。

### 可用命令

| 命令 | 用途 | 使用场景 |
|------|------|---------|
| `/opsx:propose` | 创建变更提案 | 开始一个新功能或修复 |
| `/opsx:apply` | 实施变更 | 开始实现已规划的任务 |
| `/opsx:explore` | 探索模式 | 思路探索、问题调研、需求澄清 |
| `/opsx:archive` | 归档变更 | 完成后归档变更 |

### 推荐工作流

```
┌─────────────────────────────────────────────────────────────┐
│  1. 探索想法   →   /opsx:explore                             │
│        ↓                                                     │
│  2. 提出提案   →   /opsx:propose "修复 RPC 超时问题"        │
│        ↓                                                     │
│  3. 实施变更   →   /opsx:apply                               │
│        ↓                                                     │
│  4. 归档完成   →   /opsx:archive                             │
└─────────────────────────────────────────────────────────────┘
```

### 建议优先处理的变更

根据代码审查，建议首先处理：

1. **修复 RPC 调用无超时问题** → 可能导致线程永久阻塞
2. **修复 RPCCall 内存泄漏** → 长时间运行导致内存增长
3. **修复并发安全问题** → HashMap 在多线程下的数据损坏

---

## 代码库架构

### 模块结构

```
IrisRPC/
├── Iris-core/                          # 核心 RPC 实现
│   ├── common/                        # 公共组件
│   │   ├── annotation/                # 注解定义
│   │   ├── constant/            # 常量定义
│   │   ├── exception/          # 异常类
│   │   ├── msg/               # 消息模型
│   │   └── serializer/       # JSON 序列化
│   ├── config/                      # 配置类
│   ├── listener/                    # 监听器
│   ├── mqtt/                        # MQTT 客户端
│   ├── client/                      # 客户端代理
│   └── server/                      # 服务端处理器
│
├── Iris-spring-boot-starter/           # Spring Boot 集成
│   ├── annotation/                    # Enable 注解
│   ├── IrisProperties.java       # 配置属性
│   ├── IrisReferenceBeanPostProcessor.java  # Bean 后置处理器
│   ├── RequestProcessorGenerator.java     # 请求处理器生成
│   ├── IrisMCPToolRegister.java       # MCP 工具注册
│   └── IrisSpringBootStarterConfiguration.java  # 自动配置
│
└── Iris-example/                       # 使用示例
    ├── example-common/                 # 公共 API 接口
    ├── example-server/               # 纯 Java 服务端
    ├── example-client/               # 纯 Java 客户端
    ├── example-server-spring/     # Spring Boot 服务端
    ├── example-client-spring/     # Spring Boot 客户端
    └── example-mcp-client-spring/ # MCP 客户端示例
```

### 核心流程

**服务端启动流程：
1. `@EnableIrisServer` → 导入 `RequestProcessorGenerator`
2. 扫描 `@IrisService` 标记的服务 Bean
3. 为每个 `@IrisApi` 方法订阅 MQTT Topic
4. `@IrisTool` 方法通过保留消息注册

客户端调用流程：
1. `@IrisRPC` 字段 → `IrisReferenceBeanPostProcessor` 注入代理
2. 方法调用 → 封装 `ClientProxy` → 发布 MQTT 请求
3. 等待响应 → 反序列化 → 返回结果

---

## Issue & PR 规范

### Issue 格式规范

#### 标题格式
```
[类型] 简短描述
```

**类型定义：**
| 类型 | 说明 | 标签 |
|------|------|------|
| `[Feature]` | 新功能、新特性需求 | enhancement |
| `[Bug]` | 功能缺陷、Bug修复 | bug |
| `[Refactor]` | 代码重构、架构优化 | refactor |
| `[Docs]` | 文档更新、补充 | documentation |
| `[Chore]` | 构建、CI、依赖升级等 | chore |

#### 正文格式
```markdown
### 问题/需求描述
清晰描述问题现象或需求背景

### 影响范围（Bug专用）
- 影响模块：xxx
- 复现条件：xxx
- 严重程度：高/中/低

### 建议方案（可选）
初步的解决方案思路
```

**现有 Issue 参考示例：**
- ✅ `[Bug] Timeout response causes thread blocking`
- ✅ `[Feature] Add LLM calling capability`

---

### Pull Request 格式规范

#### 标题格式
```
简短描述（首字母大写，动词开头）
```

**示例：**
- ✅ `Support rpc call register to MCP tools`
- ✅ `Reconstruct the project structure`
- ✅ `Added RPC call Timeout`

#### PR 描述格式
```markdown
### 变更内容
简要描述本次 PR 的主要变更

### 关联 Issue
Fixed #123
Closes #456

### 验证方式
- 单元测试通过
- 示例运行验证
- 其他验证说明
```

#### PR 最佳实践
1. **分支命名**: `IrisRPC-{issueNumber}-{description}` （如 `IrisRPC-12-mqtt-optimization`）
2. **粒度控制**: 一个 PR 只解决一个 Issue，避免大而全的变更
3. **描述要求**: 必须关联对应的 Issue 编号
4. **提交信息**: 清晰说明每次提交的变更内容

---

## 开发规范

### 注解使用规范

| 注解 | 位置 | 用途 | 必填 |
|------|------|------|------|
| `@IrisService(name="...")` | 类 | 标记 RPC 服务实现 | ✅ name |
| `@IrisApi(name="...")` | 方法 | 标记可远程调用方法 | ✅ name |
| `@IrisTool(desc="...")` | 方法 | 标记为 MCP 工具 | ✅ desc |
| `@IrisToolParam(desc="...")` | 参数 | 工具参数描述 | ❌ |
| `@IrisRPC` | 字段 | 注入 RPC 代理 | - |
| `@EnableIrisServer` | 配置类 | 启用服务端 | - |
| `@EnableIrisClient` | 配置类 | 启用客户端 | - |

### 代码风格

项目使用 `spotless` 格式化，基于 Eclipse 格式化配置。

**命名规范：
- 类名：大驼峰 (PascalCase)
- 方法名：小驼峰 (camelCase)
- 常量：全大写下划线分隔
- 包名：全小写点分隔

### 依赖版本

- **Java**: 17+
- **Maven**: 3.x
- **Spring Boot**: 2.3.0
- **Eclipse Paho**: 1.2.5
- **Jackson**: 2.18.2

---

## 关键文件速查

| 文件 | 作用 |
|------|------|
| `Iris-core/src/main/java/.../client/ClientProxy.java` | JDK 动态代理实现 |
| `Iris-core/src/main/java/.../client/Requester.java` | 请求发送与响应处理 |
| `Iris-core/src/main/java/.../client/RPCCall.java` | 异步调用上下文 (CompletableFuture) |
| `Iris-core/src/main/java/.../server/RequestProcessor.java` | 服务端请求处理器 |
| `Iris-core/src/main/java/.../mqtt/PahoMqttClient.java` | Eclipse Paho MQTT 实现 |
| `Iris-spring-boot-starter/.../IrisMCPToolRegister.java` | MCP 工具动态注册 |

---

## 常见开发任务

### 高优先级修复 (功能缺陷/潜在bug)

1. **【RPC 超时】** `Requester.request()` 中 `rpcCall.get()` 无超时，可能永久阻塞
   - 位置：`Iris-core/src/main/java/.../client/Requester.java`
   - 建议：使用 `get(timeout, unit)`，从配置读取

2. **【内存泄漏】** `RPCCall` Map 中的请求丢失响应时条目无法清理
   - 位置：`Iris-core/src/main/java/.../client/RPCCall.java`
   - 建议：添加定时清理任务或使用过期缓存

3. **【并发安全】** `RequestProcessor.serviceObjects` 使用 HashMap 非线程安全
   - 位置：`Iris-core/src/main/java/.../server/RequestProcessor.java`
   - 建议：改用 ConcurrentHashMap

4. **【参数类型】** JSON 反序列化后参数类型可能不匹配
   - 位置：`RequestProcessor.java` 反射调用处
   - 建议：添加类型转换逻辑

### 中优先级改进

5. **【线程池配置】** 优化 Requester 线程池配置不合理
6. **【异常处理】** 细化异常类型，避免笼统捕获 Exception
7. **【优雅关闭】** MQTT 客户端注册 JVM 关闭钩子

---

## 本地开发

### 构建项目

```bash
# 全量构建
mvn clean install

# 跳过测试
mvn test
```

### 运行示例

**启动服务端：
```bash
cd Iris-example/example-server-spring
mvn spring-boot:run
```

启动客户端：
```bash
cd Iris-example/example-client-spring
mvn spring-boot:run
```

### MQTT Broker 依赖

本地需要本地开发需要 MQTT Broker (如 EMQX 或 Mosquitto)：
```bash
# docker 配置
broker: tcp://localhost:1883
clientId: 唯一标识
```

---

## 新功能开发指引

### 添加新的序列化方式

1. 在 `Iris-core/src/main/java/.../common/serializer/` 下添加新的实现类
2. 实现 `Serializer` 接口或扩展 `JsonSerializerFacade`

### 扩展 MQTT 客户端实现

1. 继承 `MqttClient` 抽象类
2. 实现连接、发布、订阅等抽象方法

### 新增示例

1. 在 `example-common` 定义接口
2. 在 `example-server` 实现服务
3. 在 `example-client` 调用测试

---

## 文档资源

- **架构文档**: `ARCHITECTURE.md`
- **代码审查报告**: `CODE_REVIEW.md`
- **README**: `README.md`

---

---

## 文档资源

- **架构文档**: `ARCHITECTURE.md`
- **代码审查报告**: `CODE_REVIEW.md`
- **README**: `README.md`
- **OpenSpec 变更目录**: `openspec/changes/`

---

*此文件为 AI 助手提供项目上下文，已启用 OpenSpec 开发模式，更新日期: 2026-05-02*
