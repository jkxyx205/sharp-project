# ************************************************************
# Sequel Ace SQL dump
# Version 20046
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# Host: localhost (MySQL 9.1.0)
# Database: sharp-admin
# Generation Time: 2025-06-15 11:30:38 +0000
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

LOCK TABLES `sys_access_info` WRITE;
/*!40000 ALTER TABLE `sys_access_info` DISABLE KEYS */;

INSERT INTO `sys_access_info` (`id`, `content`, `create_time`)
VALUES
	(1,'用户信息:[username:admin, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:59752, 请求方式:GET, URI:/demos/htmx/demo, 请求参数值:]','2025-06-15 19:29:55'),
	(2,'用户信息:[username:admin, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:59752, 请求方式:GET, URI:/demos/htmx/demo, 请求参数值:]','2025-06-15 19:29:55'),
	(3,'用户信息:[username:admin, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:59752, 请求方式:GET, URI:/demos/htmx/about, 请求参数值:]','2025-06-15 19:29:57'),
	(4,'用户信息:[username:admin, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:59752, 请求方式:GET, URI:/demos/htmx/index, 请求参数值:]','2025-06-15 19:29:58');

/*!40000 ALTER TABLE `sys_access_info` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
