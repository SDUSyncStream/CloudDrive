#!/bin/bash

# ============================================================================
# CloudDrive Microservices Start Script
# ============================================================================
# Description: 启动CloudDrive微服务栈 (支持多种启动模式)
#
# 实际微服务 (已修复RabbitMQ问题):
# - 基础设施: MySQL, Redis, Nacos, RabbitMQ
# - 微服务: Gateway, Auth, FileUpDown, Admin, Membership, FileManage, FileRecycle, Mail
# - 大数据: Hadoop HDFS, Apache Flink  
# - 前端: Vue 3应用
#
# 启动模式:
# - core: 核心服务 (快速启动,无RabbitMQ)
# - full: 完整服务 (包含RabbitMQ,需要等待更长时间)
# - staged: 分阶段启动 (基础设施->微服务)
#
# 使用方法: 
#   ./scripts/start-microservices.sh [core|full|staged]
#   默认: staged (推荐)
#
# 要求:
# - Docker Desktop
# - Docker Compose
# - 充足的系统资源 (推荐8GB+ RAM)
# ============================================================================

set -e  # Exit on any error

# Function to show usage
show_usage() {
    echo "Usage: $0 [MODE]"
    echo ""
    echo "Available modes:"
    echo "  core    - Essential services only (Gateway, Auth, FileUpload, Admin, Membership, Frontend)"
    echo "            Fast startup, no RabbitMQ or big data services"
    echo "  full    - All services including RabbitMQ, Hadoop, Flink"
    echo "            Complete stack, may take longer to start"
    echo "  staged  - Infrastructure first, then applications (recommended)"
    echo "            Handles RabbitMQ timing issues automatically"
    echo ""
    echo "Examples:"
    echo "  $0               # Default: staged mode"
    echo "  $0 core         # Quick development setup"
    echo "  $0 full         # Complete production setup"
    echo "  $0 staged       # Safe startup with timing controls"
    echo ""
    exit 0
}

# Parse command line arguments
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_usage
fi

MODE=${1:-"staged"}  # Default to staged mode

# Validate mode
case "$MODE" in
    "core"|"full"|"staged")
        echo "🚀 Starting CloudDrive microservices in '$MODE' mode..."
        ;;
    *)
        echo "❌ Invalid mode: $MODE"
        echo ""
        show_usage
        ;;
esac

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
    echo "💡 Install Docker Desktop: https://www.docker.com/products/docker-desktop/"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "❌ Error: Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi

DOCKER_COMPOSE_CMD=$(get_docker_compose_cmd)
echo "✅ Docker environment OK"
echo "📦 Using Docker Compose command: $DOCKER_COMPOSE_CMD"

# Check if built files exist
echo "🔍 Checking if services are built..."
NEED_BUILD=false

if [ ! -d "apps/gateway/target" ] || [ -z "$(ls -A apps/gateway/target/*.jar 2>/dev/null)" ]; then
    NEED_BUILD=true
fi

if [ ! -d "apps/auth-service/target" ] || [ -z "$(ls -A apps/auth-service/target/*.jar 2>/dev/null)" ]; then
    NEED_BUILD=true
fi

if [ "$NEED_BUILD" = true ]; then
    echo "⚠️  Services not built yet. Running build first..."
    ./scripts/build-all.sh
fi

# Function to wait for RabbitMQ to be ready
wait_for_rabbitmq() {
    echo "⏳ Waiting for RabbitMQ to be ready..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec rabbitmq rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
            echo "✅ RabbitMQ is ready!"
            return 0
        fi
        echo "⏳ RabbitMQ not ready yet (attempt $attempt/$max_attempts)..."
        sleep 10
        attempt=$((attempt + 1))
    done
    
    echo "❌ RabbitMQ failed to start after $max_attempts attempts"
    return 1
}

# Function to start services based on mode
start_services() {
    case "$MODE" in
        "core")
            echo "🚀 Starting CORE services (without RabbitMQ)..."
            echo "🛑 Stopping any existing containers..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml down
            echo "🏗️  Building and starting core microservices..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml up --build -d
            echo "⏳ Waiting for core services to start..."
            sleep 15
            ;;
        "full")
            echo "🚀 Starting FULL services (including RabbitMQ)..."
            echo "🛑 Stopping any existing containers..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml down
            echo "🏗️  Building and starting all microservices..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml up --build -d
            echo "⏳ Waiting for all services to start..."
            sleep 20
            ;;
        "staged")
            echo "🚀 Starting services in STAGED mode..."
            echo "🛑 Stopping any existing containers..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml down
            
            echo "📦 Step 1: Starting infrastructure services..."
            $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml up --build -d mysql redis nacos rabbitmq
            
            echo "⏳ Waiting for infrastructure to be ready..."
            sleep 30
            
            if wait_for_rabbitmq; then
                echo "📦 Step 2: Starting application services..."
                $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml up --build -d
                echo "⏳ Waiting for application services to start..."
                sleep 15
            else
                echo "❌ Failed to start RabbitMQ, continuing without mail service..."
                echo "📦 Starting services without mail service..."
                $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml up --build -d
                sleep 15
            fi
            ;;
        *)
            echo "❌ Invalid mode: $MODE"
            echo "Valid modes: core, full, staged"
            exit 1
            ;;
    esac
}

# Start services based on selected mode
start_services

echo ""
echo "🎉 Microservices started in '$MODE' mode!"
echo ""
echo "📱 Service URLs:"
echo "┌──────────────────────────────────────────────────────────┐"
echo "│ 🎨 Frontend:         http://localhost:3000               │"
echo "│ 🌐 API Gateway:      http://localhost:8080               │"
echo "│ 🔐 Auth Service:     http://localhost:8081               │"
echo "│ 📤 File Upload:      http://localhost:8090               │"
echo "│ 🛠️  Admin Service:    http://localhost:8083               │"
echo "│ 💎 Membership:       http://localhost:8084               │"
echo "│ 📊 Nacos Console:    http://localhost:8848/nacos         │"
echo "│    (Username: nacos, Password: nacos)                   │"

if [ "$MODE" = "full" ] || [ "$MODE" = "staged" ]; then
    echo "│ 📁 File Management:  http://localhost:8099               │"
    echo "│ 🗑️  File Recycle:    http://localhost:8086               │"
    echo "│ 📧 Mail Service:     http://localhost:8087               │"
    echo "│ 🐰 RabbitMQ Mgmt:    http://localhost:15672              │"
    echo "│    (Username: guest, Password: guest)                   │"
    echo "│ 🗂️  Hadoop NameNode:  http://localhost:9870               │"
    echo "│ ⚡ Flink Dashboard:  http://localhost:8085               │"
fi

echo "└──────────────────────────────────────────────────────────┘"
echo ""
echo "🔍 Check service status:"
if [ "$MODE" = "core" ]; then
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml ps"
else
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml ps"
fi
echo ""
echo "📜 View logs:"
if [ "$MODE" = "core" ]; then
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml logs -f [service-name]"
else
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml logs -f [service-name]"
fi
echo ""
echo "🛑 Stop all services:"
if [ "$MODE" = "core" ]; then
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.core.yml down"
else
    echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml down"
fi

echo ""
echo "💡 Startup Mode Information:"
echo "   • core:   Fast startup, essential services only (no RabbitMQ)"
echo "   • full:   All services including RabbitMQ (may take longer)"
echo "   • staged: Infrastructure first, then apps (recommended for RabbitMQ)"
echo ""
echo "🔧 To switch modes, run:"
echo "   ./scripts/start-microservices.sh [core|full|staged]"