# Membership Service 文档中心

## 📖 文档概览

本目录包含 Membership Service 的完整技术文档，为开发、测试和运维提供详细指导。

## 📁 文档目录

### 1. [API 接口文档](./API_DOCUMENTATION.md)
- **内容**: 完整的 REST API 接口文档
- **包含**: 30+ 个接口的详细说明，包括请求参数、响应格式、使用示例
- **特色**: 
  - 支持直接访问和网关访问两种方式
  - 包含 Redis 缓存和 RabbitMQ 功能的特殊接口
  - 详细的错误码和响应格式说明

### 2. [RabbitMQ 测试指南](./RABBITMQ_TEST_GUIDE.md)
- **内容**: RabbitMQ 死信队列自动取消订单功能测试指南
- **包含**: 
  - 完整的测试步骤和验证方法
  - 快速测试配置（2分钟TTL）
  - 监控要点和功能验证清单

## 🚀 快速开始

### 服务访问方式

**推荐使用网关访问**:
```bash
# 通过网关访问 (推荐)
curl http://localhost:8080/api/membership/levels

# 直接访问服务
curl http://localhost:8084/membership/levels
```

### 核心功能

1. **会员等级管理**: 查询会员等级信息和权限
2. **支付订单系统**: 创建、支付、查询订单，支持自动取消
3. **订阅管理**: 用户订阅状态管理和权限控制
4. **Redis 缓存**: 提升查询性能，支持缓存管理
5. **RabbitMQ 队列**: 24小时自动取消未支付订单

### 技术特性

- ✅ **Redis 缓存**: 会员等级24h缓存，用户订阅1h缓存
- ✅ **RabbitMQ 死信队列**: 精确的24小时自动取消机制
- ✅ **微服务架构**: 通过Gateway统一路由
- ✅ **权限控制**: 智能会员升级限制
- ✅ **CORS 支持**: 支持跨域访问

## 📋 接口概览

| 模块 | 接口数量 | 主要功能 |
|------|----------|----------|
| 会员等级管理 | 3个 | 查询会员等级信息 |
| 支付订单管理 | 5个 | 订单创建、支付、查询、取消 |
| 用户订阅管理 | 5个 | 订阅状态管理、权限检查 |
| 缓存测试 | 5个 | Redis缓存功能验证 |
| 订单测试 | 4个 | RabbitMQ自动取消测试 |
| 快速测试 | 3个 | 快速验证消息队列功能 |

## 🔧 开发环境

### 依赖服务
- **MySQL**: 数据存储
- **Redis**: 缓存服务 (数据库4)
- **RabbitMQ**: 消息队列服务
- **Gateway**: API网关服务

### 测试数据
- **测试用户ID**: `292ddee0-518c-4ff9-9eb8-3feabbcaff27`
- **会员等级**: level001(免费版) ~ level005(企业版)

## 📞 技术支持

如有问题请参考相应文档或联系开发团队。

---

**最后更新**: 2025-07-10  
**版本**: v1.0.0  
**服务端口**: 8084