# CloudDrive 部署脚本说明

本目录包含CloudDrive项目的核心部署脚本，用于快速构建和启动微服务应用。

## 📁 脚本概览

### 🔨 构建脚本

#### `build-all.sh`
**功能**: 构建所有CloudDrive微服务和前端应用

**包含**:
- 所有Spring Boot微服务 (Gateway, User, File, Admin, Membership)
- Vue 3前端应用
- 自动环境检测和依赖验证

**使用**:
```bash
./scripts/build-all.sh
```

**要求**:
- Java 17+
- Maven 3.6+ 或项目自带mvnw
- Node.js 16+
- npm

---

### 🚀 启动脚本

#### `start-minimal.sh`
**功能**: 启动最小后端微服务栈（推荐用于开发）

**包含服务**:
- MySQL (3307) - 数据库，包含健康检查
- Redis (6379) - 缓存
- Nacos (8848) - 服务注册发现
- Gateway (8080) - API网关
- User Service (8081) - 用户服务

**使用**:
```bash
./scripts/start-minimal.sh
```

**特点**:
- 使用固定的Docker Compose配置
- 快速启动，资源占用少
- 不包含前端，需单独启动
- 适合后端开发和测试
- 包含MySQL健康检查和服务依赖管理

---

#### `start-microservices.sh`
**功能**: 启动完整微服务栈

**包含服务**:
- 基础设施: MySQL, Redis, Nacos
- 微服务: Gateway, User, File, Admin, Membership
- 大数据: Hadoop HDFS, Apache Flink
- 前端: Vue 3应用

**使用**:
```bash
./scripts/start-microservices.sh
```

**要求**:
- 充足系统资源 (推荐8GB+ RAM)
- 所有端口可用 (3000, 8080-8084, 8848, 9870等)

---

#### `start-frontend.sh`
**功能**: 独立启动前端服务

**包含**:
- Vue 3前端应用 (Docker容器)
- API代理配置
- 生产环境构建

**使用**:
```bash
./scripts/start-frontend.sh
```

**特点**:
- 使用固定的Docker Compose配置
- 自动构建前端应用
- 端口: http://localhost:3000
- 需要后端服务运行在localhost:8080

---

## 🏗️ 推荐部署流程

### 开发环境
1. 构建所有服务:
   ```bash
   ./scripts/build-all.sh
   ```

2. 启动后端服务:
   ```bash
   ./scripts/start-minimal.sh
   ```

3. 启动前端服务:
   ```bash
   ./scripts/start-frontend.sh
   ```

### 完整演示环境
```bash
./scripts/start-microservices.sh
```

---

## 🔧 系统要求

### 基础要求
- **Docker Desktop** - 容器运行环境
- **Docker Compose** - 多容器编排
- **8GB+ RAM** - 完整栈运行需要

### 开发要求
- **Java 17+** - Spring Boot微服务
- **Maven 3.6+** - Java项目构建
- **Node.js 16+** - 前端开发环境
- **npm** - 前端包管理

---

## 📊 服务端口映射

| 服务 | 端口 | 用途 |
|------|------|------|
| Frontend | 3000 | Vue 3前端应用 |
| Gateway | 8080 | API网关 |
| User Service | 8081 | 用户服务 |
| File Service | 8082 | 文件服务 |
| Admin Service | 8083 | 管理服务 |
| Membership Service | 8084 | 会员服务 |
| Nacos Console | 8848 | 服务注册中心 |
| MySQL | 3307 | 数据库 |
| Redis | 6379 | 缓存 |
| Hadoop NameNode | 9870 | 文件系统 |

---

## 🛠️ 常用命令

### 查看运行状态
```bash
# 最小栈状态
docker compose -f docker/docker-compose.minimal.yml ps

# 完整栈状态  
docker compose -f docker/docker-compose.microservices.yml ps

# 前端状态
docker compose -f docker/docker-compose.frontend.yml ps
```

### 停止服务
```bash
# 停止最小栈
docker compose -f docker/docker-compose.minimal.yml down

# 停止完整栈
docker compose -f docker/docker-compose.microservices.yml down

# 停止前端
docker compose -f docker/docker-compose.frontend.yml down
```

### 查看日志
```bash
# 查看特定服务日志
docker compose -f docker/docker-compose.minimal.yml logs -f [service-name]
```

---

## 🔍 故障排除

### 常见问题
1. **端口占用**: 确保所需端口未被其他服务占用
2. **内存不足**: 完整栈需要8GB+内存
3. **Docker未启动**: 确保Docker Desktop正在运行
4. **网络问题**: 检查Docker网络连接

### 重新构建
```bash
# 强制重新构建并启动
docker compose -f docker/docker-compose.minimal.yml up --build --force-recreate
```

---

*最后更新: 2025年7月8日*