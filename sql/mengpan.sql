/*
 Navicat Premium Data Transfer

 Source Server         : mysqldb
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : mengpan

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 07/07/2025 16:40:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info`  (
  `file_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
  `file_pid` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
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
  INDEX `idx_file_pid`(`file_id`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE,
  INDEX `idx_recover_time`(`recovery_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_zh_0900_as_cs ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_info
-- ----------------------------

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NOT NULL,
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_zh_0900_as_cs NULL DEFAULT NULL,
  `join_time` datetime NULL DEFAULT NULL,
  `last_login_time` datetime NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `use_space` bigint NULL DEFAULT NULL,
  `total_space` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_email`(`email`) USING BTREE,
  UNIQUE INDEX `key_nick_name`(`nick_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_zh_0900_as_cs ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
