# Zun-RPC

基于 Spring Boot Starter 的轻量级 RPC 框架，支持自定义 TCP 协议、Netty 连接池复用、Etcd 服务注册发现（或直连模式）、多种序列化方式、负载均衡及容错重试机制。

## 架构图

![架构图](docs/img/img.png)

## 特性

- **Spring Boot 集成** — 通过 `@EnableZunRpcProvider` / `@EnableZunRpcConsumer` 注解按需启用，`@ZunRpcService` / `@ZunRpcReference` 声明式使用
- **自定义 TCP 协议** — 17 字节定长协议头，支持多种序列化格式
- **Netty 通信层** — 基于 Netty 实现 TCP 通信，内置连接池复用、requestId 多路复用、心跳保活机制
- **可扩展** — 序列化器、注册中心、负载均衡器、重试/容错策略均可通过 `@Bean` 替换默认实现
- **注册中心** — 支持 Etcd（Watch 服务变更、心跳续期、优雅下线）和直连模式（无需注册中心）
- **负载均衡** — 随机、轮询、一致性哈希
- **容错重试** — 支持不重试/固定间隔重试，以及快速失败/安全失败/失败自动恢复/失败自动切换容错策略

## 项目结构

```
zun-rpc
├── zun-rpc-spring-boot-starter    # RPC 框架核心（Spring Boot Starter）
├── example-common                  # 示例 - 公共接口与模型
├── example-spring-boot-provider    # 示例 - 服务提供者
└── example-spring-boot-consumer    # 示例 - 服务消费者
```

## 调用流程

### Provider 启动流程

```mermaid
sequenceDiagram
    participant App as Provider Application
    participant Init as RpcInitBootStrap
    participant Reg as Registry
    participant Prov as RpcProviderBootStrap
    participant Server as NettyTcpServer

    App->>Init: Spring Bean 初始化
    Init->>Reg: registry.init(config)
    Init-->>Init: 注册 ShutdownHook

    App->>Prov: 扫描 @ZunRpcService Bean
    Prov->>Reg: registry.register(serviceMetaInfo)
    Prov->>Prov: LocalRegistry.register(serviceName, beanClass)
    Prov->>Server: new NettyTcpServer().doStart(port)
    Note over Server: Pipeline: IdleStateHandler →<br/>Decoder → Encoder →<br/>HeartbeatHandler → ServerHandler
```

### Consumer 启动流程

```mermaid
sequenceDiagram
    participant App as Consumer Application
    participant Init as RpcInitBootStrap
    participant Reg as Registry
    participant Cons as RpcConsumerBootStrap
    participant Factory as ServiceProxyFactory

    App->>Init: Spring Bean 初始化
    Init->>Reg: registry.init(config)

    App->>Cons: 扫描 @ZunRpcReference 字段
    Cons->>Factory: ServiceProxyFactory.getProxy(interfaceClass)
    Factory-->>Cons: 返回 JDK 动态代理对象
    Cons->>Cons: field.set(bean, proxy)
```

### RPC 调用流程

```mermaid
sequenceDiagram
    participant Caller as 业务代码
    participant Proxy as ServiceProxy
    participant LB as LoadBalancer
    participant Retry as RetryStrategy
    participant Pool as ChannelPoolManager
    participant Server as NettyTcpServer
    participant Handler as NettyTcpServerHandler
    participant IOC as Spring IOC

    Caller->>Proxy: userService.getUser("zunf")
    Proxy->>Proxy: 拦截 Object 方法（本地处理）
    Proxy->>Proxy: 构建 RpcRequest
    Proxy->>LB: registry.discover() → select()
    LB-->>Proxy: 选中的 ServiceMetaInfo

    Proxy->>Retry: doRetry(callback)
    Retry->>Pool: acquire(host, port)
    Pool-->>Retry: Channel（从连接池获取）

    Note over Retry,Server: 自定义协议编码<br/>[Magic|Ver|Serializer|Type|Status|RequestId|BodyLen|Body]

    Retry->>Server: channel.writeAndFlush(protocolMessage)
    Server->>Handler: NettyProtocolDecoder 解码
    Handler->>Handler: 提取 requestId
    Handler->>IOC: getBean(serviceClass)
    Handler->>Handler: method.invoke(bean, params)
    Handler->>Handler: 构建 RpcResponse，回写 requestId
    Handler->>Server: ctx.writeAndFlush(response)
    Server->>Retry: NettyProtocolDecoder 解码响应

    Note over Retry: requestId 匹配 →<br/>CompletableFuture.complete()

    Retry->>Pool: release(host, port, channel)
    Retry-->>Proxy: RpcResponse
    Proxy-->>Caller: 返回结果
```

### 容错流程

```mermaid
flowchart TD
    A[ServiceProxy.invoke] --> B{RetryStrategy}
    B -->|成功| C[返回结果]
    B -->|重试耗尽| D{ToleranceStrategy}

    D -->|failFast| E[抛出异常]
    D -->|failSafe| F[返回空值/默认值]
    D -->|failBack| G[记录请求，定时重试]
    D -->|failOver| H[切换节点重试]

    H --> I[选择其他服务节点]
    I --> J[重新发起 RPC 调用]
    J -->|成功| C
    J -->|失败| E
```

### 心跳与连接管理

```mermaid
flowchart LR
    subgraph Client[Consumer]
        CH[ClientHeartbeatHandler]
        CP[ChannelPoolManager]
    end

    subgraph Server[Provider]
        SH[ServerHeartbeatHandler]
    end

    CH -->|30s 写空闲| SH
    SH -->|心跳回复| CH
    SH -->|60s 读空闲| D[关闭连接]

    CP -->|连接失败| R[remove 连接池]
    CP -->|应用关闭| S[shutdown 所有连接池]
```

### 自定义协议格式

```mermaid
block-beta
    columns 7
    Magic:1 Version:1 Serializer:1 Type:1 Status:1 RequestId:8 BodyLength:4

    block:header:7
        columns 7
        M["Magic\n1 byte"] V["Version\n1 byte"] S["Serializer\n1 byte"] T["Type\n1 byte"] St["Status\n1 byte"] Rid["RequestId\n8 bytes"] Bl["BodyLength\n4 bytes"]
    end

    block:body:7
        columns 7
        B["Body (序列化后的请求/响应对象)"]
    end

    style header fill:#e1f5fe
    style body fill:#fff3e0
```

| 字段 | 长度 | 说明 |
|------|------|------|
| Magic | 1 byte | 魔数 `0x11` |
| Version | 1 byte | 协议版本 `1` |
| Serializer | 1 byte | 序列化器类型：1=JDK, 2=Kryo, 3=JSON, 4=Hessian |
| Type | 1 byte | 消息类型：1=REQUEST, 2=RESPONSE, 3=HEARTBEAT |
| Status | 1 byte | 状态：1=OK, 2=REQUEST_FAILED, 3=RESPONSE_FAILED |
| RequestId | 8 bytes | 雪花算法 ID，用于请求-响应多路复用匹配 |
| BodyLength | 4 bytes | Body 字节数 |
| Body | N bytes | 序列化后的 RpcRequest / RpcResponse |

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+

### 1. 引入依赖

在服务提供者和消费者的 `pom.xml` 中引入：

```xml
<dependency>
    <groupId>com.zunf</groupId>
    <artifactId>zun-rpc-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 定义公共接口

在公共模块中定义服务接口和模型类（需实现 `Serializable`）：

```java
// model
@Data
public class User implements Serializable {
    private String name;
    private Integer age;
}

// service interface
public interface UserService {
    User getUser(String name);
}
```

### 3. 服务提供者

启动类添加 `@EnableZunRpcProvider` 注解，服务实现类添加 `@ZunRpcService` 注解：

```java
@EnableZunRpcProvider
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}

@ZunRpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String name) {
        return new User(name, 38);
    }
}
```

`application.yaml` 配置：

```yaml
zun:
  rpc:
    application: my-provider
    version: 1.0
    serializer: jdk
    server-port: 10880
    registry:
      type: etcd
      address: http://localhost:2380
```

### 4. 服务消费者

启动类添加 `@EnableZunRpcConsumer` 注解，使用 `@ZunRpcReference` 注入远程服务代理：

```java
@EnableZunRpcConsumer
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}

@RestController
public class TestController {

    @ZunRpcReference
    private UserService userService;

    @GetMapping("/test")
    public String test() {
        return userService.getUser("zunf").toString();
    }
}
```

`application.yaml` 配置：

```yaml
zun:
  rpc:
    serializer: jdk
    load-balancer: random
    retry-strategy: fixedInterval
    tolerance-strategy: failOver
    registry:
      type: etcd
      address: http://localhost:2380
```

### 5. 构建运行

```bash
# 构建全部模块
mvn clean package -DskipTests

# 启动 Etcd（Docker）— 仅 Etcd 模式需要
docker run -d -p 2379:2379 -p 2380:2380 quay.io/coreos/etcd:v3.5.0 \
  etcd -advertise-client-urls http://0.0.0.0:2379 -listen-client-urls http://0.0.0.0:2379

# 先启动 Provider，再启动 Consumer
mvn spring-boot:run -pl example-spring-boot-provider
mvn spring-boot:run -pl example-spring-boot-consumer
```

## 直连模式

无需注册中心，Consumer 直接连接 Provider。配置方式：

```yaml
zun:
  rpc:
    serializer: jdk
    registry:
      type: direct
      address: localhost:10880   # Provider 地址
```

适用于开发测试或固定地址场景。

## 配置项

| 配置项 | 说明 | 可选值 |
|--------|------|--------|
| `zun.rpc.serializer` | 序列化方式 | `jdk` `json` `hessian` `kryo` |
| `zun.rpc.load-balancer` | 负载均衡策略 | `random` `roundRobin` `consistentHash` |
| `zun.rpc.retry-strategy` | 重试策略 | `no` `fixedInterval` |
| `zun.rpc.tolerance-strategy` | 容错策略 | `failFast` `failSafe` `failBack` `failOver` |
| `zun.rpc.registry.type` | 注册中心类型 | `etcd` `direct` |
| `zun.rpc.registry.address` | 注册中心地址 | Etcd: `http://localhost:2380`；直连: `localhost:10880` |

## 自定义扩展

框架各核心组件均支持替换默认实现。只需在项目中定义一个 `@Bean`，框架会优先使用你提供的实现：

```java
@Configuration
public class MyRpcConfig {

    @Bean
    public Serializer serializer() {
        return new MyCustomSerializer();
    }

    @Bean
    public Registry registry() {
        return new MyCustomRegistry();
    }
}
```

支持替换的组件：

- `Serializer` — 序列化器
- `Registry` — 注册中心
- `LoadBalancer` — 负载均衡器
- `RetryStrategy` — 重试策略
- `ToleranceStrategy` — 容错策略

## 技术栈

- Java 11 / Spring Boot 2.6.13
- Netty 4.1（TCP 通信、连接池、心跳）
- Etcd jetcd 0.7.7（服务注册发现）
- Jackson / Hessian / Kryo（序列化）
- Lombok / Hutool
