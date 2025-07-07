#!/bin/bash

set -e  # Exit on any error

echo "🚀 Starting CloudDrive minimal stack..."

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

# Create minimal docker-compose override
cat > docker/docker-compose.minimal.yml << 'EOF'
services:
  # Core infrastructure
  mysql:
    image: mysql:8.0
    container_name: mysql
    restart: always
    ports:
      - "3307:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=cloud_drive
      - MYSQL_USER=cloud_drive
      - MYSQL_PASSWORD=cloud123
    volumes:
      - mysql_data:/var/lib/mysql
      - ../sql:/docker-entrypoint-initdb.d
    networks:
      - cloud-drive-network

  redis:
    image: redis:7-alpine
    container_name: redis
    restart: always
    ports:
      - "6379:6379"
    networks:
      - cloud-drive-network

  # Nacos service registry
  nacos:
    image: nacos/nacos-server:v2.2.0
    platform: linux/amd64
    container_name: nacos-server
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      - MODE=standalone
      - JVM_XMS=256m
      - JVM_XMX=256m
    volumes:
      - nacos_data:/home/nacos/data
    depends_on:
      - mysql
    networks:
      - cloud-drive-network

  # API Gateway
  gateway:
    build:
      context: ../apps/gateway
      dockerfile: ../../docker/Dockerfile.gateway
    container_name: gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848
      - SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848
    networks:
      - cloud-drive-network

  # User Service
  user-service:
    build:
      context: ../apps/user-service
      dockerfile: ../../docker/Dockerfile.user-service
    container_name: user-service
    ports:
      - "8081:8081"
    depends_on:
      - nacos
      - mysql
      - redis
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848
      - SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/cloud_drive
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root123
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    networks:
      - cloud-drive-network

  # Frontend
  frontend:
    build:
      context: ../apps/frontend
      dockerfile: ../../docker/Dockerfile.frontend
    container_name: frontend
    ports:
      - "3000:3000"
    depends_on:
      - gateway
    environment:
      - NODE_ENV=production
      - VITE_API_URL=http://gateway:8080
    networks:
      - cloud-drive-network

volumes:
  mysql_data:
    driver: local
  nacos_data:
    driver: local

networks:
  cloud-drive-network:
    driver: bridge
EOF

echo "📝 Created minimal configuration"

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
fi

echo ""
echo "🛑 Stopping any existing containers..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml down 2>/dev/null || true

echo ""
echo "🏗️  Starting minimal microservices stack..."
$DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml up --build -d

echo ""
echo "⏳ Waiting for services to start..."
sleep 15

echo ""
echo "🎉 Minimal stack started successfully!"
echo ""
echo "📱 Available Services:"
echo "┌─────────────────────────────────────────────────────────┐"
echo "│ 🎨 Frontend:        http://localhost:3000              │"
echo "│ 🌐 API Gateway:     http://localhost:8080              │"
echo "│ 👤 User Service:    http://localhost:8081              │"
echo "│ 📊 Nacos Console:   http://localhost:8848/nacos        │"
echo "│    (Username: nacos, Password: nacos)                 │"
echo "│ 🗄️  MySQL:           localhost:3307                    │"
echo "│ 🔴 Redis:           localhost:6379                     │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""
echo "✨ This minimal stack includes:"
echo "   • Core authentication and user management"
echo "   • Service discovery and configuration"
echo "   • Database and caching"
echo "   • Frontend interface"
echo ""
echo "🔍 Check service status:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml ps"
echo ""
echo "🛑 Stop services:"
echo "   $DOCKER_COMPOSE_CMD -f docker/docker-compose.minimal.yml down"