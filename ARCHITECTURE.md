# IrisRPC 架构文档

## 1. 项目概述

IrisRPC 是一个基于 MQTT 协议的轻量级 RPC 框架，专为 IoT（物联网）场景设计。它借鉴了希腊神话中彩虹女神 Iris 的象征意义，通过 MQTT 消息协议实现跨网络的设备与服务间通信。

### 1.1 核心特性

- **轻量级传输**：基于 MQTT 协议，低带宽消耗，适合 IoT 场景
- **QoS 保证**：支持 MQTT QoS 机制，确保消息可靠投递
- **Spring Boot 集成**：提供 Starter 自动配置，无缝集成 Spring 生态
- **MCP 支持**：支持 Model Context Protocol，可作为 AI 工具提供者
- **跨语言能力**：基于 JSON 序列化，支持多语言客户端/服务端

---

## 2. 整体架构

### 2.1 模块划分

```
IrisRPC
├── Iris-core              # RPC 核心实现模块
├── Iris-spring-boot-starter # Spring Boot 自动配置模块
└── Iris-example           # 使用示例模块
    ├── example-common     # 公共 API 接口定义
    ├── example-server     # 纯 Java 服务端示例
    ├── example-client     # 纯 Java 客户端示例
    ├── example-server-spring  # Spring Boot 服务端示例
    ├── example-client-spring  # Spring Boot 客户端示例
    └── example-mcp-client-spring  # MCP 客户端示例
```

### 2.2 架构分层

```
┌─────────────────────────────────────────────────────────┐
│                     应用层 (Application)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Service    │  │  Controller  │  │  AI Client   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
├─────────────────────────────────────────────────────────┤
│                 Spring Boot Starter 层                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ @EnableIris  │  │ Bean 后置    │  │  MCP 工具    │  │
│  │   注解驱动   │  │  处理器      │  │  注册器      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
├─────────────────────────────────────────────────────────┤
│                      RPC 核心层 (Core)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  客户端代理  │  │  请求处理    │  │   MQTT 通信   │  │
│  │  ClientProxy │  │  RequestProcessor │  MqttClient │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │  消息模型    │  │  序列化器    │                     │
│  │   Message    │  │  Serializer  │                     │
│  └──────────────┘  └──────────────┘                     │
├─────────────────────────────────────────────────────────┤
│                     传输层 (Transport)                   │
│                    Eclipse Paho MQTT                     │
└─────────────────────────────────────────────────────────┘
```

---

## 3. 核心模块详解

### 3.1 Iris-core 核心模块

#### 3.1.1 注解系统

| 注解 | 作用域 | 用途 |
|------|--------|------|
| `@IrisService` | TYPE | 标记服务实现类，指定服务名称 |
| `@IrisApi` | METHOD | 标记可远程调用的 API 方法 |
| `@IrisTool` | METHOD | 标记可作为 MCP 工具的方法 |
| `@IrisToolParam` | PARAMETER | 工具参数描述 |
| `@IrisRPC` | FIELD/TYPE | 标记需要注入 RPC 代理的字段 |

#### 3.1.2 消息模型

基于 MQTT 消息封装了三种消息类型：

**MqttMsg - 消息基类**
```java
- messageId: 消息 ID（原子自增）
- qos: 消息服务质量等级
- clientId: 客户端 ID
```

**MqttRequest - 请求消息**
```java
- serviceName: 服务名称
- methodName: 方法名称
- args: 方法参数数组
- argsType: 参数类型数组
- requestId: 请求 ID

Topic 格式: iris/request/{serviceName}/{methodName}
```

**MqttResponse - 响应消息**
```java
- code: 响应码 (0=成功, 1=服务未找到, 2=方法未找到, 3=调用错误)
- msg: 响应消息
- data: 响应数据
- ResponseId: 对应请求 ID

Topic 格式: iris/response/{clientId}
```

**MqttRegisterMsg - 注册消息 (保留消息)**
```java
- serviceName: 服务名称
- methodName: 方法名称
- serviceDesc: 服务描述
- methodDesc: 方法描述
- argsType: 参数类型数组
- argsDesc: 参数描述数组
- interfaceType: 接口类型

Topic 格式: iris/register/{serviceName}/{methodName}
```

#### 3.1.3 序列化器

```
JsonSerializer
└── JacksonSerializer
    ├── deserialize(String, Class<T>)
    ├── deserialize(String, TypeReference<T>)
    ├── deserializeArray(String, Class<T>)
    └── serialize(Object)
```

使用 Jackson 进行 JSON 序列化，实现跨语言的数据交换。

#### 3.1.4 MQTT 客户端层

**MqttClient (抽象基类)**
```java
- connect(MqttConnectionConfig)
- publish(String topic, MqttMsg)
- publish(String[] topics, MqttMsg)
- subscribe_request(String, MqttMsgListener)
- subscribe_response(String, MqttMsgListener)
- subscribe_register(String, MqttMsgListener)
- unsubscribe(String)
- disconnect()
- register(String, MqttRegisterMsg)  # 保留消息注册
- isConnect()
```

**PahoMqttClient (Eclipse Paho 实现)**

基于 Eclipse Paho MQTT 客户端库的具体实现，支持：
- 自动重连
- 会话管理
- 多种 QoS 级别
- 保留消息

#### 3.1.5 客户端代理

**Requester - 请求发起者**

负责发送 RPC 请求并接收响应：
1. 建立 MQTT 连接
2. 订阅响应主题 `iris/response/{clientId}`
3. 发送请求消息到对应主题
4. 通过 `RPCCall` 异步等待响应结果

**RPCCall - 调用上下文**

继承 `CompletableFuture<MqttResponse>`，通过 `ConcurrentHashMap` 管理待完成的调用：
- `makeRPCCall(requestId)`: 创建调用上下文
- `getRPCCall(requestId)`: 获取调用上下文
- `removeRPCCall(requestId)`: 移除调用上下文

**ClientProxy - 动态代理**

使用 JDK 动态代理实现 RPC 调用透明化：
```java
invoke(Object proxy, Method method, Object[] args)
├── 构建 MqttRequest
├── 调用 Requester.request() 发送请求
├── 等待响应并返回结果
└── 异常处理
```

**ClientProxyFactory - 代理工厂**

创建服务接口的动态代理实例。

#### 3.1.6 服务端请求处理器

**RequestProcessor**

服务端核心组件，负责：

1. **服务注册**
   - 扫描 `@IrisService` 注解的类
   - 实例化服务对象存入 `serviceObjects Map`
   - 为 `@IrisApi` 方法订阅对应请求主题

2. **工具注册 (MCP)**
   - 对于 `@IrisTool` 标记的方法
   - 发布保留消息到 `iris/register/{serviceName}/{methodName}`
   - 包含完整的方法签名和描述信息

3. **请求处理流程**
   ```
   接收 MqttRequest
        ↓
   从 serviceObjects 获取服务实例
        ↓
   反射调用目标方法
        ↓
   构建 MqttResponse (成功/失败)
        ↓
   发布响应到 iris/response/{clientId}
   ```

---

### 3.2 Iris-spring-boot-starter 模块

#### 3.2.1 自动配置 (IrisSpringBootStarterConfiguration)

**核心 Bean 定义：**

1. **MqttClient** - MQTT 客户端
   - 从 `IrisProperties` 读取配置
   - 自动建立连接

2. **ClientProxyFactory** - 客户端代理工厂
   - 注入 MqttClient
   - 用于创建 RPC 代理

3. **MCP 服务端支持**
   - `WebMvcSseServerTransportProvider`: SSE 传输
   - `McpSyncServer`: MCP 同步服务端

#### 3.2.2 注解驱动

**@EnableIrisServer**

启用服务端功能，导入 `RequestProcessorGenerator`：
- 实现 `InitializingBean`，在 Spring 初始化后执行
- 扫描 Spring 容器中 `@IrisService` 标记的 Bean
- 调用 `RequestProcessor.start()` 注册服务

**@EnableIrisClient**

启用客户端功能，导入：
1. **IrisReferenceBeanPostProcessor**
   - Bean 后置处理器
   - 扫描 `@IrisRPC` 注解的字段
   - 自动注入 RPC 代理对象

2. **IrisMCPToolRegister**
   - 订阅 `iris/register/#` 主题
   - 监听服务注册消息
   - 动态构建 MCP Tool Specification
   - 注册到 McpSyncServer

#### 3.2.3 MCP 工具动态注册流程

```
服务端启动 (@IrisTool)
    ↓
发布保留消息到 iris/register/{service}/{method}
    ↓
客户端 IrisMCPToolRegister 监听到消息
    ↓
解析 MqttRegisterMsg (方法名、参数、描述)
    ↓
构建 McpSchema.Tool (名称、描述、Schema)
    ↓
构建 SyncToolSpecification (Tool + 调用 Handler)
    ↓
注册到 McpSyncServer
    ↓
AI 模型可通过 MCP 调用远程工具
```

---

## 4. 通信协议详情

### 4.1 Topic 设计

| Topic 模式 | 用途 | QoS |
|-----------|------|-----|
| `iris/request/{serviceName}/{methodName}` | RPC 请求 | 1 |
| `iris/response/{clientId}` | RPC 响应 | 1 |
| `iris/register/{serviceName}/{methodName}` | 工具注册（保留消息） | 2 |

### 4.2 调用时序

```
客户端                              服务端                            MQTT Broker
  │                                   │                                  │
  │  1. 构建 MqttRequest              │                                  │
  │     serviceName, methodName       │                                  │
  │     args, argsType                │                                  │
  │                                   │                                  │
  │  2. publish 请求消息  ──────────────────────────────────────────────▶│
  │     Topic: iris/request/X/Y       │                                  │
  │                                   │                                  │
  │                                   │  3. 接收请求消息                  │
  │                                   │     反序列化 MqttRequest          │
  │                                   │                                  │
  │                                   │  4. 反射调用服务方法              │
  │                                   │     method.invoke(service, args)  │
  │                                   │                                  │
  │                                   │  5. 构建 MqttResponse            │
  │                                   │     code=0/data=结果/msg          │
  │                                   │                                  │
  │  7. 接收响应  ◀──────────────────────────────────────────────────────│
  │     从 RPCCall Map 获取            │  6. publish 响应 ───────────────▶│
  │     CompletableFuture.complete()  │     Topic: iris/response/{clientId}
  │                                   │                                  │
  │  8. 返回调用结果                  │                                  │
```

---

## 5. 配置说明

### 5.1 核心配置类

**MqttConnectionConfig**
```java
- broker: MQTT 代理地址 (如 tcp://localhost:1883)
- username: 用户名 (可选)
- password: 密码 (可选)
- clientId: 客户端唯一标识
- connectionTimeout: 连接超时 (秒)
- keepAliveInterval: 心跳间隔 (秒)
- cleanSession: 是否清除会话
```

**IrisConfig**
```java
- mqttConnectionConfig: MQTT 连接配置
- timeout: RPC 调用超时时间
- timeoutUnit: 超时时间单位
```

**IrisProperties (Spring Boot)**
```yaml
iris:
  broker: tcp://localhost:1883
  username: admin
  password: secret
  clientId: my-app
  connectionTimeout: 10
  keepAliveInterval: 60
  timeout: 10
```

---

## 6. 使用方式

### 6.1 纯 Java 方式

**服务端**
```java
// 1. 定义服务接口
public interface TestService {
    String test(Integer a);
}

// 2. 实现服务
@IrisService(name = "TestService")
public class TestServiceImpl implements TestService {
    @Override
    @IrisApi(name = "test")
    public String test(Integer a) {
        return "Hello: " + a;
    }
}

// 3. 启动服务
RequestProcessor processor = new RequestProcessor();
List<Class<?>> services = List.of(TestServiceImpl.class);
MqttConnectionConfig config = MqttConnectionConfig.builder()
    .broker("tcp://localhost:1883")
    .clientId("server")
    .build();
processor.start(services, config);
```

**客户端**
```java
MqttConnectionConfig mqttConfig = MqttConnectionConfig.builder()
    .broker("tcp://localhost:1883")
    .clientId("client")
    .build();
IrisConfig config = IrisConfig.builder()
    .mqttConnectionConfig(mqttConfig)
    .build();

ClientProxyFactory factory = new ClientProxyFactory(config);
TestService service = factory.getProxy(TestService.class);

// 远程调用
String result = service.test(10);
```

### 6.2 Spring Boot 方式

**服务端**
```java
@SpringBootApplication
@EnableIrisServer
public class ServerApp {
    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
}

@IrisService(name = "LightControl")
@Service
public class Light1 implements LightControl {
    @IrisTool(desc = "打开灯光")
    @IrisApi(name = "openLight")
    public Boolean openLight() {
        return true;
    }
}
```

**客户端**
```java
@SpringBootApplication
@EnableIrisClient
public class ClientApp {
    public static void main(String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }
}

@RestController
public class TestController {
    @IrisRPC
    private TestService testService;  // 自动注入代理
    
    @GetMapping("/test")
    public String test() {
        return testService.test(1);  // 透明远程调用
    }
}
```

### 6.3 MCP AI 工具集成

```java
// 1. 服务端标记工具方法
@IrisService(name = "LightControl")
public class Light1 implements LightControl {
    @IrisTool(desc = "设置亮度，1-10")
    @IrisApi(name = "setBrightness")
    public Integer setBrightness(
        @IrisToolParam(desc = "灯光亮度，范围1-10") Integer brightness) {
        return brightness;
    }
}

// 2. 客户端启用 @EnableIrisClient
//    IrisMCPToolRegister 自动监听注册消息
//    动态注册为 MCP Tool

// 3. AI 可以通过 MCP 协议调用
// 用户: "把灯调到 5 级亮度"
// AI: 调用 LightControl-setBrightness(brightness=5)
// 框架: 通过 MQTT 执行远程调用
```

---

## 7. 关键技术点

### 7.1 动态代理机制
- 使用 JDK `Proxy.newProxyInstance` 创建接口代理
- `InvocationHandler` 拦截方法调用转换为 MQTT 消息
- 透明化 RPC 调用，用户像调用本地方法

### 7.2 异步转同步
- `RPCCall` 继承 `CompletableFuture`
- 发送请求后阻塞等待 `get()` 结果
- MQTT 回调线程完成 `complete()`

### 7.3 MQTT 保留消息
- 工具注册使用 retained = true
- 新客户端连接时立即获取所有可用工具
- 支持动态发现和热更新

### 7.4 Spring 生命周期钩子
- `BeanPostProcessor`: Bean 初始化前后处理
- `InitializingBean`: 属性设置后执行
- `SmartInitializingSingleton`: 单例初始化后执行

---

## 8. 设计亮点

1. **解耦彻底**：客户端与服务端通过 MQTT Broker 解耦，无直接网络依赖
2. **IoT 友好**：MQTT 协议天然适合物联网低带宽、不稳定网络场景
3. **AI 原生**：内置 MCP 支持，设备能力可直接暴露为 AI 工具
4. **动态发现**：通过保留消息实现服务/工具自动发现
5. **轻量级**：核心依赖少，启动快，资源占用低

---

## 9. 扩展方向

- [ ] 支持 Protobuf 序列化
- [ ] 增加调用超时和重试机制
- [ ] 服务健康检查和熔断
- [ ] 负载均衡和多服务实例支持
- [ ] 调用链路追踪
- [ ] 更多语言客户端 (Python/C++/Go)
- [ ] MQTT 5.0 协议支持
- [ ] 认证和加密增强
