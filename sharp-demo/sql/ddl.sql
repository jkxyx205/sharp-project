# ************************************************************
# Sequel Ace SQL dump
# Version 20046
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# Host: localhost (MySQL 8.0.32)
# Database: sharp-demo
# Generation Time: 2024-02-01 19:47:58 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table cat
# ------------------------------------------------------------

CREATE TABLE `cat` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `age` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='猫';



# Dump of table cat_reward
# ------------------------------------------------------------

CREATE TABLE `cat_reward` (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(32) DEFAULT NULL,
  `cat_id` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='猫的证书';



# Dump of table dog
# ------------------------------------------------------------

CREATE TABLE `dog` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `age` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table mm_serial_number
# ------------------------------------------------------------

CREATE TABLE `mm_serial_number` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '外部可见，唯一code',
  `status` varchar(16) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='序列号';



# Dump of table plant_storage_location
# ------------------------------------------------------------

CREATE TABLE `plant_storage_location` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '外部可见，唯一code',
  `description` varchar(32) DEFAULT NULL,
  `plant_id` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库位';



# Dump of table sys_dict
# ------------------------------------------------------------

CREATE TABLE `sys_dict` (
  `type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `label` varchar(32) NOT NULL,
  `sort` int DEFAULT NULL,
  PRIMARY KEY (`type`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_document
# ------------------------------------------------------------

CREATE TABLE `sys_document` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `extension` varchar(16) DEFAULT NULL,
  `content_type` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
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
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `tpl_name` varchar(32) DEFAULT NULL,
  `additional_info` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_form_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_configurer` (
  `id` bigint NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `label` varchar(16) NOT NULL,
  `type` varchar(32) NOT NULL,
  `validators` text,
  `options` varchar(1024) DEFAULT NULL,
  `default_value` varchar(64) DEFAULT NULL,
  `placeholder` varchar(32) DEFAULT NULL,
  `additional_info` varchar(1024) DEFAULT NULL,
  `cpn_value_converter_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `data_source` varchar(32) DEFAULT NULL,
  `is_disabled` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_form_cpn_configurer
# ------------------------------------------------------------

CREATE TABLE `sys_form_cpn_configurer` (
  `id` bigint NOT NULL,
  `form_id` bigint NOT NULL,
  `config_id` bigint NOT NULL,
  `order_num` int DEFAULT NULL,
  `additional_info` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_form_cpn_value
# ------------------------------------------------------------

CREATE TABLE `sys_form_cpn_value` (
  `id` bigint NOT NULL,
  `value` text,
  `form_cpn_id` bigint NOT NULL,
  `form_id` bigint NOT NULL,
  `config_id` bigint NOT NULL,
  `instance_id` bigint NOT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table sys_form_instance_first
# ------------------------------------------------------------

CREATE TABLE `sys_form_instance_first` (
  `id` bigint NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `hobby` varchar(32) DEFAULT NULL,
  `hobby2` varchar(32) DEFAULT NULL,
  `agree` varchar(32) DEFAULT NULL,
  `remark` varchar(32) DEFAULT NULL,
  `sex` varchar(32) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `file` varchar(4369) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `info` json DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息';



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
  `pageable` bit(1) DEFAULT NULL,
  `sidx` varchar(32) DEFAULT NULL,
  `sord` varchar(16) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `query_sql` text NOT NULL,
  `summary_column_names` varchar(128) DEFAULT NULL,
  `report_column_list` text,
  `query_field_list` text,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `tpl_name` varchar(32) DEFAULT NULL,
  `additional_info` varchar(512) DEFAULT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `report_advice_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表';



# Dump of table sys_user
# ------------------------------------------------------------

CREATE TABLE `sys_user` (
  `username` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_book
# ------------------------------------------------------------

CREATE TABLE `t_book` (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(32) NOT NULL,
  `person_id` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_book_title_uindex` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='书';



# Dump of table t_book_tag
# ------------------------------------------------------------

CREATE TABLE `t_book_tag` (
  `book_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `t_book_tag_pk` (`book_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_company
# ------------------------------------------------------------

CREATE TABLE `t_company` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `address` varchar(32) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `first_name` varchar(32) DEFAULT NULL,
  `last_name` varchar(32) DEFAULT NULL,
  `contact_phone` varchar(32) DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司';



# Dump of table t_company_vendor
# ------------------------------------------------------------

CREATE TABLE `t_company_vendor` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商';



# Dump of table t_demo
# ------------------------------------------------------------

CREATE TABLE `t_demo` (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(255) NOT NULL COMMENT '任务名称',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `demo_type` varchar(16) NOT NULL COMMENT '任务类型',
  `create_by` bigint NOT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NOT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) NOT NULL COMMENT '是否删除',
  `version` int DEFAULT NULL,
  `work_time` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务';



# Dump of table t_farmer
# ------------------------------------------------------------

CREATE TABLE `t_farmer` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农民';



# Dump of table t_film
# ------------------------------------------------------------

CREATE TABLE `t_film` (
  `seq_id` varchar(32) NOT NULL COMMENT '主键',
  `title` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电影';



# Dump of table t_info
# ------------------------------------------------------------

CREATE TABLE `t_info` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `native_place` varchar(32) DEFAULT NULL,
  `hobby` varchar(32) DEFAULT NULL,
  `agree` varchar(32) DEFAULT NULL,
  `remark` varchar(32) DEFAULT NULL,
  `sex` varchar(32) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `file` varchar(10000) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `info` json DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息';



# Dump of table t_material
# ------------------------------------------------------------

CREATE TABLE `t_material` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农民';



# Dump of table t_message
# ------------------------------------------------------------

CREATE TABLE `t_message` (
  `id` bigint NOT NULL COMMENT '主键',
  `text` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息';



# Dump of table t_message2
# ------------------------------------------------------------

CREATE TABLE `t_message2` (
  `seq` bigint NOT NULL COMMENT '主键',
  `text` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息2';



# Dump of table t_notice
# ------------------------------------------------------------

CREATE TABLE `t_notice` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `version` int NOT NULL COMMENT '版本号',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` bit(1) DEFAULT NULL COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告';



# Dump of table t_person
# ------------------------------------------------------------

CREATE TABLE `t_person` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `id_card_id` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `sex` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `t_person_t_person_id_card_id_fk` (`id_card_id`),
  CONSTRAINT `t_person_t_person_id_card_id_fk` FOREIGN KEY (`id_card_id`) REFERENCES `t_person_id_card` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_person_id_card
# ------------------------------------------------------------

CREATE TABLE `t_person_id_card` (
  `id` bigint NOT NULL COMMENT '主键',
  `id_num` varchar(32) DEFAULT NULL,
  `address` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_person_role
# ------------------------------------------------------------

CREATE TABLE `t_person_role` (
  `person_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `t_person_role_pk` (`person_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project_detail1
# ------------------------------------------------------------

CREATE TABLE `t_project_detail1` (
  `id` bigint NOT NULL,
  `title` varchar(32) NOT NULL,
  `project_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project_detail2
# ------------------------------------------------------------

CREATE TABLE `t_project_detail2` (
  `id` bigint NOT NULL,
  `title` varchar(32) NOT NULL,
  `project_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project_detail3
# ------------------------------------------------------------

CREATE TABLE `t_project_detail3` (
  `id` bigint NOT NULL,
  `title` varchar(32) NOT NULL,
  `project_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `create_by` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project1
# ------------------------------------------------------------

CREATE TABLE `t_project1` (
  `id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `map` text,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `cover_url` varchar(500) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  `sex` char(1) DEFAULT NULL COMMENT '性别',
  `address` text,
  `status` varchar(255) DEFAULT NULL,
  `list` text,
  `phone_number` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project2
# ------------------------------------------------------------

CREATE TABLE `t_project2` (
  `id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `cover_url` varchar(500) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  `sex` char(1) DEFAULT NULL COMMENT '性别',
  `address` text,
  `status` varchar(255) DEFAULT NULL,
  `list` text,
  `phone_number` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_project3
# ------------------------------------------------------------

CREATE TABLE `t_project3` (
  `id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `cover_url` varchar(500) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  `sex` char(1) DEFAULT NULL COMMENT '性别',
  `address` text,
  `status` varchar(255) DEFAULT NULL,
  `list` text,
  `phone_number` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_role
# ------------------------------------------------------------

CREATE TABLE `t_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_school
# ------------------------------------------------------------

CREATE TABLE `t_school` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL COMMENT '学校名称',
  `build_date` date DEFAULT NULL COMMENT '建校日期',
  `type` varchar(16) DEFAULT NULL COMMENT '学校性质 PRIVATE：私立；PUBLIC：公立',
  `budget` decimal(10,4) DEFAULT NULL COMMENT '每年经费预算',
  `score` int DEFAULT NULL COMMENT '专业数',
  `address` json DEFAULT NULL COMMENT '学校地址',
  `additional_info` json DEFAULT NULL COMMENT '其他信息',
  `leadership_information_list` json DEFAULT NULL COMMENT '历届领导信息',
  `awards_set` varchar(32) DEFAULT NULL COMMENT '历届获奖信息',
  `score_list` json DEFAULT NULL COMMENT '历届学校评分',
  `evaluate` varchar(128) DEFAULT NULL COMMENT '学校评价',
  `school_license_id` bigint DEFAULT NULL COMMENT '证书信息',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `t_school_t_school_license_id_fk` (`school_license_id`),
  CONSTRAINT `t_school_t_school_license_id_fk` FOREIGN KEY (`school_license_id`) REFERENCES `t_school_license` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校';



# Dump of table t_school_license
# ------------------------------------------------------------

CREATE TABLE `t_school_license` (
  `id` bigint NOT NULL COMMENT '主键',
  `number` varchar(32) DEFAULT NULL COMMENT '证书编号',
  `remark` varchar(32) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校证书';



# Dump of table t_school_student
# ------------------------------------------------------------

CREATE TABLE `t_school_student` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL COMMENT '姓名',
  `grade` int DEFAULT NULL COMMENT '年级',
  `sex` varchar(16) DEFAULT NULL COMMENT '性别',
  `school_id` bigint DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `t_school_student_t_school_id_fk` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生';



# Dump of table t_school_teacher
# ------------------------------------------------------------

CREATE TABLE `t_school_teacher` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) DEFAULT NULL COMMENT '姓名',
  `age` int DEFAULT NULL COMMENT '年龄',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师';



# Dump of table t_school_teacher_related
# ------------------------------------------------------------

CREATE TABLE `t_school_teacher_related` (
  `school_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `t_school_teacher_related_pk` (`school_id`,`teacher_id`),
  KEY `t_school_teacher_related_t_school_teacher_id_fk` (`teacher_id`),
  CONSTRAINT `t_school_teacher_related_t_school_id_fk` FOREIGN KEY (`school_id`) REFERENCES `t_school` (`id`),
  CONSTRAINT `t_school_teacher_related_t_school_teacher_id_fk` FOREIGN KEY (`teacher_id`) REFERENCES `t_school_teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# Dump of table t_tag
# ------------------------------------------------------------

CREATE TABLE `t_tag` (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='tag';



# Dump of table t_task
# ------------------------------------------------------------

CREATE TABLE `t_task` (
  `id` bigint NOT NULL COMMENT '主键',
  `task_name` varchar(32) NOT NULL COMMENT '任务名称',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cost_hours` int DEFAULT NULL,
  `complete` bit(1) DEFAULT NULL,
  `assign_user_id` bigint DEFAULT NULL,
  `address` json DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL COMMENT '用户状态',
  `list` json DEFAULT NULL,
  `phone_number` varchar(32) DEFAULT NULL,
  `map` json DEFAULT NULL,
  `list_map` json DEFAULT NULL,
  `num_list` json DEFAULT NULL,
  `str_list` json DEFAULT NULL,
  `dept` text NOT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务表';



# Dump of table t_user
# ------------------------------------------------------------

CREATE TABLE `t_user` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `gender` varchar(16) NOT NULL COMMENT '性别',
  `age` int NOT NULL COMMENT '年龄',
  `birthday` date DEFAULT NULL COMMENT '出生时间',
  `mobile` varchar(32) NOT NULL COMMENT '手机号码',
  `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
  `native_place` varchar(32) NOT NULL COMMENT '籍贯',
  `hobby` json NOT NULL COMMENT '兴趣爱好',
  `marriage` bit(1) DEFAULT NULL COMMENT '婚否',
  `score` decimal(10,0) DEFAULT NULL,
  `introduce` text,
  `attachment` text,
  `school_experience` text,
  `status` varchar(16) DEFAULT NULL COMMENT '用户状态',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人员信息表';



# Dump of table t_worker
# ------------------------------------------------------------

CREATE TABLE `t_worker` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工人';




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
