-- CloudDrive 微服务数据库创建脚本
-- 创建4个独立的微服务数据库

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 1. 用户服务数据库
-- =============================================================================
CREATE DATABASE IF NOT EXISTS user_service_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- =============================================================================
-- 2. 文件服务数据库
-- =============================================================================
CREATE DATABASE IF NOT EXISTS file_service_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- =============================================================================
-- 3. 管理服务数据库
-- =============================================================================
CREATE DATABASE IF NOT EXISTS admin_service_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- =============================================================================
-- 4. 会员服务数据库
-- =============================================================================
CREATE DATABASE IF NOT EXISTS membership_service_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- =============================================================================
-- 授权访问（为应用创建专用用户）
-- =============================================================================

-- 用户服务访问权限
GRANT ALL PRIVILEGES ON user_service_db.* TO 'cloud_drive'@'%';

-- 文件服务访问权限
GRANT ALL PRIVILEGES ON file_service_db.* TO 'cloud_drive'@'%';

-- 管理服务访问权限
GRANT ALL PRIVILEGES ON admin_service_db.* TO 'cloud_drive'@'%';

-- 会员服务访问权限
GRANT ALL PRIVILEGES ON membership_service_db.* TO 'cloud_drive'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

SET FOREIGN_KEY_CHECKS = 1;

-- 数据库创建完成日志
SELECT 
'CloudDrive微服务数据库创建完成' as message,
NOW() as created_at;