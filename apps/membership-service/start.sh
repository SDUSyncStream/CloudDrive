#!/bin/bash

set -e  # Exit on any error

echo "🚀 Starting Membership Service..."

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "📁 Changing to membership-service directory: $SCRIPT_DIR"
cd "$SCRIPT_DIR"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found in $SCRIPT_DIR"
    exit 1
fi

# Build the service
echo "🔨 Building Membership Service..."
if [ -f "./mvnw" ]; then
    ./mvnw clean package -DskipTests
elif command_exists mvn; then
    mvn clean package -DskipTests
else
    echo "❌ Error: Neither mvnw nor mvn found"
    exit 1
fi

echo "✅ Membership Service built successfully"

# Check if target jar exists
JAR_FILE="target/membership-service-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found at $JAR_FILE"
    exit 1
fi

# Run the service
echo "🏃 Starting Membership Service on port 8084..."
echo "💳 Membership Service will be available at: http://localhost:8084"
echo "🛑 Press Ctrl+C to stop the service"
echo ""

java -jar "$JAR_FILE"