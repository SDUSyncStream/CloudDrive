# CloudDrive 微服务架构

基于 Spring Cloud Alibaba + Hadoop + Flink 的企业级云存储微服务系统

## 架构概览

### 微服务组件
- **API Gateway (8080)**: Spring Cloud Gateway 统一入口
- **User Service (8081)**: 用户认证和管理服务
- **File Service (8082)**: 文件操作服务 + Hadoop HDFS 存储
- **Admin Service (8083)**: 系统管理服务
- **Membership Service (8084)**: 会员订阅管理服务

### 基础设施
- **Nacos (8848)**: 服务注册发现 + 配置中心
- **MySQL**: 业务数据存储
- **Redis**: 缓存和会话存储
- **Hadoop HDFS**: 分布式文件存储
- **Flink**: 实时文件处理

## 快速开始

### 前置要求
- Docker & Docker Compose
- Java 17+
- Maven 3.6+
- Node.js 16+

### 启动微服务
```bash
# 构建所有服务
./scripts/build-all.sh

# 启动微服务集群
./scripts/start-microservices.sh
```

### 服务地址
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Nacos Console: http://localhost:8848/nacos (nacos/nacos)
- Hadoop NameNode: http://localhost:9870
- Flink Dashboard: http://localhost:8081

## 技术栈

### 后端
- **Spring Boot 2.7**: 微服务框架
- **Spring Cloud Alibaba**: 微服务生态
- **Nacos**: 服务发现 + 配置管理
- **Spring Cloud Gateway**: API网关
- **MyBatis Plus**: 数据访问层
- **Spring Security + JWT**: 认证授权
- **Hadoop HDFS**: 分布式文件存储
- **Apache Flink**: 流处理引擎

### 前端
- **Vue 3**: 前端框架
- **TypeScript**: 类型安全
- **Element Plus**: UI组件库
- **Pinia**: 状态管理
- **Vite**: 构建工具

## 微服务详细设计

### 1. User Service (用户服务)
**职责**: 用户注册、登录、认证、配额管理
**技术**: Spring Security + JWT + Redis
**API端点**:
- `POST /users/register` - 用户注册
- `POST /users/login` - 用户登录
- `GET /users/profile/{id}` - 获取用户信息
- `PUT /users/quota/{id}` - 更新存储配额

### 2. File Service (文件服务)
**职责**: 文件上传/下载、HDFS存储、文件分享
**技术**: Hadoop HDFS + Flink + MyBatis Plus
**API端点**:
- `POST /files/upload` - 文件上传到HDFS
- `GET /files/download/{id}` - 文件下载
- `GET /files/list` - 文件列表
- `POST /files/share/{id}` - 创建文件分享

### 3. Admin Service (管理服务)
**职责**: 用户管理、系统监控、文件管理
**技术**: OpenFeign 服务调用
**API端点**:
- `GET /admin/users` - 用户列表管理
- `GET /admin/files` - 全局文件管理
- `GET /admin/statistics` - 系统统计

### 4. Membership Service (会员服务)
**职责**: 会员等级、订阅管理、支付处理
**技术**: MyBatis Plus + Redis
**API端点**:
- `GET /membership/levels` - 会员等级列表
- `POST /membership/subscribe` - 订阅会员
- `GET /membership/subscription` - 订阅状态

## 数据流设计

### 文件上传流程
```
前端 → Gateway → File Service → Flink预处理 → HDFS存储 → MySQL元数据
```

### 用户认证流程
```
前端 → Gateway → User Service → JWT生成 → Redis缓存 → 返回Token
```

### 服务间通信
- **同步调用**: OpenFeign + Nacos服务发现
- **异步消息**: Redis发布订阅
- **配置管理**: Nacos配置中心

## 部署架构

### Docker容器列表
- `nacos-server`: 服务注册中心
- `gateway`: API网关
- `user-service`: 用户服务
- `file-service`: 文件服务
- `admin-service`: 管理服务
- `membership-service`: 会员服务
- `mysql`: 数据库
- `redis`: 缓存
- `hadoop-namenode`: HDFS名称节点
- `hadoop-datanode`: HDFS数据节点
- `flink-jobmanager`: Flink作业管理器
- `flink-taskmanager`: Flink任务管理器
- `frontend`: Vue前端

### 网络拓扑
所有服务运行在 `cloud-drive-network` 网络中，支持服务发现和内部通信。

## 开发指南

### 本地开发
```bash
# 启动基础设施 (MySQL, Redis, Nacos)
docker-compose -f docker/docker-compose.microservices.yml up mysql redis nacos -d

# 本地运行各个服务
cd apps/user-service && mvn spring-boot:run
cd apps/gateway && mvn spring-boot:run
# ... 其他服务
```

### 配置管理
所有服务配置通过 Nacos 配置中心统一管理，支持动态配置更新。

### 监控运维
- **服务监控**: Spring Boot Actuator + Prometheus
- **链路追踪**: Sleuth + Zipkin (可选)
- **日志聚合**: ELK Stack (可选)

## 扩展计划

- [ ] 完善File Service的Hadoop HDFS集成
- [ ] 实现Admin Service跨服务管理功能
- [ ] 开发Membership Service订阅支付模块
- [ ] 集成Flink实时文件处理流水线
- [ ] 添加服务链路追踪和监控
- [ ] 实现服务熔断和限流

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License