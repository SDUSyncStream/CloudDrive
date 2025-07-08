-- 用户服务数据库初始化脚本
-- 基于原始 mengpan.sql 的 user_info 表进行扩展

USE user_service_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 用户信息表 (基于原始 user_info 表结构)
-- =============================================================================
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `nick_name` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(150) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(32) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '密码(MD5)',
  `avatar` VARCHAR(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '头像地址',
  `phone` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '手机号',
  `join_time` DATETIME NULL DEFAULT NULL COMMENT '注册时间',
  `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态:0禁用1正常2待验证',
  `use_space` BIGINT NULL DEFAULT 0 COMMENT '已使用空间(字节)',
  `total_space` BIGINT NULL DEFAULT 1073741824 COMMENT '总空间(字节,默认1GB)',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_email`(`email`) USING BTREE,
  UNIQUE INDEX `key_nick_name`(`nick_name`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_join_time`(`join_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '用户信息表';

-- =============================================================================
-- 用户会话表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions` (
  `session_id` VARCHAR(36) CHARACTER SET utf8mb4 NOT NULL COMMENT '会话ID',
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `token` VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '访问令牌',
  `device_info` VARCHAR(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '设备信息',
  `ip_address` VARCHAR(45) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT 'IP地址',
  `login_time` DATETIME NULL DEFAULT NULL COMMENT '登录时间',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态:0失效1有效',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_token`(`token`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '用户会话表';

-- =============================================================================
-- 用户设置表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
  `user_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `theme` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT 'light' COMMENT '主题:light/dark',
  `language` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT 'zh-CN' COMMENT '语言',
  `email_notify` TINYINT(1) NULL DEFAULT 1 COMMENT '邮件通知:0关闭1开启',
  `auto_backup` TINYINT(1) NULL DEFAULT 0 COMMENT '自动备份:0关闭1开启',
  `privacy_level` TINYINT NULL DEFAULT 1 COMMENT '隐私级别:1公开2好友3私密',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '用户设置表';

-- =============================================================================
-- 插入测试数据
-- =============================================================================

-- 用户信息测试数据
INSERT INTO `user_info` VALUES 
('U001', 'admin', 'admin@clouddrive.com', '21232f297a57a5a743894a0e4a801fc3', '/avatars/admin.jpg', '13800138001', '2024-01-01 10:00:00', '2025-01-08 09:30:00', 1, 0, 10737418240),
('U002', 'zhangsan', 'zhangsan@qq.com', '96e79218965eb72c92a549dd5a330112', '/avatars/zhangsan.jpg', '13800138002', '2024-01-02 11:20:00', '2025-01-07 16:45:00', 1, 524288000, 1073741824),
('U003', 'lisi', 'lisi@163.com', '96e79218965eb72c92a549dd5a330112', '/avatars/lisi.jpg', '13800138003', '2024-01-03 14:30:00', '2025-01-06 20:15:00', 1, 209715200, 1073741824),
('U004', 'wangwu', 'wangwu@gmail.com', '96e79218965eb72c92a549dd5a330112', '/avatars/wangwu.jpg', '13800138004', '2024-01-04 09:15:00', '2025-01-05 12:30:00', 1, 104857600, 1073741824),
('U005', 'maliu', 'maliu@hotmail.com', '96e79218965eb72c92a549dd5a330112', '/avatars/maliu.jpg', '13800138005', '2024-01-05 16:45:00', '2025-01-04 18:20:00', 2, 0, 1073741824),
('U006', 'zhaoliu', 'zhaoliu@sina.com', '96e79218965eb72c92a549dd5a330112', '/avatars/zhaoliu.jpg', '13800138006', '2024-01-06 13:20:00', '2025-01-03 10:45:00', 1, 314572800, 1073741824),
('U007', 'sunqi', 'sunqi@126.com', '96e79218965eb72c92a549dd5a330112', '/avatars/sunqi.jpg', '13800138007', '2024-01-07 10:30:00', '2025-01-02 14:30:00', 0, 0, 1073741824),
('U008', 'zhouba', 'zhouba@outlook.com', '96e79218965eb72c92a549dd5a330112', '/avatars/zhouba.jpg', '13800138008', '2024-01-08 15:40:00', '2025-01-01 11:15:00', 1, 419430400, 1073741824),
('U009', 'wujiu', 'wujiu@yahoo.com', '96e79218965eb72c92a549dd5a330112', '/avatars/wujiu.jpg', '13800138009', '2024-01-09 12:10:00', '2024-12-31 16:40:00', 1, 157286400, 1073741824),
('U010', 'zhengshi', 'zhengshi@qq.com', '96e79218965eb72c92a549dd5a330112', '/avatars/zhengshi.jpg', '13800138010', '2024-01-10 17:25:00', '2024-12-30 19:20:00', 1, 52428800, 1073741824);

-- 用户会话测试数据
INSERT INTO `user_sessions` VALUES 
('session-001', 'U001', 'token_admin_2025_001', 'Chrome 120.0 Windows 10', '192.168.1.100', '2025-01-08 09:30:00', '2025-01-15 09:30:00', 1),
('session-002', 'U002', 'token_zhangsan_2025_001', 'Safari 17.0 macOS 14', '192.168.1.101', '2025-01-07 16:45:00', '2025-01-14 16:45:00', 1),
('session-003', 'U003', 'token_lisi_2025_001', 'Firefox 121.0 Ubuntu 22.04', '192.168.1.102', '2025-01-06 20:15:00', '2025-01-13 20:15:00', 1),
('session-004', 'U004', 'token_wangwu_2025_001', 'Edge 120.0 Windows 11', '192.168.1.103', '2025-01-05 12:30:00', '2025-01-12 12:30:00', 1),
('session-005', 'U005', 'token_maliu_2025_001', 'Chrome 120.0 Android 14', '192.168.1.104', '2025-01-04 18:20:00', '2025-01-11 18:20:00', 0),
('session-006', 'U006', 'token_zhaoliu_2025_001', 'Safari 17.0 iOS 17', '192.168.1.105', '2025-01-03 10:45:00', '2025-01-10 10:45:00', 1),
('session-007', 'U007', 'token_sunqi_2025_001', 'Chrome 120.0 Linux', '192.168.1.106', '2025-01-02 14:30:00', '2025-01-09 14:30:00', 0),
('session-008', 'U008', 'token_zhouba_2025_001', 'Opera 105.0 Windows 10', '192.168.1.107', '2025-01-01 11:15:00', '2025-01-08 11:15:00', 1),
('session-009', 'U009', 'token_wujiu_2025_001', 'Chrome 120.0 ChromeOS', '192.168.1.108', '2024-12-31 16:40:00', '2025-01-07 16:40:00', 1),
('session-010', 'U010', 'token_zhengshi_2025_001', 'Firefox 121.0 macOS 14', '192.168.1.109', '2024-12-30 19:20:00', '2025-01-06 19:20:00', 1);

-- 用户设置测试数据
INSERT INTO `user_settings` VALUES 
('U001', 'dark', 'zh-CN', 1, 1, 1, '2024-01-01 10:00:00', '2025-01-08 09:30:00'),
('U002', 'light', 'zh-CN', 1, 0, 2, '2024-01-02 11:20:00', '2025-01-07 16:45:00'),
('U003', 'light', 'en-US', 0, 1, 1, '2024-01-03 14:30:00', '2025-01-06 20:15:00'),
('U004', 'dark', 'zh-CN', 1, 1, 3, '2024-01-04 09:15:00', '2025-01-05 12:30:00'),
('U005', 'light', 'zh-CN', 1, 0, 2, '2024-01-05 16:45:00', '2025-01-04 18:20:00'),
('U006', 'auto', 'ja-JP', 0, 0, 1, '2024-01-06 13:20:00', '2025-01-03 10:45:00'),
('U007', 'light', 'zh-CN', 1, 1, 3, '2024-01-07 10:30:00', '2025-01-02 14:30:00'),
('U008', 'dark', 'en-US', 0, 1, 2, '2024-01-08 15:40:00', '2025-01-01 11:15:00'),
('U009', 'light', 'ko-KR', 1, 0, 1, '2024-01-09 12:10:00', '2024-12-31 16:40:00'),
('U010', 'auto', 'zh-CN', 1, 1, 2, '2024-01-10 17:25:00', '2024-12-30 19:20:00');

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化完成日志
SELECT 
'用户服务数据库初始化完成' as message,
'3个表，30条测试数据' as details,
NOW() as created_at;