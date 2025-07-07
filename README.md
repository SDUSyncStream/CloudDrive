# CloudDrive

一个基于 Vue + Spring Boot 的全栈云盘项目，采用现代化 Monorepo 架构

## 项目架构

本项目采用**终极混合架构**，结合了应用分离和代码复用的最佳实践：

```
CloudDrive/
├── .github/                # CI/CD, PR模板, Issue模板
│   └── workflows/
│       └── ci.yml
│
├── apps/                   # 可独立运行的应用
│   ├── frontend/           # Vue.js 前端应用
│   └── backend/            # Spring Boot 后端应用
│
├── packages/               # 共享代码包
│   ├── shared-types/       # 前后端共享的TypeScript类型
│   ├── utils/              # 共享工具函数
│   ├── ui-components/      # 共享UI组件库
│   └── eslint-config-custom/ # 共享ESLint配置
│
├── docker/                 # Docker配置文件
├── docs/                   # 项目文档
├── sql/                    # 数据库脚本
├── package.json            # 工作区根配置
├── pnpm-workspace.yaml     # pnpm 工作区配置
└── README.md
```

## 技术栈

### 前端
- Vue.js 3
- Vue Router
- Vuex/Pinia
- Element Plus / Ant Design Vue
- TypeScript

### 后端  
- Spring Boot
- Spring Security
- MyBatis Plus
- MySQL
- Redis

### 工具链
- pnpm (包管理器)
- TypeScript (类型检查)
- ESLint (代码检查)
- Prettier (代码格式化)
- GitHub Actions (CI/CD)

## 开发环境搭建

### 1. 安装依赖
```bash
# 确保已安装 Node.js 18+ 和 pnpm 8+
pnpm install
```

### 2. 初始化应用

#### 前端初始化
```bash
cd apps/frontend
# 使用 Vue CLI 或 Vite 创建项目
npm create vue@latest . --typescript
```

#### 后端初始化
```bash
cd apps/backend
# 使用 Spring Initializr 创建项目
```

### 3. 常用命令

```bash
# 启动开发环境（前后端同时启动）
pnpm dev

# 构建所有应用
pnpm build

# 运行测试
pnpm test

# 代码检查
pnpm lint

# 清理构建产物
pnpm clean
```

## 架构优势

### 🎯 清晰的边界
- **apps/**: 存放可独立运行的应用（消费者）
- **packages/**: 存放可复用的共享代码（生产者）
- **项目级配置**: docker、docs、sql 等服务于整个项目

### 🔄 极致的代码复用
- 类型定义在 `@cloud-drive/shared-types` 中统一管理
- 工具函数在 `@cloud-drive/utils` 中复用
- UI组件在 `@cloud-drive/ui-components` 中共享

### 🚀 无与伦比的可扩展性
- 轻松添加新应用：`apps/admin-panel`、`apps/mobile-api`
- 新应用可立即复用所有 `packages` 中的代码
- 统一的依赖管理和构建流程

### 🛠️ 统一的开发体验
- 单一命令安装所有依赖
- 统一的代码风格和检查规则
- 自动化的 CI/CD 流程

## 功能模块

- [ ] 用户认证与授权
- [ ] 文件上传下载
- [ ] 文件夹管理
- [ ] 文件分享
- [ ] 回收站
- [ ] 搜索功能
- [ ] 管理员管理
- [ ] 会员系统

## 贡献指南

1. 在 `apps/` 下开发新应用
2. 在 `packages/` 下创建可复用的代码
3. 遵循现有的 TypeScript 和 ESLint 规范
4. 提交前运行 `pnpm lint` 和 `pnpm test`
