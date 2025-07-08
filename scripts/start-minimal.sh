#!/bin/bash

# ============================================================================
# CloudDrive Minimal Backend Start Script
# ============================================================================
# Description: 启动CloudDrive最小后端微服务栈
#
# 包含服务:
# - MySQL (3307) - 数据库
# - Redis (6379) - 缓存
# - Nacos (8848) - 服务注册发现中心
# - Gateway (8080) - API网关
# - User Service (8081) - 用户服务
#
# 功能:
# - 自动构建必要的微服务
# - 使用固定的Docker Compose配置
# - 启动后端微服务栈
# - 不包含前端服务 (使用start-frontend.sh单独启动)
#
# 使用方法: ./scripts/start-minimal.sh
#
# 要求:
# - Docker Desktop
# - Docker Compose
# ============================================================================

set -e  # Exit on any error

echo "🚀 Starting CloudDrive minimal backend stack..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get Docker Compose command
get_docker_compose_cmd() {
    if command_exists "docker" && docker compose version >/dev/null 2>&1; then
        echo "docker compose"
    elif command_exists "docker-compose"; then
        echo "docker-compose"
    else
        echo "❌ Error: Docker Compose not found. Please install Docker Desktop or Docker Compose."
        echo "💡 Install Docker Desktop: https://www.docker.com/products/docker-desktop/"
        exit 1
    fi
}

# Check Docker environment
echo "🔍 Checking Docker environment..."
if ! command_exists docker; then
    echo "❌ Error: Docker not found. Please install Docker."
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "❌ Error: Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi

DOCKER_COMPOSE_CMD=$(get_docker_compose_cmd)
echo "✅ Docker environment OK"

# Check if docker-compose file exists
if [ ! -f "docker/docker-compose.minimal.yml" ]; then
    echo "❌ Error: docker/docker-compose.minimal.yml not found"
    echo "💡 Make sure you are in the project root directory"
    exit 1
fi

echo "📝 Using existing minimal backend configuration"

# Check if built files exist
echo "🔍 Checking if core services are built..."
NEED_BUILD=false

if [ ! -f "apps/gateway/target/gateway-1.0.0.jar" ]; then
    NEED_BUILD=true
fi

if [ ! -f "apps/user-service/target/user-service-1.0.0.jar" ]; then
    NEED_BUILD=true
fi

if [ "$NEED_BUILD" = true ]; then
    echo "⚠️  Core services not built yet. Running build for minimal services..."
    
    # Function to build a Maven service
    build_maven_service() {
        local service_name=$1
        local service_dir=$2
        
        echo "🔨 Building $service_name..."
        if [ -d "$service_dir" ]; then
            cd "$service_dir"
            if [ -f "./mvnw" ]; then
                ./mvnw clean package -DskipTests -q
            elif command_exists mvn; then
                mvn clean package -DskipTests -q
            else
                echo "❌ Error: Neither mvnw nor mvn found in $service_dir"
                exit 1
            fi
            echo "✅ $service_name built successfully"
            cd - >/dev/null
        else
            echo "❌ Error: Directory $service_dir not found"
            exit 1
        fi
    }
    
    # Build core services
    build_maven_service "Gateway" "apps/gateway"
    build_maven_service "User Service" "apps/user-service"
fi

echo ""
echo "🛑 Stopping any existing containers..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml down 2>/dev/null || true

echo ""
echo "🏗️  Starting minimal backend microservices stack..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml up --build -d

echo ""
echo "⏳ Waiting for services to start..."
sleep 15

echo ""
echo "🎉 Minimal backend stack started successfully!"
echo ""
echo "📱 Available Services:"
echo "┌─────────────────────────────────────────────────────────┐"
echo "│ 🌐 API Gateway:     http://localhost:8080              │"
echo "│ 👤 User Service:    http://localhost:8081              │"
echo "│ 📊 Nacos Console:   http://localhost:8848/nacos        │"
echo "│    (Username: nacos, Password: nacos)                 │"
echo "│ 🗄️  MySQL:           localhost:3307                    │"
echo "│ 🔴 Redis:           localhost:6379                     │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""
echo "✨ This minimal backend stack includes:"
echo "   • Core authentication and user management"
echo "   • Service discovery and configuration"
echo "   • Database and caching"
echo "   • API Gateway for routing"
echo ""
echo "🔍 Check service status:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml ps"
echo ""
echo "🛑 Stop services:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml down"