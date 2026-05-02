## ADDED Requirements

### Requirement: RPC call has configurable timeout
RPC 调用 SHALL 在配置的时间内返回，超时则终止等待。

#### Scenario: Configured timeout applied
- **WHEN** RPC 调用发起且配置了超时时间
- **THEN** 系统 SHALL 等待最多配置的超时时间
- **THEN** 超时后 SHALL 终止等待并记录错误日志

#### Scenario: Default timeout when no config
- **WHEN** RPC 调用发起且未配置超时时间
- **THEN** 系统 SHALL 使用默认 10 秒超时

#### Scenario: RPCCall cleanup on timeout
- **WHEN** RPC 调用超时
- **THEN** 系统 SHALL 从 RPCCall Map 中移除对应的条目
- **THEN** 系统 SHALL 释放等待线程继续执行

### Requirement: Spring timeout config takes effect
Spring Boot Starter 配置的 `iris.timeout` SHALL 被正确传递到核心模块。

#### Scenario: Spring config timeout applied
- **WHEN** 在 application.yml 中配置 iris.timeout
- **THEN** 该值 SHALL 被用于 RPC 调用使用

#### Scenario: Default timeout in Spring
- **WHEN** application.yml 未配置 iris.timeout
- **THEN** 系统 SHALL 使用默认值 10 秒
