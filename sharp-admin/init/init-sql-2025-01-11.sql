# ************************************************************
# Sequel Ace SQL dump
# Version 20046
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# Host: localhost (MySQL 9.1.0)
# Database: sharp-admin
# Generation Time: 2025-01-11 05:12:29 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table sys_access_info
# ------------------------------------------------------------

CREATE TABLE `sys_access_info` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_code_description
# ------------------------------------------------------------

CREATE TABLE `sys_code_description` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '外部可见，唯一code',
  `description` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `category` enum('MATERIAL','PURCHASING_ORG','PACKAGING','SALES_ORG') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sort` int DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编号-描述 表';



# Dump of table sys_dict
# ------------------------------------------------------------

CREATE TABLE `sys_dict` (
  `id` bigint DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `sort` int DEFAULT NULL,
  `remark` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`type`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;

INSERT INTO `sys_dict` (`id`, `type`, `name`, `label`, `sort`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(3,'MATERIAL','M1','物料1',0,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(4,'MATERIAL','M2','物料2',1,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(5,'MATERIAL','M3','物料3',2,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(6,'MATERIAL','M4','物料4',3,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(695367653333712896,'MATERIAL_TYPE','HIBE','耗材用品',0,'30000',1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(862375391526948864,'UNIT','BAG','包',2,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(1,'UNIT','EA','个',0,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(2,'UNIT','KG','千克',1,NULL,1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0');

/*!40000 ALTER TABLE `sys_dict` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_document
# ------------------------------------------------------------

CREATE TABLE `sys_document` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `extension` varchar(16) DEFAULT NULL,
  `content_type` varchar(128) DEFAULT NULL,
  `size` int DEFAULT NULL,
  `group_name` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

LOCK TABLES `sys_document` WRITE;
/*!40000 ALTER TABLE `sys_document` DISABLE KEYS */;

INSERT INTO `sys_document` (`id`, `name`, `extension`, `content_type`, `size`, `group_name`, `path`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(858097155292835840,'点研营业执照new','png','image/png',2227837,'ckeditor','858097155238309888.png',1,'2024-08-22 19:44:42',1,'2024-11-28 11:16:22',b'0'),
	(858097295894294528,'点研营业执照new','png','image/png',2227837,'ckeditor','858097295885905920.png',1,'2024-08-22 19:45:16',1,'2024-11-28 11:16:22',b'0'),
	(858097296171118593,'note',NULL,'application/octet-stream',33,'ckeditor','858097296171118592',1,'2024-08-22 19:45:16',1,'2024-11-28 11:16:22',b'0'),
	(858142447786569728,'点研营业执照new','png','image/png',2227837,'ckeditor','858142447694295040.png',1,'2024-08-22 22:44:41',1,'2024-11-28 11:16:22',b'0'),
	(858143002550382592,'糖尿病肾病 试运行','docx','application/vnd.openxmlformats-officedocument.wordprocessingml.document',13535,'ckeditor','858143002420359168.docx',1,'2024-08-22 22:46:53',1,'2024-11-28 11:16:22',b'0'),
	(858143015586279424,'点研营业执照new','png','image/png',2227837,'ckeditor','858143015569502208.png',1,'2024-08-22 22:46:56',1,'2024-11-28 11:16:22',b'0'),
	(858143018480349184,'note',NULL,'application/octet-stream',33,'ckeditor','858143018471960576',1,'2024-08-22 22:46:57',1,'2024-11-28 11:16:22',b'0'),
	(858143021252784128,'投资任职情况查询报告','pdf','application/pdf',35345,'ckeditor','858143021236006912.pdf',1,'2024-08-22 22:46:58',1,'2024-11-28 11:16:22',b'0'),
	(858358554791251968,'图片','png','image/png',613008,'customize-group','858358554619285504.png',1,'2024-08-23 13:03:25',1,'2024-11-28 11:16:22',b'0'),
	(858359173056868352,'note',NULL,'application/octet-stream',33,'ckeditor','858359172964593664',1,'2024-08-23 13:05:52',1,'2024-11-28 11:16:22',b'0'),
	(858375630050299904,'点研营业执照new','png','image/png',2227837,'link','858375629681201152.png',1,'2024-08-23 14:11:16',1,'2024-11-28 11:16:22',b'0'),
	(858378161484730368,'mov_bbb','mp4','video/mp4',788493,'link','858378161467953152.mp4',1,'2024-08-23 14:21:19',1,'2024-11-28 11:16:22',b'0'),
	(858404071571238912,'mov_bbb','mp4','video/mp4',788493,'link','858404071520907264.mp4',1,'2024-08-23 16:04:17',1,'2024-11-28 11:16:22',b'0'),
	(893483237844717568,'1369-1','jpeg','image/jpeg',78402,'upload','893483237785997312.jpeg',1,'2024-11-28 11:16:22',1,'2024-11-28 11:16:22',b'0'),
	(893484203360915456,'1369-1','jpeg','image/jpeg',78402,'upload','893484203331555328.jpeg',1,'2024-11-28 11:20:12',1,'2024-11-28 11:20:12',b'0'),
	(893484216723968000,'865259019386826752','jpeg','image/jpeg',78402,'images','893484216702996480.jpeg',1,'2024-11-28 11:20:15',1,'2024-11-28 11:20:15',b'0'),
	(906928521023086592,'采购讨论3','svg','image/svg+xml',441964,'upload','906928520754651136.svg',1,'2025-01-04 13:43:07',1,'2025-01-04 13:43:07',b'0'),
	(906928534818152448,'1369-1','jpeg','image/jpeg',78402,'images','906928534776209408.jpeg',1,'2025-01-04 13:43:10',1,'2025-01-04 13:43:10',b'0');

/*!40000 ALTER TABLE `sys_document` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_form
# ------------------------------------------------------------

CREATE TABLE `sys_form` (
  `id` bigint NOT NULL,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `form_advice_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `table_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `repository_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `storage_strategy` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tpl_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `additional_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `sys_form` WRITE;
/*!40000 ALTER TABLE `sys_form` DISABLE KEYS */;

INSERT INTO `sys_form` (`id`, `code`, `name`, `form_advice_name`, `table_name`, `repository_name`, `storage_strategy`, `tpl_name`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694980924206493696,'sys_user','用户信息','userFormAdvice','sys_user','userDAO','CREATE_TABLE','tpl/form/form','{\"label-col\":1}',0,'2023-05-30 16:59:02',1,'2024-11-28 10:06:09',b'0'),
	(695312747063197696,'sys_dict','字典','dictFormService','sys_dict','dictDAO','CREATE_TABLE','tpl/form/form-full','{\"showSaveFormBtn\":false}',1,'2023-05-31 14:57:35',1,'2024-11-28 10:05:37',b'0'),
	(859875429241106432,'sys_user_form_tag','用户信息-tag','userFormAdvice','sys_user','userDAO','CREATE_TABLE','demos/student/form-tag','{\"label-col\":1}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0');

/*!40000 ALTER TABLE `sys_form` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_form_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_configurer` (
  `id` bigint NOT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `label` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `validators` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `options` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `default_value` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `placeholder` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_disabled` bit(1) DEFAULT FALSE,
  `cpn_value_converter_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `additional_info` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `sys_form_configurer` WRITE;
/*!40000 ALTER TABLE `sys_form_configurer` DISABLE KEYS */;

INSERT INTO `sys_form_configurer` (`id`, `name`, `label`, `type`, `validators`, `options`, `data_source`, `default_value`, `placeholder`, `is_disabled`, `cpn_value_converter_name`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(728895409610559488,'type','分类','SELECT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]','sys_dict_type',NULL,'请输入分类',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2024-11-28 10:05:37',b'0'),
	(728895409639919616,'name','编码','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入编码',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2024-11-28 10:05:37',b'0'),
	(728895409639919617,'label','显示值','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入显示值',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2024-11-28 10:05:37',b'0'),
	(728895409639919618,'sort','排序号','INTEGER_NUMBER','[]','[]',NULL,'0',NULL,NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2024-11-28 10:05:37',b'0'),
	(728895456838422528,'code','用户名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入用户名',NULL,NULL,'{\"tab-index\":\"1\"}',1,'2023-09-01 07:03:17',1,'2024-11-28 10:06:09',b'0'),
	(728895456842616832,'name','姓名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入姓名',NULL,NULL,'{\"tab-index\":\"1\"}',1,'2023-09-01 07:03:17',1,'2024-11-28 10:06:09',b'0'),
	(728895456842616833,'available','可用','SWITCH','[]','[]',NULL,'1',NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2023-09-01 07:03:17',1,'2024-11-28 10:06:09',b'0'),
	(728895456842616834,'roleIds','角色','CHECKBOX','[]','[]','sys_role',NULL,NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2023-09-01 07:03:17',1,'2024-11-28 10:06:09',b'0'),
	(893463693533122560,'birthday','出生日期','DATE','[]','[]',NULL,NULL,'请输入出生日期',NULL,NULL,'{\"tab-index\":\"1\"}',1,'2024-11-28 09:58:42',1,'2024-11-28 10:06:09',b'0'),
	(893463693533122561,'attachment','附件','FILE','[]','[]',NULL,NULL,NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2024-11-28 09:58:42',1,'2024-11-28 10:06:09',b'0'),
	(893484864852987904,'code','用户名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入用户名',NULL,NULL,'{\"tab-index\":\"1\"}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484864852987905,'name','姓名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]','[]',NULL,NULL,'请输入姓名',NULL,NULL,'{\"tab-index\":\"1\"}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484864852987906,'available','可用','SWITCH','[]','[]',NULL,'1',NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484864852987907,'roleIds','角色','MULTIPLE_SELECT','[]','[]','sys_role',NULL,NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484864852987908,'attachment','附件','FILE','[]','[]',NULL,NULL,NULL,NULL,NULL,'{\"tab-index\":\"2\"}',1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0');

/*!40000 ALTER TABLE `sys_form_configurer` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_form_cpn_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_cpn_configurer` (
  `id` bigint NOT NULL,
  `form_id` bigint NOT NULL,
  `config_id` bigint NOT NULL,
  `order_num` int DEFAULT NULL,
  `additional_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `sys_form_cpn_configurer` WRITE;
/*!40000 ALTER TABLE `sys_form_cpn_configurer` DISABLE KEYS */;

INSERT INTO `sys_form_cpn_configurer` (`id`, `form_id`, `config_id`, `order_num`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(728895412680790016,695312747063197696,728895409610559488,0,NULL,1,'2024-11-28 10:05:38',1,'2024-11-28 10:05:38',b'0'),
	(728895412680790017,695312747063197696,728895409639919616,1,NULL,1,'2024-11-28 10:05:38',1,'2024-11-28 10:05:38',b'0'),
	(728895412680790018,695312747063197696,728895409639919617,2,NULL,1,'2024-11-28 10:05:38',1,'2024-11-28 10:05:38',b'0'),
	(728895412680790019,695312747063197696,728895409639919618,3,NULL,1,'2024-11-28 10:05:38',1,'2024-11-28 10:05:38',b'0'),
	(728895458704887808,694980924206493696,728895456838422528,0,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(728895458704887809,694980924206493696,728895456842616832,1,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(728895458704887810,694980924206493696,728895456842616833,3,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(728895458704887811,694980924206493696,728895456842616834,4,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(893463693935775746,694980924206493696,893463693533122560,2,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(893463693935775749,694980924206493696,893463693533122561,5,NULL,1,'2024-11-28 10:06:10',1,'2024-11-28 10:06:10',b'0'),
	(893484865364692992,893484864089624576,893484864852987904,0,NULL,1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484865364692993,893484864089624576,893484864852987905,1,NULL,1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484865364692994,893484864089624576,893484864852987906,2,NULL,1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484865364692995,893484864089624576,893484864852987907,3,NULL,1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0'),
	(893484865368887296,893484864089624576,893484864852987908,4,NULL,1,'2024-11-28 11:22:50',1,'2024-11-28 11:22:50',b'0');

/*!40000 ALTER TABLE `sys_form_cpn_configurer` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_permission
# ------------------------------------------------------------

CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '外部可见，唯一code',
  `name` varchar(32) DEFAULT NULL COMMENT '权限名称',
  `pid` bigint DEFAULT NULL COMMENT '父权限',
  `permission_order` int DEFAULT NULL COMMENT 'permission_order',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='权限表';

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;

INSERT INTO `sys_permission` (`id`, `code`, `name`, `pid`, `permission_order`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694587393189089280,'dashboard','仪表盘',0,0,NULL,'2023-05-29 14:55:17',0,'2023-05-29 16:30:36',b'0'),
	(694587393189089281,'sys_management','系统管理',0,9,0,'2023-05-29 14:55:17',0,'2023-05-29 14:55:17',b'0'),
	(694587393189089283,'role_management','角色管理',694587393189089281,1,NULL,'2023-05-29 14:55:17',NULL,'2023-05-29 14:55:17',b'0'),
	(696145672415481856,'sys_user','用户管理',694587393189089281,0,1,'2023-06-02 22:07:20',1,'2023-06-02 22:07:20',b'0'),
	(696145672960741376,'sys_user_read','查看',696145672415481856,0,1,'2023-06-02 22:07:20',1,'2023-06-02 22:07:20',b'0'),
	(696145673485029376,'sys_user_add','新增',696145672415481856,1,1,'2023-06-02 22:07:20',1,'2023-06-02 22:07:20',b'0'),
	(696145674072231936,'sys_user_edit','编辑',696145672415481856,2,1,'2023-06-02 22:07:20',1,'2023-06-02 22:07:20',b'0'),
	(696145674634268672,'sys_user_delete','删除',696145672415481856,3,1,'2023-06-02 22:07:20',1,'2023-06-02 22:07:20',b'0'),
	(696146982695079936,'sys_dict','字典管理',694587393189089281,2,1,'2023-06-02 22:12:32',1,'2023-06-02 22:12:32',b'0'),
	(696146983320031232,'sys_dict_read','查看',696146982695079936,0,1,'2023-06-02 22:12:32',1,'2023-06-02 22:12:32',b'0'),
	(696146983924011008,'sys_dict_add','新增',696146982695079936,1,1,'2023-06-02 22:12:33',1,'2023-06-02 22:12:33',b'0'),
	(696146984569933824,'sys_dict_edit','编辑',696146982695079936,2,1,'2023-06-02 22:12:33',1,'2023-06-02 22:12:33',b'0'),
	(696146985190690816,'sys_dict_delete','删除',696146982695079936,3,1,'2023-06-02 22:12:33',1,'2023-06-02 22:12:33',b'0');

/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_property
# ------------------------------------------------------------

CREATE TABLE `sys_property` (
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



# Dump of table sys_report
# ------------------------------------------------------------

CREATE TABLE `sys_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tpl_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pageable` bit(1) DEFAULT NULL,
  `sidx` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sord` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `query_sql` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `summary` bit(1) DEFAULT b'0',
  `report_column_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `summary_column_names` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `query_field_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `additional_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `report_advice_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表';

LOCK TABLES `sys_report` WRITE;
/*!40000 ALTER TABLE `sys_report` DISABLE KEYS */;

INSERT INTO `sys_report` (`id`, `code`, `name`, `tpl_name`, `pageable`, `sidx`, `sord`, `query_sql`, `summary`, `report_column_list`, `summary_column_names`, `query_field_list`, `additional_info`, `report_advice_name`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694714180413960192,'sys_user','用户管理','tpl/list/list',b'1','id','ASC',' SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, IF(sys_user.is_available, \'是\', \'否\') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, \'%Y-%m-%d %H:%i:%s\') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"用户名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"birthday\",\"label\":\"出生日期\",\"sortable\":true,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"role_name\",\"label\":\"角色\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"sortable\":false,\"columnWidth\":80,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_name\",\"label\":\"创建人\",\"sortable\":false,\"columnWidth\":100,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"sortable\":false,\"columnWidth\":120,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"code\",\"label\":\"用户名\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"SELECT\",\"extraData\":\"bol\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"}]','{\"formId\":\"694980924206493696\",\"formAction\":\"link\"}','userReportAdvice',0,'2023-05-29 23:19:06',1,'2024-11-28 10:09:36',b'0'),
	(695316160014499840,'sys_dict','字典管理','tpl/list/list',b'0','id','ASC','select id, type, name, label, sort from sys_dict where type = :type and is_deleted = 0 order by type, sort asc',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"编码\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"label\",\"label\":\"显示值\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"type\",\"label\":\"分类\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"sort\",\"label\":\"排序号\",\"sortable\":false,\"columnWidth\":30,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"type\",\"label\":\"分类\",\"type\":\"SELECT\",\"extraData\":\"sys_dict_type\"}]','{\"formId\":\"695312747063197696\"}',NULL,1,'2023-05-31 15:11:09',1,'2024-11-28 09:58:59',b'0'),
	(786015805669142528,'sys_user_search','用户查询','tpl/list/dialog_report_list',b'1','id','ASC',' SELECT sys_user.id, sys_user.code, sys_user.name, IF(sys_user.is_available, \'是\', \'否\') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, \'%Y-%m-%d %H:%i:%s\') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0 and sys_user.id = :id and sys_user.id IN (:ids)',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"用户名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"role_name\",\"label\":\"角色\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"sortable\":false,\"columnWidth\":80,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_name\",\"label\":\"创建人\",\"sortable\":false,\"columnWidth\":100,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"sortable\":false,\"columnWidth\":180,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"code\",\"label\":\"用户名\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"SELECT\",\"extraData\":\"bol\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"}]',NULL,NULL,1,'2024-04-06 21:32:07',1,'2024-04-06 21:32:07',b'0'),
	(858372486293622784,'sys_document','图片管理','modules/link/list',b'1','create_time','DESC','select id, name, concat(\'http://localhost:7892/\', group_name, \'/\', path) url, extension, content_type, ROUND(size / (1024 * 1024), 1) size, create_time, \'\' copy from sys_document where name like :name and group_name = \'link\'',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"isImageType\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"url\",\"label\":\"文件\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"名称\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"content_type\",\"label\":\"类型\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"size\",\"label\":\"大小（M）\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"valueConverterNameList\":[\"localDateTimeConverter\"],\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"copy\",\"label\":\"复制\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"name\",\"label\":\"名称\",\"type\":\"TEXT\"}]',NULL,'linkReportAdvice',1,'2024-08-23 13:58:46',1,'2024-08-23 14:59:35',b'0'),
	(858937583114170368,'t_student','学生表','tpl/list/ajax_list',b'1','createTime','DESC','SELECT t_student.name AS \"name\",t_student.gender AS \"gender\",t_student.email AS \"email\",t_student.birthday AS \"birthday\",t_student.age AS \"age\",t_student.is_marriage AS \"marriage\",t_student.unit_code AS \"unit.code\",t_student.attachments AS \"attachments\",t_student.avatar AS \"avatar\",t_student.hobby_list AS \"hobbyList\",t_student.material_type AS \"materialTypeList\",t_student.category AS \"category\",t_student.is_available AS \"available\",t_student.remark AS \"remark\",t_student.code AS \"code\",t_student.create_by AS \"createBy\",t_student.create_time AS \"createTime\",t_student.update_by AS \"updateBy\",t_student.update_time AS \"updateTime\",t_student.is_deleted AS \"deleted\",t_student.id AS \"id\" FROM t_student WHERE name = :name AND gender = :gender AND email = :email AND birthday = :birthday AND age = :age AND is_marriage = :is_marriage AND is_marriage = :marriage AND unit_code = :unit_code AND unit_code = :unitCode AND attachments = :attachments AND avatar = :avatar AND hobby_list = :hobby_list AND hobby_list = :hobbyList AND material_type = :material_type AND material_type = :materialTypeList AND category = :category AND is_available = :is_available AND is_available = :available AND remark = :remark AND code = :code AND create_by = :create_by AND create_by = :createBy AND create_time = :create_time AND create_time = :createTime AND update_by = :update_by AND update_by = :updateBy AND update_time = :update_time AND update_time = :updateTime AND is_deleted = 0 AND is_deleted = :deleted AND id = :id',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"外部可见，唯一code\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"gender\",\"label\":\"gender\",\"valueConverterNameList\":[\"dictConverter\"],\"context\":\"GenderEnum\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"email\",\"label\":\"email\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"birthday\",\"label\":\"出生日期\",\"sortable\":true,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"date\"},{\"name\":\"age\",\"label\":\"年龄\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"marriage\",\"label\":\"婚否\",\"valueConverterNameList\":[\"boolConverter\"],\"sortable\":false,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"unit.code\",\"label\":\"unit.code\",\"valueConverterNameList\":[\"dictConverter\"],\"context\":\"UNIT\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"attachments\",\"label\":\"attachments\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"avatar\",\"label\":\"avatar\",\"sortable\":false,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"hobbyList\",\"label\":\"hobbyList\",\"valueConverterNameList\":[\"arrayDictConverter\"],\"context\":\"HobbyEnum\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"materialTypeList\",\"label\":\"物料类型\",\"valueConverterNameList\":[\"arrayDictConverter\"],\"context\":\"MATERIAL\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"category\",\"label\":\"分类\",\"valueConverterNameList\":[\"dictConverter\"],\"context\":\"CategoryEnum\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"available\",\"label\":\"是否可用\",\"valueConverterNameList\":[\"boolConverter\"],\"sortable\":false,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"remark\",\"label\":\"简介\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"createBy\",\"label\":\"创建人\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"createTime\",\"label\":\"创建时间\",\"valueConverterNameList\":[\"localDateTimeConverter\"],\"sortable\":false,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"datetime\"},{\"name\":\"updateBy\",\"label\":\"更新人\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"updateTime\",\"label\":\"更新时间\",\"valueConverterNameList\":[\"localDateTimeConverter\"],\"sortable\":false,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"datetime\"}]',NULL,'[{\"name\":\"code\",\"label\":\"外部可见，唯一code\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"gender\",\"label\":\"gender\",\"type\":\"SELECT\",\"extraData\":\"GenderEnum\"},{\"name\":\"email\",\"label\":\"email\",\"type\":\"TEXT\"},{\"name\":\"birthday\",\"label\":\"出生日期\",\"type\":\"DATE\"},{\"name\":\"age\",\"label\":\"年龄\",\"type\":\"TEXT\"},{\"name\":\"is_marriage\",\"label\":\"婚否\",\"type\":\"CHECKBOX\"},{\"name\":\"unit_code\",\"label\":\"unit.code\",\"type\":\"MULTIPLE_SELECT\",\"extraData\":\"UNIT\"},{\"name\":\"attachments\",\"label\":\"attachments\",\"type\":\"TEXT\"},{\"name\":\"avatar\",\"label\":\"avatar\",\"type\":\"TEXT\"},{\"name\":\"hobby_list\",\"label\":\"hobbyList\",\"type\":\"SELECT\",\"extraData\":\"HobbyEnum\"},{\"name\":\"material_type\",\"label\":\"物料类型\",\"type\":\"MULTIPLE_SELECT\",\"extraData\":\"MATERIAL\"},{\"name\":\"category\",\"label\":\"分类\",\"type\":\"SELECT\",\"extraData\":\"CategoryEnum\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"CHECKBOX\"},{\"name\":\"remark\",\"label\":\"简介\",\"type\":\"TEXT\"},{\"name\":\"create_by\",\"label\":\"创建人\",\"type\":\"TEXT\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"},{\"name\":\"update_by\",\"label\":\"更新人\",\"type\":\"TEXT\"},{\"name\":\"update_time\",\"label\":\"更新时间\",\"type\":\"DATE_RANGE\"}]','{\"operator-bar\":true,\"endpoint\":\"students\"}','studentReportAdvice',1,'2024-08-23 16:38:36',1,'2024-11-28 10:58:09',b'0'),
	(859875793323470848,'sys_user_form_tag','用户管理','tpl/list/list',b'1','id','ASC',' SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, IF(sys_user.is_available, \'是\', \'否\') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, \'%Y-%m-%d %H:%i:%s\') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"用户名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"birthday\",\"label\":\"出生日期\",\"sortable\":true,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"date\"},{\"name\":\"role_name\",\"label\":\"角色\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"sortable\":false,\"columnWidth\":80,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_name\",\"label\":\"创建人\",\"sortable\":false,\"columnWidth\":100,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"sortable\":false,\"columnWidth\":120,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"code\",\"label\":\"用户名\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"SELECT\",\"extraData\":\"bol\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"}]','{\"formId\":\"859875429241106432\",\"operator-bar\":true,\"formAction\":\"link\"}','userReportAdvice',1,'2024-11-28 11:22:25',1,'2024-11-28 11:22:25',b'0');

/*!40000 ALTER TABLE `sys_report` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_role
# ------------------------------------------------------------

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '外部可见，唯一code',
  `name` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='角色';

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;

INSERT INTO `sys_role` (`id`, `code`, `name`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694587732420202496,'admin','管理员',1,'2023-05-29 14:56:38',1,'2023-12-11 17:01:34',b'0');

/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_role_permission
# ------------------------------------------------------------

CREATE TABLE `sys_role_permission` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `sys_role_permission_pk` (`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `is_deleted`)
VALUES
	(694587732420202496,694587393189089281,b'0'),
	(694587732420202496,694587393189089283,b'0'),
	(694587732420202496,696145672415481856,b'0'),
	(694587732420202496,696145672960741376,b'0'),
	(694587732420202496,696145673485029376,b'0'),
	(694587732420202496,696145674072231936,b'0'),
	(694587732420202496,696145674634268672,b'0'),
	(694587732420202496,696146982695079936,b'0'),
	(694587732420202496,696146983320031232,b'0'),
	(694587732420202496,696146983924011008,b'0'),
	(694587732420202496,696146984569933824,b'0'),
	(694587732420202496,696146985190690816,b'0');

/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_user
# ------------------------------------------------------------

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '外部可见，唯一code',
  `name` varchar(32) DEFAULT NULL COMMENT '姓名',
  `password` varchar(128) DEFAULT NULL COMMENT '密码',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `is_available` bit(1) DEFAULT NULL COMMENT '是否可用',
  `attachment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '附件',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户信息';

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;

INSERT INTO `sys_user` (`id`, `code`, `name`, `password`, `birthday`, `is_available`, `attachment`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(1,'admin','管理员','$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS','2024-11-28',b'1','[]',1,'2023-05-29 14:55:03',1,'2024-12-10 19:37:01',b'0'),
	(786079661661646848,'test','测试','$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS',NULL,b'1','[]',1,'2023-05-29 14:55:03',1,'2024-11-28 10:11:01',b'0');

/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_user_role
# ------------------------------------------------------------

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `sys_user_role_pk` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;

INSERT INTO `sys_user_role` (`user_id`, `role_id`, `is_deleted`)
VALUES
	(1,694587732420202496,b'0');

/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_complex_model
# ------------------------------------------------------------

CREATE TABLE `t_complex_model` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `material_type_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `unit_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `category` enum('MATERIAL','PURCHASING_ORG','PACKAGING','SALES_ORG') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `work_status` int DEFAULT NULL COMMENT '状态',
  `category_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `age` int NOT NULL COMMENT '年龄',
  `birthday` date DEFAULT NULL COMMENT '出生时间',
  `category_list` json NOT NULL COMMENT '分类',
  `category_dict_list` json NOT NULL COMMENT '字典分类',
  `marriage` bit(1) NOT NULL COMMENT '婚否',
  `attachment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `school_experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `map` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `embedded_value` json DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试';

LOCK TABLES `t_complex_model` WRITE;
/*!40000 ALTER TABLE `t_complex_model` DISABLE KEYS */;

INSERT INTO `t_complex_model` (`id`, `name`, `material_type_code`, `unit_code`, `category`, `work_status`, `category_code`, `age`, `birthday`, `category_list`, `category_dict_list`, `marriage`, `attachment`, `school_experience`, `map`, `embedded_value`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(856958035153653760,'Rick.Xu','HIBE','EA','MATERIAL',0,'SALES_ORG',34,'2021-12-26','[\"MATERIAL\", \"PURCHASING_ORG\"]','[{\"code\": \"MATERIAL\"}, {\"code\": \"PURCHASING_ORG\"}]',b'1','[{\"name\":\"picture\",\"url\":\"baidu.com\"}]','[[\"2023-11-11\",\"苏州大学\"],[\"2019-11-11\",\"苏州中学\"]]','{\"name\":\"picture\",\"url\":\"baidu.com\"}','{\"text\": \"text\", \"dictValue\": {\"code\": \"HIBE\", \"label\": \"耗材用品\"}}',1,'2024-08-19 16:18:15',1,'2024-08-19 16:18:15',b'0'),
	(856958362212896768,'Rick.Xu','HIBE','EA','MATERIAL',0,'SALES_ORG',34,'2021-12-26','[\"MATERIAL\", \"PURCHASING_ORG\"]','[{\"code\": \"MATERIAL\"}, {\"code\": \"PURCHASING_ORG\"}]',b'1','[{\"name\":\"picture\",\"url\":\"baidu.com\"}]','[[\"2023-11-11\",\"苏州大学\"],[\"2019-11-11\",\"苏州中学\"]]','{\"name\":\"picture\",\"url\":\"baidu.com\"}','{\"text\": \"text\", \"dictValue\": {\"code\": \"HIBE\", \"label\": \"耗材用品\"}}',1,'2024-08-19 16:19:33',1,'2024-08-19 16:19:33',b'0'),
	(858263799545769984,'Rick.Xu','HIBE','EA','MATERIAL',0,'SALES_ORG',34,'2021-12-26','[\"MATERIAL\", \"PURCHASING_ORG\"]','[{\"code\": \"MATERIAL\"}, {\"code\": \"PURCHASING_ORG\"}]',b'1','[{\"name\":\"picture\",\"url\":\"baidu.com\"}]','[[\"2023-11-11\",\"苏州大学\"],[\"2019-11-11\",\"苏州中学\"]]','{\"name\":\"picture\",\"url\":\"baidu.com\"}','{\"text\": \"text\", \"dictValue\": {\"code\": \"HIBE\", \"label\": \"耗材用品\"}}',1,'2024-08-23 06:46:53',1,'2024-08-23 06:46:53',b'0'),
	(858272781039611904,'Rick.Xu','HIBE','EA','MATERIAL',0,'SALES_ORG',34,'2021-12-26','[\"MATERIAL\", \"PURCHASING_ORG\"]','[{\"code\": \"MATERIAL\"}, {\"code\": \"PURCHASING_ORG\"}]',b'1','[{\"name\":\"picture\",\"url\":\"baidu.com\"}]','[[\"2023-11-11\",\"苏州大学\"],[\"2019-11-11\",\"苏州中学\"]]','{\"name\":\"picture\",\"url\":\"baidu.com\"}','{\"text\": \"text\", \"dictValue\": {\"code\": \"HIBE\", \"label\": \"耗材用品\"}}',1,'2024-08-23 07:22:35',1,'2024-08-23 07:22:35',b'0'),
	(858305555469479936,'Rick.Xu','HIBE','EA','MATERIAL',0,'SALES_ORG',34,'2021-12-26','[\"MATERIAL\", \"PURCHASING_ORG\"]','[{\"code\": \"MATERIAL\"}, {\"code\": \"PURCHASING_ORG\"}]',b'1','[{\"name\":\"picture\",\"url\":\"baidu.com\"}]','[[\"2023-11-11\",\"苏州大学\"],[\"2019-11-11\",\"苏州中学\"]]','{\"name\":\"picture\",\"url\":\"baidu.com\"}','{\"text\": \"text\", \"dictValue\": {\"code\": \"HIBE\", \"label\": \"耗材用品\"}}',1,'2024-08-23 09:32:49',1,'2024-08-23 09:32:49',b'0');

/*!40000 ALTER TABLE `t_complex_model` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_student
# ------------------------------------------------------------

CREATE TABLE `t_student` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '外部可见，唯一code',
  `name` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓名',
  `gender` enum('M','F') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `age` int DEFAULT NULL COMMENT '年龄',
  `is_marriage` bit(1) DEFAULT b'0' COMMENT '婚否',
  `unit_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `attachments` json DEFAULT NULL,
  `avatar` json DEFAULT NULL,
  `hobby_list` json DEFAULT NULL,
  `material_type` json DEFAULT NULL COMMENT '物料类型',
  `category` enum('MATERIAL','PURCHASING_ORG','PACKAGING','SALES_ORG') COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类',
  `is_available` bit(1) DEFAULT b'0' COMMENT '是否可用',
  `remark` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '简介',
  `user_id` bigint DEFAULT NULL,
  `user_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT b'0' COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生表';

LOCK TABLES `t_student` WRITE;
/*!40000 ALTER TABLE `t_student` DISABLE KEYS */;

INSERT INTO `t_student` (`id`, `code`, `name`, `gender`, `email`, `birthday`, `age`, `is_marriage`, `unit_code`, `attachments`, `avatar`, `hobby_list`, `material_type`, `category`, `is_available`, `remark`, `user_id`, `user_code`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(2,'0001','张三','M','fsadfsaf@163.com','1992-11-12',19,b'1','EA','[{\"id\": \"861948954584059904\", \"url\": \"http://localhost:7892/attachments/861948954575671296.xls\", \"name\": \"报关单\", \"path\": \"861948954575671296.xls\", \"size\": 65536, \"fullName\": \"报关单.xls\", \"fullPath\": \"attachments/861948954575671296.xls\", \"extension\": \"xls\", \"groupName\": \"attachments\", \"contentType\": \"application/vnd.ms-excel\"}, {\"id\": \"861948954584059905\", \"url\": \"http://localhost:7892/attachments/861948954575671297.zip\", \"name\": \"鱼皮 - Java 学习路线一条龙版本 V2.mindnode\", \"path\": \"861948954575671297.zip\", \"size\": 408174, \"fullName\": \"鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip\", \"fullPath\": \"attachments/861948954575671297.zip\", \"extension\": \"zip\", \"groupName\": \"attachments\", \"contentType\": \"application/zip\"}]','{\"id\": \"861952755730780160\", \"url\": \"http://localhost:7892/images/861952755722391552.jpeg\", \"name\": \"avatar\", \"path\": \"861952755722391552.jpeg\", \"size\": 68783, \"fullName\": \"avatar.jpeg\", \"fullPath\": \"images/861952755722391552.jpeg\", \"extension\": \"jpeg\", \"groupName\": \"images\", \"contentType\": \"image/jpeg\"}','[\"FOOTBALL\", \"BASKETBALL\"]','[{\"code\": \"M1\"}]','MATERIAL',b'0','fsdfdasf',NULL,NULL,1,'2024-08-24 22:57:24',1,'2025-01-04 13:43:21',b'0'),
	(861949024788320256,'00012','库房','M','1050216579@qq.com','2024-08-28',2,b'1','EA','[{\"id\": \"861948954584059904\", \"url\": \"http://localhost:7892/attachments/861948954575671296.xls\", \"name\": \"报关单\", \"path\": \"861948954575671296.xls\", \"size\": 65536, \"fullName\": \"报关单.xls\", \"fullPath\": \"attachments/861948954575671296.xls\", \"extension\": \"xls\", \"groupName\": \"attachments\", \"contentType\": \"application/vnd.ms-excel\"}, {\"id\": \"861948954584059905\", \"url\": \"http://localhost:7892/attachments/861948954575671297.zip\", \"name\": \"鱼皮 - Java 学习路线一条龙版本 V2.mindnode\", \"path\": \"861948954575671297.zip\", \"size\": 408174, \"fullName\": \"鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip\", \"fullPath\": \"attachments/861948954575671297.zip\", \"extension\": \"zip\", \"groupName\": \"attachments\", \"contentType\": \"application/zip\"}]','{\"id\": \"861952755730780160\", \"url\": \"http://localhost:7892/images/861952755722391552.jpeg\", \"name\": \"avatar\", \"path\": \"861952755722391552.jpeg\", \"size\": 68783, \"fullName\": \"avatar.jpeg\", \"fullPath\": \"images/861952755722391552.jpeg\", \"extension\": \"jpeg\", \"groupName\": \"images\", \"contentType\": \"image/jpeg\"}','[\"BASKETBALL\", \"FOOTBALL\"]','[{\"code\": \"M1\"}, {\"code\": \"M4\"}]','PURCHASING_ORG',b'1','这里是简介',786079661661646848,'test',1,'2024-09-02 10:50:40',1,'2025-01-11 13:02:19',b'0'),
	(861953021922283520,'111','李岁2','F','fsadfsaf@163.com','2024-09-02',23,b'0','KG','[{\"id\": \"861952982621655040\", \"url\": \"http://localhost:7892/attachments/861952982609072128.jpeg\", \"name\": \"avatar\", \"path\": \"861952982609072128.jpeg\", \"size\": 68783, \"fullName\": \"avatar.jpeg\", \"fullPath\": \"attachments/861952982609072128.jpeg\", \"extension\": \"jpeg\", \"groupName\": \"attachments\", \"contentType\": \"image/jpeg\"}, {\"id\": \"861952982625849344\", \"url\": \"http://localhost:7892/attachments/861952982613266432.csv\", \"name\": \"Google Passwords\", \"path\": \"861952982613266432.csv\", \"size\": 13191, \"fullName\": \"Google Passwords.csv\", \"fullPath\": \"attachments/861952982613266432.csv\", \"extension\": \"csv\", \"groupName\": \"attachments\", \"contentType\": \"text/csv\"}, {\"id\": \"861967956609896448\", \"url\": \"http://localhost:7892/attachments/861967956597313536.pdf\", \"name\": \"线上VI指南\", \"path\": \"861967956597313536.pdf\", \"size\": 463981, \"fullName\": \"线上VI指南.pdf\", \"fullPath\": \"attachments/861967956597313536.pdf\", \"extension\": \"pdf\", \"groupName\": \"attachments\", \"contentType\": \"application/pdf\"}]','{\"id\": \"861976822831681536\", \"url\": \"http://localhost:7892/images/861976822642937856.webp\", \"name\": \"F5V8WS3asAAFJsC\", \"path\": \"861976822642937856.webp\", \"size\": 81884, \"fullName\": \"F5V8WS3asAAFJsC.webp\", \"fullPath\": \"images/861976822642937856.webp\", \"extension\": \"webp\", \"groupName\": \"images\", \"contentType\": \"image/webp\"}','[\"BASKETBALL\", \"FOOTBALL\"]','[{\"code\": \"M1\"}, {\"code\": \"M3\"}, {\"code\": \"M4\"}]','SALES_ORG',b'0','hello world',1,'admin',1,'2024-09-02 11:06:33',1,'2025-01-06 17:52:58',b'0'),
	(906928548739047424,'00035','TEST','M','10502165791@qq.com','2025-01-01',90,b'1','EA','[{\"id\": \"906928521023086592\", \"url\": \"http://localhost:7892/upload/906928520754651136.svg\", \"name\": \"采购讨论3\", \"path\": \"906928520754651136.svg\", \"size\": 441964, \"fullName\": \"采购讨论3.svg\", \"fullPath\": \"upload/906928520754651136.svg\", \"extension\": \"svg\", \"groupName\": \"upload\", \"contentType\": \"image/svg+xml\"}]','{\"id\": \"906928534818152448\", \"url\": \"http://localhost:7892/images/906928534776209408.jpeg\", \"name\": \"1369-1\", \"path\": \"906928534776209408.jpeg\", \"size\": 78402, \"fullName\": \"1369-1.jpeg\", \"fullPath\": \"images/906928534776209408.jpeg\", \"extension\": \"jpeg\", \"groupName\": \"images\", \"contentType\": \"image/jpeg\"}','[\"FOOTBALL\"]','[{\"code\": \"M1\"}]','MATERIAL',b'1','00',1,'admin',1,'2025-01-04 13:43:14',1,'2025-01-11 13:02:58',b'0');

/*!40000 ALTER TABLE `t_student` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
