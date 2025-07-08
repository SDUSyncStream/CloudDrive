#!/bin/bash

# ============================================================================
# CloudDrive Frontend Start Script  
# ============================================================================
# Description: 使用Docker启动CloudDrive前端服务
#
# 功能:
# - 自动构建Vue 3前端应用
# - 使用固定的Docker Compose配置
# - 配置API代理到后端Gateway
# - 独立于后端服务运行
#
# 端口:
# - Frontend: http://localhost:3000
#
# 使用方法: ./scripts/start-frontend.sh
#
# 要求:
# - Docker Desktop
# - Docker Compose
# - 后端服务运行在localhost:8080 (Gateway)
#
# 注意: 需要先启动后端服务才能正常使用前端功能
# ============================================================================

set -e  # Exit on any error

echo "🚀 Starting CloudDrive Frontend with Docker..."

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

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "📁 Project root: $PROJECT_ROOT"

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

# Change to project root
cd "$PROJECT_ROOT"

# Check if docker-compose file exists
if [ ! -f "docker/docker-compose.frontend.yml" ]; then
    echo "❌ Error: docker/docker-compose.frontend.yml not found"
    echo "💡 Make sure you are in the project root directory"
    exit 1
fi

echo "📝 Using existing frontend configuration"

# Build Frontend if needed
echo "🔍 Checking if frontend is built..."
FRONTEND_DIR="apps/frontend"
if [ -d "$FRONTEND_DIR" ]; then
    cd "$FRONTEND_DIR"
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        if command_exists npm; then
            npm install
        elif command_exists yarn; then
            yarn install
        elif command_exists pnpm; then
            pnpm install
        else
            echo "❌ Error: No package manager found (npm, yarn, or pnpm)"
            exit 1
        fi
    fi
    
    echo "🔨 Building frontend for production..."
    if command_exists npm; then
        npm run build
    elif command_exists yarn; then
        yarn build
    elif command_exists pnpm; then
        pnpm build
    else
        echo "❌ Error: No package manager found to build frontend"
        exit 1
    fi
    echo "✅ Frontend built successfully"
    cd "$PROJECT_ROOT"
else
    echo "❌ Error: Frontend directory not found"
    exit 1
fi

echo ""
echo "🛑 Stopping any existing frontend container..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.frontend.yml down 2>/dev/null || true

echo ""
echo "🏗️  Starting frontend container..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.frontend.yml up --build -d

echo ""
echo "⏳ Waiting for frontend to start..."
sleep 10

echo ""
echo "🎉 Frontend started successfully!"
echo ""
echo "📱 Frontend Service:"
echo "┌─────────────────────────────────────────────────────────┐"
echo "│ 🎨 Frontend:        http://localhost:3000              │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""
echo "💡 Note: Make sure the backend services are running for full functionality"
echo "   You can start backend services with: ./scripts/start-minimal.sh"
echo ""
echo "🔍 Check frontend status:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.frontend.yml ps"
echo ""
echo "🛑 Stop frontend:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.frontend.yml down"