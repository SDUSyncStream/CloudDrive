#!/bin/bash

echo "🔧 Setting up Docker registry mirrors for China..."

# Create Docker daemon configuration directory if it doesn't exist
sudo mkdir -p /etc/docker

# Create daemon.json with Chinese mirrors
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://reg-mirror.qiniu.com",
    "https://registry.docker-cn.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false
}
EOF

echo "✅ Docker daemon configuration updated"
echo "🔄 Please restart Docker Desktop to apply changes"
echo ""
echo "📝 Alternative: You can also set mirrors via Docker Desktop GUI:"
echo "   Docker Desktop → Settings → Docker Engine → Edit daemon.json"