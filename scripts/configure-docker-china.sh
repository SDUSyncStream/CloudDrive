#!/bin/bash

echo "🔧 配置Docker中国镜像源..."

# 检查操作系统
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    echo "📱 检测到macOS系统"
    echo ""
    echo "请手动配置Docker Desktop镜像源："
    echo "1. 打开 Docker Desktop"
    echo "2. 点击 Settings (设置) → Docker Engine"
    echo "3. 在JSON配置中添加以下内容："
    echo ""
    echo '{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://registry.docker-cn.com"
  ]
}'
    echo ""
    echo "4. 点击 Apply & Restart"
    echo ""
    echo "或者使用命令行方式："
    echo "mkdir -p ~/.docker"
    echo "cat > ~/.docker/daemon.json << 'EOF'"
    echo '{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com", 
    "https://registry.docker-cn.com"
  ]
}'
    echo "EOF"
    echo ""
    echo "然后重启Docker Desktop"
    
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    echo "🐧 检测到Linux系统，自动配置镜像源..."
    
    # 创建Docker配置目录
    sudo mkdir -p /etc/docker
    
    # 备份现有配置
    if [ -f /etc/docker/daemon.json ]; then
        sudo cp /etc/docker/daemon.json /etc/docker/daemon.json.backup
        echo "✅ 已备份现有配置到 /etc/docker/daemon.json.backup"
    fi
    
    # 创建新的镜像源配置
    sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://registry.docker-cn.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false
}
EOF
    
    echo "✅ Docker镜像源配置完成"
    echo "🔄 重启Docker服务..."
    
    sudo systemctl daemon-reload
    sudo systemctl restart docker
    
    echo "✅ Docker服务已重启"
    
else
    echo "❌ 不支持的操作系统: $OSTYPE"
    exit 1
fi

echo ""
echo "🧪 测试镜像拉取..."
if docker pull hello-world:latest; then
    echo "✅ 镜像拉取测试成功！"
    docker rmi hello-world:latest >/dev/null 2>&1 || true
else
    echo "⚠️  镜像拉取仍有问题，可能需要检查网络连接"
fi

echo ""
echo "🎯 现在可以重新运行启动脚本："
echo "   ./scripts/start-minimal.sh"