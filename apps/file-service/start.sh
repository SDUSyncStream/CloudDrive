#!/bin/bash

set -e  # Exit on any error

echo "🚀 Starting File Service..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "📁 Changing to file-service directory: $SCRIPT_DIR"
cd "$SCRIPT_DIR"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found in $SCRIPT_DIR"
    exit 1
fi

# Build the service
echo "🔨 Building File Service..."
if [ -f "./mvnw" ]; then
    ./mvnw clean package -DskipTests
elif command_exists mvn; then
    mvn clean package -DskipTests
else
    echo "❌ Error: Neither mvnw nor mvn found"
    exit 1
fi

echo "✅ File Service built successfully"

# Check if target jar exists
JAR_FILE="target/file-service-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found at $JAR_FILE"
    exit 1
fi

# Run the service
echo "🏃 Starting File Service on port 8082..."
echo "📁 File Service will be available at: http://localhost:8082"
echo "🛑 Press Ctrl+C to stop the service"
echo ""

java -jar "$JAR_FILE"