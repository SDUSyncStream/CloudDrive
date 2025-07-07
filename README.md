# CloudDrive

一个基于 Vue + Spring Boot 的全栈云盘项目，采用现代化 Monorepo 架构

## 🚀 项目状态

| 模块 | 状态 | 说明 |
|------|------|------|
| 🎨 **前端 (Vue 3)** | ✅ **完成** | 完整的用户界面，可独立运行 |
| 🔧 **后端 (Spring Boot)** | ⏳ **待开发** | 后端API服务 |
| 🐳 **Docker** | ✅ **完成** | 容器化配置完整 |
| 🗄️ **数据库** | ✅ **完成** | MySQL初始化脚本 |
| 🔄 **CI/CD** | ✅ **完成** | GitHub Actions自动化 |

## 项目架构

本项目采用**终极混合架构**，结合了应用分离和代码复用的最佳实践：

```
CloudDrive/
├── .github/workflows/      # ✅ CI/CD自动化
├── apps/
│   ├── frontend/           # ✅ Vue 3 + TypeScript + Element Plus
│   └── backend/            # ⏳ Spring Boot (待开发)
├── packages/               # ✅ 共享代码包 (预留)
│   ├── shared-types/       # TypeScript类型定义
│   ├── utils/              # 工具函数库
│   ├── ui-components/      # UI组件库
│   └── eslint-config-custom/ # ESLint配置
├── docker/                 # ✅ 完整容器化配置
├── docs/                   # 📚 项目文档
├── sql/                    # ✅ 数据库初始化脚本
└── README.md
```

## 技术栈

### 前端 ✅
- **Vue.js 3** + **Composition API**
- **TypeScript** 类型安全
- **Element Plus** UI组件库
- **Vue Router** 路由管理
- **Pinia** 状态管理
- **Axios** HTTP客户端
- **Vite** 构建工具

### 后端 ⏳ 
- Spring Boot (计划)
- Spring Security (计划)
- MyBatis Plus (计划)
- MySQL (数据库脚本已完成)
- Redis (计划)

### 工具链 ✅
- **npm** 包管理器
- **TypeScript** 类型检查
- **ESLint** 代码检查
- **Prettier** 代码格式化
- **GitHub Actions** CI/CD
- **Docker** 容器化

## 🏃‍♂️ 快速开始

### 前提条件
- Node.js 18+
- npm 或 pnpm
- Git

### 1. 克隆项目
```bash
git clone <your-repo-url>
cd CloudDrive
```

### 2. 启动前端 (当前可用)
```bash
# 安装前端依赖
cd apps/frontend
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:3000
```

### 3. 功能体验
🌟 **当前可体验的功能：**
- ✅ **响应式首页** - 项目介绍和导航
- ✅ **用户登录** - 完整的登录界面和表单验证
- ✅ **文件管理** - 模拟文件列表，支持网格/列表视图切换
- ✅ **路由守卫** - 自动跳转登录页面
- ✅ **状态管理** - 用户信息本地存储

### 4. Docker 部署 (可选)
```bash
# 构建并启动所有服务
docker-compose -f docker/docker-compose.yml up --build

# 前端: http://localhost:3000
# 后端: http://localhost:8080 (待开发)
# MySQL: localhost:3306
# Redis: localhost:6379
```

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

### 后续开发计划

#### Phase 1: 后端API开发 ⏳
- [ ] Spring Boot项目初始化
- [ ] 用户认证API (登录/注册)
- [ ] 文件管理API (上传/下载/删除)
- [ ] 数据库集成 (MySQL + MyBatis Plus)

#### Phase 2: 前后端联调 📋
- [ ] API接口对接
- [ ] 文件上传功能
- [ ] 用户认证流程
- [ ] 错误处理优化

#### Phase 3: 高级功能 🚀
- [ ] 文件分享功能
- [ ] 回收站机制
- [ ] 搜索和筛选
- [ ] 管理员面板

## 🔗 相关链接

- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

## 📝 更新日志

### v0.1.0 (当前版本)
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

**🎯 当前状态**: 前端已完成，可以立即体验！后端开发中...
