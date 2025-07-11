# CloudDrive - 多用户线上网盘

> 基于微服务架构的现代化云存储解决方案

## 📋 项目概述

CloudDrive是一个功能完整的多用户线上网盘系统，采用前后端分离架构，支持文件上传下载、分享、会员订阅等核心功能。系统基于Spring Cloud微服务架构，提供高可用、高性能的云存储服务。

## 🏗️ 技术架构

### 后端微服务架构
- **网关服务** (Gateway): Spring Cloud Gateway - 统一API入口
- **用户服务** (User Service): 用户认证和权限管理
- **文件服务** (File Service): 文件上传、下载、管理
- **管理服务** (Admin Service): 系统管理和监控
- **会员服务** (Membership Service): 订阅管理和支付处理
- **邮件服务** (Mail Service): 邮件通知和验证
- **认证服务** (Auth Service): JWT认证服务

### 前端技术栈
- **Vue 3.3.4** + TypeScript + Vite
- **Element Plus** UI组件库
- **Pinia** 状态管理
- **Vue Router 4** 路由管理
- **ECharts** 数据可视化

### 基础设施
- **Nacos**: 服务注册与发现、配置管理
- **MySQL**: 主数据库
- **Redis**: 缓存和会话存储
- **RabbitMQ**: 消息队列
- **Docker**: 容器化部署

## ✨ 核心功能

### 👤 用户功能
- **用户管理**: 注册、登录、个人资料
- **文件管理**: 上传、下载、文件夹管理、回收站
- **文件分享**: 密码保护的分享链接
- **会员系统**: 多级会员订阅、存储空间升级
- **订单管理**: 支付订单跟踪

### 🔧 管理功能
- **系统仪表板**: 用户增长、存储使用趋势
- **用户管理**: 用户信息管理、会员状态修改
- **文件管理**: 全局文件监控和管理
- **订单管理**: 支付订单监控

### 💎 会员系统
| 等级 | 价格 | 存储空间 | 单文件限制 |
|------|------|----------|------------|
| 免费版 | ¥0.00 | 1GB | 100MB |
| 标准版 | ¥19.99 | 5GB | 500MB |
| 高级版 | ¥39.99 | 50GB | 5GB |
| 专业版 | ¥79.99 | 200GB | 10GB |
| 企业版 | ¥199.99 | 1TB | 20GB |

## 🚀 快速开始

### 环境要求
- **Java 17+**
- **Node.js 18+**
- **Docker & Docker Compose**
- **至少4GB RAM**

### 部署步骤

#### 1. 启动基础服务
```bash
# 克隆项目
git clone https://github.com/SDUSyncStream/CloudDrive.git
cd CloudDrive

# 启动基础服务 (MySQL, Redis, Nacos, RabbitMQ)
cd docker
docker-compose -f docker-compose.services.yml up -d

# 等待服务启动完成 (大约2-3分钟)
docker-compose -f docker-compose.services.yml ps
```

#### 2. 数据库初始化
```bash
# 数据库初始化
docker exec -i mysql mysql -u root -p123456 < ../sql/01_create_database.sql
docker exec -i mysql mysql -u root -p123456 < ../sql/02_insert_data.sql
```

#### 3. 启动应用服务
```bash
# 启动网关服务
docker-compose -f docker-compose.gateway.yml up -d

# 启动认证服务
docker-compose -f docker-compose.auth-service.yml up -d

# 启动用户服务
docker-compose -f docker-compose.user-service.yml up -d

# 启动管理服务
docker-compose -f docker-compose.admin-service.yml up -d

# 启动会员服务
docker-compose -f docker-compose.membership-service.yml up -d

# 启动邮件服务
docker-compose -f docker-compose.mail-service.yml up -d

# 启动文件相关服务
docker-compose -f docker-compose.file-manage-service.yml up -d
docker-compose -f docker-compose.file-share.yml up -d
docker-compose -f docker-compose.fileupdown-service.yml up -d

# 启动前端服务
docker-compose -f docker-compose.frontend.yml up -d
```

#### 4. 一键启动所有服务 (可选)
```bash
# 启动所有应用服务
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

### 访问地址
- **前端应用**: http://localhost:3000
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos
- **RabbitMQ管理**: http://localhost:15672 (guest/guest)
- **系统管理**: http://localhost:3000/admin (admin/password)

## 📁 项目结构

```
CloudDrive/
├── apps/                      # 应用层
│   ├── gateway/              # API网关
│   ├── user-service/         # 用户服务
│   ├── file-service/         # 文件服务
│   ├── admin-service/        # 管理服务
│   ├── membership-service/   # 会员服务
│   ├── mail-service/         # 邮件服务
│   ├── auth-service/         # 认证服务
│   └── frontend/             # Vue前端应用
├── docker/                   # Docker配置
├── sql/                      # 数据库脚本
├── scripts/                  # 部署脚本
└── docs/                     # 项目文档
```

## 🔧 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| **基础服务** | | |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存服务 |
| Nacos | 8848 | 服务注册中心 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ管理 | 15672 | 管理界面 |
| **应用服务** | | |
| Frontend | 3000 | 前端应用 |
| Gateway | 8080 | API网关 |
| Auth Service | 8081 | 认证服务 |
| User Service | 8082 | 用户服务 |
| Admin Service | 8083 | 管理服务 |
| Membership Service | 8084 | 会员服务 |
| Mail Service | 8085 | 邮件服务 |
| FileUpDown Service | 8090 | 文件上传下载 |
| File Share | 8093 | 文件分享 |
| File Manage Service | 8099 | 文件管理 |

## 🛠️ 开发指南

### 本地开发
```bash
# 启动基础服务
cd docker
docker-compose -f docker-compose.services.yml up -d

# 启动特定微服务进行开发
docker-compose -f docker-compose.auth-service.yml up -d
docker-compose -f docker-compose.user-service.yml up -d

# 启动前端开发服务器
cd ../apps/frontend
npm install && npm run dev
```

### 服务管理
```bash
# 查看服务状态
docker-compose -f docker-compose.services.yml ps

# 查看服务日志
docker logs -f auth-service

# 重启服务
docker-compose -f docker-compose.auth-service.yml restart
```

### API文档
网关统一路由规则：
- `/api/users/**` → user-service
- `/api/files/**` → file-service  
- `/api/admin/**` → admin-service
- `/api/membership/**` → membership-service

详细API文档请参考：[Gateway架构文档](docs/GATEWAY_ARCHITECTURE.md)

## 🔒 安全特性

- **JWT认证**: 基于Token的无状态认证
- **密码加密**: SHA-256密码哈希
- **CORS保护**: 跨域请求安全配置
- **输入验证**: 前后端双重验证
- **权限控制**: 基于角色的访问控制

## 📊 监控与日志

- **健康检查**: Spring Boot Actuator
- **日志聚合**: 统一日志收集
- **性能监控**: 实时性能指标
- **错误追踪**: 分布式链路追踪

## 🚀 部署说明

### Docker模块化部署
项目采用模块化的Docker Compose配置，支持按需部署：

```bash
# 基础服务部署
cd docker
docker-compose -f docker-compose.services.yml up -d

# 核心服务部署
docker-compose -f docker-compose.gateway.yml up -d
docker-compose -f docker-compose.auth-service.yml up -d

# 完整系统部署
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

### 环境配置
- **开发环境**: `SPRING_PROFILES_ACTIVE=docker`
- **生产环境**: 修改各服务的docker-compose文件中的环境变量

## 🤝 贡献指南

1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- **项目主页**: https://github.com/SDUSyncStream/CloudDrive
- **问题反馈**: https://github.com/SDUSyncStream/CloudDrive/issues

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！