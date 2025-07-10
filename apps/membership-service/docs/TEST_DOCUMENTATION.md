# Membership Service 测试文档

## 1. 服务概述

Membership Service 是云盘系统的会员管理服务，提供会员等级管理、支付订单处理、用户订阅管理等核心功能。

**服务地址**:
- 直接访问: `http://localhost:8084`
- 网关访问: `http://localhost:8080/api`

---

## 2. 测试环境准备

### 2.1 依赖服务
- MySQL数据库
- Redis缓存服务
- RabbitMQ消息队列

### 2.2 测试用户
- 测试用户ID: `292ddee0-518c-4ff9-9eb8-3feabbcaff27`

### 2.3 响应状态码
- `200`: 操作成功
- `500`: 服务器内部错误

---

## 3. 会员等级管理 API 测试

### 3.1 获取所有会员等级

**接口**: `GET /membership/levels`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 正常获取会员等级列表 | GET | `/membership/levels` | 200 | 返回所有会员等级 |

**测试数据**:
```bash
curl -X GET "http://localhost:8084/membership/levels"
```

**期望返回结果**:
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
  ],
  "timestamp": 1752127624785
}
```

### 3.2 根据ID获取会员等级

**接口**: `GET /membership/levels/{id}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询存在的会员等级 | GET | `/membership/levels/level001` | 200 | 返回对应会员等级 |
| 查询不存在的会员等级 | GET | `/membership/levels/invalid_id` | 200 | 返回错误信息 |

**测试数据**:
```bash
# 正常查询
curl -X GET "http://localhost:8084/membership/levels/level001"

# 异常查询
curl -X GET "http://localhost:8084/membership/levels/invalid_id"
```

**期望返回结果**:
- 成功: 返回对应会员等级详情
- 失败: `{"code": 200, "message": "会员等级不存在"}`

### 3.3 根据名称获取会员等级

**接口**: `GET /membership/levels/name/{name}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询存在的会员等级名称 | GET | `/membership/levels/name/免费版` | 200 | 返回对应会员等级 |
| 查询不存在的会员等级名称 | GET | `/membership/levels/name/不存在的等级` | 200 | 返回错误信息 |

---

## 4. 支付订单管理 API 测试

### 4.1 创建支付订单

**接口**: `POST /payment/orders`

**测试用例**:

| 测试场景 | 请求方式 | 请求体 | 期望状态码 | 期望响应 |
|---------|---------|-------|----------|---------|
| 正常创建订单 | POST | 有效订单数据 | 200 | 订单创建成功 |
| 缺少必填字段 | POST | 缺少userId | 400/500 | 验证错误 |
| 无效会员等级ID | POST | 不存在的membershipLevelId | 500 | 创建失败 |

**测试数据**:
```bash
# 正常创建订单
curl -X POST "http://localhost:8084/payment/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
    "membershipLevelId": "level002",
    "paymentMethod": "alipay"
  }'

# 缺少必填字段
curl -X POST "http://localhost:8084/payment/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "membershipLevelId": "level002",
    "paymentMethod": "alipay"
  }'
```

**期望返回结果**:
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

### 4.2 处理支付

**接口**: `POST /payment/orders/{orderId}/pay`

**测试用例**:

| 测试场景 | 请求方式 | URL参数 | 期望状态码 | 期望响应 |
|---------|---------|--------|----------|---------|
| 正常支付 | POST | 有效orderId + transactionId | 200 | 支付成功 |
| 订单不存在 | POST | 无效orderId | 500 | 支付失败 |
| 订单已支付 | POST | 已支付的orderId | 500 | 重复支付错误 |

**测试数据**:
```bash
curl -X POST "http://localhost:8084/payment/orders/1943121100959940609/pay?transactionId=txn_12345"
```

### 4.3 获取订单详情

**接口**: `GET /payment/orders/{orderId}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询存在的订单 | GET | `/payment/orders/{validOrderId}` | 200 | 返回订单详情 |
| 查询不存在的订单 | GET | `/payment/orders/invalid_id` | 200 | 订单不存在 |

### 4.4 获取用户所有订单

**接口**: `GET /payment/orders/user/{userId}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询用户订单 | GET | `/payment/orders/user/292ddee0-518c-4ff9-9eb8-3feabbcaff27` | 200 | 返回用户订单列表 |

### 4.5 取消订单

**接口**: `POST /payment/orders/{orderId}/cancel`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 取消待支付订单 | POST | `/payment/orders/{pendingOrderId}/cancel` | 200 | 订单取消成功 |
| 取消已支付订单 | POST | `/payment/orders/{paidOrderId}/cancel` | 500 | 取消失败 |

---

## 5. 用户订阅管理 API 测试

### 5.1 获取用户所有订阅

**接口**: `GET /subscription/user/{userId}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询用户订阅 | GET | `/subscription/user/292ddee0-518c-4ff9-9eb8-3feabbcaff27` | 200 | 返回订阅列表 |

**测试数据**:
```bash
curl -X GET "http://localhost:8084/subscription/user/292ddee0-518c-4ff9-9eb8-3feabbcaff27"
```

### 5.2 获取用户当前订阅

**接口**: `GET /subscription/user/{userId}/current`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 查询有订阅的用户 | GET | `/subscription/user/{userId}/current` | 200 | 返回当前订阅 |
| 查询无订阅的用户 | GET | `/subscription/user/{newUserId}/current` | 200 | 用户暂无有效订阅 |

**期望返回结果**:
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

### 5.3 取消订阅

**接口**: `POST /subscription/{subscriptionId}/cancel`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 取消有效订阅 | POST | `/subscription/{validSubId}/cancel` | 200 | 订阅取消成功 |
| 取消不存在的订阅 | POST | `/subscription/invalid_id/cancel` | 500 | 取消失败 |

### 5.4 创建默认订阅

**接口**: `POST /subscription/user/{userId}/default`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 为新用户创建默认订阅 | POST | `/subscription/user/{newUserId}/default` | 200 | 默认订阅创建成功 |

### 5.5 检查订阅权限

**接口**: `GET /subscription/user/{userId}/can-subscribe/{membershipLevelId}`

**测试用例**:

| 测试场景 | 请求方式 | URL | 期望状态码 | 期望响应 |
|---------|---------|-----|----------|---------|
| 检查升级权限 | GET | `/subscription/user/{userId}/can-subscribe/level003` | 200 | 返回true/false |
| 检查降级权限 | GET | `/subscription/user/{userId}/can-subscribe/level001` | 200 | 返回false |

---

## 6. 测试专用 API

### 6.1 订单测试接口

**接口**: `POST /order-test/create-test-order`

**测试用例**:

| 测试场景 | 请求方式 | 参数 | 期望状态码 | 期望响应 |
|---------|---------|------|----------|---------|
| 创建测试订单 | POST | userId(可选) | 200 | 测试订单创建成功 |

**测试数据**:
```bash
# 使用默认用户
curl -X POST "http://localhost:8084/order-test/create-test-order"

# 指定用户
curl -X POST "http://localhost:8084/order-test/create-test-order?userId=test-user-123"
```

### 6.2 测试支付订单

**接口**: `POST /order-test/pay-order/{orderId}`

**测试数据**:
```bash
curl -X POST "http://localhost:8084/order-test/pay-order/1943121100959940609"
```

### 6.3 查看订单状态

**接口**: `GET /order-test/order-status/{orderId}`

**测试数据**:
```bash
curl -X GET "http://localhost:8084/order-test/order-status/1943121100959940609"
```

---

## 7. 缓存功能测试

### 7.1 会员等级缓存测试

**接口**: `GET /cache-test/levels`

**测试用例**:

| 测试场景 | 预期行为 | 验证方法 |
|---------|---------|---------|
| 首次查询 | 从数据库读取并缓存 | 查看日志或响应时间 |
| 二次查询 | 从缓存读取 | 响应时间明显变快 |

### 7.2 用户订阅缓存测试

**接口**: `GET /cache-test/user-subscription/{userId}`

**测试数据**:
```bash
curl -X GET "http://localhost:8084/cache-test/user-subscription/292ddee0-518c-4ff9-9eb8-3feabbcaff27"
```

### 7.3 缓存管理

**查看缓存键**:
```bash
curl -X GET "http://localhost:8084/cache-test/cache-keys"
```

**清空缓存**:
```bash
curl -X DELETE "http://localhost:8084/cache-test/clear-cache"
```

**清除用户缓存**:
```bash
curl -X POST "http://localhost:8084/cache-test/evict-user-cache/292ddee0-518c-4ff9-9eb8-3feabbcaff27"
```

---

## 8. RabbitMQ 自动取消订单测试

### 8.1 快速测试

**接口**: `POST /quick-test/send-quick-message`

**测试步骤**:
1. 发送2分钟TTL的测试消息
2. 等待2分钟
3. 验证消息是否被自动处理

**测试数据**:
```bash
# 发送快速测试消息
curl -X POST "http://localhost:8084/quick-test/send-quick-message"

# 查看队列配置
curl -X GET "http://localhost:8084/quick-test/queue-config"
```

### 8.2 24小时自动取消测试

**测试步骤**:
1. 创建测试订单
2. 不进行支付
3. 等待24小时或修改TTL设置
4. 验证订单是否自动取消

---

## 9. 数据模型说明

### 9.1 会员等级 (MembershipLevelDTO)
```json
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
```

### 9.2 支付订单 (PaymentOrderDTO)
```json
{
  "id": "1943121100959940609",
  "userId": "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
  "membershipLevelId": "level002",
  "membershipLevelName": "标准版",
  "orderNumber": "ORDER202507100932260839",
  "amount": 19.99,
  "paymentMethod": "alipay",
  "status": "pending",
  "transactionId": null,
  "paidAt": null,
  "createdAt": "2025-07-10T09:32:27"
}
```

### 9.3 用户订阅 (UserSubscriptionDTO)
```json
{
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
```

---

## 10. 测试检查清单

### 10.1 功能测试
- [ ] 会员等级查询功能正常
- [ ] 订单创建流程完整
- [ ] 支付处理逻辑正确
- [ ] 订阅管理功能完备
- [ ] 缓存机制工作正常
- [ ] 消息队列自动取消功能

### 10.2 异常测试
- [ ] 无效参数处理
- [ ] 重复操作防护
- [ ] 权限控制验证
- [ ] 错误信息返回准确

### 10.3 性能测试
- [ ] 缓存提升查询性能
- [ ] 并发订单处理
- [ ] 大量数据查询响应时间

### 10.4 集成测试
- [ ] 与Redis集成正常
- [ ] 与RabbitMQ集成正常
- [ ] 与数据库交互正常
- [ ] 网关路由功能正常

---

## 11. 注意事项

1. **测试环境**: 确保MySQL、Redis、RabbitMQ服务正常运行
2. **数据清理**: 测试完成后清理测试数据
3. **缓存清理**: 测试缓存功能时注意清理缓存
4. **消息队列**: 测试自动取消功能时注意监控队列状态
5. **网关访问**: 建议通过网关进行测试以验证完整的请求流程