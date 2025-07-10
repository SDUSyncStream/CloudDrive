-- CloudDrive 数据插入脚本
-- 包含所有基础数据和测试数据的插入

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE cloud_drive;

-- 插入默认会员等级数据
INSERT IGNORE INTO membership_levels (id, name, storage_quota, max_file_size, price, duration_days, features, priority) VALUES 
('level001', '免费版', 1073741824, 104857600, 0.00, 0, '1GB存储空间,单文件100MB', 0),
('level002', '标准版', 5368709120, 536870912, 19.99, 30, '5GB存储空间,单文件500MB', 10),
('level003', '高级版', 53687091200, 5368709120, 39.99, 30, '50GB存储空间,单文件5GB', 20),
('level004', '专业版', 214748364800, 10737418240, 79.99, 30, '200GB存储空间,单文件10GB', 30),
('level005', '企业版', 1099511627776, 21474836480, 199.99, 30, '1TB存储空间,单文件20GB', 40);

-- 插入测试用户数据
INSERT IGNORE INTO users (id, userlevel, username, email, password_hash, avatar, storage_quota, storage_used, created_at, updated_at) VALUES
('292ddee0-518c-4ff9-9eb8-3feabbcaff27', 0, 'testuser', 'test@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar1.jpg', 1073741824, 524288000, NOW(), NOW()),
('user002', 0, 'bob', 'bob@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar2.jpg', 5368709120, 1073741824, NOW(), NOW()),
('user003', 1, 'admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar3.jpg', 107374182400, 0, NOW(), NOW()),
('user004', 0, 'charlie', 'charlie@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar4.jpg', 2147483648, 1048576000, NOW(), NOW()),
('user005', 0, 'diana', 'diana@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar5.jpg', 1073741824, 268435456, NOW(), NOW()),
('user006', 0, 'edward', 'edward@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar6.jpg', 1073741824, 0, NOW(), NOW()),
('user007', 0, 'fiona', 'fiona@example.com', '$2a$10$dXJ3SW6G7P7T.5fzKUKrZ.dkWbYlHDyNNzMmP9A8q/gXVpF7jVXUK', 'avatar7.jpg', 10737418240, 2147483648, NOW(), NOW());

-- 插入测试文件信息数据
INSERT IGNORE INTO file_info (file_id, user_id, file_md5, file_pid, file_size, file_name, file_cover, file_path, create_time, last_update_time, folder_type, file_category, file_type, status, recovery_time, del_flag) VALUES
('file001', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'a1b2c3d4e5f6789012345678901234ab', '0', 0, '我的文档', '', '/', NOW(), NOW(), 1, 4, 10, 2, NULL, 2),
('file002', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'b2c3d4e5f6789012345678901234bc', 'file001', 1024000, '工作报告.pdf', '', '/documents/', NOW(), NOW(), 0, 4, 4, 2, NULL, 2),
('file003', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'c3d4e5f6789012345678901234cd', 'file001', 512000, '项目计划.docx', '', '/documents/', NOW(), NOW(), 0, 4, 5, 2, NULL, 2),
('file004', 'user002', 'd4e5f6789012345678901234de', '0', 0, '照片集', '', '/', NOW(), NOW(), 1, 3, 10, 2, NULL, 2),
('file005', 'user002', 'e5f6789012345678901234ef', 'file004', 2048000, '风景照.jpg', 'cover1.jpg', '/photos/', NOW(), NOW(), 0, 3, 3, 2, NULL, 2),
('file006', 'user002', 'f6789012345678901234f0', 'file004', 1536000, '人像照.png', 'cover2.jpg', '/photos/', NOW(), NOW(), 0, 3, 3, 2, NULL, 2),
('file007', 'user003', '789012345678901234567890ab', '0', 0, '视频库', '', '/', NOW(), NOW(), 1, 1, 10, 2, NULL, 2),
('file008', 'user003', '89012345678901234567890bc', 'file007', 52428800, '演示视频.mp4', 'cover3.jpg', '/videos/', NOW(), NOW(), 0, 1, 1, 2, NULL, 2),
('file009', 'user004', '9012345678901234567890cd', '0', 256000, '音乐.mp3', '', '/', NOW(), NOW(), 0, 2, 2, 2, NULL, 2),
('file010', 'user005', '012345678901234567890de', '0', 1024000, '代码文件.zip', '', '/', NOW(), NOW(), 0, 5, 9, 2, NULL, 1);

-- 插入测试文件分享数据
INSERT IGNORE INTO file_share (share_id, file_id, user_id, valid_type, expire_time, share_time, code, show_count) VALUES
('share001', 'file002', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 1, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), 'abc12', 15),
('share002', 'file005', 'user002', 2, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), 'def34', 8),
('share003', 'file008', 'user003', 3, NULL, NOW(), 'ghi56', 32),
('share004', 'file003', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), 'jkl78', 3),
('share005', 'file006', 'user002', 1, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), 'mno90', 21),
('share006', 'file009', 'user004', 2, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), 'pqr12', 7),
('share007', 'file010', 'user005', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), 'stu34', 1);

-- 插入测试用户订阅数据
INSERT IGNORE INTO user_subscriptions (id, user_id, membership_level_id, start_date, end_date, status, payment_method, payment_amount, created_at, updated_at) VALUES
('1943129998659944450', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'level005', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'active', 'bank_card', 199.99, NOW(), NOW()),
('sub002', 'user002', 'level002', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'active', 'credit_card', 19.99, NOW(), NOW()),
('sub003', 'user003', 'level004', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'active', 'alipay', 79.99, NOW(), NOW()),
('sub004', 'user004', 'level003', DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 'expired', 'wechat_pay', 39.99, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW()),
('sub005', 'user005', 'level002', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'active', 'paypal', 19.99, NOW(), NOW()),
('sub006', 'user006', 'level001', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 'active', 'free', 0.00, NOW(), NOW()),
('sub007', 'user007', 'level005', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'active', 'bank_transfer', 199.99, NOW(), NOW()),
('sub008', 'user002', 'level001', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), 'expired', 'free', 0.00, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW());

-- 插入测试支付订单数据
INSERT IGNORE INTO payment_orders (id, user_id, membership_level_id, order_number, amount, payment_method, status, transaction_id, paid_at, created_at, updated_at) VALUES
('order001', 'user002', 'level002', 'ORD202501090001', 19.99, 'credit_card', 'paid', 'TXN_CC_001', NOW(), NOW(), NOW()),
('order002', 'user003', 'level004', 'ORD202501090002', 79.99, 'alipay', 'paid', 'TXN_ALIPAY_002', NOW(), NOW(), NOW()),
('order003', 'user004', 'level003', 'ORD202501090003', 39.99, 'wechat_pay', 'paid', 'TXN_WECHAT_003', DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 35 DAY), NOW()),
('order004', 'user005', 'level002', 'ORD202501090004', 19.99, 'paypal', 'paid', 'TXN_PAYPAL_004', NOW(), NOW(), NOW()),
('order005', 'user007', 'level005', 'ORD202501090005', 199.99, 'bank_transfer', 'paid', 'TXN_BANK_005', NOW(), NOW(), NOW()),
('order006', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'level002', 'ORD202501090006', 19.99, 'credit_card', 'pending', NULL, NULL, NOW(), NOW()),
('order007', 'user006', 'level003', 'ORD202501090007', 39.99, 'alipay', 'failed', 'TXN_ALIPAY_007', NULL, NOW(), NOW()),
('order008', 'user004', 'level002', 'ORD202501090008', 19.99, 'wechat_pay', 'cancelled', NULL, NULL, NOW(), NOW()),
('order009', 'user002', 'level003', 'ORD202501090009', 39.99, 'paypal', 'paid', 'TXN_PAYPAL_009', NOW(), NOW(), NOW()),
('order010', '292ddee0-518c-4ff9-9eb8-3feabbcaff27', 'level005', 'ORD202507100001', 199.99, 'bank_card', 'paid', 'TXN_BANK_010', NOW(), NOW(), NOW());

-- 数据插入完成
SELECT 'CloudDrive测试数据插入完成！' as message;
SELECT '会员等级数据：' as info;
SELECT id, name, price, duration_days, priority FROM membership_levels ORDER BY priority;