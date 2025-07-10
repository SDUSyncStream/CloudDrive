# Membership Service API Documentation

## 概述

Membership Service 提供了完整的会员管理功能，包括会员等级查询、订单管理、订阅管理和自动订单取消等功能。

## 服务访问地址

### 直接访问服务
- **服务地址**: `http://localhost:8084`
- **说明**: 直接访问membership-service

### 通过网关访问（推荐）
- **网关地址**: `http://localhost:8080/api`
- **说明**: 通过gateway服务统一路由访问
- **路由规则**: `/api/membership/**` → `membership-service:8084`

**示例**:
- 直接访问: `http://localhost:8084/membership/levels`
- 网关访问: `http://localhost:8080/api/membership/levels`

---

## 1. 会员等级管理 API

### 1.1 获取所有会员等级

- **直接访问**: `GET http://localhost:8084/membership/levels`
- **网关访问**: `GET http://localhost:8080/api/membership/levels`
- **描述**: 获取所有可用的会员等级列表
- **响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "level001",
      "name": "免费版",
      "storageQuota": 1073741824,
      "maxFileSize": 104857600,
      "price": 0.00,
      "durationDays": 0,
      "features": "基础存储,单文件100MB限制",
      "priority": 0,
      "storageQuotaFormatted": "1 GB",
      "maxFileSizeFormatted": "100 MB",
      "isRecommended": false
    }
  ]
}
```

### 1.2 根据ID获取会员等级

- **直接访问**: `GET http://localhost:8084/membership/levels/{id}`
- **网关访问**: `GET http://localhost:8080/api/membership/levels/{id}`
- **参数**: 
  - `id` (String): 会员等级ID
- **描述**: 根据会员等级ID获取详细信息

### 1.3 根据名称获取会员等级

- **直接访问**: `GET http://localhost:8084/membership/levels/name/{name}`
- **网关访问**: `GET http://localhost:8080/api/membership/levels/name/{name}`
- **参数**: 
  - `name` (String): 会员等级名称
- **描述**: 根据会员等级名称获取详细信息

---

## 2. 支付订单管理 API

### 2.1 创建支付订单

- **直接访问**: `POST http://localhost:8084/payment/orders`
- **网关访问**: `POST http://localhost:8080/api/payment/orders`
- **描述**: 创建新的支付订单（自动发送到RabbitMQ队列，24小时后未支付将自动取消）
- **请求体**:
```json
{
  "userId": "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
  "membershipLevelId": "level002",
  "paymentMethod": "alipay"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "id": "1943121100959940609",
    "userId": "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
    "membershipLevelId": "level002",
    "membershipLevelName": "标准版",
    "orderNumber": "ORDER202507100932260839",
    "amount": 19.99,
    "paymentMethod": "alipay",
    "status": "pending",
    "createdAt": "2025-07-10T09:32:27"
  }
}
```

### 2.2 处理支付

- **直接访问**: `POST http://localhost:8084/payment/orders/{orderId}/pay`
- **网关访问**: `POST http://localhost:8080/api/payment/orders/{orderId}/pay`
- **参数**: 
  - `orderId` (String): 订单ID
  - `transactionId` (String): 交易ID
- **描述**: 处理订单支付，更新订单状态并创建用户订阅

### 2.3 获取订单详情

- **直接访问**: `GET http://localhost:8084/payment/orders/{orderId}`
- **网关访问**: `GET http://localhost:8080/api/payment/orders/{orderId}`
- **参数**: 
  - `orderId` (String): 订单ID
- **描述**: 获取指定订单的详细信息

### 2.4 获取用户所有订单

- **直接访问**: `GET http://localhost:8084/payment/orders/user/{userId}`
- **网关访问**: `GET http://localhost:8080/api/payment/orders/user/{userId}`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 获取用户的所有支付订单列表

### 2.5 取消订单

- **直接访问**: `POST http://localhost:8084/payment/orders/{orderId}/cancel`
- **网关访问**: `POST http://localhost:8080/api/payment/orders/{orderId}/cancel`
- **参数**: 
  - `orderId` (String): 订单ID
- **描述**: 手动取消待支付状态的订单

---

## 3. 用户订阅管理 API

### 3.1 获取用户所有订阅

- **直接访问**: `GET http://localhost:8084/subscription/user/{userId}`
- **网关访问**: `GET http://localhost:8080/api/subscription/user/{userId}`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 获取用户的所有订阅记录

### 3.2 获取用户当前订阅

- **直接访问**: `GET http://localhost:8084/subscription/user/{userId}/current`
- **网关访问**: `GET http://localhost:8080/api/subscription/user/{userId}/current`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 获取用户当前有效的订阅信息（支持Redis缓存）
- **响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": "sub001",
    "userId": "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
    "membershipLevelId": "level005",
    "membershipLevelName": "企业版",
    "startDate": "2025-07-10T09:32:27",
    "endDate": "2025-08-10T09:32:27",
    "status": "active",
    "paymentMethod": "wechat",
    "paymentAmount": 199.99,
    "isActive": true,
    "daysRemaining": 30
  }
}
```

### 3.3 取消订阅

- **直接访问**: `POST http://localhost:8084/subscription/{subscriptionId}/cancel`
- **网关访问**: `POST http://localhost:8080/api/subscription/{subscriptionId}/cancel`
- **参数**: 
  - `subscriptionId` (String): 订阅ID
- **描述**: 取消指定的订阅

### 3.4 创建默认订阅

- **直接访问**: `POST http://localhost:8084/subscription/user/{userId}/default`
- **网关访问**: `POST http://localhost:8080/api/subscription/user/{userId}/default`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 为用户创建默认的免费版订阅

### 3.5 检查订阅权限

- **直接访问**: `GET http://localhost:8084/subscription/user/{userId}/can-subscribe/{membershipLevelId}`
- **网关访问**: `GET http://localhost:8080/api/subscription/user/{userId}/can-subscribe/{membershipLevelId}`
- **参数**: 
  - `userId` (String): 用户ID
  - `membershipLevelId` (String): 会员等级ID
- **描述**: 检查用户是否可以订阅指定的会员等级

---

## 4. 缓存测试 API

### 4.1 测试会员等级缓存

- **直接访问**: `GET http://localhost:8084/cache-test/levels`
- **网关访问**: `GET http://localhost:8080/api/cache-test/levels`
- **描述**: 测试会员等级Redis缓存功能

### 4.2 测试用户订阅缓存

- **直接访问**: `GET http://localhost:8084/cache-test/user-subscription/{userId}`
- **网关访问**: `GET http://localhost:8080/api/cache-test/user-subscription/{userId}`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 测试用户订阅Redis缓存功能

### 4.3 查看缓存键

- **直接访问**: `GET http://localhost:8084/cache-test/cache-keys`
- **网关访问**: `GET http://localhost:8080/api/cache-test/cache-keys`
- **描述**: 查看Redis中所有缓存键

### 4.4 清空缓存

- **直接访问**: `DELETE http://localhost:8084/cache-test/clear-cache`
- **网关访问**: `DELETE http://localhost:8080/api/cache-test/clear-cache`
- **描述**: 清空所有Redis缓存

### 4.5 清除用户缓存

- **直接访问**: `POST http://localhost:8084/cache-test/evict-user-cache/{userId}`
- **网关访问**: `POST http://localhost:8080/api/cache-test/evict-user-cache/{userId}`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 清除指定用户的订阅缓存

---

## 5. 订单测试 API

### 5.1 创建测试订单

- **直接访问**: `POST http://localhost:8084/order-test/create-test-order`
- **网关访问**: `POST http://localhost:8080/api/order-test/create-test-order`
- **参数**: 
  - `userId` (可选): 用户ID
- **描述**: 创建测试订单，会发送到RabbitMQ队列进行自动取消监控

### 5.2 测试支付订单

- **直接访问**: `POST http://localhost:8084/order-test/pay-order/{orderId}`
- **网关访问**: `POST http://localhost:8080/api/order-test/pay-order/{orderId}`
- **参数**: 
  - `orderId` (String): 订单ID
- **描述**: 测试订单支付功能

### 5.3 查看订单状态

- **直接访问**: `GET http://localhost:8084/order-test/order-status/{orderId}`
- **网关访问**: `GET http://localhost:8080/api/order-test/order-status/{orderId}`
- **参数**: 
  - `orderId` (String): 订单ID
- **描述**: 查看订单当前状态

### 5.4 获取用户订单

- **直接访问**: `GET http://localhost:8084/order-test/user-orders/{userId}`
- **网关访问**: `GET http://localhost:8080/api/order-test/user-orders/{userId}`
- **参数**: 
  - `userId` (String): 用户ID
- **描述**: 获取用户所有订单列表

---

## 6. 快速测试 API

### 6.1 发送快速测试消息

- **直接访问**: `POST http://localhost:8084/quick-test/send-quick-message`
- **网关访问**: `POST http://localhost:8080/api/quick-test/send-quick-message`
- **描述**: 发送2分钟TTL的测试消息到RabbitMQ队列，用于快速验证自动取消功能

### 6.2 查看队列配置

- **直接访问**: `GET http://localhost:8084/quick-test/queue-config`
- **网关访问**: `GET http://localhost:8080/api/quick-test/queue-config`
- **描述**: 查看RabbitMQ队列配置信息

### 6.3 检查订单

- **直接访问**: `GET http://localhost:8084/quick-test/check-order/{orderId}`
- **网关访问**: `GET http://localhost:8080/api/quick-test/check-order/{orderId}`
- **参数**: 
  - `orderId` (String): 订单ID
- **描述**: 检查指定订单的详细状态

---

## 响应格式

所有API响应都遵循统一格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {},
  "timestamp": 1752127624785
}
```

### 状态码说明

- `200`: 成功
- `500`: 服务器内部错误

---

## 特殊功能

### 1. Redis缓存

- **会员等级**: 缓存24小时，提升查询性能
- **用户订阅**: 缓存1小时，支付后自动清除缓存

### 2. RabbitMQ自动取消订单

- **机制**: 死信队列 + TTL
- **时间**: 24小时未支付自动取消
- **队列**: `membership.order.pending.queue` → `membership.order.cancel.queue`
- **保护**: 已支付订单不会被取消

### 3. 会员权限控制

- **升级限制**: 只能购买更高等级的会员
- **重复购买**: 防止购买同等级或更低等级会员

---

## 网关路由配置

Gateway服务提供统一的API入口，将请求路由到对应的微服务：

```
http://localhost:8080/api/membership/** → membership-service:8084
http://localhost:8080/api/payment/**    → membership-service:8084  
http://localhost:8080/api/subscription/** → membership-service:8084
http://localhost:8080/api/cache-test/** → membership-service:8084
http://localhost:8080/api/order-test/** → membership-service:8084
http://localhost:8080/api/quick-test/** → membership-service:8084
```

---

## 测试用户ID

- `292ddee0-518c-4ff9-9eb8-3feabbcaff27` - 测试用户，已有多个订单记录

## 注意事项

1. **推荐使用网关访问**: 统一入口，便于管理和监控
2. 所有API支持跨域访问（CORS）
3. 创建订单后会自动发送消息到RabbitMQ进行监控
4. 缓存功能需要Redis服务运行
5. 自动取消功能需要RabbitMQ服务运行
6. 部分测试接口仅用于开发和测试环境