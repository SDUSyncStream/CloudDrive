# CloudDrive 数据库设计文档

## 📊 数据库架构概述

CloudDrive采用**统一数据库架构**，所有微服务共享同一个`cloud_drive`数据库，通过表设计和业务逻辑实现数据隔离和服务自治。

### 🏗️ 统一数据库架构

| 数据库名 | 说明 | 表数量 | 服务使用 |
|---------|------|--------|----------|
| `cloud_drive` | 统一业务数据库 | 5个表 | 所有微服务共享 |

**架构优势:**
- ✅ **简化部署**: 单一数据库，降低运维复杂度
- ✅ **事务一致性**: 支持跨表事务，保证数据一致性
- ✅ **开发效率**: 简化微服务间数据关联
- ✅ **成本优化**: 减少数据库实例和资源消耗

## 📋 数据库表结构详细说明

### 数据库: `cloud_drive`

#### 1. users - 用户信息表
用户账户和基本信息管理，支持管理员和普通用户。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | VARCHAR(36) | 用户ID | 主键 |
| userlevel | INT | 用户级别 | 默认0，1为管理员 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| email | VARCHAR(100) | 邮箱地址 | 唯一，非空 |
| password_hash | VARCHAR(255) | 密码哈希 | 非空 |
| avatar | VARCHAR(255) | 头像URL | 可空 |
| storage_quota | BIGINT | 存储配额(字节) | 默认1GB |
| storage_used | BIGINT | 已使用存储(字节) | 默认0 |
| created_at | TIMESTAMP | 创建时间 | 默认当前时间 |
| updated_at | TIMESTAMP | 更新时间 | 自动更新 |

**索引:**
- `idx_username` - 用户名索引
- `idx_email` - 邮箱索引

#### 2. files - 文件信息表
文件和文件夹的层级结构管理，支持文件共享和组织。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | VARCHAR(36) | 文件ID | 主键 |
| name | VARCHAR(255) | 文件/文件夹名 | 非空 |
| path | VARCHAR(400) | 完整路径 | 非空 |
| size | BIGINT | 文件大小(字节) | 默认0 |
| type | VARCHAR(50) | 文件类型 | 非空 |
| mime_type | VARCHAR(100) | MIME类型 | 可空 |
| is_directory | BOOLEAN | 是否为目录 | 默认false |
| parent_id | VARCHAR(36) | 父目录ID | 外键，可空 |
| owner_id | VARCHAR(36) | 所有者ID | 外键，非空 |
| created_at | TIMESTAMP | 创建时间 | 默认当前时间 |
| updated_at | TIMESTAMP | 更新时间 | 自动更新 |

**外键约束:**
- `parent_id` → `files(id)` ON DELETE CASCADE
- `owner_id` → `users(id)` ON DELETE CASCADE

**索引:**
- `idx_parent_id` - 父目录索引
- `idx_owner_id` - 所有者索引
- `idx_path` - 路径索引

#### 3. file_shares - 文件分享表
文件分享链接管理，支持密码保护和过期时间控制。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | VARCHAR(36) | 分享ID | 主键 |
| file_id | VARCHAR(36) | 文件ID | 外键，非空 |
| share_code | VARCHAR(20) | 分享码 | 唯一，非空 |
| password | VARCHAR(100) | 访问密码 | 可空 |
| expires_at | TIMESTAMP | 过期时间 | 可空 |
| download_count | INT | 下载次数 | 默认0 |
| max_downloads | INT | 最大下载次数 | 可空 |
| created_at | TIMESTAMP | 创建时间 | 默认当前时间 |

**外键约束:**
- `file_id` → `files(id)` ON DELETE CASCADE

**索引:**
- `idx_share_code` - 分享码索引
- `idx_file_id` - 文件ID索引

#### 4. membership_levels - 会员等级表
会员等级配置，定义不同会员的权益和定价。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | VARCHAR(36) | 等级ID | 主键 |
| name | VARCHAR(50) | 等级名称 | 唯一，非空 |
| storage_quota | BIGINT | 存储配额(字节) | 非空 |
| max_file_size | BIGINT | 单文件最大大小(字节) | 非空 |
| price | DECIMAL(10,2) | 价格(元) | 非空 |
| duration_days | INT | 有效期(天) | 非空 |
| features | TEXT | 功能特性描述 | 可空 |
| created_at | TIMESTAMP | 创建时间 | 默认当前时间 |
| updated_at | TIMESTAMP | 更新时间 | 自动更新 |

**索引:**
- `idx_name` - 名称索引

#### 5. user_subscriptions - 用户订阅表
用户会员订阅记录，管理会员状态和付费信息。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | VARCHAR(36) | 订阅ID | 主键 |
| user_id | VARCHAR(36) | 用户ID | 外键，非空 |
| membership_level_id | VARCHAR(36) | 会员等级ID | 外键，非空 |
| start_date | TIMESTAMP | 开始时间 | 非空 |
| end_date | TIMESTAMP | 结束时间 | 非空 |
| status | ENUM | 状态 | active/expired/cancelled |
| payment_method | VARCHAR(50) | 支付方式 | 可空 |
| payment_amount | DECIMAL(10,2) | 支付金额 | 可空 |
| created_at | TIMESTAMP | 创建时间 | 默认当前时间 |
| updated_at | TIMESTAMP | 更新时间 | 自动更新 |

**外键约束:**
- `user_id` → `users(id)` ON DELETE CASCADE
- `membership_level_id` → `membership_levels(id)` ON DELETE CASCADE

**索引:**
- `idx_user_id` - 用户ID索引
- `idx_membership_level_id` - 会员等级索引
- `idx_status` - 状态索引

## 🎯 初始数据说明

数据库包含以下初始数据：

### 会员等级数据 (4条)
- **免费版**: 1GB存储，单文件100MB，免费
- **基础版**: 10GB存储，单文件1GB，9.99元/月
- **高级版**: 100GB存储，单文件5GB，19.99元/月
- **企业版**: 1TB存储，单文件20GB，99.99元/月

### 管理员账户 (1条)
- **用户名**: admin
- **邮箱**: admin@clouddrive.com
- **密码**: admin123 (需要前端加密)

## 🔧 数据库初始化

### 自动初始化流程

1. **MySQL容器启动**: Docker自动挂载`/sql`目录到`/docker-entrypoint-initdb.d/`
2. **脚本执行**: MySQL自动执行`init.sql`脚本
3. **数据库创建**: 创建`cloud_drive`数据库
4. **表结构创建**: 创建5个业务表
5. **初始数据插入**: 插入会员等级和管理员数据

### 初始化脚本位置
```
sql/
├── init.sql        # 统一初始化脚本
├── extra/          # 额外脚本目录
└── README.md       # 本文档
```

## 🔌 数据库连接配置

### 📋 连接信息
| 配置项 | 值 |
|--------|-----|
| **连接类型** | MySQL |
| **主机名/IP** | `localhost` 或 `127.0.0.1` |
| **端口** | `3307` |
| **用户名** | `root` |
| **密码** | `root123` |
| **数据库** | `cloud_drive` |

### 🗂️ 数据库结构
连接成功后，可以看到：
- `cloud_drive` - 主业务数据库 (5个表)
- MySQL系统数据库 (information_schema, mysql, performance_schema, sys)

### 📝 详细连接步骤

#### 1. 启动CloudDrive服务
```bash
# 启动最小服务栈
./scripts/start-minimal.sh

# 或启动完整微服务栈
./scripts/start-microservices.sh
```

#### 2. 等待MySQL服务启动
- 等待约1-2分钟直到看到 "🎉 Minimal backend stack started successfully!"
- 或者检查服务状态：
```bash
docker-compose -f docker/docker-compose.minimal.yml ps
```
- 确认MySQL状态为 "Up X minutes (healthy)"

#### 3. 使用Navicat连接

**新建连接：**
1. 打开Navicat
2. 点击左上角 "连接" → "MySQL"
3. 填写连接信息：
   - **连接名**：CloudDrive (可自定义)
   - **主机**：`localhost`
   - **端口**：`3307`
   - **用户名**：`root`
   - **密码**：`root123`
4. 点击 "测试连接"
5. 提示连接成功后，点击 "确定"

#### 4. 验证数据库结构

**查看表结构：**
1. 展开CloudDrive连接
2. 展开`cloud_drive`数据库
3. 查看5个表：users, files, file_shares, membership_levels, user_subscriptions
4. 右键表名 → "打开表"，查看初始数据

#### 5. 数据验证检查项

**基础表验证：**
- ✅ `users` 表：包含admin管理员账户
- ✅ `files` 表：文件表结构正确，支持层级目录
- ✅ `file_shares` 表：文件分享表结构正确
- ✅ `membership_levels` 表：包含4个会员等级
- ✅ `user_subscriptions` 表：用户订阅表结构正确

## 🛠️ 数据库初始化问题排除

### 常见问题和解决方案

#### 问题1：看不到`cloud_drive`数据库
**症状**: 连接成功但只能看到MySQL系统数据库
**原因**: 数据库初始化脚本未执行
**解决方案**:
```bash
# 停止服务并删除数据卷
docker-compose -f docker/docker-compose.minimal.yml down
docker volume rm docker_mysql_data

# 重新启动服务以触发初始化
docker-compose -f docker/docker-compose.minimal.yml up -d
```

#### 问题2：连接超时
**症状**: Navicat提示连接超时
**原因**: MySQL服务未完全启动或端口冲突
**解决方案**:
```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 检查端口占用
lsof -i :3307

# 重启MySQL服务
docker-compose -f docker/docker-compose.minimal.yml restart mysql
```

#### 问题3：认证失败
**症状**: 提示用户名或密码错误
**原因**: 密码错误或用户权限问题
**解决方案**: 确认使用正确的用户名密码 (root/root123)

#### 问题4：数据不完整
**症状**: 数据库存在但表或数据缺失
**原因**: 初始化脚本部分失败
**解决方案**:
```bash
# 手动执行初始化脚本
docker exec mysql mysql -u root -proot123 < /docker-entrypoint-initdb.d/init.sql

# 或重新完全初始化
docker-compose -f docker/docker-compose.minimal.yml down
docker volume rm docker_mysql_data
docker-compose -f docker/docker-compose.minimal.yml up -d
```

### 🔍 验证命令

如果无法使用Navicat，可以通过命令行验证：

```bash
# 检查数据库列表
docker exec mysql mysql -u root -proot123 -e "SHOW DATABASES;"

# 检查cloud_drive数据库表
docker exec mysql mysql -u root -proot123 cloud_drive -e "SHOW TABLES;"

# 检查users表数据
docker exec mysql mysql -u root -proot123 cloud_drive -e "SELECT username, email FROM users;"

# 检查会员等级数据
docker exec mysql mysql -u root -proot123 cloud_drive -e "SELECT name, storage_quota, price FROM membership_levels;"

# 检查表记录统计
docker exec mysql mysql -u root -proot123 cloud_drive -e "
SELECT 
    'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'files', COUNT(*) FROM files
UNION ALL  
SELECT 'file_shares', COUNT(*) FROM file_shares
UNION ALL
SELECT 'membership_levels', COUNT(*) FROM membership_levels
UNION ALL
SELECT 'user_subscriptions', COUNT(*) FROM user_subscriptions;"
```

### ✅ 成功验证标志

如果初始化成功，您应该能够：
1. 🔗 **成功连接**: Navicat显示连接成功
2. 📂 **看到cloud_drive数据库**: 包含业务数据的主数据库
3. 🗃️ **5个业务表**: users, files, file_shares, membership_levels, user_subscriptions
4. 📊 **初始数据**: 4个会员等级 + 1个管理员账户
5. 🔍 **表结构正确**: 外键约束、索引、数据类型匹配

## 🚀 部署说明

### Docker自动初始化
- MySQL容器启动时自动执行`/docker-entrypoint-initdb.d/`目录下的SQL文件
- 统一的`init.sql`脚本确保数据库结构一致性

### 健康检查
- 添加了MySQL健康检查，确保数据库完全启动后再启动应用服务
- 其他服务依赖MySQL健康状态启动

### 数据持久化
- 使用Docker卷`mysql_data`确保数据在容器重启后保持
- 数据库配置和数据完全持久化

### 重要提醒
- ⚠️ **初始化脚本仅在首次启动时执行**
- ⚠️ **如需重新初始化，必须删除数据卷**
- ⚠️ **生产环境请修改默认密码**

## 📈 扩展性考虑

### 微服务数据隔离
- 虽然使用统一数据库，但通过业务逻辑实现服务数据隔离
- 各微服务只访问相关的表和数据

### 分库分表准备
- 表设计支持水平扩展
- 用户ID作为分片键，便于后续分库分表

### 读写分离
- 数据库可以配置主从复制
- 应用层可以实现读写分离

## 🔍 监控和维护

### 性能监控
- 监控数据库连接数、查询性能、存储使用情况
- 关注热点表的查询性能

### 备份策略
- 定期备份`cloud_drive`数据库
- 重要操作前进行数据备份

### 索引优化
- 已为常用查询字段创建索引
- 可根据实际使用情况调整索引策略

---

*最后更新: 2025年7月8日 - 更新为统一数据库架构，添加初始化问题排除指南*