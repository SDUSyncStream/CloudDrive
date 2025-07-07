#!/bin/bash

echo "🔍 Docker网络连接诊断..."

echo ""
echo "1. 检查Docker版本..."
docker --version

echo ""
echo "2. 检查Docker状态..."
if docker info >/dev/null 2>&1; then
    echo "✅ Docker运行正常"
else
    echo "❌ Docker未运行或有问题"
    exit 1
fi

echo ""
echo "3. 测试基础网络连接..."

# 测试DNS解析
echo "   测试DNS解析..."
if nslookup registry-1.docker.io >/dev/null 2>&1; then
    echo "   ✅ DNS解析正常"
else
    echo "   ❌ DNS解析失败"
fi

# 测试网络连通性
echo "   测试Docker Hub连通性..."
if curl -s --connect-timeout 5 https://registry-1.docker.io/v2/ >/dev/null; then
    echo "   ✅ Docker Hub连接正常"
else
    echo "   ❌ Docker Hub连接失败"
fi

echo ""
echo "4. 测试镜像拉取..."

echo "   尝试拉取hello-world镜像..."
if timeout 30 docker pull hello-world:latest >/dev/null 2>&1; then
    echo "   ✅ 镜像拉取成功"
    docker rmi hello-world:latest >/dev/null 2>&1 || true
else
    echo "   ❌ 镜像拉取失败"
fi

echo ""
echo "5. 检查Docker镜像源配置..."

# 检查Docker Desktop配置 (macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if [ -f ~/.docker/daemon.json ]; then
        echo "   发现Docker配置文件: ~/.docker/daemon.json"
        echo "   当前镜像源配置:"
        cat ~/.docker/daemon.json | grep -A 10 "registry-mirrors" || echo "   未配置镜像源"
    else
        echo "   未找到Docker配置文件"
    fi
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    if [ -f /etc/docker/daemon.json ]; then
        echo "   发现Docker配置文件: /etc/docker/daemon.json"
        echo "   当前镜像源配置:"
        sudo cat /etc/docker/daemon.json | grep -A 10 "registry-mirrors" || echo "   未配置镜像源"
    else
        echo "   未找到Docker配置文件"
    fi
fi

echo ""
echo "📋 诊断完成！"
echo ""
echo "🔧 如果网络有问题，可以尝试:"
echo "1. 配置Docker镜像源: ./scripts/configure-docker-china.sh"
echo "2. 检查VPN设置"
echo "3. 重启Docker Desktop"
echo "4. 等待网络稳定后重试"