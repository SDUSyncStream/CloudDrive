#!/bin/bash

set -e  # Exit on any error

echo "🚀 Starting CloudDrive microservices..."

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

if [ ! -d "apps/user-service/target" ] || [ -z "$(ls -A apps/user-service/target/*.jar 2>/dev/null)" ]; then
    NEED_BUILD=true
fi

if [ "$NEED_BUILD" = true ]; then
    echo "⚠️  Services not built yet. Running build first..."
    ./scripts/build-all.sh
fi

echo ""
echo "🛑 Stopping any existing containers..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml down

echo ""
echo "🏗️  Building and starting all microservices..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml up --build -d

echo ""
echo "⏳ Waiting for services to start..."
sleep 10

echo ""
echo "🎉 Microservices started!"
echo ""
echo "📱 Service URLs:"
echo "┌──────────────────────────────────────────────────────────┐"
echo "│ 🎨 Frontend:         http://localhost:3000               │"
echo "│ 🌐 API Gateway:      http://localhost:8080               │"
echo "│ 👤 User Service:     http://localhost:8081               │"
echo "│ 📁 File Service:     http://localhost:8082               │"
echo "│ 🛠️  Admin Service:    http://localhost:8083               │"
echo "│ 💎 Membership:       http://localhost:8084               │"
echo "│ 📊 Nacos Console:    http://localhost:8848/nacos         │"
echo "│    (Username: nacos, Password: nacos)                   │"
echo "│ 🗂️  Hadoop NameNode:  http://localhost:9870               │"
echo "│ ⚡ Flink Dashboard:  http://localhost:8081               │"
echo "└──────────────────────────────────────────────────────────┘"
echo ""
echo "🔍 Check service status:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml ps"
echo ""
echo "📜 View logs:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml logs -f [service-name]"
echo ""
echo "🛑 Stop all services:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.microservices.yml down"