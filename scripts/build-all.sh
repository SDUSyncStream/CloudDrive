#!/bin/bash

set -e  # Exit on any error

echo "🚀 Building CloudDrive microservices..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to use Maven or Maven Wrapper
get_maven_cmd() {
    if [ -f "./mvnw" ]; then
        echo "./mvnw"
    elif command_exists mvn; then
        echo "mvn"
    else
        echo "❌ Error: Neither mvn nor mvnw found. Please install Maven or use Maven Wrapper."
        exit 1
    fi
}

# Function to check Node.js and npm
check_nodejs() {
    if ! command_exists node; then
        echo "❌ Error: Node.js not found. Please install Node.js 18+ from https://nodejs.org/"
        exit 1
    fi
    
    if ! command_exists npm; then
        echo "❌ Error: npm not found. Please install npm."
        exit 1
    fi
    
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -lt 16 ]; then
        echo "⚠️  Warning: Node.js version $NODE_VERSION detected. Recommended: 18+"
    fi
}

# Check Node.js for frontend
echo "🔍 Checking Node.js environment..."
check_nodejs
echo "✅ Node.js environment OK"

# Build services
MAVEN_CMD=$(get_maven_cmd)
echo "📦 Using Maven command: $MAVEN_CMD"

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

# Build all Maven services
build_maven_service "API Gateway" "apps/gateway"
build_maven_service "User Service" "apps/user-service"
build_maven_service "File Service" "apps/file-service"
build_maven_service "Admin Service" "apps/admin-service"
build_maven_service "Membership Service" "apps/membership-service"

# Build Frontend
echo "🎨 Building Frontend..."
if [ -d "apps/frontend" ]; then
    cd apps/frontend
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        npm install
    fi
    npm run build
    echo "✅ Frontend built successfully"
    cd - >/dev/null
else
    echo "❌ Error: apps/frontend directory not found"
    exit 1
fi

echo ""
echo "🎉 All services built successfully!"
echo ""
echo "Next steps:"
echo "1. Start microservices: ./scripts/start-microservices.sh"
echo "2. Or start minimal stack: ./scripts/start-minimal.sh"