#!/bin/bash

set -e  # Exit on any error

echo "🚀 Starting Admin Service..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "📁 Changing to admin-service directory: $SCRIPT_DIR"
cd "$SCRIPT_DIR"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found in $SCRIPT_DIR"
    exit 1
fi

# Build the service
echo "🔨 Building Admin Service..."
if [ -f "./mvnw" ]; then
    ./mvnw clean package -DskipTests
elif command_exists mvn; then
    mvn clean package -DskipTests
else
    echo "❌ Error: Neither mvnw nor mvn found"
    exit 1
fi

echo "✅ Admin Service built successfully"

# Check if target jar exists
JAR_FILE="target/admin-service-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found at $JAR_FILE"
    exit 1
fi

# Run the service
echo "🏃 Starting Admin Service on port 8083..."
echo "📊 Admin Service will be available at: http://localhost:8083"
echo "🛑 Press Ctrl+C to stop the service"
echo ""

java -jar "$JAR_FILE"