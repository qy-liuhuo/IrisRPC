## Context

**当前状态：**
- `Requester.request()` 方法第89行使用 `rpcCall.get()` 无超时等待
- Spring Boot Starter 使用 `ClientProxyFactory(MqttClient)` 构造函数，未传递 `IrisConfig`
- 配置类 `IrisProperties.timeout` 默认值为 10，但未被实际使用
- 当 MQTT 响应丢失、网络异常或服务端无响应时，调用线程会永久阻塞

**约束：**
- 保持 API 向后兼容
- 使用现有的配置字段，不新增配置项
- 确保超时后的资源清理（RPCCall Map）

## Goals / Non-Goals

**Goals:**
- 修复线程永久阻塞问题
- 正确传递超时配置到核心模块
- 提供清晰的超时错误日志
- 确保超时后正确清理 RPCCall 资源

**Non-Goals:**
- 不引入新的配置项（复用现有 timeout 配置）
- 不修改 RPC 调用核心流程
- 不实现重试机制（超时即失败）

## Decisions

**1. 超时时间来源优先级**
- 优先使用 `IrisConfig.timeout` 和 `timeoutUnit`
- 如果 config 为 null（使用 MqttClient 构造函数时），使用默认值 10 秒
- 理由：保持两种构造函数路径的一致性

**2. 异常处理策略**
- 捕获 `TimeoutException`，记录 ERROR 级别日志
- 返回 `null`（与现有异常处理路径一致）
- 理由：保持与现有 `Exception` 处理的一致性，避免破坏调用方预期

**3. 配置传递修复**
- 在 `IrisSpringBootStarterConfiguration` 中构建完整的 `IrisConfig`
- 使用 `IrisConfig.builder()` 设置 `mqttConnectionConfig`、`timeout`、`timeoutUnit`
- 理由：Spring Boot 用户期望通过 `application.yml` 配置生效

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 超时时间过短导致正常请求被中断 | 可用性 | 默认值设置为 10 秒，可配置 |
| 调用方依赖永久阻塞的行为（反模式） | 兼容性 | 保留返回 null 语义，日志可观测 |
| 超时清理后，迟到的响应到达 | 资源泄漏 | RPCCall Map 使用 ConcurrentHashMap，移除后迟到响应不会造成问题 |
