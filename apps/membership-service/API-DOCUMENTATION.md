# CloudDrive 会员服务 API 接口文档

## 服务概述

会员服务 (Membership Service) 负责管理用户会员等级、订阅和支付功能。服务运行在端口 8084。

**基础URL**: `http://localhost:8084`

## 通用响应格式

所有接口都使用统一的响应格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {...},
  "timestamp": 1703123456789
}
```

- `code`: 状态码 (200=成功, 500=错误)
- `message`: 响应消息
- `data`: 具体数据内容
- `timestamp`: 响应时间戳

## 1. 会员等级管理 API

### 1.1 获取所有会员等级

**接口地址**: `GET /api/membership/levels`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "level_001",
      "name": "普通会员",
      "storageQuota": 5368709120,
      "maxFileSize": 104857600,
      "price": 0.00,
      "durationDays": 365,
      "features": "基础存储功能",
      "storageQuotaFormatted": "5GB",
      "maxFileSizeFormatted": "100MB",
      "isRecommended": false
    },
    {
      "id": "level_002",
      "name": "VIP会员",
      "storageQuota": 107374182400,
      "maxFileSize": 1073741824,
      "price": 99.00,
      "durationDays": 365,
      "features": "高级存储+文件分享",
      "storageQuotaFormatted": "100GB",
      "maxFileSizeFormatted": "1GB",
      "isRecommended": true
    }
  ],
  "timestamp": 1703123456789
}
```

### 1.2 根据ID获取会员等级

**接口地址**: `GET /api/membership/levels/{id}`

**路径参数**:
- `id` (string): 会员等级ID

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": "level_002",
    "name": "VIP会员",
    "storageQuota": 107374182400,
    "maxFileSize": 1073741824,
    "price": 99.00,
    "durationDays": 365,
    "features": "高级存储+文件分享",
    "storageQuotaFormatted": "100GB",
    "maxFileSizeFormatted": "1GB",
    "isRecommended": true
  },
  "timestamp": 1703123456789
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "会员等级不存在",
  "data": null,
  "timestamp": 1703123456789
}
```

### 1.3 根据名称获取会员等级

**接口地址**: `GET /api/membership/levels/name/{name}`

**路径参数**:
- `name` (string): 会员等级名称

**响应示例**: 同1.2

## 2. 支付订单管理 API

### 2.1 创建支付订单

**接口地址**: `POST /api/payment/orders`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": "user_123",
  "membershipLevelId": "level_002",
  "paymentMethod": "alipay"
}
```

**请求参数说明**:
- `userId` (string, 必填): 用户ID
- `membershipLevelId` (string, 必填): 会员等级ID
- `paymentMethod` (string, 必填): 支付方式
  - `alipay`: 支付宝
  - `wechat`: 微信支付
  - `bank_card`: 银行卡
  - `bank_transfer`: 银行转账

**响应示例**:
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "id": "order_001",
    "userId": "user_123",
    "membershipLevelId": "level_002",
    "membershipLevelName": "VIP会员",
    "orderNumber": "ORD20231201001",
    "amount": 99.00,
    "paymentMethod": "alipay",
    "status": "PENDING",
    "transactionId": null,
    "paidAt": null,
    "createdAt": "2023-12-01T10:30:00"
  },
  "timestamp": 1703123456789
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "创建订单失败: 用户不存在",
  "data": null,
  "timestamp": 1703123456789
}
```

### 2.2 处理支付

**接口地址**: `POST /api/payment/orders/{orderId}/pay`

**路径参数**:
- `orderId` (string): 订单ID

**查询参数**:
- `transactionId` (string): 第三方支付交易ID

**响应示例**:
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "id": "order_001",
    "userId": "user_123",
    "membershipLevelId": "level_002",
    "membershipLevelName": "VIP会员",
    "orderNumber": "ORD20231201001",
    "amount": 99.00,
    "paymentMethod": "alipay",
    "status": "PAID",
    "transactionId": "alipay_123456789",
    "paidAt": "2023-12-01T10:35:00",
    "createdAt": "2023-12-01T10:30:00"
  },
  "timestamp": 1703123456789
}
```

### 2.3 获取支付订单详情

**接口地址**: `GET /api/payment/orders/{orderId}`

**路径参数**:
- `orderId` (string): 订单ID

**响应示例**: 同2.2

**错误响应**:
```json
{
  "code": 500,
  "message": "订单不存在",
  "data": null,
  "timestamp": 1703123456789
}
```

### 2.4 获取用户支付订单列表

**接口地址**: `GET /api/payment/orders/user/{userId}`

**路径参数**:
- `userId` (string): 用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "order_001",
      "userId": "user_123",
      "membershipLevelId": "level_002",
      "membershipLevelName": "VIP会员",
      "orderNumber": "ORD20231201001",
      "amount": 99.00,
      "paymentMethod": "alipay",
      "status": "PAID",
      "transactionId": "alipay_123456789",
      "paidAt": "2023-12-01T10:35:00",
      "createdAt": "2023-12-01T10:30:00"
    }
  ],
  "timestamp": 1703123456789
}
```

## 3. 用户订阅管理 API

### 3.1 获取用户订阅列表

**接口地址**: `GET /api/subscription/user/{userId}`

**路径参数**:
- `userId` (string): 用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "sub_001",
      "userId": "user_123",
      "membershipLevelId": "level_002",
      "membershipLevelName": "VIP会员",
      "startDate": "2023-12-01T10:35:00",
      "endDate": "2024-12-01T10:35:00",
      "status": "ACTIVE",
      "paymentMethod": "alipay",
      "paymentAmount": 99.00,
      "createdAt": "2023-12-01T10:35:00",
      "isActive": true,
      "daysRemaining": 365
    }
  ],
  "timestamp": 1703123456789
}
```

### 3.2 获取用户当前有效订阅

**接口地址**: `GET /api/subscription/user/{userId}/current`

**路径参数**:
- `userId` (string): 用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": "sub_001",
    "userId": "user_123",
    "membershipLevelId": "level_002",
    "membershipLevelName": "VIP会员",
    "startDate": "2023-12-01T10:35:00",
    "endDate": "2024-12-01T10:35:00",
    "status": "ACTIVE",
    "paymentMethod": "alipay",
    "paymentAmount": 99.00,
    "createdAt": "2023-12-01T10:35:00",
    "isActive": true,
    "daysRemaining": 365
  },
  "timestamp": 1703123456789
}
```

**无有效订阅响应**:
```json
{
  "code": 500,
  "message": "用户暂无有效订阅",
  "data": null,
  "timestamp": 1703123456789
}
```

### 3.3 取消订阅

**接口地址**: `POST /api/subscription/{subscriptionId}/cancel`

**路径参数**:
- `subscriptionId` (string): 订阅ID

**响应示例**:
```json
{
  "code": 200,
  "message": "订阅取消成功",
  "data": "订阅取消成功",
  "timestamp": 1703123456789
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "取消订阅失败: 订阅不存在或已过期",
  "data": null,
  "timestamp": 1703123456789
}
```

## 4. 数据模型

### 4.1 会员等级 (MembershipLevelDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 会员等级唯一标识 |
| name | String | 会员等级名称 |
| storageQuota | Long | 存储配额 (字节) |
| maxFileSize | Long | 最大单文件大小 (字节) |
| price | BigDecimal | 价格 |
| durationDays | Integer | 有效期 (天) |
| features | String | 功能特性描述 |
| storageQuotaFormatted | String | 格式化的存储配额显示 |
| maxFileSizeFormatted | String | 格式化的文件大小显示 |
| isRecommended | Boolean | 是否推荐 |

### 4.2 支付订单 (PaymentOrderDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 订单唯一标识 |
| userId | String | 用户ID |
| membershipLevelId | String | 会员等级ID |
| membershipLevelName | String | 会员等级名称 |
| orderNumber | String | 订单号 |
| amount | BigDecimal | 订单金额 |
| paymentMethod | String | 支付方式 |
| status | String | 订单状态 (PENDING/PAID/CANCELLED) |
| transactionId | String | 第三方交易ID |
| paidAt | LocalDateTime | 支付时间 |
| createdAt | LocalDateTime | 创建时间 |

### 4.3 用户订阅 (UserSubscriptionDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 订阅唯一标识 |
| userId | String | 用户ID |
| membershipLevelId | String | 会员等级ID |
| membershipLevelName | String | 会员等级名称 |
| startDate | LocalDateTime | 开始时间 |
| endDate | LocalDateTime | 结束时间 |
| status | String | 订阅状态 (ACTIVE/EXPIRED/CANCELLED) |
| paymentMethod | String | 支付方式 |
| paymentAmount | BigDecimal | 支付金额 |
| createdAt | LocalDateTime | 创建时间 |
| isActive | Boolean | 是否有效 |
| daysRemaining | Long | 剩余天数 |

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 500 | 服务器内部错误 |

## 6. 使用示例

### 6.1 完整的会员购买流程

```bash
# 1. 获取会员等级列表
curl -X GET "http://localhost:8084/api/membership/levels"

# 2. 创建支付订单
curl -X POST "http://localhost:8084/api/payment/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_123",
    "membershipLevelId": "level_002",
    "paymentMethod": "alipay"
  }'

# 3. 模拟支付完成
curl -X POST "http://localhost:8084/api/payment/orders/order_001/pay?transactionId=alipay_123456789"

# 4. 查看用户当前订阅
curl -X GET "http://localhost:8084/api/subscription/user/user_123/current"
```

### 6.2 JavaScript 调用示例

```javascript
// 获取会员等级列表
async function getMembershipLevels() {
  const response = await fetch('http://localhost:8084/api/membership/levels');
  const result = await response.json();
  return result.data;
}

// 创建支付订单
async function createPaymentOrder(userId, membershipLevelId, paymentMethod) {
  const response = await fetch('http://localhost:8084/api/payment/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId,
      membershipLevelId,
      paymentMethod
    })
  });
  const result = await response.json();
  return result;
}

// 获取用户当前订阅
async function getCurrentSubscription(userId) {
  const response = await fetch(`http://localhost:8084/api/subscription/user/${userId}/current`);
  const result = await response.json();
  return result.data;
}
```

## 7. 注意事项

1. **CORS支持**: 所有接口都支持跨域请求 (`@CrossOrigin(origins = "*")`)
2. **参数验证**: 创建订单接口使用了 JSR-303 验证注解
3. **错误处理**: 所有接口都包含完善的错误处理和友好的错误消息
4. **时间格式**: 所有时间字段使用 ISO 8601 格式 (`yyyy-MM-ddTHH:mm:ss`)
5. **金额格式**: 所有金额使用 BigDecimal 类型，精确到分

## 8. 后续扩展计划

- [ ] 添加退款功能
- [ ] 支持批量操作
- [ ] 添加订阅自动续费
- [ ] 增加优惠券系统
- [ ] 实现分页查询
- [ ] 添加订阅统计报表