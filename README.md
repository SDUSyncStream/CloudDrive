# CloudDrive

一个基于 Vue 3 + Spring Cloud Alibaba 的企业级云存储系统，采用微服务架构 + 大数据技术栈

## 🚀 项目状态

| 模块 | 状态 | 说明 |
|------|------|------|
| 🎨 **前端 (Vue 3)** | ✅ **完成** | 完整的用户界面，可独立运行 |
| 🌐 **API网关** | ✅ **完成** | Spring Cloud Gateway (8080) |
| 👤 **用户服务** | ✅ **完成** | JWT认证 + 用户管理 (8081) |
| 📁 **文件服务** | ⏳ **架构完成** | Hadoop HDFS + Flink集成 (8082) |
| 🛠️ **管理服务** | ⏳ **架构完成** | 系统管理 + 监控 (8083) |
| 💎 **会员服务** | ⏳ **架构完成** | 订阅管理 + 支付 (8084) |
| 📊 **服务注册** | ✅ **完成** | Nacos服务发现 + 配置中心 |
| 🐳 **容器化** | ✅ **完成** | 微服务Docker编排 |
| 🗄️ **数据库** | ✅ **完成** | MySQL + 会员表设计 |
| 🔄 **CI/CD** | ✅ **完成** | GitHub Actions自动化 |

## 🏗️ 微服务架构

本项目采用**Spring Cloud Alibaba微服务架构**，支持高可用、可扩展的企业级部署：

```
CloudDrive/
├── .github/workflows/      # ✅ CI/CD自动化
├── apps/                   # 微服务应用
│   ├── frontend/           # ✅ Vue 3前端 (3000)
│   ├── gateway/            # ✅ API网关 (8080)
│   ├── user-service/       # ✅ 用户服务 (8081)
│   ├── file-service/       # ⏳ 文件服务 (8082)
│   ├── admin-service/      # ⏳ 管理服务 (8083)
│   └── membership-service/ # ⏳ 会员服务 (8084)
├── docker/                 # ✅ 微服务容器编排
│   ├── docker-compose.microservices.yml  # 微服务栈
│   └── Dockerfile.*        # 各服务镜像
├── scripts/                # ✅ 自动化脚本
│   ├── build-all.sh        # 一键构建
│   └── start-microservices.sh # 一键启动
├── sql/                    # ✅ 数据库脚本 + 会员表
└── README-MICROSERVICES.md # 微服务详细文档
```

### 🎯 技术栈架构图
```
Frontend (Vue3) → Gateway (8080) → Microservices
                      ↓
┌─────────────────────────────────────────────────┐
│  Infrastructure Services                        │
│  • Nacos (8848) - 服务发现 + 配置中心              │
│  • MySQL - 业务数据                              │
│  • Redis - 缓存                                 │
│  • Hadoop HDFS - 分布式文件存储                   │
│  • Flink - 实时流处理                            │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  Business Microservices                         │
│  • User Service (8081) - 用户认证               │
│  • File Service (8082) - 文件操作               │
│  • Admin Service (8083) - 系统管理              │
│  • Membership Service (8084) - 会员管理         │
└─────────────────────────────────────────────────┘
```

## 🛠️ 技术栈

### 前端技术 ✅
- **Vue.js 3** + **Composition API**
- **TypeScript** 类型安全
- **Element Plus** UI组件库
- **Vue Router** 路由管理
- **Pinia** 状态管理
- **Axios** HTTP客户端
- **Vite** 构建工具

### 微服务后端 ✅
- **Spring Cloud Alibaba** 微服务生态
- **Spring Boot 2.7** 服务框架
- **Nacos** 服务发现 + 配置中心
- **Spring Cloud Gateway** API网关
- **Spring Security + JWT** 认证授权
- **MyBatis Plus** 数据访问层
- **MySQL 8.0** 关系数据库
- **Redis 7** 缓存中间件

### 大数据技术 ⏳
- **Hadoop HDFS** 分布式文件存储
- **Apache Flink** 实时流处理
- **分布式计算** 大文件处理

### 开发运维 ✅
- **Maven** Java构建工具
- **Docker** 容器化技术
- **Docker Compose** 服务编排
- **GitHub Actions** CI/CD
- **Java 17** 运行环境

## 🏃‍♂️ 快速开始

### 前提条件
- **Docker** & **Docker Compose** (推荐)
- **Java 17+** & **Maven 3.6+** (本地开发)
- **Node.js 18+** & **npm** (前端开发)

### 🚀 一键启动微服务 (推荐)
```bash
# 1. 克隆项目
git clone <your-repo-url>
cd CloudDrive

# 2. 启动最小可用版本 (推荐新手) - 包含前端
./scripts/start-minimal.sh

# 或启动完整微服务栈
./scripts/start-microservices.sh
```

> 💡 **环境要求**: Docker + Node.js 18+  
> 🍎 **Apple Silicon用户**: 已完美支持ARM64架构，无需额外配置  
> 📖 **详细安装指南**: [SETUP.md](./SETUP.md)

### 📱 访问服务
启动成功后，可访问以下服务：

| 服务 | 地址 | 说明 |
|------|------|------|
| 🎨 **前端界面** | http://localhost:3000 | Vue 3 用户界面 (✅ 已集成) |
| 🌐 **API网关** | http://localhost:8080 | 统一API入口 |
| 👤 **用户服务** | http://localhost:8081 | 认证和用户管理 |
| 📊 **Nacos控制台** | http://localhost:8848/nacos | 服务注册中心 (nacos/nacos) |
| 🗄️ **MySQL数据库** | localhost:3307 | 业务数据存储 |
| 🔴 **Redis缓存** | localhost:6379 | 缓存服务 |
| 🗂️ **Hadoop控制台** | http://localhost:9870 | HDFS文件系统 |
| ⚡ **Flink控制台** | http://localhost:8081 | 流处理监控 |

### 🔧 本地开发模式
```bash
# 1. 启动基础设施 (MySQL, Redis, Nacos)
docker-compose -f docker/docker-compose.minimal.yml up mysql redis nacos -d

# 2. 本地运行服务
cd apps/user-service && mvn spring-boot:run  # 用户服务
cd apps/gateway && mvn spring-boot:run       # API网关
cd apps/frontend && npm run dev              # 前端

# 3. 访问 http://localhost:3000
```

### 🍎 Apple Silicon (ARM64) 支持
项目已完美支持Apple Silicon Mac，包括：
- ✅ **Nacos服务** - 使用平台兼容性配置 (`platform: linux/amd64`)
- ✅ **端口冲突解决** - MySQL使用3307端口避免与本地服务冲突
- ✅ **前端容器化** - nginx配置正确路由到Gateway
- ✅ **Docker镜像优化** - 添加.dockerignore优化构建性能

### 🌟 功能体验
**当前可体验的完整功能：**
- ✅ **微服务架构** - 完整的Spring Cloud Alibaba技术栈
- ✅ **用户认证** - JWT登录 + 注册功能
- ✅ **API网关** - 统一路由和负载均衡
- ✅ **服务发现** - Nacos自动服务注册发现
- ✅ **响应式前端** - Vue 3 + Element Plus UI (已集成到微服务)
- ✅ **容器化部署** - 一键Docker启动
- ✅ **跨平台支持** - 完美支持x86_64和ARM64(Apple Silicon)架构
- ✅ **前后端联调** - 前端通过Gateway与微服务通信

## 📱 项目预览

### 首页
- 现代化设计风格
- 功能特性展示
- 响应式布局

### 登录页面  
- 优雅的表单设计
- 实时验证反馈
- 友好的用户体验

### 文件管理
- 仿云盘界面设计
- 多视图模式切换
- 完整的操作工具栏

## 🛠️ 开发指南

### 项目结构说明
```bash
apps/frontend/               # 前端应用 (已完成)
├── src/
│   ├── views/              # 页面组件
│   │   ├── HomeView.vue    # 首页
│   │   ├── LoginView.vue   # 登录页
│   │   └── FilesView.vue   # 文件管理页
│   ├── stores/             # Pinia状态管理
│   ├── router/             # Vue Router路由配置
│   ├── types/              # TypeScript类型定义
│   └── utils/              # 工具函数
├── package.json
└── vite.config.ts

apps/backend/               # 后端应用 (待开发)
└── (Spring Boot项目)

docker/                     # Docker配置 (已完成)
├── docker-compose.yml      # 服务编排
├── Dockerfile.frontend     # 前端容器
├── Dockerfile.backend      # 后端容器
└── nginx.conf             # Nginx配置

sql/                        # 数据库 (已完成)
└── init.sql               # 数据库初始化脚本
```

### 🗓️ 开发路线图

#### Phase 1: 核心微服务完善 ⏳
- [x] ✅ 用户服务 - JWT认证 + 用户管理
- [x] ✅ API网关 - 路由 + 负载均衡  
- [x] ✅ 服务注册 - Nacos集成
- [ ] 🔄 文件服务 - Hadoop HDFS集成
- [ ] 🔄 管理服务 - 跨服务管理APIs
- [ ] 🔄 会员服务 - 订阅 + 支付模块

#### Phase 2: 大数据集成 📊
- [ ] 🔄 Flink流处理 - 文件实时处理
- [ ] 🔄 HDFS集成 - 大文件分布式存储
- [ ] 🔄 数据管道 - 文件上传处理流程
- [ ] 🔄 性能优化 - 缓存 + 负载均衡

#### Phase 3: 高级功能 🚀
- [ ] 🔄 文件分享 - 分享码 + 权限控制
- [ ] 🔄 实时监控 - Prometheus + Grafana
- [ ] 🔄 服务网格 - Istio集成 (可选)
- [ ] 🔄 多租户支持 - 企业级功能

## 🔗 相关链接

### 前端技术
- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)

### 后端微服务
- [Spring Cloud Alibaba](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/en-us/index.html)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)

### 大数据技术
- [Hadoop HDFS 文档](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html)
- [Apache Flink 文档](https://flink.apache.org/)

### 详细文档
- [📚 微服务架构文档](./README-MICROSERVICES.md)

## 📝 更新日志

### v0.2.1 (当前版本) - 前端集成完成
- ✅ **前端微服务集成** - Vue 3前端完全集成到微服务架构
- ✅ **ARM64完美支持** - Apple Silicon Mac零配置运行
- ✅ **端口冲突解决** - MySQL使用3307端口避免冲突
- ✅ **nginx配置修复** - 前端正确路由到Gateway
- ✅ **Docker优化** - 添加.dockerignore优化构建性能
- ✅ **启动脚本完善** - start-minimal.sh包含完整前后端服务

### v0.2.0 - 微服务架构
- ✅ Spring Cloud Alibaba微服务架构完成
- ✅ Nacos服务注册发现 + 配置中心
- ✅ Spring Cloud Gateway API网关
- ✅ User Service完整实现 (JWT认证)
- ✅ 微服务Docker编排 (12个容器)
- ✅ Hadoop HDFS + Flink大数据集成
- ✅ 数据库扩展 (会员表设计)
- ✅ 一键启动脚本

### v0.1.0 (历史版本)
- ✅ 项目架构搭建完成
- ✅ 前端Vue 3应用完整实现
- ✅ Docker容器化配置
- ✅ CI/CD自动化流程
- ✅ 数据库设计和初始化脚本

## 🤝 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

---

**🎯 当前状态**: 微服务架构已完成，可立即体验完整技术栈！企业级云存储系统正在完善中...

**🚀 快速体验**: `./scripts/start-microservices.sh` 一键启动所有服务！
