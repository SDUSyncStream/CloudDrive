# CloudDrive 数据库设计文档

## 📋 SQL文件说明

CloudDrive数据库初始化分为两个阶段：

### 🗃️ SQL文件结构
```
sql/
├── 01_create_database.sql    # 数据库和表结构创建
├── 02_insert_data.sql        # 基础数据和测试数据插入  
└── README.md                 # 本文档
```

### 🚀 快速使用

#### 数据库初始化
```bash
# 1. 创建数据库和表结构
mysql -u root -p123456 < 01_create_database.sql

# 2. 插入基础数据和测试数据
mysql -u root -p123456 < 02_insert_data.sql
```

#### Docker环境使用
```bash
# 使用Docker MySQL容器执行
docker exec -i mysql mysql -u root -p123456 < 01_create_database.sql
docker exec -i mysql mysql -u root -p123456 < 02_insert_data.sql
```

## 📊 数据库架构概述

CloudDrive采用**统一数据库架构**，所有微服务共享同一个`cloud_drive`数据库。

### 🏗️ 表结构设计

| 表名 | 说明 | 主要功能 |
|------|------|----------|
| `users` | 用户信息表 | 用户账户和基本信息管理 |
| `file_info` | 文件信息表 | 文件和文件夹的层级结构管理 |
| `file_share` | 文件分享表 | 文件分享链接管理 |
| `membership_levels` | 会员等级表 | 会员等级配置和定价 |
| `user_subscriptions` | 用户订阅表 | 用户会员订阅记录管理 |
| `payment_orders` | 支付订单表 | 支付订单和交易记录 |

## 🎯 会员等级优先级系统

### 💎 会员等级数据

| 等级名称 | Priority | 价格 | 时长 | 存储空间 | 单文件限制 |
|----------|----------|------|------|----------|------------|
| 免费版 | 0 | ¥0.00 | 永久 | 1GB | 100MB |
| 标准版 | 10 | ¥19.99 | 30天 | 5GB | 500MB |
| 高级版 | 20 | ¥39.99 | 30天 | 50GB | 5GB |
| 专业版 | 30 | ¥79.99 | 30天 | 200GB | 10GB |
| 企业版 | 40 | ¥199.99 | 30天 | 1TB | 20GB |

### 🔒 优先级规则

**核心逻辑**：用户只能订阅比当前等级更高优先级的会员

**示例**：
- 如果用户订阅了"高级版"（priority=20），则：
  - ❌ 免费版（priority=0）- 禁用
  - ❌ 标准版（priority=10）- 禁用  
  - ❌ 高级版（priority=20）- 禁用
  - ✅ 专业版（priority=30）- 可订阅
  - ✅ 企业版（priority=40）- 可订阅

**优先级分配**：每个等级间隔10个优先级值，便于后续扩展

## 🔧 数据库连接配置

### 📋 连接信息
| 配置项 | 值 |
|--------|-----|
| **主机名** | `127.0.0.1` |
| **端口** | `3306` |
| **用户名** | `root` |
| **密码** | `123456` |
| **数据库** | `cloud_drive` |

### 🔍 数据验证命令

```bash
# 检查会员等级数据
docker exec -i mysql mysql -u root -p123456 cloud_drive -e "
SELECT id, name, price, duration_days, priority 
FROM membership_levels 
ORDER BY priority;"

# 检查用户订阅数据
docker exec -i mysql mysql -u root -p123456 cloud_drive -e "
SELECT u.username, ml.name as membership_level, us.status, us.end_date
FROM user_subscriptions us
JOIN users u ON us.user_id = u.id
JOIN membership_levels ml ON us.membership_level_id = ml.id
WHERE us.status = 'active';"
```

## 🛠️ 数据库初始化问题排除

### 问题1：priority字段不存在
**症状**: 后端报错 "Unknown column 'priority'"
**解决方案**:
```bash
# 执行结构创建脚本
docker exec -i mysql mysql -u root -p123456 < 01_create_database.sql
```

### 问题2：会员等级数据缺失
**症状**: 前端显示空白或加载失败
**解决方案**:
```bash
# 执行数据插入脚本
docker exec -i mysql mysql -u root -p123456 < 02_insert_data.sql
```

### 问题3：完全重新初始化
```bash
# 停止服务并删除数据
docker-compose down
docker volume rm docker_mysql_data  # 如果使用Docker Compose

# 重新启动并初始化
docker-compose up -d
docker exec -i mysql mysql -u root -p123456 < 01_create_database.sql
docker exec -i mysql mysql -u root -p123456 < 02_insert_data.sql
```

## 📈 扩展性考虑

### 微服务数据隔离
- 统一数据库，通过业务逻辑实现服务数据隔离
- 各微服务只访问相关的表和数据

### 性能优化
- 已为常用查询字段创建索引
- 支持读写分离和分库分表扩展

---

*最后更新: 2025年7月10日 - 清理SQL文件结构，仅保留必要文件*