-- CloudDrive 数据库初始化脚本

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
CREATE TABLE IF NOT EXISTS files (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(400) NOT NULL,
    size BIGINT NOT NULL DEFAULT 0,
    type VARCHAR(50) NOT NULL,
    mime_type VARCHAR(100),
    is_directory BOOLEAN NOT NULL DEFAULT FALSE,
    parent_id VARCHAR(36),
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_parent_id (parent_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_path (path)
);

-- 文件分享表
CREATE TABLE IF NOT EXISTS file_shares (
    id VARCHAR(36) PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL,
    share_code VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100),
    expires_at TIMESTAMP,
    download_count INT DEFAULT 0,
    max_downloads INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    INDEX idx_share_code (share_code),
    INDEX idx_file_id (file_id)
);

-- 会员等级表
CREATE TABLE IF NOT EXISTS membership_levels (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    storage_quota BIGINT NOT NULL,
    max_file_size BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_days INT NOT NULL,
    features TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
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
    INDEX idx_membership_level_id (membership_level_id),
    INDEX idx_status (status)
);

-- 插入默认会员等级
INSERT IGNORE INTO membership_levels (id, name, storage_quota, max_file_size, price, duration_days, features) VALUES 
('level-free', '免费版', 1073741824, 104857600, 0.00, 0, '1GB存储空间,单文件100MB'),
('level-basic', '基础版', 10737418240, 1073741824, 9.99, 30, '10GB存储空间,单文件1GB'),
('level-premium', '高级版', 107374182400, 5368709120, 19.99, 30, '100GB存储空间,单文件5GB'),
('level-enterprise', '企业版', 1099511627776, 21474836480, 99.99, 30, '1TB存储空间,单文件20GB');

-- 插入默认管理员用户（密码：admin123）
INSERT IGNORE INTO users (id, username, email, password_hash) VALUES 
('admin-user-id', 'admin', 'admin@clouddrive.com', '$2a$10$example_hash_for_admin123');