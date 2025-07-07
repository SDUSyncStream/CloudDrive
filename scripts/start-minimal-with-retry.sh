#!/bin/bash

set -e

echo "🚀 Starting CloudDrive minimal stack with network retry..."

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
        exit 1
    fi
}

# Function to test network connectivity
test_docker_connectivity() {
    echo "🔍 Testing Docker Hub connectivity..."
    if docker pull hello-world >/dev/null 2>&1; then
        echo "✅ Docker Hub connectivity OK"
        docker rmi hello-world >/dev/null 2>&1 || true
        return 0
    else
        echo "⚠️  Docker Hub connectivity issues detected"
        return 1
    fi
}

# Function to suggest network solutions
suggest_network_solutions() {
    echo ""
    echo "🔧 Network connectivity issues detected. Try these solutions:"
    echo ""
    echo "1. 🌐 Setup Docker registry mirrors:"
    echo "   ./scripts/setup-docker-mirror.sh"
    echo ""
    echo "2. 🔄 Or restart Docker Desktop and try again"
    echo ""
    echo "3. 📡 Check your network connection and VPN settings"
    echo ""
    echo "4. ⏳ Wait a moment and retry (sometimes temporary network issues)"
    echo ""
}

DOCKER_COMPOSE_CMD=$(get_docker_compose_cmd)

# Test connectivity first
if ! test_docker_connectivity; then
    suggest_network_solutions
    echo "❓ Do you want to continue anyway? (y/N): "
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "❌ Aborted. Please fix network issues and try again."
        exit 1
    fi
fi

echo ""
echo "🏗️  Starting services with retry logic..."

# Retry logic for docker compose
MAX_RETRIES=3
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo "🔄 Attempt $((RETRY_COUNT + 1))/$MAX_RETRIES..."
    
    if $DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml up --build -d; then
        echo "✅ Services started successfully!"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo "⚠️  Attempt failed, retrying in 10 seconds..."
            sleep 10
        else
            echo "❌ All attempts failed. Please check your network connection."
            suggest_network_solutions
            exit 1
        fi
    fi
done

echo ""
echo "🎉 Minimal stack started successfully!"
echo ""
echo "📱 Available Services:"
echo "┌─────────────────────────────────────────────────────────┐"
echo "│ 🎨 Frontend:        http://localhost:3000              │"
echo "│ 🌐 API Gateway:     http://localhost:8080              │"
echo "│ 👤 User Service:    http://localhost:8081              │"
echo "│ 📊 Nacos Console:   http://localhost:8848/nacos        │"
echo "│ 🗄️  MySQL:           localhost:3306                    │"
echo "│ 🔴 Redis:           localhost:6379                     │"
echo "└─────────────────────────────────────────────────────────┘"