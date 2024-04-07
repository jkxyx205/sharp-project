# ************************************************************
# Sequel Ace SQL dump
# Version 20046
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# Host: localhost (MySQL 8.3.0)
# Database: sharp-admin
# Generation Time: 2024-04-07 02:38:08 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table sys_dict
# ------------------------------------------------------------

CREATE TABLE `sys_dict` (
  `id` bigint DEFAULT NULL,
  `type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `label` varchar(32) NOT NULL,
  `sort` int DEFAULT NULL,
  `remark` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`type`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;

INSERT INTO `sys_dict` (`id`, `type`, `name`, `label`, `sort`, `remark`)
VALUES
	(695367653333712896,'MATERIAL_TYPE','HIBE','耗材用品',0,'30000'),
	(1,'UNIT','EA','个',0,NULL);

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
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;



# Dump of table sys_form
# ------------------------------------------------------------

CREATE TABLE `sys_form` (
  `id` bigint NOT NULL,
  `code` varchar(32) DEFAULT NULL,
  `name` varchar(32) NOT NULL,
  `form_advice_name` varchar(32) DEFAULT NULL,
  `table_name` varchar(32) DEFAULT NULL,
  `repository_name` varchar(32) DEFAULT NULL,
  `storage_strategy` varchar(16) DEFAULT NULL,
  `tpl_name` varchar(32) DEFAULT NULL,
  `additional_info` text,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `sys_form` WRITE;
/*!40000 ALTER TABLE `sys_form` DISABLE KEYS */;

INSERT INTO `sys_form` (`id`, `code`, `name`, `form_advice_name`, `table_name`, `repository_name`, `storage_strategy`, `tpl_name`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694980924206493696,'sys_user','人员信息表','userFormAdvice','sys_user','userDAO','CREATE_TABLE','tpl/form',NULL,0,'2023-05-30 16:59:02',1,'2023-09-01 07:03:17',b'0'),
	(695312747063197696,'sys_dict','字典表','dictFormService','sys_dict','dictDAO','CREATE_TABLE','tpl/form',NULL,1,'2023-05-31 14:57:35',1,'2023-09-01 07:03:06',b'0');

/*!40000 ALTER TABLE `sys_form` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_form_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_configurer` (
  `id` bigint NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `label` varchar(16) NOT NULL,
  `type` varchar(32) NOT NULL,
  `validators` text,
  `options` varchar(5000) DEFAULT NULL,
  `data_source` varchar(32) DEFAULT NULL,
  `default_value` varchar(64) DEFAULT NULL,
  `placeholder` varchar(32) DEFAULT NULL,
  `is_disabled` bit(1) DEFAULT NULL,
  `cpn_value_converter_name` varchar(32) DEFAULT NULL,
  `additional_info` varchar(1024) DEFAULT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `sys_form_configurer` WRITE;
/*!40000 ALTER TABLE `sys_form_configurer` DISABLE KEYS */;

INSERT INTO `sys_form_configurer` (`id`, `name`, `label`, `type`, `validators`, `options`, `data_source`, `default_value`, `placeholder`, `is_disabled`, `cpn_value_converter_name`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(728895409610559488,'type','分类','SELECT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]',NULL,'sys_dict_type',NULL,'请输入分类',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895409639919616,'name','编码','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]',NULL,NULL,NULL,'请输入编码',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895409639919617,'label','显示值','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]',NULL,NULL,NULL,'请输入显示值',NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895409639919618,'sort','排序号','INTEGER_NUMBER','[]',NULL,NULL,'0',NULL,NULL,NULL,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895456838422528,'code','用户名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"regex\":\"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$\",\"message\":\"CODE只能包含数字、字母、下划线、中划线\",\"validatorType\":\"REGEX\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]',NULL,NULL,NULL,'请输入用户名',NULL,NULL,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895456842616832,'name','姓名','TEXT','[{\"required\":true,\"validatorType\":\"REQUIRED\",\"message\":\"必填项需要填写\"},{\"min\":0,\"max\":16,\"validatorType\":\"LENGTH\",\"message\":\"长度范围 0 - 16 个字符\"}]',NULL,NULL,NULL,'请输入姓名',NULL,NULL,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895456842616833,'available','可用','SWITCH','[]',NULL,NULL,'1',NULL,NULL,NULL,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895456842616834,'roleIds','角色','CHECKBOX','[]',NULL,'sys_role',NULL,NULL,NULL,NULL,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0');

/*!40000 ALTER TABLE `sys_form_configurer` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_form_cpn_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_cpn_configurer` (
  `id` bigint NOT NULL,
  `form_id` bigint NOT NULL,
  `config_id` bigint NOT NULL,
  `order_num` int DEFAULT NULL,
  `additional_info` text,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `sys_form_cpn_configurer` WRITE;
/*!40000 ALTER TABLE `sys_form_cpn_configurer` DISABLE KEYS */;

INSERT INTO `sys_form_cpn_configurer` (`id`, `form_id`, `config_id`, `order_num`, `additional_info`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(728895412680790016,695312747063197696,728895409610559488,0,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895412680790017,695312747063197696,728895409639919616,1,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895412680790018,695312747063197696,728895409639919617,2,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895412680790019,695312747063197696,728895409639919618,3,NULL,1,'2023-09-01 07:03:06',1,'2023-09-01 07:03:06',b'0'),
	(728895458704887808,694980924206493696,728895456838422528,0,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895458704887809,694980924206493696,728895456842616832,1,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895458704887810,694980924206493696,728895456842616833,2,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0'),
	(728895458704887811,694980924206493696,728895456842616834,3,NULL,1,'2023-09-01 07:03:17',1,'2023-09-01 07:03:17',b'0');

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
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_report
# ------------------------------------------------------------

CREATE TABLE `sys_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `tpl_name` varchar(32) DEFAULT NULL,
  `pageable` bit(1) DEFAULT NULL,
  `sidx` varchar(32) DEFAULT NULL,
  `sord` varchar(16) DEFAULT NULL,
  `query_sql` text,
  `summary` bit(1) DEFAULT b'0',
  `report_column_list` text,
  `summary_column_names` text,
  `query_field_list` text,
  `additional_info` text,
  `report_advice_name` varchar(64) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表';

LOCK TABLES `sys_report` WRITE;
/*!40000 ALTER TABLE `sys_report` DISABLE KEYS */;

INSERT INTO `sys_report` (`id`, `code`, `name`, `tpl_name`, `pageable`, `sidx`, `sord`, `query_sql`, `summary`, `report_column_list`, `summary_column_names`, `query_field_list`, `additional_info`, `report_advice_name`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(694714180413960192,'sys_user','用户管理','tpl/list',b'1','id','ASC',' SELECT sys_user.id, sys_user.code, sys_user.name, IF(sys_user.is_available, \'是\', \'否\') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, \'%Y-%m-%d %H:%i:%s\') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"用户名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"role_name\",\"label\":\"角色\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"sortable\":false,\"columnWidth\":80,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_name\",\"label\":\"创建人\",\"sortable\":false,\"columnWidth\":100,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"sortable\":false,\"columnWidth\":180,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"code\",\"label\":\"用户名\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"SELECT\",\"extraData\":\"bol\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"}]','{\"formId\":\"694980924206493696\"}',NULL,0,'2023-05-29 23:19:06',1,'2023-09-01 07:03:18',b'0'),
	(695316160014499840,'sys_dict','字典管理','tpl/list',b'0','id','ASC','select id, type, name, label, sort from sys_dict where type = :type order by type, sort asc',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"编码\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"label\",\"label\":\"显示值\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"type\",\"label\":\"分类\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"sort\",\"label\":\"排序号\",\"sortable\":false,\"columnWidth\":30,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"type\",\"label\":\"分类\",\"type\":\"SELECT\",\"extraData\":\"sys_dict_type\"}]','{\"formId\":\"695312747063197696\"}',NULL,1,'2023-05-31 15:11:09',1,'2023-09-01 07:03:07',b'0'),
	(786015805669142528,'sys_user_search','用户查询','tpl/dialog_report_list',b'1','id','ASC',' SELECT sys_user.id, sys_user.code, sys_user.name, IF(sys_user.is_available, \'是\', \'否\') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, \'%Y-%m-%d %H:%i:%s\') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0 and sys_user.id = :id and sys_user.id IN (:ids)',b'0','[{\"name\":\"id\",\"sortable\":false,\"hidden\":true,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"code\",\"label\":\"用户名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"name\",\"label\":\"姓名\",\"sortable\":true,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"role_name\",\"label\":\"角色\",\"sortable\":false,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"sortable\":false,\"columnWidth\":80,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_name\",\"label\":\"创建人\",\"sortable\":false,\"columnWidth\":100,\"hidden\":false,\"align\":\"left\",\"tooltip\":false,\"type\":\"text\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"sortable\":false,\"columnWidth\":180,\"hidden\":false,\"align\":\"center\",\"tooltip\":false,\"type\":\"text\"}]',NULL,'[{\"name\":\"code\",\"label\":\"用户名\",\"type\":\"TEXT\"},{\"name\":\"name\",\"label\":\"姓名\",\"type\":\"TEXT\"},{\"name\":\"is_available\",\"label\":\"是否可用\",\"type\":\"SELECT\",\"extraData\":\"bol\"},{\"name\":\"create_time\",\"label\":\"创建时间\",\"type\":\"DATE_RANGE\"}]',NULL,NULL,1,'2024-04-06 21:32:07',1,'2024-04-06 21:32:07',b'0');

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
  `is_available` bit(1) DEFAULT NULL COMMENT '是否可用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户信息';

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;

INSERT INTO `sys_user` (`id`, `code`, `name`, `password`, `is_available`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
VALUES
	(1,'admin','管理员','$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS',b'1',1,'2023-05-29 14:55:03',1,'2023-09-03 04:30:42',b'0'),
	(786079661661646848,'test','测试','$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS',b'1',1,'2023-05-29 14:55:03',1,'2023-09-03 04:30:42',b'0');

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



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
