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
- **MySQL 8.0+**
- **Redis 7+**

### 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/SDUSyncStream/CloudDrive.git
cd CloudDrive

# 2. 启动基础设施和微服务
cd docker
docker-compose up -d

# 3. 数据库初始化
docker exec -i mysql mysql -u root -p123456 < ../sql/01_create_database.sql
docker exec -i mysql mysql -u root -p123456 < ../sql/02_insert_data.sql

# 4. 启动前端
cd ../apps/frontend
npm install
npm run dev
```

### 访问地址
- **前端应用**: http://localhost:3000
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos
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
| Gateway | 8080 | API网关 |
| User Service | 8081 | 用户服务 |
| File Service | 8082 | 文件服务 |
| Admin Service | 8083 | 管理服务 |
| Membership Service | 8084 | 会员服务 |
| Mail Service | 8085 | 邮件服务 |
| Frontend | 3000 | 前端应用 |
| Nacos | 8848 | 服务注册中心 |
| MySQL | 3307 | 数据库 |
| Redis | 6379 | 缓存服务 |

## 🛠️ 开发指南

### 本地开发
```bash
# 启动后端微服务
./scripts/start-microservices.sh

# 启动前端开发服务器
./scripts/start-frontend.sh

# 构建所有服务
./scripts/build-all.sh
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

### Docker部署
```bash
# 生产环境部署
docker-compose -f docker/docker-compose.services.yml up -d
```

### 环境配置
- **开发环境**: `application-dev.yml`
- **生产环境**: `application-prod.yml`

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