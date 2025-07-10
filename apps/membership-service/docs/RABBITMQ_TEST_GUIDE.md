# RabbitMQ自动取消订单功能测试指南

## 测试准备

1. **确保服务运行**：
   - RabbitMQ服务已启动
   - membership-service已重启（包含最新代码）

2. **RabbitMQ管理界面**：
   - 访问 http://localhost:15672
   - 用户名/密码：guest/guest

## 测试步骤

### 步骤1：创建测试订单

```bash
curl -X POST http://localhost:8084/order-test/create-test-order \
-H "Content-Type: application/json" \
-w "\n\nHTTP Status: %{http_code}\n"
```

**预期结果**：
- 返回订单创建成功
- 在RabbitMQ管理界面看到消息进入 `membership.order.pending.queue`

### 步骤2：验证队列状态

在RabbitMQ管理界面检查：
- **Queues** 标签页
- 查看 `membership.order.pending.queue` 
- 应该有 1 条消息，TTL 24小时

### 步骤3：测试支付阻止自动取消

```bash
# 假设订单ID为 {ORDER_ID}
curl -X POST http://localhost:8084/order-test/pay-order/{ORDER_ID} \
-H "Content-Type: application/json"
```

**预期结果**：
- 订单状态变为 "paid"
- 即使24小时后也不会被自动取消

### 步骤4：测试自动取消功能

1. 创建新测试订单（重复步骤1）
2. **不支付**，等待24小时
3. 检查订单状态：

```bash
curl -X GET http://localhost:8084/order-test/order-status/{ORDER_ID}
```

**预期结果**：
- 24小时后订单状态自动变为 "cancelled"

## 快速测试（2分钟TTL）

为了快速验证，可以：

1. 修改 `RabbitMQConfig.java` 中的 `ORDER_TTL` 为 `120000L` (2分钟)
2. 重启服务
3. 创建测试订单
4. 等待2分钟观察自动取消

## 监控要点

1. **RabbitMQ队列**：
   - `membership.order.pending.queue` - 待处理订单
   - `membership.order.cancel.queue` - 死信队列

2. **日志输出**：
   - 创建订单时的消息发送日志
   - 死信消费者处理过期订单的日志

3. **数据库状态**：
   - `payment_orders` 表中订单状态变化

## 功能验证清单

- [ ] 订单创建时发送消息到RabbitMQ
- [ ] 消息在队列中正确设置TTL
- [ ] 支付成功的订单不会被取消
- [ ] 未支付订单24小时后自动取消
- [ ] 死信队列正确接收过期消息
- [ ] OrderCancelService正确处理死信消息
- [ ] 订单状态正确更新为"cancelled"