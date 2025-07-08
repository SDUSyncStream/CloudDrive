-- 文件服务数据库初始化脚本
-- 基于原始 mengpan.sql 的 file_info 表进行扩展

USE file_service_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 文件信息表 (基于原始 file_info 表结构)
-- =============================================================================
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `file_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件ID',
  `user_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `file_md5` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件MD5',
  `file_pid` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '父级目录ID',
  `file_size` BIGINT NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_name` VARCHAR(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '文件名',
  `file_cover` VARCHAR(100) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '文件封面',
  `file_path` VARCHAR(100) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '文件路径',
  `create_time` DATETIME NULL DEFAULT NULL COMMENT '创建时间',
  `last_update_time` DATETIME NULL DEFAULT NULL COMMENT '最后更新时间',
  `folder_type` TINYINT(1) NULL DEFAULT NULL COMMENT '类型:0文件1目录',
  `file_category` TINYINT(1) NULL DEFAULT NULL COMMENT '分类:1视频2音频3图片4文档5其他',
  `file_type` TINYINT NULL DEFAULT NULL COMMENT '类型:1视频2音频3图片4pdf5doc6excel7txt8code9zip10其他',
  `status` TINYINT NULL DEFAULT NULL COMMENT '状态:0转码中1转码失败2转码成功',
  `recovery_time` DATETIME NULL DEFAULT NULL COMMENT '进入回收站时间',
  `del_flag` TINYINT(1) NULL DEFAULT NULL COMMENT '删除标记:0删除1回收站2正常',
  PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_md5`(`file_md5`) USING BTREE,
  INDEX `idx_file_pid`(`file_pid`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE,
  INDEX `idx_recover_time`(`recovery_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '文件信息表';

-- =============================================================================
-- 文件分享表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `file_shares`;
CREATE TABLE `file_shares` (
  `share_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '分享ID',
  `file_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件ID',
  `user_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '分享用户ID',
  `share_code` VARCHAR(8) CHARACTER SET utf8mb4 NOT NULL COMMENT '分享码',
  `password` VARCHAR(10) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '访问密码',
  `share_type` TINYINT NULL DEFAULT 1 COMMENT '分享类型:1公开分享2密码分享',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  `download_count` INT NULL DEFAULT 0 COMMENT '下载次数',
  `max_downloads` INT NULL DEFAULT -1 COMMENT '最大下载次数(-1无限制)',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` TINYINT NULL DEFAULT 1 COMMENT '状态:0失效1有效',
  PRIMARY KEY (`share_id`) USING BTREE,
  UNIQUE INDEX `key_share_code`(`share_code`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '文件分享表';

-- =============================================================================
-- 文件版本表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `file_versions`;
CREATE TABLE `file_versions` (
  `version_id` VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COMMENT '版本ID',
  `file_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件ID',
  `user_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `version_num` INT NULL DEFAULT 1 COMMENT '版本号',
  `file_size` BIGINT NULL DEFAULT NULL COMMENT '文件大小',
  `file_path` VARCHAR(200) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '文件存储路径',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `remark` VARCHAR(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '版本备注',
  PRIMARY KEY (`version_id`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_version_num`(`version_num`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '文件版本表';

-- =============================================================================
-- 上传任务表 (新增)
-- =============================================================================
DROP TABLE IF EXISTS `upload_tasks`;
CREATE TABLE `upload_tasks` (
  `task_id` VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '任务ID',
  `user_id` VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
  `file_name` VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件名',
  `file_size` BIGINT NULL DEFAULT NULL COMMENT '文件大小',
  `chunk_total` INT NULL DEFAULT 1 COMMENT '分片总数',
  `chunk_uploaded` INT NULL DEFAULT 0 COMMENT '已上传分片数',
  `status` TINYINT NULL DEFAULT 0 COMMENT '状态:0上传中1已完成2已失败3已暂停',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 ROW_FORMAT = Dynamic COMMENT = '上传任务表';

-- =============================================================================
-- 插入测试数据
-- =============================================================================

-- 文件信息测试数据 (包含文件夹和文件)
INSERT INTO `file_info` VALUES 
('F001', 'U001', 'd41d8cd98f00b204e9800998ecf8427e', NULL, 0, '我的文档', NULL, '/documents', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 1, 4, 4, 2, NULL, 2),
('F002', 'U001', '5d41402abc4b2a76b9719d911017c592', 'F001', 1024000, '工作报告.docx', '/covers/doc.png', '/documents/report.docx', '2024-01-01 10:30:00', '2024-01-02 15:20:00', 0, 4, 5, 2, NULL, 2),
('F003', 'U002', 'aab4c61ddcc5e8a2dabede0f3b482cd9', NULL, 524288000, '旅游照片.zip', '/covers/zip.png', '/photos.zip', '2024-01-02 14:00:00', '2024-01-02 14:00:00', 0, 5, 9, 2, NULL, 2),
('F004', 'U002', 'f47ac10b58cc4372a5670e02b2c3d479', NULL, 0, '视频收藏', NULL, '/videos', '2024-01-02 16:00:00', '2024-01-02 16:00:00', 1, 1, 1, 2, NULL, 2),
('F005', 'U003', '63293334dcd5458b8d994e50a3c9b5c1', 'F004', 156789123, '演示视频.mp4', '/covers/video.jpg', '/videos/demo.mp4', '2024-01-03 09:30:00', '2024-01-03 09:30:00', 0, 1, 1, 2, NULL, 2),
('F006', 'U003', '098f6bcd4621d373cade4e832627b4f6', NULL, 2048576, '音乐专辑.mp3', '/covers/music.png', '/music.mp3', '2024-01-03 11:45:00', '2024-01-03 11:45:00', 0, 2, 2, 2, NULL, 2),
('F007', 'U004', '5e884898da28047151d0e56f8dc62927', NULL, 4096000, '设计稿.psd', '/covers/image.png', '/design.psd', '2024-01-04 13:20:00', '2024-01-04 13:20:00', 0, 3, 3, 0, NULL, 2),
('F008', 'U005', 'e3b0c44298fc1c149afbf4c8996fb924', NULL, 8192000, '代码备份.tar.gz', '/covers/code.png', '/backup.tar.gz', '2024-01-05 08:15:00', '2024-01-05 08:15:00', 0, 5, 8, 2, NULL, 2),
('F009', 'U006', 'b5d4045c3f466fa91fe2cc6abe79232a', NULL, 512000, '临时文件.tmp', NULL, '/temp.tmp', '2024-01-06 17:30:00', '2024-01-06 17:30:00', 0, 5, 10, 2, '2024-01-07 10:00:00', 1),
('F010', 'U007', 'c89efdaa54c0f20c7adf612882df0950', NULL, 1536000, '删除文件.txt', NULL, '/deleted.txt', '2024-01-07 12:00:00', '2024-01-07 12:00:00', 0, 4, 7, 2, '2024-01-08 09:00:00', 0);

-- 文件分享测试数据
INSERT INTO `file_shares` VALUES 
('S001', 'F002', 'U001', 'AB12CD34', NULL, 1, '2025-02-01 23:59:59', 15, -1, '2024-01-02 16:00:00', 1),
('S002', 'F003', 'U002', 'EF56GH78', '1234', 2, '2025-01-15 23:59:59', 8, 100, '2024-01-02 17:30:00', 1),
('S003', 'F005', 'U003', 'IJ90KL12', NULL, 1, '2025-03-01 23:59:59', 45, -1, '2024-01-03 10:00:00', 1),
('S004', 'F006', 'U003', 'MN34OP56', 'music', 2, '2025-01-20 23:59:59', 22, 50, '2024-01-03 12:00:00', 1),
('S005', 'F007', 'U004', 'QR78ST90', NULL, 1, '2025-01-10 23:59:59', 3, -1, '2024-01-04 14:00:00', 0),
('S006', 'F008', 'U005', 'UV12WX34', 'code123', 2, '2025-04-01 23:59:59', 1, 10, '2024-01-05 09:00:00', 1),
('S007', 'F001', 'U001', 'YZ56AB78', NULL, 1, '2025-01-25 23:59:59', 0, -1, '2024-01-01 11:00:00', 1),
('S008', 'F004', 'U002', 'CD90EF12', 'video', 2, '2025-02-15 23:59:59', 12, 200, '2024-01-02 18:00:00', 1),
('S009', 'F009', 'U006', 'GH34IJ56', NULL, 1, '2024-12-31 23:59:59', 0, -1, '2024-01-06 18:00:00', 0),
('S010', 'F010', 'U007', 'KL78MN90', 'temp', 2, '2024-12-30 23:59:59', 2, 5, '2024-01-07 13:00:00', 0);

-- 文件版本测试数据
INSERT INTO `file_versions` VALUES 
('V001', 'F002', 'U001', 1, 1024000, '/storage/v1/report_v1.docx', '2024-01-01 10:30:00', '初始版本'),
('V002', 'F002', 'U001', 2, 1056000, '/storage/v2/report_v2.docx', '2024-01-02 15:20:00', '修正错别字'),
('V003', 'F005', 'U003', 1, 156789123, '/storage/v1/demo_v1.mp4', '2024-01-03 09:30:00', '原始录制版本'),
('V004', 'F006', 'U003', 1, 2048576, '/storage/v1/music_v1.mp3', '2024-01-03 11:45:00', '高品质版本'),
('V005', 'F007', 'U004', 1, 4096000, '/storage/v1/design_v1.psd', '2024-01-04 13:20:00', '设计初稿'),
('V006', 'F007', 'U004', 2, 4234000, '/storage/v2/design_v2.psd', '2024-01-04 16:45:00', '增加图层效果'),
('V007', 'F008', 'U005', 1, 8192000, '/storage/v1/backup_v1.tar.gz', '2024-01-05 08:15:00', '每日备份'),
('V008', 'F003', 'U002', 1, 524288000, '/storage/v1/photos_v1.zip', '2024-01-02 14:00:00', '完整相册'),
('V009', 'F009', 'U006', 1, 512000, '/storage/v1/temp_v1.tmp', '2024-01-06 17:30:00', '临时数据'),
('V010', 'F010', 'U007', 1, 1536000, '/storage/v1/deleted_v1.txt', '2024-01-07 12:00:00', '待删除文件');

-- 上传任务测试数据
INSERT INTO `upload_tasks` VALUES 
('T001', 'U001', '新建文档.docx', 2048000, 20, 20, 1, '2024-01-01 09:00:00', '2024-01-01 09:15:00'),
('T002', 'U002', '大视频文件.mov', 1073741824, 100, 85, 0, '2024-01-02 10:00:00', '2024-01-02 12:30:00'),
('T003', 'U003', '音频专辑.flac', 536870912, 50, 50, 1, '2024-01-03 08:30:00', '2024-01-03 09:45:00'),
('T004', 'U004', '设计素材包.zip', 268435456, 25, 12, 3, '2024-01-04 14:00:00', '2024-01-04 14:20:00'),
('T005', 'U005', '代码项目.tar', 134217728, 15, 15, 1, '2024-01-05 16:00:00', '2024-01-05 16:10:00'),
('T006', 'U006', '图片集合.rar', 67108864, 8, 3, 2, '2024-01-06 11:00:00', '2024-01-06 11:05:00'),
('T007', 'U007', '备份数据.7z', 33554432, 4, 4, 1, '2024-01-07 15:30:00', '2024-01-07 15:35:00'),
('T008', 'U008', '演示PPT.pptx', 16777216, 2, 1, 0, '2024-01-08 13:00:00', '2024-01-08 13:02:00'),
('T009', 'U009', '电子书.pdf', 8388608, 1, 0, 3, '2024-01-09 10:30:00', '2024-01-09 10:30:00'),
('T010', 'U010', '表格数据.xlsx', 4194304, 1, 1, 1, '2024-01-10 09:45:00', '2024-01-10 09:46:00');

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化完成日志
SELECT 
'文件服务数据库初始化完成' as message,
'4个表，40条测试数据' as details,
NOW() as created_at;