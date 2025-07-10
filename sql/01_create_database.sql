-- CloudDrive 数据库结构初始化脚本
-- 只包含数据库和表结构的创建，不包含数据插入

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS cloud_drive CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cloud_drive;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    userlevel INT DEFAULT 0,    -- 若为1，则为管理员
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    storage_quota BIGINT DEFAULT 1073741824, -- 1GB 默认配额
    storage_used BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- 文件表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info`  (
    `file_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
    `user_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
    `file_md5` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
    `file_pid` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
    `file_size` bigint NULL DEFAULT NULL,
    `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
    `file_cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
    `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `last_update_time` datetime NULL DEFAULT NULL,
    `folder_type` tinyint(1) NULL DEFAULT NULL COMMENT '0文件1目录',
    `file_category` tinyint(1) NULL DEFAULT NULL COMMENT '1视频2音频3图片4文档5其他',
    `file_type` tinyint NULL DEFAULT NULL COMMENT '1视频2音频3图片4pdf5doc6excel7txt8code9zip10其他',
    `status` tinyint NULL DEFAULT NULL COMMENT '0转码中1转码失败2转码成功',
    `recovery_time` datetime NULL DEFAULT NULL COMMENT '进入回收站时间',
    `del_flag` tinyint(1) NULL DEFAULT NULL COMMENT '0删除1回收站2正常',
    PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
    INDEX `idx_create_time`(`create_time`) USING BTREE,
    INDEX `idx_user_id`(`user_id`) USING BTREE,
    INDEX `idx_md5`(`file_md5`) USING BTREE,
    INDEX `idx_del_flag`(`del_flag`) USING BTREE,
    INDEX `idx_pid`(`file_pid`) USING BTREE,
    INDEX `idx_show_name`(`file_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息';

-- 文件分享表
DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share` (
    `share_id` varchar(20) NOT NULL COMMENT '分享ID',
    `file_id` varchar(20) DEFAULT NULL COMMENT '文件ID',
    `user_id` varchar(20) DEFAULT NULL COMMENT '用户ID',
    `valid_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型 0:1天 1:7天 2:30天 3:永久有效',
    `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
    `share_time` datetime DEFAULT NULL COMMENT '分享时间',
    `code` varchar(5) DEFAULT NULL COMMENT '提取码',
    `show_count` int DEFAULT '0' COMMENT '浏览次数',
    PRIMARY KEY (`share_id`) USING BTREE,
    UNIQUE KEY `idx_share_id` (`share_id`) USING BTREE,
    KEY `idx_file_id` (`file_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_share_time` (`share_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享信息';

-- 会员等级表
CREATE TABLE IF NOT EXISTS membership_levels (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    storage_quota BIGINT NOT NULL,
    max_file_size BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_days INT NOT NULL,
    features TEXT,
    priority INT NOT NULL DEFAULT 0 COMMENT '会员等级优先级，数值越大优先级越高',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_priority (priority)
);

-- 用户订阅表
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    membership_level_id VARCHAR(36) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status ENUM('active', 'expired', 'cancelled') NOT NULL DEFAULT 'active',
    payment_method VARCHAR(50),
    payment_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (membership_level_id) REFERENCES membership_levels(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_end_date (end_date)
);

-- 支付订单表
CREATE TABLE IF NOT EXISTS payment_orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    membership_level_id VARCHAR(36) NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status ENUM('pending', 'paid', 'failed', 'cancelled') NOT NULL DEFAULT 'pending',
    transaction_id VARCHAR(100),
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (membership_level_id) REFERENCES membership_levels(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status (status)
);

-- 数据库结构创建完成
SELECT 'CloudDrive数据库结构创建完成！' as message;