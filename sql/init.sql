-- CloudDrive 数据库初始化脚本

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

-- 插入测试用户数据
INSERT IGNORE INTO users (id, userlevel, username, email, password_hash, avatar, storage_quota, storage_used) VALUES 
('admin-user-id', 1, 'admin', 'admin@clouddrive.com', '$2a$10$example_hash_for_admin123', '/avatars/admin.jpg', 107374182400, 0),
('user-001', 0, 'john_doe', 'john.doe@example.com', '$2a$10$example_hash_for_password', '/avatars/john.jpg', 1073741824, 524288000),
('user-002', 0, 'jane_smith', 'jane.smith@example.com', '$2a$10$example_hash_for_password', '/avatars/jane.jpg', 10737418240, 2147483648),
('user-003', 0, 'mike_wilson', 'mike.wilson@example.com', '$2a$10$example_hash_for_password', '/avatars/mike.jpg', 1073741824, 104857600),
('user-004', 0, 'sarah_johnson', 'sarah.johnson@example.com', '$2a$10$example_hash_for_password', '/avatars/sarah.jpg', 107374182400, 53687091200),
('user-005', 0, 'david_brown', 'david.brown@example.com', '$2a$10$example_hash_for_password', NULL, 1073741824, 0),
('user-006', 0, 'lisa_davis', 'lisa.davis@example.com', '$2a$10$example_hash_for_password', '/avatars/lisa.jpg', 10737418240, 1073741824),
('user-007', 1, 'tom_admin', 'tom.admin@clouddrive.com', '$2a$10$example_hash_for_password', '/avatars/tom.jpg', 1099511627776, 107374182400),
('user-008', 0, 'emma_white', 'emma.white@example.com', '$2a$10$example_hash_for_password', '/avatars/emma.jpg', 1073741824, 209715200),
('user-009', 0, 'alex_garcia', 'alex.garcia@example.com', '$2a$10$example_hash_for_password', NULL, 107374182400, 21474836480);

-- 插入测试文件和文件夹数据
INSERT IGNORE INTO files (id, name, path, size, type, mime_type, is_directory, parent_id, owner_id) VALUES 
-- 根目录文件夹
('root-001', 'Documents', '/Documents', 0, 'folder', NULL, TRUE, NULL, 'user-001'),
('root-002', 'Pictures', '/Pictures', 0, 'folder', NULL, TRUE, NULL, 'user-001'),
('root-003', 'Videos', '/Videos', 0, 'folder', NULL, TRUE, NULL, 'user-002'),
('root-004', 'Music', '/Music', 0, 'folder', NULL, TRUE, NULL, 'user-002'),
('root-005', 'Projects', '/Projects', 0, 'folder', NULL, TRUE, NULL, 'user-003'),

-- 子文件夹
('folder-001', 'Work', '/Documents/Work', 0, 'folder', NULL, TRUE, 'root-001', 'user-001'),
('folder-002', 'Personal', '/Documents/Personal', 0, 'folder', NULL, TRUE, 'root-001', 'user-001'),
('folder-003', 'Vacation 2024', '/Pictures/Vacation 2024', 0, 'folder', NULL, TRUE, 'root-002', 'user-001'),
('folder-004', 'Screenshots', '/Pictures/Screenshots', 0, 'folder', NULL, TRUE, 'root-002', 'user-001'),
('folder-005', 'Movies', '/Videos/Movies', 0, 'folder', NULL, TRUE, 'root-003', 'user-002'),

-- 文件
('file-001', 'resume.pdf', '/Documents/Work/resume.pdf', 2097152, 'pdf', 'application/pdf', FALSE, 'folder-001', 'user-001'),
('file-002', 'report.docx', '/Documents/Work/report.docx', 5242880, 'docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', FALSE, 'folder-001', 'user-001'),
('file-003', 'budget.xlsx', '/Documents/Personal/budget.xlsx', 1048576, 'xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', FALSE, 'folder-002', 'user-001'),
('file-004', 'photo1.jpg', '/Pictures/Vacation 2024/photo1.jpg', 4194304, 'jpg', 'image/jpeg', FALSE, 'folder-003', 'user-001'),
('file-005', 'photo2.jpg', '/Pictures/Vacation 2024/photo2.jpg', 3145728, 'jpg', 'image/jpeg', FALSE, 'folder-003', 'user-001'),
('file-006', 'screen1.png', '/Pictures/Screenshots/screen1.png', 1572864, 'png', 'image/png', FALSE, 'folder-004', 'user-001'),
('file-007', 'movie1.mp4', '/Videos/Movies/movie1.mp4', 1073741824, 'mp4', 'video/mp4', FALSE, 'folder-005', 'user-002'),
('file-008', 'song1.mp3', '/Music/song1.mp3', 10485760, 'mp3', 'audio/mpeg', FALSE, 'root-004', 'user-002'),
('file-009', 'song2.flac', '/Music/song2.flac', 52428800, 'flac', 'audio/flac', FALSE, 'root-004', 'user-002'),
('file-010', 'presentation.pptx', '/Projects/presentation.pptx', 15728640, 'pptx', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', FALSE, 'root-005', 'user-003'),
('file-011', 'code.zip', '/Projects/code.zip', 104857600, 'zip', 'application/zip', FALSE, 'root-005', 'user-003'),
('file-012', 'database.sql', '/Projects/database.sql', 2097152, 'sql', 'text/plain', FALSE, 'root-005', 'user-003');

-- 插入文件分享数据
INSERT IGNORE INTO file_shares (id, file_id, share_code, password, expires_at, download_count, max_downloads) VALUES 
('share-001', 'file-001', 'ABCD1234', NULL, '2025-08-08 23:59:59', 5, 50),
('share-002', 'file-004', 'EFGH5678', 'photo123', '2025-07-15 23:59:59', 12, 100),
('share-003', 'file-007', 'IJKL9012', 'movie456', '2025-12-31 23:59:59', 3, 10),
('share-004', 'file-010', 'MNOP3456', NULL, '2025-09-01 23:59:59', 0, -1),
('share-005', 'file-011', 'QRST7890', 'code789', '2025-08-01 23:59:59', 8, 25),
('share-006', 'folder-003', 'UVWX1234', 'vacation', '2025-07-20 23:59:59', 2, 5),
('share-007', 'file-008', 'YZAB5678', NULL, NULL, 15, -1),
('share-008', 'file-002', 'CDEF9012', 'work123', '2025-07-31 23:59:59', 1, 3),
('share-009', 'root-005', 'GHIJ3456', 'project', '2025-10-01 23:59:59', 0, 20),
('share-010', 'file-012', 'KLMN7890', NULL, '2025-08-15 23:59:59', 7, 50);

-- 插入用户订阅数据
INSERT IGNORE INTO user_subscriptions (id, user_id, membership_level_id, start_date, end_date, status, payment_method, payment_amount) VALUES 
('sub-001', 'user-002', 'level-basic', '2025-06-01 00:00:00', '2025-07-01 00:00:00', 'active', 'alipay', 9.99),
('sub-002', 'user-004', 'level-premium', '2025-05-15 00:00:00', '2025-06-15 00:00:00', 'expired', 'wechat', 19.99),
('sub-003', 'user-006', 'level-basic', '2025-07-01 00:00:00', '2025-08-01 00:00:00', 'active', 'alipay', 9.99),
('sub-004', 'user-009', 'level-premium', '2025-06-10 00:00:00', '2025-07-10 00:00:00', 'active', 'bank_card', 19.99),
('sub-005', 'user-003', 'level-free', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'active', NULL, 0.00),
('sub-006', 'user-005', 'level-free', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'active', NULL, 0.00),
('sub-007', 'user-007', 'level-enterprise', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'active', 'bank_transfer', 99.99),
('sub-008', 'user-008', 'level-basic', '2025-05-01 00:00:00', '2025-06-01 00:00:00', 'cancelled', 'alipay', 9.99),
('sub-009', 'user-001', 'level-free', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'active', NULL, 0.00),
('sub-010', 'admin-user-id', 'level-enterprise', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'active', NULL, 0.00);