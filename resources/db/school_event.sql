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
INSERT INTO `activity` VALUES ('10', 'party1', '1', 'c22750f6-c954-4c0a-8004-6ce5005b2350', 1, '2025-11-19 16:21:00', '2025-11-19 16:23:00', 1763540518048, 'approved', NULL);
INSERT INTO `activity` VALUES ('16', 'impart', '大型室内多人活动·', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', 20, '2025-11-22 23:00:00', '2025-11-23 01:30:00', 1763735607162, 'approved', NULL);
INSERT INTO `activity` VALUES ('18', '111', '111', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', 2, '2025-11-22 16:58:00', '2025-11-23 16:58:00', 1763801937003, 'approved', '111');
INSERT INTO `activity` VALUES ('2', 'party2', '2', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', 24624, '2025-11-18 21:19:00', '2025-11-18 21:21:00', 1763471932744, 'approved', NULL);
INSERT INTO `activity` VALUES ('20', '123', '123', '71137629', 12, '2025-11-22 17:10:00', '2025-11-23 17:10:00', 1763802620027, 'approved', '123');
INSERT INTO `activity` VALUES ('21', '123', '123', '71137629', 2, '2025-11-22 17:23:00', '2025-11-23 17:23:00', 1763803408023, 'approved', '123');
INSERT INTO `activity` VALUES ('22', '123', '123', '71137629', 2, '2025-11-23 17:45:00', '2025-11-30 21:45:00', 1763804761560, 'approved', '123');
INSERT INTO `activity` VALUES ('23', '123', '123', '71137629', 2, '2025-11-23 17:46:00', '2025-11-29 17:46:00', 1763804787558, 'approved', '123');
INSERT INTO `activity` VALUES ('24', '123', '123', '71137629', 2, '2025-11-22 17:49:00', '2025-11-23 17:49:00', 1763804960936, 'approved', '123');
INSERT INTO `activity` VALUES ('25', '123', '123', '71137629', 2, '2025-11-24 12:02:00', '2025-11-28 12:02:00', 1763956941030, 'approved', '123');
INSERT INTO `activity` VALUES ('26', '124', '124', '66859640', 2, '2025-11-24 12:02:00', '2025-11-28 12:02:00', 1763956976964, 'approved', '123');
INSERT INTO `activity` VALUES ('4', 'party4', '4', 'c22750f6-c954-4c0a-8004-6ce5005b2350', 78, '2025-11-18 23:20:00', '2025-11-22 21:20:00', 1763472050238, 'approved', NULL);

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
INSERT INTO `registration` VALUES ('16306150', '16306150', '2', '已拒绝');
INSERT INTO `registration` VALUES ('17e37da8-83d8-42e9-af3f-e1334b5034aa', '71137629', '18', '已通过');
INSERT INTO `registration` VALUES ('1e0955c8-1f6f-4f29-b53a-a34822b08c0d', 'c22750f6-c954-4c0a-8004-6ce5005b2350', '2', '已拒绝');
INSERT INTO `registration` VALUES ('358e4acb-488b-40e0-b694-f478ee748b5b', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '20', '已通过');
INSERT INTO `registration` VALUES ('4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '4', '已申请');
INSERT INTO `registration` VALUES ('57790ff5-abca-4101-9651-23e2c02ba826', '71137629', '26', '已申请');
INSERT INTO `registration` VALUES ('62c98ea5-56d1-4102-8995-5f13822b5fe9', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '16', '已通过');
INSERT INTO `registration` VALUES ('80cbc167-c370-4b02-8482-0d90816c38f2', '71137629', '24', '已申请');
INSERT INTO `registration` VALUES ('80f1db94-12d8-4ac8-b3e9-1fc5e124a6a4', '71137629', '20', '已申请');
INSERT INTO `registration` VALUES ('a48bcdd6-7030-443d-9e91-cec415b40c8c', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '2', '已拒绝');
INSERT INTO `registration` VALUES ('b3249c41-fbf1-4d83-a801-fef84a678e08', '71137629', '23', '已申请');
INSERT INTO `registration` VALUES ('c14428bf-97e9-4e31-afc6-050dfc650d4c', '4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', '18', '已申请');

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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('16306150', 'user3', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('29989497', 'testadmin', 'testadmin', 'testadmin@example.com', NULL, 'user');
INSERT INTO `user` VALUES ('4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8', 'admin', '123', '1@qq.com', '/resources/images/4d7e420c-5c81-48ef-9d7a-b106aa8ef3f8.jpg', 'admin');
INSERT INTO `user` VALUES ('60730593', '125', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('62844214', '127', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('66859640', '124', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('71137629', '123', '123', '1@qq.com', NULL, 'admin');
INSERT INTO `user` VALUES ('75598986', '126', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('c0e65d7e-bf8c-4170-8dee-9e0a388ea76d', 'user2', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('c22750f6-c954-4c0a-8004-6ce5005b2350', 'user', '123', '1@qq.com', NULL, 'user');
INSERT INTO `user` VALUES ('e76c74be-918b-44d0-ac99-c795e7c4214f', 'admin1', '123', '1@qq.com', NULL, 'user');

SET FOREIGN_KEY_CHECKS = 1;
