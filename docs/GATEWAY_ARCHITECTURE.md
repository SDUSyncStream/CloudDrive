# CloudDrive Gateway Architecture Documentation

## 📋 目录
- [1. Gateway总体架构](#1-gateway总体架构)
- [2. 端口架构设计](#2-端口架构设计)
- [3. 接口架构设计](#3-接口架构设计)
- [4. 服务发现架构](#4-服务发现架构)
- [5. 网关过滤器架构](#5-网关过滤器架构)
- [6. 跨域和安全架构](#6-跨域和安全架构)
- [7. 监控和日志架构](#7-监控和日志架构)
- [8. 部署架构](#8-部署架构)
- [9. API接口清单](#9-api接口清单)
- [10. 最佳实践和扩展](#10-最佳实践和扩展)

---

## 1. Gateway总体架构

### 1.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   Client Layer                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                │
│  │   Web UI    │  │ Mobile App  │  │   Admin     │  │  3rd Party  │                │
│  │  (Vue.js)   │  │             │  │  Console    │  │     API     │                │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘                │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                Gateway Layer                                       │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                    Spring Cloud Gateway (8080)                             │   │
│  │                                                                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │   │
│  │  │   Routing   │  │  Load       │  │   Filter    │  │    CORS     │       │   │
│  │  │   Engine    │  │  Balancer   │  │   Chain     │  │   Handler   │       │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘       │   │
│  │                                                                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │   │
│  │  │   Service   │  │  Path       │  │   Health    │  │   Metrics   │       │   │
│  │  │  Discovery  │  │  Rewrite    │  │   Check     │  │ Collection  │       │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Service Registry                                       │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                            Nacos (8848)                                    │   │
│  │                                                                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │   │
│  │  │   Service   │  │   Config    │  │   Health    │  │   Namespace │       │   │
│  │  │  Registry   │  │  Center     │  │   Monitor   │  │  Management │       │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Microservices Layer                                   │
│                                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
│  │    User     │  │    File     │  │    Admin    │  │ Membership  │               │
│  │  Service    │  │  Service    │  │   Service   │  │   Service   │               │
│  │   (8081)    │  │   (8082)    │  │   (8083)    │  │   (8084)    │               │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘               │
│                                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
│  │   MySQL     │  │    Redis    │  │   Hadoop    │  │    Flink    │               │
│  │   (3307)    │  │   (6379)    │  │   HDFS      │  │   (8085)    │               │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘               │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 组件角色说明

| 组件 | 角色 | 职责 |
|------|------|------|
| **Spring Cloud Gateway** | API网关 | 统一入口、路由转发、负载均衡 |
| **Nacos** | 服务注册中心 | 服务发现、配置管理、健康检查 |
| **User Service** | 用户服务 | 用户认证、权限管理 |
| **File Service** | 文件服务 | 文件上传、下载、管理 |
| **Admin Service** | 管理服务 | 系统管理、监控 |
| **Membership Service** | 会员服务 | 订阅管理、支付处理 |

### 1.3 技术栈选择

- **网关框架**: Spring Cloud Gateway 3.1.x
- **服务注册**: Nacos 2.2.0
- **负载均衡**: Spring Cloud LoadBalancer
- **配置管理**: Spring Cloud Config + Nacos
- **监控**: Spring Boot Actuator + Micrometer
- **日志**: SLF4J + Logback

---

## 2. 端口架构设计

### 2.1 服务端口分配表

| 服务名称 | 内部端口 | 外部端口 | 协议 | 状态 | 说明 |
|----------|----------|----------|------|------|------|
| **Gateway** | 8080 | 8080 | HTTP | ✅ 运行 | API网关统一入口 |
| **User Service** | 8081 | 8081 | HTTP | ✅ 运行 | 用户认证服务 |
| **File Service** | 8082 | 8082 | HTTP | ✅ 运行 | 文件管理服务 |
| **Admin Service** | 8083 | 8083 | HTTP | ✅ 运行 | 系统管理服务 |
| **Membership Service** | 8084 | 8084 | HTTP | ✅ 运行 | 会员订阅服务 |
| **Nacos** | 8848 | 8848 | HTTP | ✅ 运行 | 服务注册中心 |
| **MySQL** | 3306 | 3307 | TCP | ✅ 运行 | 主数据库 |
| **Redis** | 6379 | 6379 | TCP | ✅ 运行 | 缓存服务 |
| **Hadoop NameNode** | 9870 | 9870 | HTTP | ✅ 运行 | HDFS管理界面 |
| **Flink Dashboard** | 8081 | 8085 | HTTP | ✅ 运行 | 流处理监控 |

### 2.2 网络拓扑图

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                Docker Network                                       │
│                              cloud-drive-network                                   │
│                                                                                     │
│  ┌─────────────┐ :8080    ┌─────────────┐ :8081    ┌─────────────┐ :8082          │
│  │   Gateway   │◄────────►│ User Service│◄────────►│File Service │                │
│  │   (8080)    │          │   (8081)    │          │   (8082)    │                │
│  └─────────────┘          └─────────────┘          └─────────────┘                │
│         │                         │                         │                      │
│         │                         │                         │                      │
│         ▼                         ▼                         ▼                      │
│  ┌─────────────┐ :8848    ┌─────────────┐ :8083    ┌─────────────┐ :8084          │
│  │    Nacos    │◄────────►│Admin Service│◄────────►│ Membership  │                │
│  │   (8848)    │          │   (8083)    │          │   (8084)    │                │
│  └─────────────┘          └─────────────┘          └─────────────┘                │
│         │                         │                         │                      │
│         │                         │                         │                      │
│         ▼                         ▼                         ▼                      │
│  ┌─────────────┐ :3307    ┌─────────────┐ :6379    ┌─────────────┐ :9870          │
│  │    MySQL    │◄────────►│    Redis    │◄────────►│   Hadoop    │                │
│  │   (3307)    │          │   (6379)    │          │   (9870)    │                │
│  └─────────────┘          └─────────────┘          └─────────────┘                │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                Host Network                                         │
│                                                                                     │
│  ┌─────────────┐ :8080    ┌─────────────┐ :3000    ┌─────────────┐ :8848          │
│  │   Gateway   │◄────────►│  Frontend   │◄────────►│    Nacos    │                │
│  │   (8080)    │          │   (3000)    │          │   (8848)    │                │
│  └─────────────┘          └─────────────┘          └─────────────┘                │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 端口冲突解决方案

#### 2.3.1 MySQL端口冲突
- **问题**: 本地MySQL使用3306端口
- **解决**: Docker映射 `3307:3306`
- **配置**: 应用连接`localhost:3307`

#### 2.3.2 Flink端口冲突
- **问题**: Flink Dashboard与User Service都使用8081
- **解决**: Flink Dashboard映射到8085
- **配置**: 访问`localhost:8085`查看Flink

#### 2.3.3 开发环境端口管理
```bash
# 检查端口占用
netstat -tulpn | grep :8080

# 端口冲突时的处理
docker-compose down
docker-compose up -d --remove-orphans
```

---

## 3. 接口架构设计

### 3.1 API路由映射规则

#### 3.1.1 路由规则表

| 外部路径 | 内部路径 | 目标服务 | 过滤器 | 状态 |
|----------|----------|----------|--------|------|
| `/api/users/**` | `/users/**` | user-service | StripPrefix=1 | ✅ 激活 |
| `/api/files/**` | `/files/**` | file-service | StripPrefix=1 | ✅ 激活 |
| `/api/admin/**` | `/admin/**` | admin-service | StripPrefix=1 | ✅ 激活 |
| `/api/membership/**` | `/membership/**` | membership-service | StripPrefix=1 | ✅ 激活 |
| `/api/subscription/**` | `/subscription/**` | membership-service | StripPrefix=1 | ✅ 激活 |
| `/api/payment/**` | `/payment/**` | membership-service | StripPrefix=1 | ✅ 激活 |
| `/api/auth/**` | `/auth/**` | auth-service | StripPrefix=1 | ⏳ 待开发 |

#### 3.1.2 路径重写机制

```yaml
# 路径重写规则
filters:
  - StripPrefix=1  # 去掉第一个路径段
  
# 示例：
# 请求: GET /api/membership/levels
# 重写: GET /membership/levels
# 转发: http://membership-service:8084/membership/levels
```

### 3.2 请求转发流程

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Request Flow                                           │
│                                                                                     │
│  Client Request                                                                     │
│  GET /api/membership/levels                                                         │
│           │                                                                         │
│           ▼                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        Gateway (8080)                                      │   │
│  │                                                                             │   │
│  │  1. Route Matching                                                          │   │
│  │     Pattern: /api/membership/**                                             │   │
│  │     Match: ✅ /api/membership/levels                                        │   │
│  │                                                                             │   │
│  │  2. Service Discovery                                                       │   │
│  │     Service: membership-service                                             │   │
│  │     Instance: 172.18.0.11:8084                                             │   │
│  │                                                                             │   │
│  │  3. Path Rewriting                                                          │   │
│  │     Original: /api/membership/levels                                        │   │
│  │     Rewritten: /membership/levels                                           │   │
│  │                                                                             │   │
│  │  4. Load Balancing                                                          │   │
│  │     Algorithm: Round Robin                                                  │   │
│  │     Target: http://172.18.0.11:8084                                        │   │
│  │                                                                             │   │
│  │  5. Request Forwarding                                                      │   │
│  │     URL: http://172.18.0.11:8084/membership/levels                         │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│           │                                                                         │
│           ▼                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                    Membership Service (8084)                               │   │
│  │                                                                             │   │
│  │  @RestController                                                            │   │
│  │  @RequestMapping("/membership")                                             │   │
│  │  public class MembershipController {                                        │   │
│  │                                                                             │   │
│  │    @GetMapping("/levels")                                                   │   │
│  │    public Result<List<MembershipLevel>> getAllLevels() {                   │   │
│  │      // 业务逻辑处理                                                          │   │
│  │      return Result.success(levels);                                        │   │
│  │    }                                                                        │   │
│  │  }                                                                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│           │                                                                         │
│           ▼                                                                         │
│  Response                                                                           │
│  {                                                                                  │
│    "code": 200,                                                                    │
│    "message": "成功",                                                               │
│    "data": [...]                                                                   │
│  }                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.3 负载均衡策略

#### 3.3.1 默认配置
```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      cache:
        enabled: true
        ttl: 35s
```

#### 3.3.2 健康检查
```yaml
# 服务健康检查配置
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

---

## 4. 服务发现架构

### 4.1 Nacos集成配置

#### 4.1.1 Gateway配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: ""  # 使用默认命名空间
        group: DEFAULT_GROUP
        service: gateway
        
      config:
        server-addr: localhost:8848
        file-extension: yaml
        group: DEFAULT_GROUP
        namespace: ""
```

#### 4.1.2 服务注册状态

| 服务名称 | 注册状态 | 健康状态 | 实例数 | 负载均衡 |
|----------|----------|----------|--------|----------|
| **gateway** | ✅ 已注册 | ✅ 健康 | 1 | - |
| **user-service** | ✅ 已注册 | ✅ 健康 | 1 | Round Robin |
| **file-service** | ✅ 已注册 | ✅ 健康 | 1 | Round Robin |
| **admin-service** | ✅ 已注册 | ✅ 健康 | 1 | Round Robin |
| **membership-service** | ✅ 已注册 | ✅ 健康 | 1 | Round Robin |

### 4.2 服务发现流程

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           Service Discovery Flow                                   │
│                                                                                     │
│  1. Service Registration                                                            │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Service   │──POST──►│    Nacos    │◄──GET───│   Gateway   │                  │
│  │  Instance   │         │  Registry   │         │             │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
│                                                                                     │
│  2. Health Check                                                                   │
│  ┌─────────────┐         ┌─────────────┐                                          │
│  │   Service   │◄──Ping──┤    Nacos    │                                          │
│  │  Instance   │         │  Registry   │                                          │
│  └─────────────┘         └─────────────┘                                          │
│                                                                                     │
│  3. Service Discovery                                                              │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Gateway   │──Query──►│    Nacos    │──List──►│  Service    │                  │
│  │             │         │  Registry   │         │  Instances  │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
│                                                                                     │
│  4. Load Balancing                                                                 │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Gateway   │──Select─►│Load Balancer│──Route──►│  Target     │                  │
│  │             │         │   Strategy  │         │  Instance   │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. 网关过滤器架构

### 5.1 过滤器链结构

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                Filter Chain                                         │
│                                                                                     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │   Global    │───►│   Route     │───►│   Custom    │───►│   Target    │          │
│  │  Filters    │    │  Filters    │    │  Filters    │    │  Service    │          │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘          │
│                                                                                     │
│  Pre-Filters:                                                                      │
│  • CorsFilter                                                                      │
│  • StripPrefixFilter                                                               │
│  • LoadBalancerClientFilter                                                        │
│                                                                                     │
│  Post-Filters:                                                                     │
│  • NettyWriteResponseFilter                                                        │
│  • NettyRoutingFilter                                                              │
│  • ResponseHeaderFilter                                                            │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 当前过滤器配置

#### 5.2.1 全局过滤器
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

#### 5.2.2 路由过滤器
```yaml
# 路径前缀过滤器
filters:
  - StripPrefix=1  # 去掉第一个路径段

# 示例配置
routes:
  - id: membership-service
    uri: lb://membership-service
    predicates:
      - Path=/api/membership/**
    filters:
      - StripPrefix=1
```

### 5.3 自定义过滤器扩展点

#### 5.3.1 认证过滤器 (未来扩展)
```java
@Component
public class AuthenticationFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // JWT Token验证逻辑
        return chain.filter(exchange);
    }
}
```

#### 5.3.2 限流过滤器 (未来扩展)
```java
@Component
public class RateLimitFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 限流逻辑
        return chain.filter(exchange);
    }
}
```

---

## 6. 跨域和安全架构

### 6.1 CORS配置策略

#### 6.1.1 当前CORS配置
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            # allow-credentials: true  # 生产环境启用
```

#### 6.1.2 生产环境CORS配置 (推荐)
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "https://clouddrive.example.com"
              - "https://admin.clouddrive.example.com"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders:
              - Authorization
              - Content-Type
              - X-Requested-With
            allowCredentials: true
            maxAge: 3600
```

### 6.2 安全架构设计

#### 6.2.1 认证授权流程 (未来实现)
```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Security Flow                                          │
│                                                                                     │
│  1. Authentication                                                                  │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Client    │──Login──►│   Gateway   │──Verify─►│Auth Service │                  │
│  │             │         │             │         │             │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
│                                 │                         │                        │
│                                 │                         │                        │
│                                 ▼                         ▼                        │
│  2. Authorization                                                                   │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Client    │──Request►│   Gateway   │──Check──►│  JWT Token  │                  │
│  │             │         │  Filter     │         │ Validation  │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
│                                 │                         │                        │
│                                 │                         │                        │
│                                 ▼                         ▼                        │
│  3. Service Access                                                                  │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐                  │
│  │   Gateway   │──Route──►│Load Balancer│──Forward►│Target Service│                  │
│  │             │         │             │         │             │                  │
│  └─────────────┘         └─────────────┘         └─────────────┘                  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 6.3 安全最佳实践

#### 6.3.1 请求头安全
```yaml
# 安全响应头配置
spring:
  cloud:
    gateway:
      default-filters:
        - AddResponseHeader=X-Content-Type-Options, nosniff
        - AddResponseHeader=X-Frame-Options, DENY
        - AddResponseHeader=X-XSS-Protection, 1; mode=block
```

#### 6.3.2 敏感信息过滤
```yaml
# 敏感路径保护
routes:
  - id: actuator-security
    uri: lb://user-service
    predicates:
      - Path=/actuator/**
    filters:
      - name: RequestRateLimiter
        args:
          rate-limiter: "#{@redisRateLimiter}"
          key-resolver: "#{@ipKeyResolver}"
```

---

## 7. 监控和日志架构

### 7.1 监控指标配置

#### 7.1.1 Actuator端点配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 7.1.2 关键监控指标

| 指标类型 | 指标名称 | 说明 | 阈值 |
|----------|----------|------|------|
| **吞吐量** | `gateway.requests.total` | 总请求数 | - |
| **响应时间** | `gateway.requests.duration` | 请求响应时间 | < 500ms |
| **错误率** | `gateway.requests.errors` | 错误请求率 | < 5% |
| **服务健康** | `gateway.services.health` | 服务健康状态 | = UP |
| **连接数** | `gateway.connections.active` | 活跃连接数 | < 1000 |

### 7.2 日志架构设计

#### 7.2.1 日志级别配置
```yaml
logging:
  level:
    cn.sdu.clouddrive: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web: INFO
    org.springframework.cloud.loadbalancer: INFO
    com.alibaba.nacos: INFO
```

#### 7.2.2 日志格式标准
```yaml
# 日志格式配置
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{50}] - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{50}] - %msg%n"
```

### 7.3 故障排查指南

#### 7.3.1 常见问题诊断

| 问题类型 | 症状 | 诊断步骤 | 解决方案 |
|----------|------|----------|----------|
| **路由失败** | 404 Not Found | 检查路由配置 | 修正路径映射 |
| **服务不可用** | 503 Service Unavailable | 检查服务注册 | 重启服务 |
| **负载均衡失败** | 间歇性错误 | 检查健康检查 | 配置健康检查 |
| **CORS错误** | 跨域请求失败 | 检查CORS配置 | 调整CORS策略 |

#### 7.3.2 日志分析命令
```bash
# 查看网关日志
docker logs gateway --tail 100 -f

# 查看特定服务日志
docker logs membership-service --tail 100 -f

# 搜索错误日志
docker logs gateway 2>&1 | grep -i error

# 分析路由日志
docker logs gateway 2>&1 | grep -i "route"
```

---

## 8. 部署架构

### 8.1 Docker容器化配置

#### 8.1.1 Gateway Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 8.1.2 Docker Compose配置
```yaml
version: '3.8'

services:
  gateway:
    build:
      context: ../apps/gateway
      dockerfile: ../../docker/Dockerfile.gateway
    container_name: gateway
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      nacos:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848
      - SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848
    networks:
      - cloud-drive-network
```

### 8.2 环境变量管理

#### 8.2.1 开发环境
```bash
# 开发环境变量
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export GATEWAY_PORT=8080
```

#### 8.2.2 生产环境
```bash
# 生产环境变量
export SPRING_PROFILES_ACTIVE=prod
export NACOS_SERVER_ADDR=nacos-cluster:8848
export GATEWAY_PORT=8080
export JAVA_OPTS="-Xms512m -Xmx1g"
```

### 8.3 生产环境部署

#### 8.3.1 高可用部署
```yaml
# 生产环境高可用配置
version: '3.8'

services:
  gateway-1:
    image: clouddrive/gateway:latest
    ports:
      - "8080:8080"
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

#### 8.3.2 负载均衡器配置
```nginx
# Nginx负载均衡配置
upstream gateway_backend {
    server gateway-1:8080;
    server gateway-2:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name api.clouddrive.com;
    
    location / {
        proxy_pass http://gateway_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 9. API接口清单

### 9.1 完整接口映射表

#### 9.1.1 用户服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| GET | `/api/users/profile` | `/users/profile` | 获取用户信息 | ⏳ 待开发 |
| POST | `/api/users/register` | `/users/register` | 用户注册 | ⏳ 待开发 |
| POST | `/api/users/login` | `/users/login` | 用户登录 | ⏳ 待开发 |
| PUT | `/api/users/profile` | `/users/profile` | 更新用户信息 | ⏳ 待开发 |

#### 9.1.2 文件服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| GET | `/api/files/list` | `/files/list` | 获取文件列表 | ⏳ 待开发 |
| POST | `/api/files/upload` | `/files/upload` | 上传文件 | ⏳ 待开发 |
| GET | `/api/files/download/{id}` | `/files/download/{id}` | 下载文件 | ⏳ 待开发 |
| DELETE | `/api/files/{id}` | `/files/{id}` | 删除文件 | ⏳ 待开发 |

#### 9.1.3 管理服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| GET | `/api/admin/hello` | `/admin/hello` | 测试接口 | ✅ 可用 |
| POST | `/api/admin/hello2` | `/admin/hello2` | POST测试 | ✅ 可用 |
| PUT | `/api/admin/hello3` | `/admin/hello3` | PUT测试 | ✅ 可用 |
| POST | `/api/admin/login` | `/admin/login` | 管理员登录 | ✅ 可用 |

#### 9.1.4 会员服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| GET | `/api/membership/levels` | `/membership/levels` | 获取会员等级 | ✅ 可用 |
| GET | `/api/membership/levels/{id}` | `/membership/levels/{id}` | 获取特定会员等级 | ✅ 可用 |
| GET | `/api/membership/levels/name/{name}` | `/membership/levels/name/{name}` | 按名称获取会员等级 | ✅ 可用 |

#### 9.1.5 订阅服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| GET | `/api/subscription/user/{userId}` | `/subscription/user/{userId}` | 获取用户订阅 | ✅ 可用 |
| GET | `/api/subscription/user/{userId}/current` | `/subscription/user/{userId}/current` | 获取当前订阅 | ✅ 可用 |
| POST | `/api/subscription/{id}/cancel` | `/subscription/{id}/cancel` | 取消订阅 | ✅ 可用 |

#### 9.1.6 支付服务接口
| 方法 | 外部路径 | 内部路径 | 功能 | 状态 |
|------|----------|----------|------|------|
| POST | `/api/payment/orders` | `/payment/orders` | 创建支付订单 | ✅ 可用 |
| GET | `/api/payment/orders/{id}` | `/payment/orders/{id}` | 获取支付订单 | ✅ 可用 |
| GET | `/api/payment/orders/user/{userId}` | `/payment/orders/user/{userId}` | 获取用户订单 | ✅ 可用 |
| POST | `/api/payment/orders/{id}/pay` | `/payment/orders/{id}/pay` | 处理支付 | ✅ 可用 |

### 9.2 请求/响应示例

#### 9.2.1 获取会员等级
```bash
# 请求
curl -X GET http://localhost:8080/api/membership/levels

# 响应
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "level-basic",
      "name": "基础版",
      "storageQuota": 10737418240,
      "maxFileSize": 1073741824,
      "price": 9.99,
      "durationDays": 30,
      "features": "10GB存储空间,单文件1GB",
      "storageQuotaFormatted": "10 GB",
      "maxFileSizeFormatted": "1 GB",
      "isRecommended": true
    }
  ],
  "timestamp": 1751986040197
}
```

#### 9.2.2 管理员登录
```bash
# 请求
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# 响应
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": "admin-001",
    "username": "admin",
    "userLevel": "admin",
    "email": "admin@example.com"
  }
}
```

### 9.3 错误处理规范

#### 9.3.1 错误响应格式
```json
{
  "timestamp": "2025-07-08T15:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Service not available",
  "path": "/api/users/profile"
}
```

#### 9.3.2 HTTP状态码规范
| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 200 | 成功 | 请求成功处理 |
| 400 | 请求错误 | 参数错误、格式错误 |
| 401 | 未授权 | 需要认证 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 未找到 | 资源不存在 |
| 500 | 服务器错误 | 内部服务错误 |
| 503 | 服务不可用 | 服务暂时不可用 |

---

## 10. 最佳实践和扩展

### 10.1 性能优化建议

#### 10.1.1 连接池优化
```yaml
# Netty连接池配置
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
        connect-timeout: 5000
        response-timeout: 10000
```

#### 10.1.2 缓存优化
```yaml
# 路由缓存配置
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      redis-rate-limiter:
        include-headers: false
        redis-script-name: request_rate_limiter
```

### 10.2 扩展点说明

#### 10.2.1 自定义过滤器
```java
@Component
public class CustomGatewayFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 自定义过滤逻辑
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -1; // 过滤器顺序
    }
}
```

#### 10.2.2 自定义断言
```java
@Component
public class CustomRoutePredicateFactory 
    extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // 自定义断言逻辑
            return true;
        };
    }
    
    public static class Config {
        // 配置参数
    }
}
```

### 10.3 故障恢复策略

#### 10.3.1 熔断器配置
```yaml
# Hystrix熔断器配置
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
      circuitBreaker:
        requestVolumeThreshold: 20
        sleepWindowInMilliseconds: 5000
        errorThresholdPercentage: 50
```

#### 10.3.2 重试策略
```yaml
# 重试配置
spring:
  cloud:
    gateway:
      routes:
        - id: retry-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
                methods: GET,POST
```

### 10.4 监控和告警

#### 10.4.1 Prometheus监控
```yaml
# Prometheus配置
management:
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
        descriptions: true
```

#### 10.4.2 告警规则
```yaml
# 告警规则配置
groups:
  - name: gateway_alerts
    rules:
      - alert: GatewayHighErrorRate
        expr: rate(gateway_requests_total{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Gateway high error rate detected"
          
      - alert: GatewayHighLatency
        expr: histogram_quantile(0.95, rate(gateway_request_duration_seconds_bucket[5m])) > 0.5
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Gateway high latency detected"
```

---

## 📋 总结

本文档详细描述了CloudDrive项目的Gateway架构设计，包括：

1. **总体架构**: 基于Spring Cloud Gateway的微服务网关
2. **端口设计**: 清晰的端口分配和冲突解决
3. **接口设计**: RESTful API路由和路径重写
4. **服务发现**: Nacos集成的服务注册与发现
5. **过滤器**: 可扩展的过滤器链架构
6. **安全机制**: CORS和认证授权设计
7. **监控日志**: 完整的可观测性方案
8. **部署策略**: Docker容器化和生产环境配置
9. **接口清单**: 完整的API接口文档
10. **最佳实践**: 性能优化和扩展指南

当前Gateway已经成功实现了基本的路由转发功能，为CloudDrive微服务架构提供了统一的API入口。未来可以根据业务需求在此基础上扩展认证、限流、监控等高级功能。

---

*文档版本: v1.0*  
*更新日期: 2025-07-08*  
*作者: CloudDrive团队*