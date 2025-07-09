# CloudDrive 环境安装指南

本文档帮助你快速配置CloudDrive微服务开发环境。

## 🚀 快速开始 (推荐)

如果你已安装Docker，可以直接使用：

```bash
# 启动最小可用版本 (推荐新手)
./scripts/start-minimal.sh

# 启动完整微服务栈
./scripts/start-microservices.sh
```

## 📋 环境要求

### 必需环境
| 工具 | 版本要求 | 用途 | 安装链接 |
|------|----------|------|----------|
| 🐳 **Docker** | 20.0+ | 容器化运行 | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| 🟢 **Node.js** | 18+ | 前端开发 | [Node.js](https://nodejs.org/) |

### 可选环境 (本地开发)
| 工具 | 版本要求 | 用途 | 安装链接 |
|------|----------|------|----------|
| ☕ **Java** | 17+ | 后端开发 | [OpenJDK](https://adoptium.net/) |
| 📦 **Maven** | 3.6+ | Java构建 | [Maven](https://maven.apache.org/) |

## 🔧 详细安装步骤

### 1. Docker 安装 (必需)

#### macOS
```bash
# 方法1: 官网下载 (推荐)
# 访问 https://www.docker.com/products/docker-desktop/
# 下载 Docker Desktop for Mac

# 方法2: Homebrew
brew install --cask docker
```

#### Windows
```bash
# 访问 https://www.docker.com/products/docker-desktop/
# 下载 Docker Desktop for Windows
# 注意: 需要启用 WSL2
```

#### Linux (Ubuntu/Debian)
```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 添加用户到 docker 组
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo apt-get update
sudo apt-get install docker-compose-plugin
```

### 2. Node.js 安装 (必需)

#### 所有平台 (推荐)
```bash
# 访问 https://nodejs.org/
# 下载 LTS 版本 (18.x+)
```

#### macOS
```bash
# 使用 Homebrew
brew install node

# 或使用 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install --lts
```

#### Linux
```bash
# 使用 NodeSource
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs

# 或使用 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install --lts
```

### 3. Java 安装 (本地开发可选)

#### 所有平台
```bash
# 访问 https://adoptium.net/
# 下载 OpenJDK 17+ LTS 版本
```

#### macOS
```bash
# 使用 Homebrew
brew install openjdk@17

# 添加到 PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# 验证安装
java -version
```

### 4. Maven 安装 (本地开发可选)

#### macOS
```bash
brew install maven
```

#### Linux
```bash
sudo apt update
sudo apt install maven
```

#### 或使用项目自带的 Maven Wrapper
```bash
# 项目已包含 mvnw，无需单独安装 Maven
./mvnw --version  # 在各个微服务目录下
```

## ✅ 环境验证

运行以下命令验证环境：

```bash
# 验证 Docker
docker --version
docker compose version

# 验证 Node.js
node --version
npm --version

# 验证 Java (可选)
java -version

# 验证 Maven (可选)
mvn --version
```

## 🚀 启动项目

### 方法1: 一键启动 (推荐)
```bash
# 克隆项目
git clone <your-repo-url>
cd CloudDrive

# 启动最小版本 (核心功能)
./scripts/start-minimal.sh

# 或启动完整版本 (所有服务)
./scripts/start-microservices.sh
```

### 方法2: 本地开发模式
```bash
# 1. 启动基础设施
docker compose -f docker/docker-compose.minimal.yml up mysql redis nacos -d

# 2. 本地运行微服务
cd apps/user-service && ./mvnw spring-boot:run
cd apps/gateway && ./mvnw spring-boot:run

# 3. 本地运行前端
cd apps/frontend && npm run dev
```

## 🔍 故障排除

### Docker 相关问题

**问题**: `docker: command not found`
```bash
# 解决: 安装 Docker Desktop
# macOS: https://www.docker.com/products/docker-desktop/
# Windows: https://www.docker.com/products/docker-desktop/
```

**问题**: `Cannot connect to the Docker daemon`
```bash
# 解决: 启动 Docker Desktop
# 确保 Docker Desktop 应用正在运行
```

**问题**: `docker-compose: command not found`
```bash
# 解决: 使用新版本 Docker Compose
docker compose version  # 新版本语法
```

### Node.js 相关问题

**问题**: `node: command not found`
```bash
# 解决: 安装 Node.js
# 访问 https://nodejs.org/ 下载安装
```

**问题**: `npm install` 失败
```bash
# 清除缓存重试
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Java/Maven 相关问题

**问题**: `mvn: command not found`
```bash
# 解决: 使用项目自带的 Maven Wrapper
./mvnw clean package  # 替代 mvn clean package
```

**问题**: `JAVA_HOME not set`
```bash
# macOS
export JAVA_HOME=$(/usr/libexec/java_home)

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### 端口冲突问题

**问题**: 端口被占用
```bash
# 查找占用端口的进程
lsof -i :8080  # 查看 8080 端口
lsof -i :3000  # 查看 3000 端口

# 停止占用端口的服务
kill -9 <PID>

# 或修改配置文件中的端口
```

## 📞 获取帮助

如果遇到问题：

1. 📖 查看 [README.md](./README.md) 
2. 📚 查看 [微服务文档](./README-MICROSERVICES.md)
3. 🔍 搜索项目 Issues
4. 💬 提交新的 Issue

## 🎯 下一步

环境配置完成后：

1. 🎨 访问前端: http://localhost:3000
2. 🌐 测试API: http://localhost:8080
3. 📊 查看服务: http://localhost:8848/nacos
4. 📖 阅读开发文档: [CLAUDE.md](./CLAUDE.md)