# CloudDrive 数据库设计文档

## 📊 数据库架构概述

CloudDrive采用微服务架构，为每个服务创建独立的数据库，确保数据隔离和服务自治。

### 🏗️ 微服务数据库分配

| 服务 | 数据库名 | 说明 | 表数量 |
|------|----------|------|--------|
| 用户服务 | `user_service_db` | 用户管理、会话、设置 | 3个表 |
| 文件服务 | `file_service_db` | 文件管理、分享、版本控制 | 4个表 |
| 管理服务 | `admin_service_db` | 系统管理、日志、举报 | 4个表 |
| 会员服务 | `membership_service_db` | 会员等级、订阅、支付 | 4个表 |

## 📋 数据库表结构详细说明

### 1. 用户服务数据库 (user_service_db)

#### user_info - 用户信息表
基于原始`mengpan.sql`的`user_info`表设计，保持业务连续性。

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | VARCHAR(15) | 用户ID，主键 |
| nick_name | VARCHAR(20) | 昵称，唯一 |
| email | VARCHAR(150) | 邮箱，唯一 |
| password | VARCHAR(32) | 密码(MD5) |
| avatar | VARCHAR(200) | 头像地址 |
| phone | VARCHAR(20) | 手机号 |
| join_time | DATETIME | 注册时间 |
| last_login_time | DATETIME | 最后登录时间 |
| status | TINYINT(1) | 状态:0禁用1正常2待验证 |
| use_space | BIGINT | 已使用空间(字节) |
| total_space | BIGINT | 总空间(字节) |

#### user_sessions - 用户会话表
管理用户登录会话和令牌。

| 字段 | 类型 | 说明 |
|------|------|------|
| session_id | VARCHAR(36) | 会话ID，主键 |
| user_id | VARCHAR(15) | 用户ID |
| token | VARCHAR(255) | 访问令牌 |
| device_info | VARCHAR(200) | 设备信息 |
| ip_address | VARCHAR(45) | IP地址 |
| login_time | DATETIME | 登录时间 |
| expire_time | DATETIME | 过期时间 |
| status | TINYINT(1) | 状态:0失效1有效 |

#### user_settings - 用户设置表
存储用户个性化配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | VARCHAR(15) | 用户ID，主键 |
| theme | VARCHAR(20) | 主题:light/dark |
| language | VARCHAR(10) | 语言 |
| email_notify | TINYINT(1) | 邮件通知:0关闭1开启 |
| auto_backup | TINYINT(1) | 自动备份:0关闭1开启 |
| privacy_level | TINYINT | 隐私级别:1公开2好友3私密 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. 文件服务数据库 (file_service_db)

#### file_info - 文件信息表
基于原始`mengpan.sql`的`file_info`表设计，支持文件层级结构。

| 字段 | 类型 | 说明 |
|------|------|------|
| file_id | VARCHAR(10) | 文件ID，复合主键 |
| user_id | VARCHAR(10) | 用户ID，复合主键 |
| file_md5 | VARCHAR(32) | 文件MD5值 |
| file_pid | VARCHAR(10) | 父级目录ID |
| file_size | BIGINT | 文件大小(字节) |
| file_name | VARCHAR(200) | 文件名 |
| file_cover | VARCHAR(100) | 文件封面 |
| file_path | VARCHAR(100) | 文件路径 |
| create_time | DATETIME | 创建时间 |
| last_update_time | DATETIME | 最后更新时间 |
| folder_type | TINYINT(1) | 类型:0文件1目录 |
| file_category | TINYINT(1) | 分类:1视频2音频3图片4文档5其他 |
| file_type | TINYINT | 类型:1视频2音频3图片4pdf5doc6excel7txt8code9zip10其他 |
| status | TINYINT | 状态:0转码中1转码失败2转码成功 |
| recovery_time | DATETIME | 进入回收站时间 |
| del_flag | TINYINT(1) | 删除标记:0删除1回收站2正常 |

#### file_shares - 文件分享表
支持公开分享和密码分享。

| 字段 | 类型 | 说明 |
|------|------|------|
| share_id | VARCHAR(20) | 分享ID，主键 |
| file_id | VARCHAR(10) | 文件ID |
| user_id | VARCHAR(10) | 分享用户ID |
| share_code | VARCHAR(8) | 分享码，唯一 |
| password | VARCHAR(10) | 访问密码 |
| share_type | TINYINT | 分享类型:1公开分享2密码分享 |
| expire_time | DATETIME | 过期时间 |
| download_count | INT | 下载次数 |
| max_downloads | INT | 最大下载次数(-1无限制) |
| create_time | DATETIME | 创建时间 |
| status | TINYINT | 状态:0失效1有效 |

#### file_versions - 文件版本表
文件版本控制功能。

| 字段 | 类型 | 说明 |
|------|------|------|
| version_id | VARCHAR(20) | 版本ID，主键 |
| file_id | VARCHAR(10) | 文件ID |
| user_id | VARCHAR(10) | 用户ID |
| version_num | INT | 版本号 |
| file_size | BIGINT | 文件大小 |
| file_path | VARCHAR(200) | 文件存储路径 |
| create_time | DATETIME | 创建时间 |
| remark | VARCHAR(500) | 版本备注 |

#### upload_tasks - 上传任务表
支持大文件分片上传。

| 字段 | 类型 | 说明 |
|------|------|------|
| task_id | VARCHAR(32) | 任务ID，主键 |
| user_id | VARCHAR(10) | 用户ID |
| file_name | VARCHAR(200) | 文件名 |
| file_size | BIGINT | 文件大小 |
| chunk_total | INT | 分片总数 |
| chunk_uploaded | INT | 已上传分片数 |
| status | TINYINT | 状态:0上传中1已完成2已失败3已暂停 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 3. 管理服务数据库 (admin_service_db)

#### admin_users - 管理员表
支持多级权限管理。

| 字段 | 类型 | 说明 |
|------|------|------|
| admin_id | VARCHAR(15) | 管理员ID，主键 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(32) | 密码(MD5) |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(150) | 邮箱，唯一 |
| phone | VARCHAR(20) | 手机号 |
| role_type | TINYINT | 角色类型:1超级管理员2系统管理员3内容管理员 |
| permissions | TEXT | JSON格式权限配置 |
| status | TINYINT(1) | 状态:0禁用1正常 |
| create_time | DATETIME | 创建时间 |
| last_login_time | DATETIME | 最后登录时间 |

#### system_configs - 系统配置表
动态系统配置管理。

| 字段 | 类型 | 说明 |
|------|------|------|
| config_id | VARCHAR(20) | 配置ID，主键 |
| config_key | VARCHAR(100) | 配置键，唯一 |
| config_value | TEXT | 配置值 |
| config_desc | VARCHAR(200) | 配置描述 |
| config_type | VARCHAR(20) | 配置类型:string/number/boolean/json |
| is_public | TINYINT(1) | 是否对用户公开:0否1是 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### operation_logs - 操作日志表
完整的操作审计日志。

| 字段 | 类型 | 说明 |
|------|------|------|
| log_id | VARCHAR(32) | 日志ID，主键 |
| operator_id | VARCHAR(15) | 操作者ID |
| operator_type | TINYINT | 操作者类型:1管理员2用户 |
| operation_type | VARCHAR(50) | 操作类型 |
| operation_desc | VARCHAR(500) | 操作描述 |
| target_type | VARCHAR(50) | 目标类型 |
| target_id | VARCHAR(50) | 目标ID |
| ip_address | VARCHAR(45) | IP地址 |
| user_agent | VARCHAR(500) | 用户代理 |
| create_time | DATETIME | 创建时间 |

#### user_reports - 用户举报表
用户内容举报处理流程。

| 字段 | 类型 | 说明 |
|------|------|------|
| report_id | VARCHAR(20) | 举报ID，主键 |
| reporter_id | VARCHAR(15) | 举报人ID |
| reported_user_id | VARCHAR(15) | 被举报用户ID |
| reported_file_id | VARCHAR(10) | 被举报文件ID |
| report_type | TINYINT | 举报类型:1违规内容2版权侵犯3恶意行为4其他 |
| report_reason | VARCHAR(1000) | 举报原因 |
| evidence_files | TEXT | JSON格式证据文件列表 |
| status | TINYINT | 处理状态:0待处理1处理中2已处理3已驳回 |
| admin_id | VARCHAR(15) | 处理管理员ID |
| admin_remark | VARCHAR(500) | 管理员备注 |
| create_time | DATETIME | 举报时间 |
| handle_time | DATETIME | 处理时间 |

### 4. 会员服务数据库 (membership_service_db)

#### membership_levels - 会员等级表
多种会员等级配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| level_id | VARCHAR(10) | 等级ID，主键 |
| level_name | VARCHAR(50) | 等级名称，唯一 |
| level_code | VARCHAR(20) | 等级代码，唯一 |
| storage_quota | BIGINT | 存储配额(字节) |
| max_file_size | BIGINT | 单文件最大大小(字节) |
| download_speed_limit | INT | 下载速度限制(KB/s,-1无限制) |
| upload_speed_limit | INT | 上传速度限制(KB/s,-1无限制) |
| share_expire_days | INT | 分享链接最长有效期(天) |
| price | DECIMAL(10,2) | 价格(元/月) |
| features | TEXT | JSON格式功能特性 |
| sort_order | INT | 排序顺序 |
| status | TINYINT(1) | 状态:0禁用1启用 |
| create_time | DATETIME | 创建时间 |

#### user_memberships - 用户会员表
用户会员状态管理。

| 字段 | 类型 | 说明 |
|------|------|------|
| membership_id | VARCHAR(20) | 会员记录ID，主键 |
| user_id | VARCHAR(15) | 用户ID |
| level_id | VARCHAR(10) | 会员等级ID |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| auto_renew | TINYINT(1) | 自动续费:0关闭1开启 |
| payment_method | VARCHAR(20) | 支付方式 |
| original_price | DECIMAL(10,2) | 原价 |
| actual_price | DECIMAL(10,2) | 实付金额 |
| discount_amount | DECIMAL(10,2) | 优惠金额 |
| status | TINYINT | 状态:0未生效1生效中2已过期3已取消 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### payment_orders - 支付订单表
支付订单和交易记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| order_id | VARCHAR(32) | 订单ID，主键 |
| user_id | VARCHAR(15) | 用户ID |
| membership_id | VARCHAR(20) | 会员记录ID |
| order_type | TINYINT | 订单类型:1新购2续费3升级 |
| amount | DECIMAL(10,2) | 订单金额 |
| discount_amount | DECIMAL(10,2) | 优惠金额 |
| actual_amount | DECIMAL(10,2) | 实付金额 |
| payment_method | VARCHAR(20) | 支付方式:alipay/wechat/bank |
| payment_channel | VARCHAR(50) | 支付渠道 |
| trade_no | VARCHAR(64) | 第三方交易号 |
| status | TINYINT | 状态:0待支付1支付中2已支付3支付失败4已退款 |
| create_time | DATETIME | 创建时间 |
| pay_time | DATETIME | 支付时间 |
| callback_data | TEXT | 回调数据 |

#### subscription_history - 订阅历史表
会员操作历史记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| history_id | VARCHAR(20) | 历史记录ID，主键 |
| user_id | VARCHAR(15) | 用户ID |
| operation_type | TINYINT | 操作类型:1开通2续费3升级4降级5取消6恢复 |
| old_level_id | VARCHAR(10) | 原等级ID |
| new_level_id | VARCHAR(10) | 新等级ID |
| old_end_time | DATETIME | 原结束时间 |
| new_end_time | DATETIME | 新结束时间 |
| amount | DECIMAL(10,2) | 涉及金额 |
| operator_type | TINYINT | 操作者类型:1用户2管理员3系统 |
| operator_id | VARCHAR(15) | 操作者ID |
| remark | VARCHAR(500) | 备注说明 |
| create_time | DATETIME | 操作时间 |

## 🎯 测试数据说明

每个数据库包含丰富的测试数据：

### 用户数据 (30条)
- 10个不同状态的用户
- 包含管理员、普通用户、禁用用户
- 10个用户会话记录
- 10个用户设置配置

### 文件数据 (40条)
- 10个文件/文件夹，包含层级结构
- 10个分享链接，不同类型和状态
- 10个文件版本记录
- 10个上传任务，不同状态

### 管理数据 (40条)
- 10个管理员账户，不同权限级别
- 10个系统配置项
- 10条操作日志记录
- 10个用户举报记录

### 会员数据 (40条)
- 10个会员等级，从免费到企业版
- 10个用户会员记录
- 10个支付订单
- 10条订阅历史记录

## 🔧 初始化脚本执行顺序

脚本按文件名数字前缀顺序执行：

1. `01-create-databases.sql` - 创建4个数据库
2. `02-user-service-init.sql` - 用户服务表和数据
3. `03-file-service-init.sql` - 文件服务表和数据
4. `04-admin-service-init.sql` - 管理服务表和数据
5. `05-membership-service-init.sql` - 会员服务表和数据

## 🔌 Navicat 数据库连接配置

启动CloudDrive后，可以使用Navicat等数据库工具连接查看数据库：

### 📋 连接信息
| 配置项 | 值 |
|--------|-----|
| **连接类型** | MySQL |
| **主机名/IP** | `localhost` 或 `127.0.0.1` |
| **端口** | `3307` |
| **用户名** | `root` |
| **密码** | `root123` |

### 🗂️ 数据库列表
连接成功后，可以看到以下数据库：
- `user_service_db` - 用户服务数据库 (3个表，30条记录)
- `file_service_db` - 文件服务数据库 (4个表，40条记录)
- `admin_service_db` - 管理服务数据库 (4个表，40条记录)
- `membership_service_db` - 会员服务数据库 (4个表，40条记录)

### 📝 详细连接步骤

#### 1. 启动CloudDrive服务
```bash
./scripts/start-minimal.sh
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

**验证数据库和数据：**
1. 展开连接，可以看到4个服务数据库
2. 展开任意数据库，查看表结构
3. 右键表名 → "打开表"，查看测试数据

#### 4. 数据验证检查项

**用户服务数据库 (user_service_db)：**
- ✅ `user_info` 表：10个用户，包含不同状态
- ✅ `user_sessions` 表：10个会话记录
- ✅ `user_settings` 表：10个用户设置

**文件服务数据库 (file_service_db)：**
- ✅ `file_info` 表：10个文件/文件夹，包含层级结构
- ✅ `file_shares` 表：10个分享链接，不同类型
- ✅ `file_versions` 表：10个文件版本记录
- ✅ `upload_tasks` 表：10个上传任务，不同状态

**管理服务数据库 (admin_service_db)：**
- ✅ `admin_users` 表：10个管理员，不同权限级别
- ✅ `system_configs` 表：10个系统配置项
- ✅ `operation_logs` 表：10条操作日志
- ✅ `user_reports` 表：10个举报记录

**会员服务数据库 (membership_service_db)：**
- ✅ `membership_levels` 表：10个会员等级
- ✅ `user_memberships` 表：10个用户会员记录
- ✅ `payment_orders` 表：10个支付订单
- ✅ `subscription_history` 表：10条订阅历史

### 🛠️ 常见连接问题解决

#### 问题1：连接超时
**原因**：MySQL服务未完全启动  
**解决**：等待更长时间或重启服务
```bash
docker-compose -f docker/docker-compose.minimal.yml restart mysql
```

#### 问题2：端口占用
**原因**：本地3307端口被占用  
**解决**：检查端口占用并停止冲突服务
```bash
lsof -i :3307
```

#### 问题3：认证失败
**原因**：密码错误或用户权限问题  
**解决**：确认使用正确的用户名密码 (root/root123)

#### 问题4：看不到数据库
**原因**：数据库初始化失败  
**解决**：重新启动服务，确保初始化脚本执行成功
```bash
docker-compose -f docker/docker-compose.minimal.yml down
docker-compose -f docker/docker-compose.minimal.yml up --build
```

### 🔍 连接验证命令

如果无法使用Navicat，可以通过命令行验证：

```bash
# 检查数据库列表
docker exec mysql mysql -u root -proot123 -e "SHOW DATABASES;"

# 检查用户服务表
docker exec mysql mysql -u root -proot123 -e "USE user_service_db; SHOW TABLES; SELECT COUNT(*) FROM user_info;"

# 检查文件服务表
docker exec mysql mysql -u root -proot123 -e "USE file_service_db; SHOW TABLES; SELECT COUNT(*) FROM file_info;"

# 检查管理服务表
docker exec mysql mysql -u root -proot123 -e "USE admin_service_db; SHOW TABLES; SELECT COUNT(*) FROM admin_users;"

# 检查会员服务表
docker exec mysql mysql -u root -proot123 -e "USE membership_service_db; SHOW TABLES; SELECT COUNT(*) FROM membership_levels;"
```

### ✅ 连接验证成功标志

如果连接配置正确，您应该能够：
1. 🔗 **成功连接**：Navicat显示连接成功
2. 📂 **看到4个数据库**：user_service_db, file_service_db, admin_service_db, membership_service_db
3. 🗃️ **总计15个表**：每个数据库包含3-4个表
4. 📊 **总计150条记录**：每个表包含10条测试数据
5. 🔍 **数据完整性**：表结构正确，数据类型匹配，关联关系清晰

*最后验证时间: 2025年7月8日 02:18 - 所有数据库和表已成功创建并包含测试数据*

## 🚀 部署说明

### Docker自动初始化
MySQL容器启动时会自动执行`/docker-entrypoint-initdb.d/`目录下的SQL文件。

### 健康检查
添加了MySQL健康检查，确保数据库完全启动后再启动应用服务。

### 数据持久化
使用Docker卷`mysql_data`确保数据在容器重启后保持。

## 📈 扩展性考虑

### 跨库关联
使用用户ID等关键字段在不同数据库间建立逻辑关联。

### 分库分表
当数据量增长时，可以按用户ID或时间进行分片。

### 读写分离
每个数据库都可以独立配置主从复制。

## 🔍 监控和维护

### 性能监控
建议监控各数据库的连接数、查询性能、存储使用情况。

### 备份策略
建议对每个数据库设置独立的备份策略。

### 索引优化
已为常用查询字段创建索引，可根据实际使用情况调整。

---

*最后更新: 2025年1月8日 - 补充Navicat连接信息和完整表结构*