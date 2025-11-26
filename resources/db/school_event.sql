/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : school_event

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 24/11/2025 12:08:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('16306150', 'student1', '123', 'student1@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('29989497', 'student2', '123', 'student2@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('37654321', 'admin', '123', 'admin@university.edu', NULL, 'admin');
INSERT INTO `user` VALUES ('60730593', 'student3', '123', 'student3@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('62844214', 'student4', '123', 'student4@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('66859640', 'student5', '123', 'student5@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('71137629', 'teacher1', '123', 'teacher1@university.edu', NULL, 'admin');
INSERT INTO `user` VALUES ('75598986', 'student6', '123', 'student6@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('82345671', 'student7', '123', 'student7@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('98765432', 'student8', '123', 'student8@university.edu', NULL, 'user');
INSERT INTO `user` VALUES ('91234567', 'teacher2', '123', 'teacher2@university.edu', NULL, 'user');

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `publisher_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `max_num` int NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `published_at` bigint NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'approved',
  `location` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `activity_ibfk_1`(`publisher_id` ASC) USING BTREE,
  CONSTRAINT `activity_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity
-- ----------------------------
-- 按照发布时间顺序排列的活动（ID按提交顺序分配）
-- 最早发布的活动
INSERT INTO `activity` VALUES ('1', '迎新晚会', '迎接新生的文艺晚会表演', '98765432', 500, '2025-09-28 19:00:00', '2025-09-28 21:30:00', 1763472050238, 'approved', '学校大礼堂');
INSERT INTO `activity` VALUES ('2', '校园歌手大赛', '年度校园歌手大赛，欢迎所有热爱音乐的同学参加', '98765432', 100, '2025-10-01 18:00:00', '2025-10-01 21:00:00', 1763540518048, 'approved', '大学生活动中心');
INSERT INTO `activity` VALUES ('3', '科技创新讲座', '邀请知名科技企业专家分享前沿技术趋势', '37654321', 200, '2025-11-20 14:00:00', '2025-11-20 16:30:00', 1763735607162, 'approved', '图书馆报告厅');
INSERT INTO `activity` VALUES ('4', '秋季运动会', '学校年度秋季运动会', '71137629', 300, '2025-10-15 08:00:00', '2025-10-16 17:00:00', 1763801937003, 'approved', '学校操场');
INSERT INTO `activity` VALUES ('5', '辩论赛初赛', '校园辩论赛初赛环节', '66859640', 50, '2025-11-05 14:00:00', '2025-11-05 17:00:00', 1763802620027, 'approved', '教学楼A101');
INSERT INTO `activity` VALUES ('6', '学术研讨会', '计算机科学前沿技术学术研讨会', '71137629', 80, '2025-11-25 08:00:00', '2025-11-28 18:00:00', 1763803408023, 'approved', '学术报告厅');
INSERT INTO `activity` VALUES ('7', '创新创业大赛', '大学生创新创业项目路演比赛', '71137629', 100, '2025-11-25 09:00:00', '2025-11-25 17:00:00', 1763804761560, 'approved', '创新创业中心');
INSERT INTO `activity` VALUES ('8', '志愿者招募', '社区服务志愿者招募活动', '71137629', 20, '2025-11-25 10:00:00', '2025-11-27 17:00:00', 1763804960936, 'approved', '行政楼大厅');
INSERT INTO `activity` VALUES ('9', '读书分享会', '经典文学作品读书分享交流会', '71137629', 40, '2025-11-25 14:00:00', '2025-11-25 16:30:00', 1763956941030, 'approved', '图书馆阅览室');
INSERT INTO `activity` VALUES ('10', '职业规划讲座', '邀请资深HR为同学们讲解职业规划', '71137629', 150, '2025-11-25 15:00:00', '2025-11-25 17:00:00', 1763956941031, 'approved', '就业指导中心');
-- 后续发布的活动
INSERT INTO `activity` VALUES ('11', '校园文化节', '为期三天的校园文化节活动', '37654321', 500, '2025-11-24 09:00:00', '2025-11-26 17:00:00', 1763956941032, 'approved', '全校范围');
INSERT INTO `activity` VALUES ('12', '英语角活动', '提高英语口语交流能力的开放活动', '37654321', 30, '2025-11-27 19:00:00', '2025-11-27 21:00:00', 1763956941033, 'approved', '外语学院楼前广场');
INSERT INTO `activity` VALUES ('13', '篮球友谊赛', '学院间篮球友谊赛，促进各学院交流', '37654321', 50, '2025-12-10 16:00:00', '2025-12-10 18:00:00', 1763956941034, 'approved', '学校体育馆');
INSERT INTO `activity` VALUES ('14', '读书分享会二期', '经典文学作品读书分享交流会第二期', '71137629', 40, '2025-12-12 14:00:00', '2025-12-12 16:30:00', 1763956941035, 'approved', '图书馆阅览室');
INSERT INTO `activity` VALUES ('15', '志愿者培训', '新志愿者培训活动', '71137629', 30, '2025-12-15 10:00:00', '2025-12-15 17:00:00', 1763956941036, 'approved', '行政楼大厅');
INSERT INTO `activity` VALUES ('16', '心理健康讲座', '关注大学生心理健康，提供专业指导', '71137629', 80, '2025-12-22 14:00:00', '2025-12-22 16:00:00', 1763956941037, 'approved', '心理咨询中心');
INSERT INTO `activity` VALUES ('17', '摄影比赛', '校园风景摄影比赛，展现校园美丽风光', '71137629', 50, '2025-12-20 09:00:00', '2025-12-20 17:00:00', 1763956941038, 'approved', '艺术学院展厅');
INSERT INTO `activity` VALUES ('18', '毕业典礼彩排', '春季毕业典礼彩排活动', '66859640', 300, '2025-12-25 10:00:00', '2025-12-25 12:00:00', 1763956941039, 'approved', '学校大礼堂');

-- ----------------------------
-- Table structure for registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `activity_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `registration_ibfk_1`(`user_id` ASC) USING BTREE,
  INDEX `registration_ibfk_2`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `registration_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `registration_ibfk_2` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of registration
-- ----------------------------
INSERT INTO `registration` VALUES ('100000000001', '16306150', '12', '已拒绝');
INSERT INTO `registration` VALUES ('100000000002', '71137629', '11', '已通过');
INSERT INTO `registration` VALUES ('100000000003', '98765432', '12', '已拒绝');
INSERT INTO `registration` VALUES ('100000000004', '37654321', '8', '已通过');
INSERT INTO `registration` VALUES ('100000000005', '37654321', '1', '已申请');
INSERT INTO `registration` VALUES ('100000000006', '71137629', '18', '已申请');
INSERT INTO `registration` VALUES ('100000000007', '37654321', '3', '已通过');
INSERT INTO `registration` VALUES ('100000000008', '71137629', '7', '已申请');
INSERT INTO `registration` VALUES ('100000000009', '71137629', '8', '已申请');
INSERT INTO `registration` VALUES ('100000000010', '37654321', '12', '已拒绝');
INSERT INTO `registration` VALUES ('100000000011', '71137629', '9', '已申请');
INSERT INTO `registration` VALUES ('100000000012', '37654321', '11', '已申请');

SET FOREIGN_KEY_CHECKS = 1;