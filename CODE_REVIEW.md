# IrisRPC 代码审查 - 可优化点汇总

## 🔴 高优先级问题 (功能缺陷/潜在bug)

---

### 1. RPC 调用超时未实现

**文件**: `Iris-core/src/main/java/io/github/qylh/iris/core/client/Requester.java`

**问题**: 
```java
public MqttResponse request(MqttRequest request) {
    try {
        mqttClient.publish(request.getTopic(), request);
    } catch (MqttClientException e) {
        logger.error("Failed to invoke remote method ReasonCode is:" + e.getMessage());
        return null;
    }
    RPCCall rpcCall = RPCCall.makeRPCCall(request.getRequestId());
    try {
        // todo 设置超时时间 ⚠️ 未实现超时，可能永久阻塞
        return rpcCall.get();  // 无超时的阻塞调用！
    } catch (Exception e) {
        logger.error("Failed to get response ReasonCode is:" + e.getMessage());
        return null;
    } finally {
        RPCCall.removeRPCCall(request.getRequestId());
    }
}
```

**风险**: 如果 MQTT 消息丢失或服务端无响应，调用线程会永久阻塞。

**优化方案**:
```java
// 使用配置的超时时间
return rpcCall.get(config.getTimeout(), config.getTimeoutUnit());
```

---

### 2. RPCCall 内存泄漏风险

**文件**: `Iris-core/src/main/java/io/github/qylh/iris/core/client/RPCCall.java`

**问题**:
```java
private static ConcurrentHashMap<Integer, RPCCall> calls = new ConcurrentHashMap<>();
```

如果请求发送成功但响应永远不回来，`finally` 块永远不会执行，导致 `calls` Map 内存泄漏。

**优化方案**:
```java
// 1. 添加定时清理任务
// 2. 或者使用过期淘汰的 Map 如 Guava Cache
// 3. 发送前启动超时清理定时器
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.schedule(() -> calls.remove(requestId), timeout, unit);
```

---

### 3. RequestProcessor 反射调用参数不匹配

**文件**: `RequestProcessor.java` 第 129-131 行

**问题**:
```java
Object[] args = mqttRequest.getArgs();
try {
    Object res = method.invoke(serviceObjects.get(serviceName), args);
```

MQTT 传输的 JSON 反序列化后，参数类型可能与实际方法参数不匹配（如 `int` 变成 `Integer`，`long` 变成 `Integer` 等）。

**优化方案**:
```java
// 添加类型转换
Object[] convertedArgs = convertArgs(args, method.getParameterTypes());
Object res = method.invoke(service, convertedArgs);
```

---

### 4. MCP 工具重复注册问题

**文件**: `IrisMCPToolRegister.java`

**问题**:
```java
mqttClient.subscribe_register(Constants.MQTT_REGISTER_TOPIC_SUFFIX + "#", (topic, message) -> {
    try {
        MqttRegisterMsg msg = MqttRegisterMsg.fromPahoMqttMessage(message.toPahoMqttMessage());
        buildSyncToolSpecification(msg);  // ⚠️ 每次收到保留消息都会重新注册
    } catch (UnsupportedEncodingException | NoSuchMethodException e) {
        throw new RuntimeException(e);
    }
});
```

MQTT 保留消息每次订阅都会收到一次，可能导致重复注册。

**优化方案**:
```java
// 先检查是否已存在
String toolKey = serviceName + "-" + methodName;
if (!syncToolSpecifications.containsKey(toolKey)) {
    buildSyncToolSpecification(msg);
}
```

---

## 🟠 中优先级问题 (并发安全/健壮性)

---

### 5. 线程池配置不合理

**文件**: `Requester.java`

**问题**:
```java
private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        10,
        20,
        60,
        java.util.concurrent.TimeUnit.SECONDS,
        new java.util.concurrent.ArrayBlockingQueue<>(1000));
```

1. 核心线程数 10、最大 20，但队列容量 1000。根据 ThreadPoolExecutor 规则，队列满了才会扩容到最大线程，所以 20 几乎永远用不到。
2. 没有配置拒绝策略，默认 `AbortPolicy` 可能导致任务丢失。
3. 线程池没有优雅关闭钩子。

**优化方案**:
```java
private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors() * 2,
        60, TimeUnit.SECONDS,
        new SynchronousQueue<>(),  // 或者较小的队列
        new ThreadPoolExecutor.CallerRunsPolicy());  // 调用者回退策略
```

---

### 6. serviceObjects 并发安全

**文件**: `RequestProcessor.java`

**问题**:
```java
private Map<String, Object> serviceObjects = new HashMap<>();
```

多线程环境下（MQTT 回调是多线程的），`HashMap` 不是线程安全的。

**优化方案**:
```java
private Map<String, Object> serviceObjects = new ConcurrentHashMap<>();
```

---

### 7. syncToolSpecifications 并发安全

**文件**: `IrisMCPToolRegister.java`

**问题**:
```java
private static Map<String, McpServerFeatures.SyncToolSpecification> syncToolSpecifications 
    = new ConcurrentHashMap<>();
```

虽然用了 `ConcurrentHashMap`，但 `put` 操作没有检查先验状态，存在竞态条件。

---

### 8. 异常处理过于宽泛

多处代码存在捕获 `Exception` 而非具体异常的问题：

```java
catch (Exception e) {  // ⚠️ 过于宽泛
    logger.error("...");
    return null;
}
```

**优化方案**: 捕获具体异常类型，避免掩盖 `InterruptedException` 等重要异常。

---

## 🟡 低优先级问题 (代码质量/可维护性)

---

### 9. 魔法值散落

**问题**: 多处硬编码的 Topic 前缀、超时时间等。

**建议**: 集中到 `Constants` 类统一管理。

---

### 10. 日志级别不合理

**问题**:
```java
catch (MqttClientException e) {
    logger.error("Failed to connect...");  // 连接失败应该是 ERROR，但有些业务失败可能用 WARN
}
```

**建议**: 区分日志级别，合理使用 DEBUG、INFO、WARN、ERROR。

---

### 11. 空指针风险

多处直接使用可能为 null 的对象：
```java
mqttClient.publish(request.getTopic(), request);  // mqttClient 可能为 null
rpcCall.complete(mqttResponse);  // mqttResponse 可能为 null
```

**建议**: 添加 `Objects.requireNonNull()` 或空值检查。

---

### 12. 资源泄漏: MqttClient 未关闭

**问题**: 多处创建 MqttClient 但没有注册 JVM 关闭钩子。

**优化方案**:
```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    if (mqttClient.isConnect()) {
        mqttClient.disconnect();
    }
}));
```

---

### 13. IrisReferenceBeanPostProcessor 使用反射效率低

**文件**: `IrisReferenceBeanPostProcessor.java`

**问题**: 每个 Bean 初始化时都反射扫描字段。

**优化方案**: 可以缓存已扫描过的类信息。

---

### 14. RequestProcessor 服务实例化无依赖注入

**文件**: `RequestProcessor.java` 第 85 行

**问题**:
```java
serviceObjects.put(serviceName, service.newInstance());  // ⚠️ 直接 newInstance，无法注入依赖
```

纯 Java 模式下的服务无法使用依赖注入。

---

## 📐 设计层面可优化

---

### 15. 缺少调用链追踪

当前无 traceId，难以追踪分布式调用。

---

### 16. 缺少重试机制

网络抖动时请求失败无自动重试能力。

---

### 17. 缺少熔断保护

服务端不可用时，客户端不应持续发送请求。

---

### 18. MQTT 客户端单例模式

当前 `ClientProxyFactory` 和 `RequestProcessor` 各自创建 MqttClient，同一进程中可以复用连接。

---

### 19. 序列化方式可扩展

当前只有 JSON 序列化，设计上可以支持策略模式，允许 Protobuf 等其他序列化方式。

---

### 20. 错误码与异常体系不完善

仅用 4 个错误码，粒度太粗，难以区分具体错误场景。

```java
public static final int IRIS_MQTT_SUCCESS = 0;
public static final int IRIS_MQTT_SERVICE_NOT_FOUND = 1;
public static final int IRIS_MQTT_METHOD_NOT_FOUND = 2;
public static final int IRIS_MQTT_INVOKE_ERROR = 3;
```

建议扩展：超时、序列化失败、网络异常、权限不足等。

---

## 🎯 优先修复建议 (Top 5)

| 优先级 | 问题 | 影响 |
|--------|------|------|
| 1 | RPC 调用无超时，可能永久阻塞 | 服务不可用时线程挂死 |
| 2 | RPCCall 内存泄漏 | 长时间运行内存溢出 |
| 3 | HashMap 非线程安全 | 并发场景下数据损坏 |
| 4 | 反射调用参数类型不匹配 | 方法调用失败 |
| 5 | MCP 工具重复注册 | 资源浪费，行为异常 |

---

## 💡 改进方向

1. **第一阶段**: 修复上述 Top 5 高优先级问题
2. **第二阶段**: 完善异常体系和错误码设计
3. **第三阶段**: 添加超时、重试、熔断等服务治理能力
4. **第四阶段**: 性能优化、监控指标、链路追踪
