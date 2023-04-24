/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50721
 Source Host           : localhost
 Source Database       : fastdfs

 Target Server Type    : MySQL
 Target Server Version : 50721
 File Encoding         : utf-8

 Date: 10/01/2021 09:00:58 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `sys_document`
-- ----------------------------
DROP TABLE IF EXISTS `sys_document`;
CREATE TABLE `sys_document` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `extension` varchar(16) DEFAULT NULL,
  `content_type` varchar(128) DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `group_name` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
