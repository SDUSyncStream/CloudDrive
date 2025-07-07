#!/bin/bash

set -e

echo "🧪 Testing build environment..."

# Test Gateway build
echo "Testing Gateway build..."
cd apps/gateway
if [ -f "./mvnw" ]; then
    echo "✅ mvnw found in gateway"
    ./mvnw --version
else
    echo "❌ mvnw not found in gateway"
fi
cd ../..

# Test User Service build
echo "Testing User Service build..."
cd apps/user-service
if [ -f "./mvnw" ]; then
    echo "✅ mvnw found in user-service"
    ./mvnw --version
else
    echo "❌ mvnw not found in user-service"
fi
cd ../..

# Test Frontend
echo "Testing Frontend..."
cd apps/frontend
if [ -f "package.json" ]; then
    echo "✅ package.json found in frontend"
    npm --version
else
    echo "❌ package.json not found in frontend"
fi
cd ../..

echo "🎉 Build environment test completed!"