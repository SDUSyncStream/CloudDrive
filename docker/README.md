# CloudDrive Docker 部署文档

本文档介绍了 CloudDrive 项目的 Docker 容器化部署方案。项目采用微服务架构，每个服务都有独立的 Docker Compose 文件，支持模块化部署。

## 📁 文件结构

```
docker/
├── README.md                              # 本文档
├── docker-compose.services.yml           # 基础服务 (MySQL, Redis, Nacos, RabbitMQ)
├── docker-compose.frontend.yml           # 前端服务
├── docker-compose.gateway.yml            # API 网关
├── docker-compose.auth-service.yml       # 认证服务
├── docker-compose.user-service.yml       # 用户服务
├── docker-compose.admin-service.yml      # 管理员服务
├── docker-compose.membership-service.yml # 会员服务
├── docker-compose.mail-service.yml       # 邮件服务
├── docker-compose.file-manage-service.yml # 文件管理服务
├── docker-compose.file-share.yml         # 文件分享服务
├── docker-compose.fileupdown-service.yml # 文件上传下载服务
├── Dockerfile.frontend                   # 前端镜像构建文件
├── Dockerfile.gateway                    # 网关镜像构建文件
├── Dockerfile.auth-service               # 认证服务镜像构建文件
├── Dockerfile.user-service               # 用户服务镜像构建文件
├── Dockerfile.admin-service              # 管理员服务镜像构建文件
├── Dockerfile.membership-service         # 会员服务镜像构建文件
├── Dockerfile.mail-service               # 邮件服务镜像构建文件
├── Dockerfile.file-manage-service        # 文件管理服务镜像构建文件
├── Dockerfile.file-service               # 文件服务镜像构建文件
├── Dockerfile.fileupdown-service         # 文件上传下载服务镜像构建文件
├── mysql-init/                           # MySQL 初始化脚本
│   └── nacos-mysql.sql
└── nginx.conf                            # Nginx 配置文件
```

## 🏗️ 架构概览

### 基础服务层

- **MySQL 8.0**: 主数据库
- **Redis 7**: 缓存服务
- **Nacos 2.2.0**: 服务注册与配置中心
- **RabbitMQ 3**: 消息队列

### 应用服务层

- **Frontend (Vue.js)**: 用户界面 - 端口 3000
- **Gateway**: API 网关 - 端口 8080
- **Auth Service**: 认证服务 - 端口 8081
- **User Service**: 用户服务 - 端口 8087
- **Admin Service**: 管理员服务 - 端口 8083
- **Membership Service**: 会员服务 - 端口 8084
- **Mail Service**: 邮件服务 - 端口 8085
- **File Manage Service**: 文件管理服务 - 端口 8099
- **File Share**: 文件分享服务 - 端口 8082
- **FileUpDown Service**: 文件上传下载服务 - 端口 8090

## 🚀 快速开始

### 1. 启动基础服务

首先启动所有基础服务（MySQL、Redis、Nacos、RabbitMQ）：

```bash
cd docker
docker-compose -f docker-compose.services.yml up -d
```

等待服务启动完成（大约 2-3 分钟），可以通过以下命令检查状态：

```bash
docker-compose -f docker-compose.services.yml ps
```

### 2. 访问基础服务管理界面

- **Nacos 管理界面**: http://localhost:8848/nacos
- **RabbitMQ 管理界面**: http://localhost:15672 (用户名/密码: guest/guest)

### 3. 启动应用服务

#### 方式一：启动单个服务

```bash
# 启动网关服务
docker-compose -f docker-compose.gateway.yml up -d

# 启动认证服务
docker-compose -f docker-compose.auth-service.yml up -d

# 启动前端服务
docker-compose -f docker-compose.frontend.yml up -d
```

#### 方式二：启动所有应用服务

```bash
# 启动所有服务
docker-compose -f docker-compose.gateway.yml \
               -f docker-compose.auth-service.yml \
               -f docker-compose.user-service.yml \
               -f docker-compose.admin-service.yml \
               -f docker-compose.membership-service.yml \
               -f docker-compose.mail-service.yml \
               -f docker-compose.file-manage-service.yml \
               -f docker-compose.file-share.yml \
               -f docker-compose.fileupdown-service.yml \
               -f docker-compose.frontend.yml \
               up -d
```

### 4. 访问应用

- **前端应用**: http://localhost:3000
- **API 网关**: http://localhost:8080

## 📊 端口映射

| 服务                | 内部端口 | 外部端口 | 用途         |
| ------------------- | -------- | -------- | ------------ |
| MySQL               | 3306     | 3306     | 数据库       |
| Redis               | 6379     | 6379     | 缓存         |
| Nacos               | 8848     | 8848     | 服务注册中心 |
| RabbitMQ            | 5672     | 5672     | 消息队列     |
| RabbitMQ Management | 15672    | 15672    | 管理界面     |
| Frontend            | 3000     | 3000     | 前端应用     |
| Gateway             | 8080     | 8080     | API 网关     |
| Auth Service        | 8081     | 8081     | 认证服务     |
| File Share          | 8082     | 8082     | 文件分享     |
| Admin Service       | 8083     | 8083     | 管理员服务   |
| Mail Service        | 8085     | 8085     | 邮件服务     |
| Membership Service  | 8086     | 8086     | 会员服务     |
| User Service        | 8087     | 8087     | 用户服务     |
| FileUpDown Service  | 8090     | 8090     | 文件上传下载 |
| File Manage Service | 8099     | 8099     | 文件管理     |

## 🔧 环境变量配置

### 数据库配置

- `MYSQL_ROOT_PASSWORD`: 123456
- `MYSQL_DATABASE`: cloud_drive
- `MYSQL_ROOT_HOST`: %

### Redis 配置

- 默认配置，无密码

### Nacos 配置

- `MODE`: standalone
- `NACOS_AUTH_ENABLE`: false

### RabbitMQ 配置

- `RABBITMQ_DEFAULT_USER`: guest
- `RABBITMQ_DEFAULT_PASS`: guest

### Spring Boot 服务通用配置

- `SPRING_PROFILES_ACTIVE`: docker
- `SPRING_DATASOURCE_URL`: jdbc:mysql://mysql:3306/cloud_drive
- `SPRING_DATASOURCE_USERNAME`: root
- `SPRING_DATASOURCE_PASSWORD`: 123456
- `SPRING_REDIS_HOST`: redis
- `SPRING_REDIS_PORT`: 6379
- `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR`: nacos:8848

## 🐳 服务管理

### 查看服务状态

```bash
# 查看基础服务状态
docker-compose -f docker-compose.services.yml ps

# 查看特定服务状态
docker-compose -f docker-compose.auth-service.yml ps

# 查看所有容器
docker ps
```

### 查看服务日志

```bash
# 查看基础服务日志
docker-compose -f docker-compose.services.yml logs -f

# 查看特定服务日志
docker logs auth-service

# 实时跟踪日志
docker logs -f auth-service
```

### 停止服务

```bash
# 停止特定服务
docker-compose -f docker-compose.auth-service.yml down

# 停止基础服务
docker-compose -f docker-compose.services.yml down

# 停止所有容器
docker stop $(docker ps -q)
```

### 重启服务

```bash
# 重启特定服务
docker-compose -f docker-compose.auth-service.yml restart

# 重新构建并启动
docker-compose -f docker-compose.auth-service.yml up -d --build
```

## 🔍 健康检查

所有服务都配置了健康检查，可以通过以下方式查看：

```bash
# 查看容器健康状态
docker ps --format "table {{.Names}}\t{{.Status}}"

# 查看特定服务健康检查详情
docker inspect auth-service | grep -A 10 Health
```

### 健康检查端点

部分服务提供健康检查端点：

- Gateway: http://localhost:8080/actuator/health
- Auth Service: http://localhost:8081/actuator/health
- User Service: http://localhost:8087/actuator/health

## 🔄 部署流程建议

### 开发环境

1. 启动基础服务
2. 根据开发需要启动相关的应用服务
3. 使用 `docker logs` 查看服务日志进行调试

### 生产环境

1. 确保所有环境变量配置正确
2. 按照服务依赖顺序启动：
   - 基础服务 → 网关服务 → 业务服务 → 前端服务
3. 配置监控和日志收集
4. 设置服务自动重启策略

## 🚨 故障排除

### 常见问题

1. **端口冲突**

   ```bash
   # 检查端口占用
   lsof -i :8081

   # 杀死占用进程
   kill <PID>
   ```
2. **网络连接问题**

   ```bash
   # 检查 Docker 网络
   docker network ls

   # 检查容器网络连接
   docker exec auth-service ping mysql
   ```
3. **服务启动失败**

   ```bash
   # 查看详细日志
   docker logs auth-service

   # 进入容器调试
   docker exec -it auth-service /bin/sh
   ```
4. **数据库连接失败**

   ```bash
   # 检查 MySQL 是否正常运行
   docker exec -it mysql mysql -uroot -p123456 -e "SHOW DATABASES;"
   ```

### 服务依赖关系

确保按照以下顺序启动服务：

1. **基础服务**: MySQL → Redis → Nacos → RabbitMQ
2. **核心服务**: Gateway → Auth Service
3. **业务服务**: User Service → Admin Service → File Services
4. **附加服务**: Mail Service → Membership Service
5. **前端服务**: Frontend

## 📝 维护建议

1. **定期备份数据**

   ```bash
   # 备份 MySQL 数据
   docker exec mysql mysqldump -uroot -p123456 cloud_drive > backup.sql

   # 备份 Redis 数据
   docker exec redis redis-cli BGSAVE
   ```
2. **监控资源使用**

   ```bash
   # 查看容器资源使用情况
   docker stats
   ```
3. **定期更新镜像**

   ```bash
   # 拉取最新镜像
   docker-compose pull

   # 重新构建服务
   docker-compose build --no-cache
   ```
4. **清理未使用的资源**

   ```bash
   # 清理未使用的镜像、容器、网络
   docker system prune -f
   ```

## 📞 技术支持

如果遇到问题，请检查：

1. Docker 和 Docker Compose 版本是否兼容
2. 系统资源是否充足（至少 4GB RAM）
3. 网络端口是否被占用
4. 服务启动顺序是否正确

更多详细信息请参考各服务的日志文件和配置文件。
