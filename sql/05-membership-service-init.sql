-- 会员服务数据库初始化脚本
-- 会员等级、用户会员状态、支付订单、订阅历史

USE membership_service_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 会员等级表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `membership_levels`;
CREATE TABLE `membership_levels` (
  `level_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '等级ID',
  `level_name` VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '等级名称',
  `level_code` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '等级代码',
  `storage_quota` BIGINT NOT NULL COMMENT '存储配额(字节)',
  `max_file_size` BIGINT NOT NULL COMMENT '单文件最大大小(字节)',
  `download_speed_limit` INT NULL DEFAULT -1 COMMENT '下载速度限制(KB/s,-1无限制)',
  `upload_speed_limit` INT NULL DEFAULT -1 COMMENT '上传速度限制(KB/s,-1无限制)',
  `share_expire_days` INT NULL DEFAULT 30 COMMENT '分享链接最长有效期(天)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格(元/月)',
  `features` TEXT CHARACTER SET utf8mb4 NULL COMMENT 'JSON格式功能特性',
  `sort_order` INT NULL DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态:0禁用1启用',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`level_id`) USING BTREE,
  UNIQUE INDEX `key_level_name`(`level_name`) USING BTREE,
  UNIQUE INDEX `key_level_code`(`level_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '会员等级表';

-- =============================================================================
-- 用户会员表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `user_memberships`;
CREATE TABLE `user_memberships` (
  `membership_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '会员记录ID',
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `level_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '会员等级ID',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `auto_renew` TINYINT(1) NULL DEFAULT 0 COMMENT '自动续费:0关闭1开启',
  `payment_method` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '支付方式',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `actual_price` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `discount_amount` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `status` TINYINT NULL DEFAULT 1 COMMENT '状态:0未生效1生效中2已过期3已取消',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`membership_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_level_id`(`level_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_end_time`(`end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '用户会员表';

-- =============================================================================
-- 支付订单表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `payment_orders`;
CREATE TABLE `payment_orders` (
  `order_id` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '订单ID',
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `membership_id` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '会员记录ID',
  `order_type` TINYINT NOT NULL COMMENT '订单类型:1新购2续费3升级',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `discount_amount` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `payment_method` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '支付方式:alipay/wechat/bank',
  `payment_channel` VARCHAR(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '支付渠道',
  `trade_no` VARCHAR(64) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '第三方交易号',
  `status` TINYINT NULL DEFAULT 0 COMMENT '状态:0待支付1支付中2已支付3支付失败4已退款',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` DATETIME NULL DEFAULT NULL COMMENT '支付时间',
  `callback_data` TEXT CHARACTER SET utf8mb4 NULL COMMENT '回调数据',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_membership_id`(`membership_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_trade_no`(`trade_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '支付订单表';

-- =============================================================================
-- 订阅历史表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `subscription_history`;
CREATE TABLE `subscription_history` (
  `history_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '历史记录ID',
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `operation_type` TINYINT NOT NULL COMMENT '操作类型:1开通2续费3升级4降级5取消6恢复',
  `old_level_id` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '原等级ID',
  `new_level_id` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '新等级ID',
  `old_end_time` DATETIME NULL DEFAULT NULL COMMENT '原结束时间',
  `new_end_time` DATETIME NULL DEFAULT NULL COMMENT '新结束时间',
  `amount` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '涉及金额',
  `operator_type` TINYINT NULL DEFAULT 1 COMMENT '操作者类型:1用户2管理员3系统',
  `operator_id` VARCHAR(15) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '操作者ID',
  `remark` VARCHAR(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_operation_type`(`operation_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '订阅历史表';

-- =============================================================================
-- 插入测试数据
-- =============================================================================

-- 会员等级测试数据
INSERT INTO `membership_levels` VALUES 
('L001', '免费版', 'FREE', 1073741824, 104857600, 1024, 512, 7, 0.00, '{"ads": true, "support": "community", "backup": false, "preview": "basic"}', 1, 1, '2024-01-01 08:00:00'),
('L002', '基础版', 'BASIC', 10737418240, 1073741824, 5120, 2048, 30, 9.90, '{"ads": false, "support": "email", "backup": true, "preview": "standard"}', 2, 1, '2024-01-01 08:00:00'),
('L003', '高级版', 'PREMIUM', 107374182400, 5368709120, 10240, 5120, 90, 19.90, '{"ads": false, "support": "priority", "backup": true, "preview": "advanced"}', 3, 1, '2024-01-01 08:00:00'),
('L004', '专业版', 'PROFESSIONAL', 536870912000, 21474836480, -1, -1, 365, 39.90, '{"ads": false, "support": "phone", "backup": true, "preview": "professional", "api": true}', 4, 1, '2024-01-01 08:00:00'),
('L005', '企业版', 'ENTERPRISE', 1099511627776, 53687091200, -1, -1, -1, 99.90, '{"ads": false, "support": "dedicated", "backup": true, "preview": "enterprise", "api": true, "team": true}', 5, 1, '2024-01-01 08:00:00'),
('L006', '学生版', 'STUDENT', 5368709120, 536870912, 2048, 1024, 60, 4.90, '{"ads": false, "support": "email", "backup": false, "preview": "standard", "education": true}', 6, 1, '2024-01-01 08:00:00'),
('L007', '体验版', 'TRIAL', 2147483648, 209715200, 2048, 1024, 14, 0.00, '{"ads": true, "support": "community", "backup": false, "preview": "basic", "trial": true}', 7, 0, '2024-01-01 08:00:00'),
('L008', 'VIP版', 'VIP', 214748364800, 10737418240, 20480, 10240, 180, 29.90, '{"ads": false, "support": "priority", "backup": true, "preview": "advanced", "vip": true}', 8, 1, '2024-01-01 08:00:00'),
('L009', '限时版', 'LIMITED', 53687091200, 2147483648, 4096, 2048, 45, 14.90, '{"ads": false, "support": "email", "backup": true, "preview": "standard", "limited": true}', 9, 0, '2024-01-01 08:00:00'),
('L010', '至尊版', 'ULTIMATE', 2199023255552, 107374182400, -1, -1, -1, 199.90, '{"ads": false, "support": "dedicated", "backup": true, "preview": "ultimate", "api": true, "team": true, "priority": true}', 10, 1, '2024-01-01 08:00:00');

-- 用户会员测试数据
INSERT INTO `user_memberships` VALUES 
('M001', 'U001', 'L004', '2024-01-01 10:00:00', '2025-01-01 10:00:00', 1, 'alipay', 39.90, 39.90, 0.00, 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
('M002', 'U002', 'L002', '2024-01-02 11:20:00', '2024-02-02 11:20:00', 0, 'wechat', 9.90, 9.90, 0.00, 2, '2024-01-02 11:20:00', '2024-02-02 11:20:00'),
('M003', 'U003', 'L003', '2024-01-03 14:30:00', '2024-04-03 14:30:00', 1, 'alipay', 59.70, 47.76, 11.94, 1, '2024-01-03 14:30:00', '2024-01-03 14:30:00'),
('M004', 'U004', 'L002', '2024-01-04 09:15:00', '2024-02-04 09:15:00', 0, 'bank', 9.90, 9.90, 0.00, 1, '2024-01-04 09:15:00', '2024-01-04 09:15:00'),
('M005', 'U005', 'L001', '2024-01-05 16:45:00', '2099-12-31 23:59:59', 0, NULL, 0.00, 0.00, 0.00, 1, '2024-01-05 16:45:00', '2024-01-05 16:45:00'),
('M006', 'U006', 'L006', '2024-01-06 13:20:00', '2024-03-06 13:20:00', 1, 'alipay', 9.80, 4.90, 4.90, 1, '2024-01-06 13:20:00', '2024-01-06 13:20:00'),
('M007', 'U007', 'L001', '2024-01-07 10:30:00', '2099-12-31 23:59:59', 0, NULL, 0.00, 0.00, 0.00, 1, '2024-01-07 10:30:00', '2024-01-07 10:30:00'),
('M008', 'U008', 'L008', '2024-01-08 15:40:00', '2024-07-08 15:40:00', 0, 'wechat', 179.40, 149.50, 29.90, 1, '2024-01-08 15:40:00', '2024-01-08 15:40:00'),
('M009', 'U009', 'L005', '2024-01-09 12:10:00', '2024-04-09 12:10:00', 1, 'alipay', 299.70, 199.80, 99.90, 1, '2024-01-09 12:10:00', '2024-01-09 12:10:00'),
('M010', 'U010', 'L003', '2024-01-10 17:25:00', '2024-02-10 17:25:00', 0, 'bank', 19.90, 19.90, 0.00, 1, '2024-01-10 17:25:00', '2024-01-10 17:25:00');

-- 支付订单测试数据
INSERT INTO `payment_orders` VALUES 
('ORDER001', 'U001', 'M001', 1, 39.90, 0.00, 39.90, 'alipay', 'alipay_app', '2024010110000001', 2, '2024-01-01 09:45:00', '2024-01-01 09:58:32', '{"trade_status":"TRADE_SUCCESS","buyer_id":"2088012345678901"}'),
('ORDER002', 'U002', 'M002', 1, 9.90, 0.00, 9.90, 'wechat', 'wechat_h5', 'wx20240102112000001', 2, '2024-01-02 11:15:00', '2024-01-02 11:19:45', '{"trade_state":"SUCCESS","transaction_id":"4200001234567890123"}'),
('ORDER003', 'U003', 'M003', 1, 59.70, 11.94, 47.76, 'alipay', 'alipay_pc', '2024010314300001', 2, '2024-01-03 14:25:00', '2024-01-03 14:29:18', '{"trade_status":"TRADE_SUCCESS","discount_detail":"新用户优惠20%"}'),
('ORDER004', 'U004', 'M004', 1, 9.90, 0.00, 9.90, 'bank', 'icbc_online', 'ICBC20240104091500001', 2, '2024-01-04 09:10:00', '2024-01-04 09:14:28', '{"pay_result":"SUCCESS","bank_type":"ICBC"}'),
('ORDER005', 'U006', 'M006', 1, 9.80, 4.90, 4.90, 'alipay', 'alipay_app', '2024010613200001', 2, '2024-01-06 13:15:00', '2024-01-06 13:19:52', '{"trade_status":"TRADE_SUCCESS","discount_detail":"学生优惠50%"}'),
('ORDER006', 'U008', 'M008', 1, 179.40, 29.90, 149.50, 'wechat', 'wechat_app', 'wx20240108154000001', 2, '2024-01-08 15:35:00', '2024-01-08 15:39:15', '{"trade_state":"SUCCESS","coupon_fee":"2990"}'),
('ORDER007', 'U009', 'M009', 1, 299.70, 99.90, 199.80, 'alipay', 'alipay_pc', '2024010912100001', 2, '2024-01-09 12:05:00', '2024-01-09 12:09:33', '{"trade_status":"TRADE_SUCCESS","discount_detail":"首充优惠+VIP折扣"}'),
('ORDER008', 'U010', 'M010', 1, 19.90, 0.00, 19.90, 'bank', 'ccb_online', 'CCB20240110172500001', 2, '2024-01-10 17:20:00', '2024-01-10 17:24:41', '{"pay_result":"SUCCESS","bank_type":"CCB"}'),
('ORDER009', 'U003', NULL, 2, 19.90, 0.00, 19.90, 'alipay', 'alipay_app', '2024010520000001', 1, '2024-01-05 20:00:00', NULL, NULL),
('ORDER010', 'U007', NULL, 1, 9.90, 0.00, 9.90, 'wechat', 'wechat_h5', 'wx20240107203000001', 3, '2024-01-07 20:30:00', NULL, '{"error_code":"ORDERPAID","error_msg":"订单已支付"}');

-- 订阅历史测试数据
INSERT INTO `subscription_history` VALUES 
('H001', 'U001', 1, NULL, 'L004', NULL, '2025-01-01 10:00:00', 39.90, 1, 'U001', '开通专业版会员', '2024-01-01 10:00:00'),
('H002', 'U002', 1, NULL, 'L002', NULL, '2024-02-02 11:20:00', 9.90, 1, 'U002', '开通基础版会员', '2024-01-02 11:20:00'),
('H003', 'U003', 1, NULL, 'L003', NULL, '2024-04-03 14:30:00', 47.76, 1, 'U003', '开通高级版会员(3个月)', '2024-01-03 14:30:00'),
('H004', 'U004', 1, NULL, 'L002', NULL, '2024-02-04 09:15:00', 9.90, 1, 'U004', '开通基础版会员', '2024-01-04 09:15:00'),
('H005', 'U006', 1, NULL, 'L006', NULL, '2024-03-06 13:20:00', 4.90, 1, 'U006', '开通学生版会员', '2024-01-06 13:20:00'),
('H006', 'U008', 1, NULL, 'L008', NULL, '2024-07-08 15:40:00', 149.50, 1, 'U008', '开通VIP版会员(6个月)', '2024-01-08 15:40:00'),
('H007', 'U009', 1, NULL, 'L005', NULL, '2024-04-09 12:10:00', 199.80, 1, 'U009', '开通企业版会员(3个月)', '2024-01-09 12:10:00'),
('H008', 'U010', 1, NULL, 'L003', NULL, '2024-02-10 17:25:00', 19.90, 1, 'U010', '开通高级版会员', '2024-01-10 17:25:00'),
('H009', 'U007', 5, 'L001', NULL, '2099-12-31 23:59:59', NULL, 0.00, 2, 'A002', '管理员取消用户会员', '2024-01-08 10:30:00'),
('H010', 'U002', 2, 'L002', 'L002', '2024-02-02 11:20:00', '2024-03-02 11:20:00', 9.90, 3, 'SYSTEM', '系统自动续费失败', '2024-02-02 11:20:01');

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化完成日志
SELECT 
'会员服务数据库初始化完成' as message,
'4个表，40条测试数据' as details,
NOW() as created_at;