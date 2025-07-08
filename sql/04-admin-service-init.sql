-- 管理服务数据库初始化脚本
-- 管理员功能、系统配置、操作日志、用户举报

USE admin_service_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 管理员用户表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `admin_users`;
CREATE TABLE `admin_users` (
  `admin_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '管理员ID',
  `username` VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户名',
  `password` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '密码(MD5)',
  `real_name` VARCHAR(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(150) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '手机号',
  `role_type` TINYINT NULL DEFAULT 2 COMMENT '角色类型:1超级管理员2系统管理员3内容管理员',
  `permissions` TEXT CHARACTER SET utf8mb4 NULL COMMENT 'JSON格式权限配置',
  `status` TINYINT(1) NULL DEFAULT 1 COMMENT '状态:0禁用1正常',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `key_username`(`username`) USING BTREE,
  UNIQUE INDEX `key_email`(`email`) USING BTREE,
  INDEX `idx_role_type`(`role_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '管理员用户表';

-- =============================================================================
-- 系统配置表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `config_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '配置ID',
  `config_key` VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COMMENT '配置键',
  `config_value` TEXT CHARACTER SET utf8mb4 NULL COMMENT '配置值',
  `config_desc` VARCHAR(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '配置描述',
  `config_type` VARCHAR(20) CHARACTER SET utf8mb4 NULL DEFAULT 'string' COMMENT '配置类型:string/number/boolean/json',
  `is_public` TINYINT(1) NULL DEFAULT 0 COMMENT '是否对用户公开:0否1是',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `key_config_key`(`config_key`) USING BTREE,
  INDEX `idx_is_public`(`is_public`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '系统配置表';

-- =============================================================================
-- 操作日志表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `log_id` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '日志ID',
  `operator_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '操作者ID',
  `operator_type` TINYINT NULL DEFAULT 1 COMMENT '操作者类型:1管理员2用户',
  `operation_type` VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '操作描述',
  `target_type` VARCHAR(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` VARCHAR(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '目标ID',
  `ip_address` VARCHAR(45) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '用户代理',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_operator_id`(`operator_id`) USING BTREE,
  INDEX `idx_operation_type`(`operation_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_target_type`(`target_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '操作日志表';

-- =============================================================================
-- 用户举报表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `user_reports`;
CREATE TABLE `user_reports` (
  `report_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '举报ID',
  `reporter_id` VARCHAR(15) CHARACTER SET utf8mb4 NOT NULL COMMENT '举报人ID',
  `reported_user_id` VARCHAR(15) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '被举报用户ID',
  `reported_file_id` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '被举报文件ID',
  `report_type` TINYINT NOT NULL COMMENT '举报类型:1违规内容2版权侵犯3恶意行为4其他',
  `report_reason` VARCHAR(1000) CHARACTER SET utf8mb4 NOT NULL COMMENT '举报原因',
  `evidence_files` TEXT CHARACTER SET utf8mb4 NULL COMMENT 'JSON格式证据文件列表',
  `status` TINYINT NULL DEFAULT 0 COMMENT '处理状态:0待处理1处理中2已处理3已驳回',
  `admin_id` VARCHAR(15) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '处理管理员ID',
  `admin_remark` VARCHAR(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '管理员备注',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `handle_time` DATETIME NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id`) USING BTREE,
  INDEX `idx_reported_user_id`(`reported_user_id`) USING BTREE,
  INDEX `idx_report_type`(`report_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '用户举报表';

-- =============================================================================
-- 插入测试数据
-- =============================================================================

-- 管理员用户测试数据
INSERT INTO `admin_users` VALUES 
('A001', 'superadmin', '21232f297a57a5a743894a0e4a801fc3', '超级管理员', 'superadmin@clouddrive.com', '13900000001', 1, '{"user_manage": true, "file_manage": true, "system_config": true, "report_handle": true, "data_export": true}', 1, '2024-01-01 08:00:00', '2025-01-08 09:00:00'),
('A002', 'sysadmin', '5e884898da28047151d0e56f8dc62927', '系统管理员', 'sysadmin@clouddrive.com', '13900000002', 2, '{"user_manage": true, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 1, '2024-01-01 09:00:00', '2025-01-07 14:30:00'),
('A003', 'contentadmin', 'e3b0c44298fc1c149afbf4c8996fb924', '内容管理员', 'content@clouddrive.com', '13900000003', 3, '{"user_manage": false, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 1, '2024-01-02 10:00:00', '2025-01-06 16:20:00'),
('A004', 'admin001', 'f47ac10b58cc4372a567046fc8a3d479', '张管理', 'zhang.admin@clouddrive.com', '13900000004', 2, '{"user_manage": true, "file_manage": false, "system_config": false, "report_handle": true, "data_export": false}', 1, '2024-01-03 11:00:00', '2025-01-05 10:45:00'),
('A005', 'admin002', '098f6bcd4621d373cade4e832627b4f6', '李管理', 'li.admin@clouddrive.com', '13900000005', 3, '{"user_manage": false, "file_manage": true, "system_config": false, "report_handle": false, "data_export": false}', 1, '2024-01-04 12:00:00', '2025-01-04 11:30:00'),
('A006', 'admin003', 'aab4c61ddcc5e8a2dabede0f3b482cd9', '王管理', 'wang.admin@clouddrive.com', '13900000006', 2, '{"user_manage": true, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 0, '2024-01-05 13:00:00', '2025-01-01 08:20:00'),
('A007', 'admin004', '6329334bdcd5458b8d994e50a3c9b5c1', '赵管理', 'zhao.admin@clouddrive.com', '13900000007', 3, '{"user_manage": false, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 1, '2024-01-06 14:00:00', '2024-12-30 15:40:00'),
('A008', 'admin005', 'c89efdaa54c0f20c7adf612882df0950', '刘管理', 'liu.admin@clouddrive.com', '13900000008', 2, '{"user_manage": true, "file_manage": false, "system_config": false, "report_handle": false, "data_export": false}', 1, '2024-01-07 15:00:00', '2024-12-29 12:15:00'),
('A009', 'admin006', 'b5d4045c3f466fa91fe2cc6abe79232a', '陈管理', 'chen.admin@clouddrive.com', '13900000009', 3, '{"user_manage": false, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 1, '2024-01-08 16:00:00', '2024-12-28 09:50:00'),
('A010', 'admin007', 'd41d8cd98f00b204e9800998ecf8427e', '周管理', 'zhou.admin@clouddrive.com', '13900000010', 2, '{"user_manage": true, "file_manage": true, "system_config": false, "report_handle": true, "data_export": false}', 0, '2024-01-09 17:00:00', '2024-12-27 17:30:00');

-- 系统配置测试数据
INSERT INTO `system_configs` VALUES 
('CFG001', 'default_storage_quota', '1073741824', '默认用户存储配额(字节)', 'number', 0, '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
('CFG002', 'max_file_size', '104857600', '单文件最大大小(字节)', 'number', 1, '2024-01-01 08:00:00', '2024-01-05 10:30:00'),
('CFG003', 'allow_file_types', '["jpg","png","gif","pdf","docx","xlsx","mp4","mp3","zip","rar"]', '允许上传的文件类型', 'json', 1, '2024-01-01 08:00:00', '2024-01-03 14:20:00'),
('CFG004', 'site_title', 'CloudDrive云盘', '网站标题', 'string', 1, '2024-01-01 08:00:00', '2024-01-02 16:45:00'),
('CFG005', 'enable_registration', 'true', '是否允许用户注册', 'boolean', 1, '2024-01-01 08:00:00', '2024-01-04 11:15:00'),
('CFG006', 'max_download_speed', '10485760', '最大下载速度(字节/秒)', 'number', 0, '2024-01-01 08:00:00', '2024-01-06 09:40:00'),
('CFG007', 'share_expire_days', '30', '分享链接默认有效期(天)', 'number', 0, '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
('CFG008', 'backup_enable', 'false', '是否启用自动备份', 'boolean', 0, '2024-01-01 08:00:00', '2024-01-07 13:25:00'),
('CFG009', 'maintenance_mode', 'false', '维护模式', 'boolean', 1, '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
('CFG010', 'contact_email', 'support@clouddrive.com', '客服邮箱', 'string', 1, '2024-01-01 08:00:00', '2024-01-01 08:00:00');

-- 操作日志测试数据
INSERT INTO `operation_logs` VALUES 
('LOG001', 'A001', 1, 'USER_CREATE', '创建用户账户: zhangsan', 'user', 'U002', '192.168.1.100', 'Chrome/120.0 Admin Panel', '2024-01-02 11:20:00'),
('LOG002', 'A002', 1, 'FILE_DELETE', '删除违规文件', 'file', 'F010', '192.168.1.101', 'Firefox/121.0 Admin Panel', '2024-01-08 09:00:00'),
('LOG003', 'A003', 1, 'USER_DISABLE', '禁用用户账户: sunqi', 'user', 'U007', '192.168.1.102', 'Safari/17.0 Admin Panel', '2024-01-08 10:30:00'),
('LOG004', 'A001', 1, 'CONFIG_UPDATE', '更新系统配置: max_file_size', 'config', 'CFG002', '192.168.1.100', 'Chrome/120.0 Admin Panel', '2024-01-05 10:30:00'),
('LOG005', 'A004', 1, 'REPORT_HANDLE', '处理用户举报', 'report', 'R003', '192.168.1.103', 'Edge/120.0 Admin Panel', '2024-01-07 14:20:00'),
('LOG006', 'A002', 1, 'USER_UNLOCK', '解锁用户账户', 'user', 'U006', '192.168.1.101', 'Firefox/121.0 Admin Panel', '2024-01-06 15:45:00'),
('LOG007', 'A005', 1, 'FILE_RESTORE', '恢复文件: 临时文件.tmp', 'file', 'F009', '192.168.1.104', 'Chrome/120.0 Admin Panel', '2024-01-07 09:15:00'),
('LOG008', 'A001', 1, 'ADMIN_CREATE', '创建管理员账户: admin003', 'admin', 'A006', '192.168.1.100', 'Chrome/120.0 Admin Panel', '2024-01-05 13:00:00'),
('LOG009', 'A003', 1, 'SHARE_DISABLE', '禁用分享链接', 'share', 'S005', '192.168.1.102', 'Safari/17.0 Admin Panel', '2024-01-09 11:30:00'),
('LOG010', 'A007', 1, 'SYSTEM_BACKUP', '执行系统备份', 'system', 'backup_20250108', '192.168.1.105', 'Opera/105.0 Admin Panel', '2024-01-08 02:00:00');

-- 用户举报测试数据
INSERT INTO `user_reports` VALUES 
('R001', 'U002', 'U007', 'F010', 1, '该文件包含不当内容，建议删除', '["evidence1.jpg", "evidence2.pdf"]', 2, 'A002', '经核实确实违规，已删除文件', '2024-01-07 14:30:00', '2024-01-08 09:00:00'),
('R002', 'U003', 'U009', NULL, 3, '该用户恶意刷屏，影响正常使用', '["screenshot1.png"]', 1, 'A004', NULL, '2024-01-06 16:20:00', NULL),
('R003', 'U001', 'U005', 'F008', 2, '疑似盗版软件分享', '["license_check.txt"]', 2, 'A004', '已联系用户确认，非盗版软件', '2024-01-05 10:15:00', '2024-01-07 14:20:00'),
('R004', 'U004', 'U006', 'F009', 1, '临时文件包含病毒', '["virus_report.pdf"]', 0, NULL, NULL, '2024-01-07 09:30:00', NULL),
('R005', 'U008', 'U010', NULL, 4, '用户名包含不当词汇', NULL, 3, 'A003', '用户名正常，举报无效', '2024-01-08 11:45:00', '2024-01-08 15:30:00'),
('R006', 'U001', 'U002', 'F003', 2, '照片包含版权保护内容', '["copyright_notice.pdf"]', 1, 'A007', NULL, '2024-01-03 12:00:00', NULL),
('R007', 'U005', 'U004', 'F007', 1, '设计稿涉嫌抄袭', '["original_design.psd"]', 2, 'A005', '设计稿为原创，无抄袭行为', '2024-01-05 14:20:00', '2024-01-06 10:30:00'),
('R008', 'U006', 'U003', 'F005', 1, '视频内容不适宜', '["video_analysis.txt"]', 0, NULL, NULL, '2024-01-04 16:45:00', NULL),
('R009', 'U009', 'U001', NULL, 3, '管理员滥用权限', '["chat_log.txt"]', 3, 'A001', '经调查无滥用行为', '2024-01-02 18:30:00', '2024-01-03 09:15:00'),
('R010', 'U010', 'U008', NULL, 4, '用户频繁修改昵称干扰他人', NULL, 1, 'A002', NULL, '2024-01-09 13:20:00', NULL);

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化完成日志
SELECT 
'管理服务数据库初始化完成' as message,
'4个表，40条测试数据' as details,
NOW() as created_at;