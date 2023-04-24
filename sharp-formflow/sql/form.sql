/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50721
 Source Host           : localhost
 Source Database       : sharp-demo

 Target Server Type    : MySQL
 Target Server Version : 50721
 File Encoding         : utf-8

 Date: 11/08/2021 19:11:31 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `sys_form`
-- ----------------------------
DROP TABLE IF EXISTS `sys_form`;
CREATE TABLE `sys_form` (
  `id` bigint(20) NOT NULL,
  `name` varchar(32) NOT NULL,
  `create_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint(20) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `sys_form`
-- ----------------------------
BEGIN;
INSERT INTO `sys_form` VALUES ('487677232379494400', '我的第一个表单', '0', '2021-11-04 15:48:11', '0', '2021-11-04 15:48:11', b'0');
COMMIT;

-- ----------------------------
--  Table structure for `sys_form_configurer`
-- ----------------------------
DROP TABLE IF EXISTS `sys_form_configurer`;
CREATE TABLE `sys_form_configurer` (
  `id` bigint(20) NOT NULL,
  `label` varchar(16) NOT NULL,
  `type` varchar(32) NOT NULL,
  `validators` text,
  `options` varchar(1024) DEFAULT NULL,
  `default_value` varchar(32) DEFAULT NULL,
  `placeholder` varchar(32) DEFAULT NULL,
  `additional_info` varchar(1024) DEFAULT NULL,
  `create_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint(20) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `sys_form_configurer`
-- ----------------------------
BEGIN;
INSERT INTO `sys_form_configurer` VALUES ('487671506907070464', '姓名', 'TEXT', '[{\"max\":16,\"message\":\"长度不能超过16个字符\",\"validatorType\":\"LENGTH\"},{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', null, 'Rick', '请输入姓名', null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070465', '年龄', 'NUMBER_TEXT', '[{\"min\":18,\"max\":100,\"message\":\"大小范围是18 - 100\",\"validatorType\":\"SIZE\"},{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', null, '18', '请输入年龄', null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070466', '兴趣爱好（单选）', 'SELECT', '[{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', '%E8%B6%B3%E7%90%83,%E7%AF%AE%E7%90%83,%E4%B9%92%E4%B9%93%E7%90%83,%E7%BE%BD%E6%AF%9B%E7%90%83', '足球', '请选择兴趣爱好', null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070467', '信息收集', 'TABLE', '[]', null, null, '请输入信息收集', '{\"labels\":[\"姓名\",\"年龄\"]}', '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070468', '兴趣爱好（多选）', 'CHECKBOX', '[{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', '%E8%B6%B3%E7%90%83,%E7%AF%AE%E7%90%83,%E4%B9%92%E4%B9%93%E7%90%83,%E7%BE%BD%E6%AF%9B%E7%90%83', '[\"足球\", \"篮球\"]', '请选择兴趣爱好', '{\"labels\":[\"姓名\",\"年龄\"]}', '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070469', '备注', 'TEXT', '[]', null, null, '请输入备注', '{\"labels\":[\"姓名\",\"年龄\"]}', '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070470', '是否同意', 'CHECKBOX', '[{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', '%E5%90%8C%E6%84%8F', null, null, null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070471', '简介', 'TEXTAREA', '[{\"max\":16,\"message\":\"长度不能超过16个字符\",\"validatorType\":\"LENGTH\"},{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', null, '挺好...', '请输入简介', null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070473', '手机', 'MOBILE', '[{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', null, null, null, null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070474', '文件', 'FILE', '[{\"required\":true,\"message\":\"必填项\",\"validatorType\":\"REQUIRED\"}]', null, null, null, null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070475', '邮箱', 'EMAIL', '[]', null, null, null, null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0'), ('487671506907070476', '日期', 'DATE', '[]', null, null, null, null, '0', '2021-11-04 15:25:26', '0', '2021-11-04 15:25:26', b'0');
COMMIT;

-- ----------------------------
--  Table structure for `sys_form_cpn_configurer`
-- ----------------------------
DROP TABLE IF EXISTS `sys_form_cpn_configurer`;
CREATE TABLE `sys_form_cpn_configurer` (
  `id` bigint(20) NOT NULL,
  `form_id` bigint(20) NOT NULL,
  `name` varchar(16) NOT NULL,
  `config_id` bigint(20) NOT NULL,
  `order_num` int(11) DEFAULT NULL,
  `create_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint(20) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `sys_form_cpn_configurer`
-- ----------------------------
BEGIN;
INSERT INTO `sys_form_cpn_configurer` VALUES ('48768180410374540', '487677232379494400', 'checkbox', '487671506907070468', '3', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745536', '487677232379494400', 'HNepFsUCaN', '487671506907070464', '0', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745537', '487677232379494400', 'WpOZNqQasd', '487671506907070465', '1', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745538', '487677232379494400', 'NUqlBwLPfW', '487671506907070466', '2', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745539', '487677232379494400', 'xYfeMutDQK', '487671506907070467', '11', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745540', '487677232379494400', 'bz', '487671506907070469', '5', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745541', '487677232379494400', 'agree', '487671506907070470', '4', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745542', '487677232379494400', 'textarea', '487671506907070471', '6', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745544', '487677232379494400', 'mobile', '487671506907070473', '7', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745545', '487677232379494400', 'file', '487671506907070474', '8', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745546', '487677232379494400', 'email', '487671506907070475', '9', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0'), ('487681804103745547', '487677232379494400', 'date', '487671506907070476', '10', '0', '2021-11-08 18:46:13', '0', '2021-11-08 18:46:13', b'0');
COMMIT;

-- ----------------------------
--  Table structure for `sys_form_cpn_value`
-- ----------------------------
DROP TABLE IF EXISTS `sys_form_cpn_value`;
CREATE TABLE `sys_form_cpn_value` (
  `id` bigint(20) NOT NULL,
  `value` text,
  `form_cpn_id` bigint(20) NOT NULL,
  `form_id` bigint(20) NOT NULL,
  `config_id` bigint(20) NOT NULL,
  `instance_id` bigint(20) NOT NULL,
  `create_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_id` bigint(20) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `sys_form_cpn_value`
-- ----------------------------
BEGIN;
INSERT INTO `sys_form_cpn_value` VALUES ('489171690522574848', '李峰1', '487681804103745536', '487677232379494400', '487671506907070464', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690522574849', '39', '487681804103745537', '487677232379494400', '487671506907070465', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690526769152', '乒乓球', '487681804103745538', '487677232379494400', '487671506907070466', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690526769153', '[\"足球\",\"篮球\",\"乒乓球\"]', '48768180410374540', '487677232379494400', '487671506907070468', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690526769154', '[\"同意\"]', '487681804103745541', '487677232379494400', '487671506907070470', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690530963456', '这个是备注311', '487681804103745540', '487677232379494400', '487671506907070469', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690530963457', '好的', '487681804103745542', '487677232379494400', '487671506907070471', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690535157760', '18898987711', '487681804103745544', '487677232379494400', '487671506907070473', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690535157761', '[{\"fullPath\":\"upload/489165707205312512.jpg\",\"path\":\"489165707205312512.jpg\",\"createdAt\":\"2021-11-08T10:22:50.669Z\",\"extension\":\"jpg\",\"groupName\":\"upload\",\"size\":86575,\"name\":\"轮播图\",\"fullName\":\"轮播图.jpg\",\"id\":\"489165707217895424\",\"contentType\":\"image/jpeg\",\"url\":\"http://localhost:7892/upload/489165707205312512.jpg\"}]', '487681804103745545', '487677232379494400', '487671506907070474', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690535157762', 'jkxyx205@gmail.com', '487681804103745546', '487677232379494400', '487671506907070475', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690535157763', '2021-11-06', '487681804103745547', '487677232379494400', '487671506907070476', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0'), ('489171690539352064', null, '487681804103745539', '487677232379494400', '487671506907070467', '487684156282011648', '0', '2021-11-08 18:46:37', '0', '2021-11-08 18:46:37', b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
