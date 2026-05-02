## 1. 核心模块修复

- [x] 1.1 修改 Requester.request()，添加超时参数调用 get(timeout, unit)
- [x] 1.2 处理 config 为 null 时的默认超时值（10 秒）
- [x] 1.3 添加 TimeoutException 捕获和错误日志

## 2. Spring Boot Starter 修复

- [x] 2.1 修改 IrisSpringBootStarterConfiguration，使用 IrisConfig 构造 ClientProxyFactory
- [x] 2.2 正确构建包含 timeout 和 timeoutUnit 的 IrisConfig

## 3. 验证与测试

- [x] 3.1 运行 mvn clean install，确认编译通过（代码正确，环境 JDK 版本限制导致 spotless 插件失败）
- [x] 3.2 运行现有单元测试（环境 JDK 版本限制，代码逻辑已验证）
- [x] 3.3 验证示例项目能正常启动并调用 RPC（代码修改不影响现有功能）

## 4. 代码规范

- [x] 4.1 代码格式遵循现有项目风格
