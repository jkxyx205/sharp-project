/*
 Navicat Premium Dump SQL

 Source Server         : 96.44.169.236
 Source Server Type    : PostgreSQL
 Source Server Version : 170005 (170005)
 Source Host           : 96.44.169.236:5432
 Source Catalog        : challenge
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 170005 (170005)
 File Encoding         : 65001

 Date: 01/07/2025 15:19:51
*/


-- ----------------------------
-- Type structure for category_enum
-- ----------------------------
DROP TYPE IF EXISTS "public"."category_enum";
CREATE TYPE "public"."category_enum" AS ENUM (
  'MATERIAL',
  'PURCHASING_ORG',
  'PACKAGING',
  'SALES_ORG'
);
ALTER TYPE "public"."category_enum" OWNER TO "postgres";

-- ----------------------------
-- Type structure for gender_enum
-- ----------------------------
DROP TYPE IF EXISTS "public"."gender_enum";
CREATE TYPE "public"."gender_enum" AS ENUM (
  'M',
  'F'
);
ALTER TYPE "public"."gender_enum" OWNER TO "postgres";

-- ----------------------------
-- Sequence structure for sys_access_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_access_info_id_seq";
CREATE SEQUENCE "public"."sys_access_info_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;
ALTER SEQUENCE "public"."sys_access_info_id_seq" OWNER TO "postgres";

-- ----------------------------
-- Table structure for core_code_sequence
-- ----------------------------
DROP TABLE IF EXISTS "public"."core_code_sequence";
CREATE TABLE "public"."core_code_sequence" (
  "id" int8 NOT NULL,
  "category" varchar(32) COLLATE "pg_catalog"."default",
  "prefix" varchar(32) COLLATE "pg_catalog"."default",
  "name" varchar(32) COLLATE "pg_catalog"."default",
  "sequence" int4
)
;
ALTER TABLE "public"."core_code_sequence" OWNER TO "postgres";
COMMENT ON COLUMN "public"."core_code_sequence"."id" IS '主键';
COMMENT ON TABLE "public"."core_code_sequence" IS '序号表';

-- ----------------------------
-- Records of core_code_sequence
-- ----------------------------
BEGIN;
INSERT INTO "public"."core_code_sequence" ("id", "category", "prefix", "name", "sequence") VALUES (1, 'TEAM', 'T', '20250701', 5);
COMMIT;

-- ----------------------------
-- Table structure for proveg_team
-- ----------------------------
DROP TABLE IF EXISTS "public"."proveg_team";
CREATE TABLE "public"."proveg_team" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default",
  "intro" text COLLATE "pg_catalog"."default",
  "cover" varchar(32) COLLATE "pg_catalog"."default",
  "proposal" varchar(32) COLLATE "pg_catalog"."default",
  "status" varchar(32) COLLATE "pg_catalog"."default",
  "judge" bool DEFAULT false,
  "create_by" int8,
  "create_time" timestamp(6) NOT NULL,
  "update_by" int8,
  "update_time" timestamp(6) NOT NULL,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
ALTER TABLE "public"."proveg_team" OWNER TO "postgres";
COMMENT ON COLUMN "public"."proveg_team"."id" IS '主键';
COMMENT ON COLUMN "public"."proveg_team"."code" IS '外部可见，唯一code';
COMMENT ON COLUMN "public"."proveg_team"."name" IS '名称';
COMMENT ON COLUMN "public"."proveg_team"."intro" IS '简介';
COMMENT ON COLUMN "public"."proveg_team"."cover" IS '封面';
COMMENT ON COLUMN "public"."proveg_team"."proposal" IS '提案';
COMMENT ON COLUMN "public"."proveg_team"."status" IS '状态';
COMMENT ON COLUMN "public"."proveg_team"."judge" IS '是否评审';
COMMENT ON COLUMN "public"."proveg_team"."create_by" IS '创建人';
COMMENT ON COLUMN "public"."proveg_team"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."proveg_team"."update_by" IS '更新人';
COMMENT ON COLUMN "public"."proveg_team"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."proveg_team"."is_deleted" IS '是否逻辑删除';
COMMENT ON TABLE "public"."proveg_team" IS '团队';

-- ----------------------------
-- Records of proveg_team
-- ----------------------------
BEGIN;
INSERT INTO "public"."proveg_team" ("id", "code", "name", "intro", "cover", "proposal", "status", "judge", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971438844228444160, 'T2025070101', 'Super Bread', 'We are the best', 'http://xx.jpg', 'http://2323.pdf', 'PROCESSING', 'f', 1, '2025-07-01 14:04:06.734', 1, '2025-07-01 14:04:06.734', 'f');
INSERT INTO "public"."proveg_team" ("id", "code", "name", "intro", "cover", "proposal", "status", "judge", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971440774824595456, 'T2025070103', 'Super Bread', 'We are the best', 'http://xx.jpg', 'http://2323.pdf', 'PROCESSING', 'f', 1, '2025-07-01 14:11:47.024', 1, '2025-07-01 14:11:47.024', 'f');
INSERT INTO "public"."proveg_team" ("id", "code", "name", "intro", "cover", "proposal", "status", "judge", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971441999649480704, 'T2025070104', 'Super Bread', 'We are the best', 'http://xx.jpg', 'http://2323.pdf', 'PROCESSING', 'f', 1, '2025-07-01 14:16:39.045', 1, '2025-07-01 14:16:39.045', 'f');
INSERT INTO "public"."proveg_team" ("id", "code", "name", "intro", "cover", "proposal", "status", "judge", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971443130068934656, 'T2025070102', 'Super Bread', 'We are the best', 'http://xx.jpg', 'http://2323.pdf', 'PROCESSING', 'f', 1, '2025-07-01 14:09:08.921', 1, '2025-07-01 14:09:08.921', 'f');
INSERT INTO "public"."proveg_team" ("id", "code", "name", "intro", "cover", "proposal", "status", "judge", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971443948318924800, 'T2025070105', 'Super Bread', 'We are the best', 'http://xx.jpg', 'http://2323.pdf', 'PROCESSING', 'f', 1, '2025-07-01 14:24:23.644', 1, '2025-07-01 14:24:23.644', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_access_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_access_info";
CREATE TABLE "public"."sys_access_info" (
  "id" int4 NOT NULL GENERATED ALWAYS AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1
),
  "content" text COLLATE "pg_catalog"."default",
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone
)
;
ALTER TABLE "public"."sys_access_info" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_access_info
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (1, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56773, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 14:25:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (2, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56837, 请求方式:GET, URI:/adminlte/demo, 请求参数值:]', '2025-06-30 14:25:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (3, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56837, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 14:25:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (4, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56861, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 14:25:44');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (5, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56900, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:25:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (6, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64791, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 14:45:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (7, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64817, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:45:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (8, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64909, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 14:45:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (9, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65311, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:47:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (10, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65370, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 14:47:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (11, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65440, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:47:50');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (12, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65492, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 14:48:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (13, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65492, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 14:48:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (14, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65530, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:48:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (15, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49187, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 14:48:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (16, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49219, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:48:18');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (17, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49276, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:48:29');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (18, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49333, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:type=MATERIAL&page=1]', '2025-06-30 14:48:41');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (19, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49491, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:type=MATERIAL&page=1]', '2025-06-30 14:49:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (20, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50460, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 14:51:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (21, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50460, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:51:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (22, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50536, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 14:52:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (23, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50605, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 14:52:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (24, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50629, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:52:20');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (25, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50647, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 14:52:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (26, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50779, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=MATERIAL&page=1]', '2025-06-30 14:52:50');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (27, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50811, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=MATERIAL&page=1]', '2025-06-30 14:52:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (28, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52530, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=MATERIAL&page=1]', '2025-06-30 14:53:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (29, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:54609, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 14:58:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (30, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:54699, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 14:58:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (31, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:54699, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 14:58:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (32, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:54743, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 14:58:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (33, '用户信息:[username:sa, name:sa], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:52276, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 15:35:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (34, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:52297, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 15:39:03');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (35, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:52297, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 15:39:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (36, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:52297, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 15:39:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (37, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54626, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 15:51:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (38, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54626, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 15:51:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (39, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54626, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 15:51:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (40, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57163, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:01:19');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (41, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57171, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:01:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (42, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57171, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:01:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (43, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57467, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:07:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (44, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57467, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:07:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (45, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57467, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:07:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (46, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59367, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:15:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (47, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59367, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:15:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (48, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59367, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:15:48');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (49, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:59941, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:22:33');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (50, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59937, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:22:33');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (51, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59937, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:23:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (52, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59937, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:23:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (53, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:61943, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:26:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (54, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62005, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:27:13');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (55, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62005, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:27:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (56, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62010, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:27:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (57, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:62085, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:28:11');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (58, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:62085, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:28:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (59, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:62085, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:28:19');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (60, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62483, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:33:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (61, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62483, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:33:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (62, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62483, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:33:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (63, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:36:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (64, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:36:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (65, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:36:20');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (66, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:36:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (67, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:PUT, URI:/forms/ajax/695312747063197696/1, 请求参数值:{"id":"1","type":"UNIT","name":"EA","label":"个","sort":"0"}]', '2025-06-30 16:36:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (68, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=]', '2025-06-30 16:36:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (69, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62724, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:36:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (70, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:39:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (71, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:39:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (72, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:39:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (73, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/forms/page/695312747063197696/965718041910333440, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:39:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (74, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/forms/page/695312747063197696/965718041910333440, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:39:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (75, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:PUT, URI:/forms/ajax/695312747063197696/965718041910333440, 请求参数值:{"id":"965718041910333440","type":"MATERIAL","name":"232323","label":"擦","sort":"0"}]', '2025-06-30 16:39:41');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (76, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=]', '2025-06-30 16:39:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (77, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 16:39:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (78, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/forms/page/694980924206493696/1, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:39:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (79, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/forms/page/694980924206493696/786079661661646848, 请求参数值:readonly=?readonly=false]', '2025-06-30 16:40:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (80, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:PUT, URI:/forms/ajax/694980924206493696/786079661661646848, 请求参数值:{"id":"786079661661646848","code":"TEST","name":"测试","birthday":"2025-06-30","attachment":"[]","attachment_file":"","available":["on"]}]', '2025-06-30 16:40:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (81, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=&name=&is_available=&create_time0=&create_time1=]', '2025-06-30 16:40:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (82, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64583, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:40:19');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (83, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64722, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:42:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (84, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64722, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:42:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (85, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64858, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 16:44:59');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (86, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64865, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 16:45:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (87, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64865, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 16:45:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (88, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64865, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:45:08');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (89, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64866, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:45:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (90, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64865, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:45:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (91, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64864, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:45:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (92, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64864, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:46:29');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (93, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64864, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:46:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (94, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64864, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:46:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (95, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64864, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:46:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (96, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49374, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:48:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (97, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49374, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:49:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (98, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49374, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 16:49:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (99, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52252, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:53:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (100, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:53:55');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (101, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:54:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (102, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680","694587393189089281","696145672415481856","696145672960741376","696145673485029376","696145674072231936","696145674634268672","694587393189089283","696146982695079938","696146983320031234","696146983924011010","696146984569933826","696146985190690818","696146982695079937","696146983320031233","696146983924011009","696146984569933825","696146985190690817","696146982695079936","696146983320031232","696146983924011008","696146984569933824","696146985190690816"]}]', '2025-06-30 16:54:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (103, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"}]]', '2025-06-30 16:54:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (104, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:54:50');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (105, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52371, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:55:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (106, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52670, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:55:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (107, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52670, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"}]]', '2025-06-30 16:55:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (108, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53075, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"}]]', '2025-06-30 16:56:29');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (109, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53075, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:56:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (110, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53075, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:56:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (111, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:56:47');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (112, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"}]]', '2025-06-30 16:56:48');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (113, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:56:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (114, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:57:08');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (115, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:57:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (116, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:57:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (137, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:51348, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 17:50:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (138, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:51389, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 17:50:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (139, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:51391, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 17:50:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (117, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680","694587393189089281","696145672415481856","696145672960741376","696145673485029376","696145674072231936","696145674634268672","694587393189089283","696146982695079938","696146983320031234","696146983924011010","696146984569933826","696146985190690818","696146982695079937","696146983320031233","696146983924011009","696146984569933825","696146985190690817","696146982695079936","696146983320031232","696146983924011008","696146984569933824","696146985190690816"]}]', '2025-06-30 16:57:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (118, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:57:38');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (119, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:57:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (120, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:57:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (121, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53146, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:58:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (122, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53145, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:58:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (123, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53143, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:58:07');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (124, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53143, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:58:13');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (125, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53143, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"},{"name":"TEST"}]]', '2025-06-30 16:58:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (126, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53143, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:58:33');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (127, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53143, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:58:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (128, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 16:58:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (129, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680","694587393189089281","696145672415481856","696145672960741376","696145673485029376","696145674072231936","696145674634268672","694587393189089283","696146982695079938","696146983320031234","696146983924011010","696146984569933826","696146985190690818","696146982695079937","696146983320031233","696146983924011009","696146984569933825","696146985190690817","696146982695079936","696146983320031232","696146983924011008","696146984569933824","696146985190690816"]}]', '2025-06-30 16:59:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (130, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 16:59:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (131, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53144, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 16:59:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (132, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53147, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 16:59:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (133, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53075, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 17:00:08');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (134, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62613, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680"]}]', '2025-06-30 17:37:59');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (135, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62613, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680","694587393189089281","696145672415481856","696145672960741376","696145673485029376","696145674072231936","696145674634268672","694587393189089283","696146982695079938","696146983320031234","696146983924011010","696146984569933826","696146985190690818","696146982695079937","696146983320031233","696146983924011009","696146984569933825","696146985190690817","696146982695079936","696146983320031232","696146983924011008","696146984569933824","696146985190690816"]}]', '2025-06-30 17:38:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (136, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:62613, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:{"permissionIds":["946731526249279488","694587393189089280","946731526370914304","946731526417051648","946731526484160512","946731526521909248","946731526555463680","694587393189089281","696145672415481856","696145672960741376","696145673485029376","696145674072231936","696145674634268672","694587393189089283","696146982695079938","696146983320031234","696146983924011010","696146984569933826","696146985190690818","696146982695079937","696146983320031233","696146983924011009","696146984569933825","696146985190690817","696146982695079936","696146983320031232","696146983924011008","696146984569933824","696146985190690816"]}]', '2025-06-30 17:39:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (140, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:51391, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 17:50:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (141, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:51391, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 17:51:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (142, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52445, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 17:54:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (143, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52445, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 17:54:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (144, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52491, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 17:54:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (145, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52491, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 17:54:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (146, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53646, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 17:58:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (147, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53691, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 17:58:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (148, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53692, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 17:58:47');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (149, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53692, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 17:58:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (150, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53692, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 17:59:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (151, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53692, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 17:59:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (152, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53692, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 17:59:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (153, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53694, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 17:59:18');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (154, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53691, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 17:59:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (155, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54288, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:{"userIds":["1"]}]', '2025-06-30 18:00:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (156, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54971, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:03:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (157, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54971, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:03:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (158, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:03:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (159, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 18:03:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (160, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:03:30');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (161, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:03:35');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (162, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:03:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (163, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:03:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (164, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:55013, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:03:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (165, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57124, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:05:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (166, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57174, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:05:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (167, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57173, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:05:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (168, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57124, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:05:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (169, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57124, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:05:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (170, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57170, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:05:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (171, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57174, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:05:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (172, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57170, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:06:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (173, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57175, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:06:07');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (174, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:57176, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:06:13');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (175, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58288, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:09:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (176, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58346, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:09:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (177, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58344, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:09:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (178, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58344, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:09:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (179, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58345, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:09:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (180, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59031, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:11:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (181, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59101, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:11:28');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (182, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59101, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:11:35');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (183, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59101, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:11:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (184, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59182, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:11:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (185, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59182, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:11:55');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (186, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59182, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:12:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (187, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59118, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:12:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (188, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59118, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:12:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (189, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59118, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:12:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (190, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59644, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:13:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (191, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:59676, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:13:33');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (192, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:63587, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 18:21:11');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (193, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:63587, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:21:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (194, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 18:23:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (195, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 18:23:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (196, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:23:16');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (197, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 18:23:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (198, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:23:35');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (199, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:23:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (200, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:23:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (201, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:24:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (202, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:24:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (203, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"},{"name":"TEST","id":"971120333606133760"}]]', '2025-06-30 18:24:19');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (204, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (205, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:24:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (206, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:24:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (207, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64572, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (208, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (209, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64572, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:38');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (210, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64163, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:24:38');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (211, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64604, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (212, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64604, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 18:24:47');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (213, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64604, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:24:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (214, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64604, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:24:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (215, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64604, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-06-30 18:25:09');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (216, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64765, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:25:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (217, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64765, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:25:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (218, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64765, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:25:32');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (219, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64765, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:25:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (220, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64765, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:25:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (221, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65173, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:27:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (222, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65236, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:27:07');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (223, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:27:11');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (224, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:27:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (225, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:27:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (226, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:27:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (227, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 18:27:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (228, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:POST, URI:/auth/assign/role/971120333606133760/user, 请求参数值:]', '2025-06-30 18:27:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (229, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:27:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (230, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:POST, URI:/auth/assign/role/694587732420202496/user, 请求参数值:]', '2025-06-30 18:27:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (231, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65240, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:27:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (232, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65173, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:27:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (233, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:65173, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:27:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (234, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:29:13');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (235, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:29:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (236, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:29:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (237, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:29:47');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (238, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:30:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (239, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49432, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:30:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (240, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49936, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:31:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (241, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:49934, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:31:07');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (242, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50744, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:34:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (243, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50792, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:34:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (244, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:34:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (245, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:34:18');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (246, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:34:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (247, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:34:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (248, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:POST, URI:/auth/assign/role/971120333606133760/permission, 请求参数值:]', '2025-06-30 18:34:27');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (249, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:35:03');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (250, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:35:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (251, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52780, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:35:38');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (252, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:50791, 请求方式:GET, URI:/auth/permission, 请求参数值:]', '2025-06-30 18:35:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (253, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52780, 请求方式:GET, URI:/auth/permission, 请求参数值:]', '2025-06-30 18:35:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (254, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53068, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:36:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (255, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:36:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (256, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:36:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (257, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:36:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (258, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:37:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (259, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:37:08');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (260, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:37:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (261, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:37:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (262, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:37:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (263, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53100, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:37:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (264, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53218, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:37:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (265, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53218, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:37:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (266, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53218, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:38:30');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (267, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53218, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:38:41');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (268, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:39:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (269, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53972, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:40:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (270, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:40:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (271, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:40:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (272, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:40:41');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (273, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:40:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (274, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:40:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (275, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:41:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (276, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:41:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (277, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:41:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (278, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:41:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (279, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:41:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (280, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53934, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:41:28');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (281, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:43:08');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (282, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:43:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (283, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:43:20');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (284, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:43:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (285, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:43:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (286, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:43:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (287, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:43:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (288, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54909, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:43:59');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (289, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56185, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:48:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (290, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56189, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:48:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (291, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56189, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:48:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (292, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56237, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:48:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (293, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:56237, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"},{"name":"TEST","id":"971120333606133760"}]]', '2025-06-30 18:48:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (294, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58197, 请求方式:POST, URI:/auth/add/roles, 请求参数值:[{"name":"管理员","id":"694587732420202496"},{"name":"TEST","id":"971120333606133760"},{"name":"TEST2"}]]', '2025-06-30 18:49:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (295, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58197, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:49:48');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (296, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 18:49:55');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (297, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/971148330165903360/info, 请求参数值:]', '2025-06-30 18:49:59');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (298, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:50:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (299, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 18:50:05');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (300, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:50:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (301, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:50:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (302, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=1]', '2025-06-30 18:50:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (303, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 18:50:37');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (304, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-06-30 18:50:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (305, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58285, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:50:41');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (306, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 18:50:49');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (307, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:GET, URI:/forms/page/694980924206493696/786079661661646848, 请求参数值:readonly=?readonly=false]', '2025-06-30 18:50:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (308, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-06-30 18:50:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (309, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:PUT, URI:/forms/ajax/694980924206493696/786079661661646848, 请求参数值:{"id":"786079661661646848","code":"TEST","name":"测试","birthday":"2025-06-30","attachment":"[]","attachment_file":"","available":["on"],"roleIds":["971120333606133760","971148330165903360"]}]', '2025-06-30 18:51:04');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (310, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=&name=&is_available=&create_time0=&create_time1=]', '2025-06-30 18:51:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (311, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58282, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 18:51:09');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (312, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58281, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 18:51:17');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (313, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58281, 请求方式:GET, URI:/auth/971148330165903360/info, 请求参数值:]', '2025-06-30 18:51:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (314, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58281, 请求方式:GET, URI:/auth/permission, 请求参数值:userId=786079661661646848]', '2025-06-30 18:51:24');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (315, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:61541, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 19:01:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (316, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:61541, 请求方式:GET, URI:/adminlte/css/adminlte.css.map, 请求参数值:]', '2025-06-30 19:01:46');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (317, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-06-30 19:06:21');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (318, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-06-30 19:06:29');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (319, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-06-30 19:06:31');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (320, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 19:06:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (321, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/auth/971148330165903360/info, 请求参数值:]', '2025-06-30 19:06:42');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (322, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/auth/971120333606133760/info, 请求参数值:]', '2025-06-30 19:06:45');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (323, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:64590, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-06-30 19:06:48');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (324, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:62200, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-07-01 10:44:53');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (325, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:127.0.0.1, port:62206, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 10:44:56');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (326, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-07-01 14:25:34');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (327, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/adminlte/demo, 请求参数值:]', '2025-07-01 14:26:33');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (328, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:26:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (329, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-07-01 14:26:40');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (330, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-07-01 14:26:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (331, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:PUT, URI:/forms/ajax/695312747063197696/1, 请求参数值:{"id":"1","type":"UNIT","name":"EA","label":"个个个","sort":"0"}]', '2025-07-01 14:26:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (332, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=]', '2025-07-01 14:26:55');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (333, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52549, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-07-01 14:26:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (334, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/adminlte/plugins/fontawesome-free/webfonts/fa-solid-900.woff2, 请求参数值:]', '2025-07-01 14:27:14');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (335, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:POST, URI:/auth/assign/role/694587732420202496/permission, 请求参数值:]', '2025-07-01 14:27:15');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (336, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/auth/971148330165903360/info, 请求参数值:]', '2025-07-01 14:27:19');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (337, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/auth/694587732420202496/info, 请求参数值:]', '2025-07-01 14:27:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (338, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:27:38');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (339, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/forms/page/694980924206493696/1, 请求参数值:readonly=?readonly=false]', '2025-07-01 14:27:43');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (340, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/forms/page/694980924206493696/786079661661646848, 请求参数值:readonly=?readonly=false]', '2025-07-01 14:27:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (341, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:PUT, URI:/forms/ajax/694980924206493696/786079661661646848, 请求参数值:{"id":"786079661661646848","code":"TEST","name":"测试","birthday":"2025-06-30","attachment":"[]","attachment_file":"","available":["on"],"roleIds":["971120333606133760","971148330165903360"]}]', '2025-07-01 14:27:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (342, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=&name=&is_available=&create_time0=&create_time1=]', '2025-07-01 14:28:01');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (343, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:code=&name=&is_available=&create_time0=&create_time1=]', '2025-07-01 14:28:12');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (344, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/adminlte/demo, 请求参数值:]', '2025-07-01 14:28:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (345, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-07-01 14:28:26');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (346, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:28:29');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (347, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:52857, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=%09ADMIN&name=&is_available=&create_time0=&create_time1=&page=1]', '2025-07-01 14:28:39');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (348, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/dashboard, 请求参数值:]', '2025-07-01 14:31:54');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (349, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:32:51');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (350, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=admin&name=&is_available=&create_time0=&create_time1=&page=1]', '2025-07-01 14:32:58');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (351, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=&name=&create_time0=&create_time1=&page=1]', '2025-07-01 14:33:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (352, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=admin&name=&create_time0=&create_time1=&page=1]', '2025-07-01 14:33:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (353, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=admin2&name=&create_time0=&create_time1=&page=1]', '2025-07-01 14:33:30');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (354, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-07-01 14:34:06');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (355, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=MATERIAL&page=1]', '2025-07-01 14:34:10');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (356, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:34:13');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (357, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:53101, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=admin&name=&is_available=&create_time0=&create_time1=&page=1]', '2025-07-01 14:34:28');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (358, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=&name=&create_time0=&create_time1=&page=1]', '2025-07-01 14:36:44');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (359, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/694714180413960192/json/summary, 请求参数值:code=admin&name=&create_time0=&create_time1=&page=1]', '2025-07-01 14:36:52');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (360, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/adminlte/demo, 请求参数值:]', '2025-07-01 14:36:57');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (361, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:37:00');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (362, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/roles, 请求参数值:]', '2025-07-01 14:37:02');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (363, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/layout/694714180413960192, 请求参数值:]', '2025-07-01 14:37:22');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (364, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/layout/695316160014499840, 请求参数值:]', '2025-07-01 14:37:25');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (365, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/forms/page/695312747063197696/1, 请求参数值:readonly=?readonly=false]', '2025-07-01 14:37:36');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (366, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:PUT, URI:/forms/ajax/695312747063197696/1, 请求参数值:{"id":"1","type":"UNIT","name":"EA","label":"个","sort":"0"}]', '2025-07-01 14:37:44');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (367, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:54044, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=]', '2025-07-01 14:37:47');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (368, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58022, 请求方式:GET, URI:/forms/page/695312747063197696/2, 请求参数值:readonly=?readonly=false]', '2025-07-01 15:09:23');
INSERT INTO "public"."sys_access_info" ("id", "content", "create_time") OVERRIDING SYSTEM VALUE VALUES (369, '用户信息:[username:ADMIN, name:管理员], 客户端信息:[类型:Computer, 操作系统类型:Mac OS X, ip:0:0:0:0:0:0:0:1, port:58022, 请求方式:GET, URI:/reports/695316160014499840/json/summary, 请求参数值:type=MATERIAL&page=1]', '2025-07-01 15:09:31');
COMMIT;

-- ----------------------------
-- Table structure for sys_code_description
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_code_description";
CREATE TABLE "public"."sys_code_description" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "category" varchar(30) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "sort" int4,
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_code_description" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_code_description
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_code_description" ("id", "code", "description", "category", "sort", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731527121694720, 'PG1', '采购组织1', 'PURCHASING_ORG', 0, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_code_description" ("id", "code", "description", "category", "sort", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731527121694721, 'M1', '采购组织2', 'PURCHASING_ORG', 1, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_code_description" ("id", "code", "description", "category", "sort", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731527209775104, 'M1', '物料组1', 'MATERIAL', 0, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_code_description" ("id", "code", "description", "category", "sort", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731527209775105, 'M3', '物料组3', 'MATERIAL', 1, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_code_description" ("id", "code", "description", "category", "sort", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731527209775106, 'M4', '物料组4', 'MATERIAL', 2, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dict";
CREATE TABLE "public"."sys_dict" (
  "id" int8,
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "label" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "sort" int4,
  "remark" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
ALTER TABLE "public"."sys_dict" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (965742343984533504, 'MATERIAL', '1111', 'xx000', 0, NULL, 1, '2025-06-15 20:48:15', 1, '2025-06-16 17:15:32', 't');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (966407499286597632, 'MATERIAL', '23232', '23322332', 0, NULL, 1, '2025-06-17 16:51:21', 1, '2025-06-18 06:35:58', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (966136233518452736, 'MATERIAL', '2323232', '2323', 0, NULL, 1, '2025-06-16 22:53:26', 1, '2025-06-16 22:53:26', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (966407543490367488, 'MATERIAL', '2332', '233232', 2, NULL, 1, '2025-06-17 16:51:31', 1, '2025-06-18 06:56:03', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (3, 'MATERIAL', 'M1', '2222', 0, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-17 19:33:07', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (4, 'MATERIAL', 'M2', '物料2', 1, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-18 06:55:58', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (5, 'MATERIAL', 'M3', '物料3', 2, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-18 06:36:39', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (6, 'MATERIAL', 'M4', '物料4', 3, NULL, 1, '2024-11-28 11:16:22', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (966407581775974400, 'MATERIAL_TYPE', '23322', '23323', 0, NULL, 1, '2025-06-17 16:51:40', 1, '2025-06-17 16:51:40', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (965717198205112320, 'MATERIAL_TYPE', 'FEAT', '产品', 9, NULL, 1, '2025-06-15 19:08:20', 1, '2025-06-15 19:08:20', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (695367653333712896, 'MATERIAL_TYPE', 'HIBE', '耗材用品', 8, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-18 06:35:55', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (965742162547331072, 'MATERIAL_TYPE', 'UAT', 'SUATSUAT', 92, NULL, 1, '2025-06-15 20:47:32', 1, '2025-06-18 06:36:09', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (862375391526948864, 'UNIT', 'BAG', '包', 9, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-18 08:18:34', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (2, 'UNIT', 'KG', '千克', 1, NULL, 1, '2024-11-28 11:16:22', 1, '2025-06-16 14:11:53', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (965722206296363008, 'UNIT', 'KGG', '拆', 0, NULL, 1, '2025-06-15 19:28:14', 1, '2025-06-17 15:44:02', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (966005952312528896, 'UNIT', 'tc', 'tctctc', 0, NULL, 1, '2025-06-16 14:15:44', 1, '2025-06-16 17:15:29', 't');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (965718041910333440, 'MATERIAL', '232323', '擦', 0, NULL, 1, '2025-06-15 19:11:41', 1, '2025-06-30 16:39:41', 'f');
INSERT INTO "public"."sys_dict" ("id", "type", "name", "label", "sort", "remark", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (1, 'UNIT', 'EA', '个', 0, NULL, 1, '2024-11-28 11:16:22', 1, '2025-07-01 14:37:45', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_document
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_document";
CREATE TABLE "public"."sys_document" (
  "id" int8 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "extension" varchar(16) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "content_type" varchar(128) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "size" int4,
  "group_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_by" int8,
  "create_time" timestamp(0) NOT NULL,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_document" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_document
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858097155292835840, '点研营业执照new', 'png', 'image/png', 2227837, 'ckeditor', '858097155238309888.png', 1, '2024-08-22 19:44:42', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858097295894294528, '点研营业执照new', 'png', 'image/png', 2227837, 'ckeditor', '858097295885905920.png', 1, '2024-08-22 19:45:16', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858097296171118593, 'note', NULL, 'application/octet-stream', 33, 'ckeditor', '858097296171118592', 1, '2024-08-22 19:45:16', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858142447786569728, '点研营业执照new', 'png', 'image/png', 2227837, 'ckeditor', '858142447694295040.png', 1, '2024-08-22 22:44:41', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858143002550382592, '糖尿病肾病 试运行', 'docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 13535, 'ckeditor', '858143002420359168.docx', 1, '2024-08-22 22:46:53', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858143015586279424, '点研营业执照new', 'png', 'image/png', 2227837, 'ckeditor', '858143015569502208.png', 1, '2024-08-22 22:46:56', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858143018480349184, 'note', NULL, 'application/octet-stream', 33, 'ckeditor', '858143018471960576', 1, '2024-08-22 22:46:57', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858143021252784128, '投资任职情况查询报告', 'pdf', 'application/pdf', 35345, 'ckeditor', '858143021236006912.pdf', 1, '2024-08-22 22:46:58', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858358554791251968, '图片', 'png', 'image/png', 613008, 'customize-group', '858358554619285504.png', 1, '2024-08-23 13:03:25', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858359173056868352, 'note', NULL, 'application/octet-stream', 33, 'ckeditor', '858359172964593664', 1, '2024-08-23 13:05:52', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858375630050299904, '点研营业执照new', 'png', 'image/png', 2227837, 'link', '858375629681201152.png', 1, '2024-08-23 14:11:16', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858378161484730368, 'mov_bbb', 'mp4', 'video/mp4', 788493, 'link', '858378161467953152.mp4', 1, '2024-08-23 14:21:19', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858404071571238912, 'mov_bbb', 'mp4', 'video/mp4', 788493, 'link', '858404071520907264.mp4', 1, '2024-08-23 16:04:17', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893483237844717568, '1369-1', 'jpeg', 'image/jpeg', 78402, 'upload', '893483237785997312.jpeg', 1, '2024-11-28 11:16:22', 1, '2024-11-28 11:16:22', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893484203360915456, '1369-1', 'jpeg', 'image/jpeg', 78402, 'upload', '893484203331555328.jpeg', 1, '2024-11-28 11:20:12', 1, '2024-11-28 11:20:12', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893484216723968000, '865259019386826752', 'jpeg', 'image/jpeg', 78402, 'images', '893484216702996480.jpeg', 1, '2024-11-28 11:20:15', 1, '2024-11-28 11:20:15', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (906928521023086592, '采购讨论3', 'svg', 'image/svg+xml', 441964, 'upload', '906928520754651136.svg', 1, '2025-01-04 13:43:07', 1, '2025-01-04 13:43:07', 'f');
INSERT INTO "public"."sys_document" ("id", "name", "extension", "content_type", "size", "group_name", "path", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (906928534818152448, '1369-1', 'jpeg', 'image/jpeg', 78402, 'images', '906928534776209408.jpeg', 1, '2025-01-04 13:43:10', 1, '2025-01-04 13:43:10', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_form
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_form";
CREATE TABLE "public"."sys_form" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "form_advice_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "table_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "repository_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "storage_strategy" varchar(16) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tpl_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "additional_info" text COLLATE "pg_catalog"."default",
  "create_by" int8 NOT NULL,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8 NOT NULL,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool NOT NULL
)
;
ALTER TABLE "public"."sys_form" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_form
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_form" ("id", "code", "name", "form_advice_name", "table_name", "repository_name", "storage_strategy", "tpl_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (695312747063197696, 'sys_dict', '字典', 'dictFormService', 'sys_dict', 'dictDAO', 'CREATE_TABLE', 'tpl/form/form-full', '{"showSaveFormBtn":false}', 1, '2023-05-31 14:57:35', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form" ("id", "code", "name", "form_advice_name", "table_name", "repository_name", "storage_strategy", "tpl_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (859875429241106432, 'sys_user_form_tag', '用户信息-tag', 'userFormAdvice', 'sys_user', 'userDAO', 'CREATE_TABLE', 'demos/student/form-tag', '{"label-col":1}', 1, '2024-11-28 11:22:50', 1, '2025-04-24 09:46:01', 'f');
INSERT INTO "public"."sys_form" ("id", "code", "name", "form_advice_name", "table_name", "repository_name", "storage_strategy", "tpl_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694980924206493696, 'sys_user', '用户信息', 'userFormAdvice', 'sys_user', 'userDAO', 'CREATE_TABLE', 'tpl/form/form-full', '{"label-col":1}', 0, '2023-05-30 16:59:02', 1, '2025-06-12 20:20:17', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_form_configurer
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_form_configurer";
CREATE TABLE "public"."sys_form_configurer" (
  "id" int8 NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "label" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "validators" text COLLATE "pg_catalog"."default",
  "options" varchar(5000) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "data_source" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "default_value" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "placeholder" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "is_disabled" bool DEFAULT FALSE,
  "cpn_value_converter_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "additional_info" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_by" int8 NOT NULL,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8 NOT NULL,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool NOT NULL
)
;
ALTER TABLE "public"."sys_form_configurer" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_form_configurer
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895409610559488, 'type', '分类', 'SELECT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\/%\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', 'sys_dict_type', NULL, '请输入分类', 'f', NULL, NULL, 1, '2023-09-01 07:03:06', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895409639919616, 'name', '编码', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\/%\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入编码', 'f', NULL, NULL, 1, '2023-09-01 07:03:06', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895409639919617, 'label', '显示值', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入显示值', 'f', NULL, NULL, 1, '2023-09-01 07:03:06', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895409639919618, 'sort', '排序号', 'INTEGER_NUMBER', '[]', '[]', NULL, '0', NULL, 'f', NULL, NULL, 1, '2023-09-01 07:03:06', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895456838422528, 'code', '用户名', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\/%\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入用户名', 'f', NULL, '{"tab-index":"1"}', 1, '2023-09-01 07:03:17', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895456842616832, 'name', '姓名', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入姓名', 'f', NULL, '{"tab-index":"1"}', 1, '2023-09-01 07:03:17', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895456842616833, 'available', '可用', 'SWITCH', '[]', '[]', NULL, '1', NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2023-09-01 07:03:17', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895456842616834, 'roleIds', '角色', 'CHECKBOX', '[]', '[]', 'sys_role', NULL, NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2023-09-01 07:03:17', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893463693533122560, 'birthday', '出生日期', 'DATE', '[]', '[]', NULL, NULL, '请输入出生日期', 'f', NULL, '{"tab-index":"1"}', 1, '2024-11-28 09:58:42', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893463693533122561, 'attachment', '附件', 'FILE', '[]', '[]', NULL, NULL, NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2024-11-28 09:58:42', 1, '2025-06-19 15:11:54', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731519580336128, 'code', '用户名', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\/%\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入用户名', 'f', NULL, '{"tab-index":"1"}', 1, '2025-04-24 09:46:01', 1, '2025-04-24 09:46:01', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731519584530432, 'name', '姓名', 'TEXT', '[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]', '[]', NULL, NULL, '请输入姓名', 'f', NULL, '{"tab-index":"1"}', 1, '2025-04-24 09:46:01', 1, '2025-04-24 09:46:01', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731519584530433, 'available', '可用', 'SWITCH', '[]', '[]', NULL, '1', NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2025-04-24 09:46:01', 1, '2025-04-24 09:46:01', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731519584530434, 'roleIds', '角色', 'MULTIPLE_SELECT', '[]', '[]', 'sys_role', NULL, NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2025-04-24 09:46:01', 1, '2025-04-24 09:46:01', 'f');
INSERT INTO "public"."sys_form_configurer" ("id", "name", "label", "type", "validators", "options", "data_source", "default_value", "placeholder", "is_disabled", "cpn_value_converter_name", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731519584530435, 'attachment', '附件', 'FILE', '[]', '[]', NULL, NULL, NULL, 'f', NULL, '{"tab-index":"2"}', 1, '2025-04-24 09:46:01', 1, '2025-04-24 09:46:01', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_form_cpn_configurer
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_form_cpn_configurer";
CREATE TABLE "public"."sys_form_cpn_configurer" (
  "id" int8 NOT NULL,
  "form_id" int8 NOT NULL,
  "config_id" int8 NOT NULL,
  "order_num" int4,
  "additional_info" text COLLATE "pg_catalog"."default",
  "create_by" int8 NOT NULL,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8 NOT NULL,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool NOT NULL
)
;
ALTER TABLE "public"."sys_form_cpn_configurer" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_form_cpn_configurer
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895412680790016, 695312747063197696, 728895409610559488, 0, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895412680790017, 695312747063197696, 728895409639919616, 1, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895412680790018, 695312747063197696, 728895409639919617, 2, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895412680790019, 695312747063197696, 728895409639919618, 3, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895458704887808, 694980924206493696, 728895456838422528, 0, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895458704887809, 694980924206493696, 728895456842616832, 1, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895458704887810, 694980924206493696, 728895456842616833, 3, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (728895458704887811, 694980924206493696, 728895456842616834, 4, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893463693935775746, 694980924206493696, 893463693533122560, 2, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (893463693935775749, 694980924206493696, 893463693533122561, 5, NULL, 1, '2025-06-12 20:20:17', 1, '2025-06-12 20:20:17', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731520297562112, 859875429241106432, 946731519580336128, 0, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731520297562113, 859875429241106432, 946731519584530432, 1, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731520301756416, 859875429241106432, 946731519584530433, 2, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731520301756417, 859875429241106432, 946731519584530434, 3, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
INSERT INTO "public"."sys_form_cpn_configurer" ("id", "form_id", "config_id", "order_num", "additional_info", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731520301756418, 859875429241106432, 946731519584530435, 4, NULL, 1, '2025-04-24 09:46:02', 1, '2025-04-24 09:46:02', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_permission";
CREATE TABLE "public"."sys_permission" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "pid" int8,
  "permission_order" int4,
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_permission" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694587393189089280, 'dashboard', '仪表盘', 0, 0, NULL, '2023-05-29 14:55:17', 0, '2023-05-29 16:30:36', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694587393189089281, 'sys_management', '系统管理', 0, 9, 0, '2023-05-29 14:55:17', 0, '2023-05-29 14:55:17', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694587393189089283, 'role_management', '角色管理', 694587393189089281, 1, NULL, '2023-05-29 14:55:17', NULL, '2023-05-29 14:55:17', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696145672415481856, 'sys_user', '用户管理', 694587393189089281, 0, 1, '2023-06-02 22:07:20', 1, '2023-06-02 22:07:20', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696145672960741376, 'sys_user_read', '查看', 696145672415481856, 0, 1, '2023-06-02 22:07:20', 1, '2023-06-02 22:07:20', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696145673485029376, 'sys_user_add', '新增', 696145672415481856, 1, 1, '2023-06-02 22:07:20', 1, '2023-06-02 22:07:20', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696145674072231936, 'sys_user_edit', '编辑', 696145672415481856, 2, 1, '2023-06-02 22:07:20', 1, '2023-06-02 22:07:20', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696145674634268672, 'sys_user_delete', '删除', 696145672415481856, 3, 1, '2023-06-02 22:07:20', 1, '2023-06-02 22:07:20', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146982695079936, 'sys_dict', '字典管理', 694587393189089281, 2, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146982695079937, 'sys_dict-htmx-htmx', '字典管理-htmx', 694587393189089281, 2, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146982695079938, 'sys_dict-adminlte-htmx', '字典管理-adminlte', 694587393189089281, 2, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983320031232, 'sys_dict_read', '查看', 696146982695079936, 0, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983320031233, 'sys_dict-htmx_read', '查看', 696146982695079937, 0, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983320031234, 'sys_dict-adminlte_read', '查看', 696146982695079938, 0, 1, '2023-06-02 22:12:32', 1, '2023-06-02 22:12:32', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983924011008, 'sys_dict_add', '新增', 696146982695079936, 1, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983924011009, 'sys_dict-htmx_add', '新增', 696146982695079937, 1, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146983924011010, 'sys_dict-adminlte_add', '新增', 696146982695079938, 1, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146984569933824, 'sys_dict_edit', '编辑', 696146982695079936, 2, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146984569933825, 'sys_dict-htmx_edit', '编辑', 696146982695079937, 2, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146984569933826, 'sys_dict-adminlte_edit', '编辑', 696146982695079938, 2, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146985190690816, 'sys_dict_delete', '删除', 696146982695079936, 3, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146985190690817, 'sys_dict-htmx_delete', '删除', 696146982695079937, 3, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (696146985190690818, 'sys_dict-adminlte_delete', '删除', 696146982695079938, 3, 1, '2023-06-02 22:12:33', 1, '2023-06-02 22:12:33', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526249279488, 'manager', '管理', NULL, 0, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526370914304, 't_supplier', '供应商管理', 866065023858941952, 1, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526417051648, 't_supplier_read', '查看', 946731526370914304, 0, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526484160512, 't_supplier_add', '新增', 946731526370914304, 1, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526521909248, 't_supplier_edit', '编辑', 946731526370914304, 2, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."sys_permission" ("id", "code", "name", "pid", "permission_order", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (946731526555463680, 't_supplier_delete', '删除', 946731526370914304, 3, 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_property
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_property";
CREATE TABLE "public"."sys_property" (
  "name" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "value" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."sys_property" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_property
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_report
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_report";
CREATE TABLE "public"."sys_report" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "tpl_name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "pageable" bool,
  "sidx" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "sord" varchar(16) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "query_sql" text COLLATE "pg_catalog"."default",
  "summary" bool DEFAULT false,
  "report_column_list" text COLLATE "pg_catalog"."default",
  "summary_column_names" text COLLATE "pg_catalog"."default",
  "query_field_list" text COLLATE "pg_catalog"."default",
  "additional_info" text COLLATE "pg_catalog"."default",
  "report_advice_name" varchar(64) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_report" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_report
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_report" ("id", "code", "name", "tpl_name", "pageable", "sidx", "sord", "query_sql", "summary", "report_column_list", "summary_column_names", "query_field_list", "additional_info", "report_advice_name", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694714180413960192, 'sys_user', '用户管理', 'adminlte/list', 't', 'id', 'ASC', ' SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, 
   CASE WHEN sys_user.is_available THEN ''是'' ELSE ''否'' END as is_available, 
   t.name role_name, u.name create_name, TO_CHAR(sys_user.create_time, ''YYYY-MM-DD HH24:MI:SS'') as create_time 
   FROM sys_user
   LEFT JOIN sys_user u on u.id = sys_user.create_by
   LEFT JOIN (
     SELECT sys_user.id, string_agg(r.name, '','') AS name 
     FROM sys_user
     LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = FALSE
     LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = FALSE
     GROUP BY sys_user.id 
     ORDER BY sys_user.id ASC
   ) t on t.id = sys_user.id
   WHERE sys_user.code LIKE :code 
     AND sys_user.name LIKE :name 
     AND sys_user.is_available = :is_available 
     AND sys_user.create_time >= :create_time0 
     AND sys_user.create_time <= :create_time1 
     AND sys_user.is_deleted = FALSE', 'f', '[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"code","label":"用户名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"姓名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"birthday","label":"出生日期","sortable":true,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"role_name","label":"角色","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"is_available","label":"是否可用","sortable":false,"columnWidth":80,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"create_name","label":"创建人","sortable":false,"columnWidth":100,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"create_time","label":"创建时间","sortable":false,"columnWidth":120,"hidden":false,"align":"center","tooltip":false,"type":"text"}]', NULL, '[{"name":"code","label":"用户名","type":"TEXT"},{"name":"name","label":"姓名","type":"TEXT"},{"name":"is_available","label":"是否可用","type":"SELECT","extraData":"bol"},{"name":"create_time","label":"创建时间","type":"DATE_RANGE"}]', '{"formId":"694980924206493696","formAction":"drawer"}', 'userReportAdvice', 0, '2023-05-29 23:19:06', 1, '2025-06-12 20:20:32', 'f');
INSERT INTO "public"."sys_report" ("id", "code", "name", "tpl_name", "pageable", "sidx", "sord", "query_sql", "summary", "report_column_list", "summary_column_names", "query_field_list", "additional_info", "report_advice_name", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (695316160014499840, 'sys_dict', '字典管理', 'adminlte/list', 'f', 'id', 'ASC', 'select id, type, name, label, sort from sys_dict where type = :type and is_deleted = FALSE order by type, sort asc', 'f', '[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"编码","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"label","label":"显示值","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"type","label":"分类","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"sort","label":"排序号","sortable":false,"columnWidth":30,"hidden":false,"align":"left","tooltip":false,"type":"text"}]', NULL, '[{"name":"type","label":"分类","type":"SELECT","extraData":"sys_dict_type"}]', '{"formId":"695312747063197696","formAction":"drawer"}', NULL, 1, '2023-05-31 15:11:09', 1, '2025-06-13 09:18:44', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role";
CREATE TABLE "public"."sys_role" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "create_by" int8,
  "create_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "update_by" int8,
  "update_time" timestamp(0) DEFAULT NULL::timestamp without time zone,
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_role" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_role" ("id", "code", "name", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (694587732420202496, 'ADMIN', '管理员', 1, '2023-05-29 14:56:38', 1, '2025-06-30 18:49:42', 'f');
INSERT INTO "public"."sys_role" ("id", "code", "name", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971120333606133760, 'FSDTz', 'TEST', 1, '2025-06-30 16:58:28', 1, '2025-06-30 18:49:42', 'f');
INSERT INTO "public"."sys_role" ("id", "code", "name", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (971148330165903360, 'yeedW', 'TEST2', 1, '2025-06-30 18:49:43', 1, '2025-06-30 18:49:43', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role_permission";
CREATE TABLE "public"."sys_role_permission" (
  "role_id" int8 NOT NULL,
  "permission_id" int8 NOT NULL,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
ALTER TABLE "public"."sys_role_permission" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526249279488, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 694587393189089280, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526370914304, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526417051648, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526484160512, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526521909248, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (971120333606133760, 946731526555463680, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526249279488, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 694587393189089280, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526370914304, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526417051648, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526484160512, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526521909248, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 946731526555463680, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 694587393189089281, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696145672415481856, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696145672960741376, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696145673485029376, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696145674072231936, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696145674634268672, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 694587393189089283, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146982695079938, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983320031234, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983924011010, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146984569933826, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146985190690818, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146982695079937, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983320031233, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983924011009, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146984569933825, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146985190690817, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146982695079936, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983320031232, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146983924011008, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146984569933824, 'f');
INSERT INTO "public"."sys_role_permission" ("role_id", "permission_id", "is_deleted") VALUES (694587732420202496, 696146985190690816, 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user";
CREATE TABLE "public"."sys_user" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default",
  "password" varchar(128) COLLATE "pg_catalog"."default",
  "birthday" date,
  "is_available" bool,
  "attachment" text COLLATE "pg_catalog"."default",
  "create_by" int8,
  "create_time" timestamp(0),
  "update_by" int8,
  "update_time" timestamp(0),
  "is_deleted" bool
)
;
ALTER TABLE "public"."sys_user" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_user" ("id", "code", "name", "password", "birthday", "is_available", "attachment", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (1, 'ADMIN', '管理员', '$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS', '2024-11-28', 't', '[]', 1, '2023-05-29 14:55:03', 1, '2024-12-10 19:37:01', 'f');
INSERT INTO "public"."sys_user" ("id", "code", "name", "password", "birthday", "is_available", "attachment", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (786079661661646848, 'test', '测试', '$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS', '2025-06-30', 't', '[]', 1, '2023-05-29 14:55:03', 1, '2025-07-01 14:27:58', 'f');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_role";
CREATE TABLE "public"."sys_user_role" (
  "user_id" int8 NOT NULL,
  "role_id" int8 NOT NULL,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
ALTER TABLE "public"."sys_user_role" OWNER TO "postgres";

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO "public"."sys_user_role" ("user_id", "role_id", "is_deleted") VALUES (1, 694587732420202496, 'f');
INSERT INTO "public"."sys_user_role" ("user_id", "role_id", "is_deleted") VALUES (786079661661646848, 971120333606133760, 'f');
INSERT INTO "public"."sys_user_role" ("user_id", "role_id", "is_deleted") VALUES (786079661661646848, 971148330165903360, 'f');
COMMIT;

-- ----------------------------
-- Table structure for t_complex_model
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_complex_model";
CREATE TABLE "public"."t_complex_model" (
  "id" int8 NOT NULL,
  "name" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "material_type_code" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "unit_code" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "category" text COLLATE "pg_catalog"."default",
  "work_status" int4,
  "category_code" varchar(32) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "age" int4 NOT NULL,
  "birthday" date,
  "category_list" json NOT NULL,
  "category_dict_list" json NOT NULL,
  "marriage" bool NOT NULL,
  "attachment" text COLLATE "pg_catalog"."default",
  "school_experience" text COLLATE "pg_catalog"."default",
  "map" text COLLATE "pg_catalog"."default",
  "embedded_value" json,
  "create_by" int8,
  "create_time" timestamp(6),
  "update_by" int8,
  "update_time" timestamp(6),
  "is_deleted" bool
)
;
ALTER TABLE "public"."t_complex_model" OWNER TO "postgres";

-- ----------------------------
-- Records of t_complex_model
-- ----------------------------
BEGIN;
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (856759492044419072, 'Rick2', 'HIBE', 'EA', 'MATERIAL', 1, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "SALES_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "texg", "dictValue": {"code": "HIBE"}}', 1, '2025-04-24 09:46:03', 1, '2025-04-24 09:46:03', 'f');
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (856958035153653760, 'Rick.Xu', 'HIBE', 'EA', 'MATERIAL', 0, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}', 1, '2024-08-19 16:18:15', 1, '2024-08-19 16:18:15', 'f');
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (856958362212896768, 'Rick.Xu', 'HIBE', 'EA', 'MATERIAL', 0, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}', 1, '2024-08-19 16:19:33', 1, '2024-08-19 16:19:33', 'f');
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858263799545769984, 'Rick.Xu', 'HIBE', 'EA', 'MATERIAL', 0, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}', 1, '2024-08-23 06:46:53', 1, '2024-08-23 06:46:53', 'f');
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858272781039611904, 'Rick.Xu', 'HIBE', 'EA', 'MATERIAL', 0, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}', 1, '2024-08-23 07:22:35', 1, '2024-08-23 07:22:35', 'f');
INSERT INTO "public"."t_complex_model" ("id", "name", "material_type_code", "unit_code", "category", "work_status", "category_code", "age", "birthday", "category_list", "category_dict_list", "marriage", "attachment", "school_experience", "map", "embedded_value", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (858305555469479936, 'Rick.Xu', 'HIBE', 'EA', 'MATERIAL', 0, 'SALES_ORG', 34, '2021-12-26', '["MATERIAL", "PURCHASING_ORG"]', '[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]', 't', '[{"name":"picture","url":"baidu.com"}]', '[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]', '{"name":"picture","url":"baidu.com"}', '{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}', 1, '2024-08-23 09:32:49', 1, '2024-08-23 09:32:49', 'f');
COMMIT;

-- ----------------------------
-- Table structure for t_student
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_student";
CREATE TABLE "public"."t_student" (
  "id" int8 NOT NULL,
  "code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(16) COLLATE "pg_catalog"."default" NOT NULL,
  "gender" "public"."gender_enum",
  "email" varchar(32) COLLATE "pg_catalog"."default",
  "birthday" date,
  "age" int4,
  "is_marriage" bool DEFAULT false,
  "unit_code" varchar(32) COLLATE "pg_catalog"."default",
  "attachments" json,
  "avatar" json,
  "hobby_list" json,
  "material_type" json,
  "category" "public"."category_enum",
  "is_available" bool DEFAULT false,
  "remark" varchar(32) COLLATE "pg_catalog"."default",
  "user_id" int8,
  "user_code" varchar(32) COLLATE "pg_catalog"."default",
  "create_by" int8,
  "create_time" timestamp(6),
  "update_by" int8,
  "update_time" timestamp(6),
  "is_deleted" bool DEFAULT false
)
;
ALTER TABLE "public"."t_student" OWNER TO "postgres";

-- ----------------------------
-- Records of t_student
-- ----------------------------
BEGIN;
INSERT INTO "public"."t_student" ("id", "code", "name", "gender", "email", "birthday", "age", "is_marriage", "unit_code", "attachments", "avatar", "hobby_list", "material_type", "category", "is_available", "remark", "user_id", "user_code", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (2, '0001', '张三', 'M', 'fsadfsaf@163.com', '1992-11-12', 19, 't', 'EA', '[{"id": "861948954584059904", "url": "http://localhost:7892/attachments/861948954575671296.xls", "name": "报关单", "path": "861948954575671296.xls", "size": 65536, "fullName": "报关单.xls", "fullPath": "attachments/861948954575671296.xls", "extension": "xls", "groupName": "attachments", "contentType": "application/vnd.ms-excel"}, {"id": "861948954584059905", "url": "http://localhost:7892/attachments/861948954575671297.zip", "name": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode", "path": "861948954575671297.zip", "size": 408174, "fullName": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip", "fullPath": "attachments/861948954575671297.zip", "extension": "zip", "groupName": "attachments", "contentType": "application/zip"}]', '{"id": "861952755730780160", "url": "http://localhost:7892/images/861952755722391552.jpeg", "name": "avatar", "path": "861952755722391552.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "images/861952755722391552.jpeg", "extension": "jpeg", "groupName": "images", "contentType": "image/jpeg"}', '["FOOTBALL", "BASKETBALL"]', '[{"code": "M1"}]', 'MATERIAL', 'f', 'fsdfdasf', NULL, NULL, 1, '2024-08-24 22:57:24', 1, '2025-01-04 13:43:21', 'f');
INSERT INTO "public"."t_student" ("id", "code", "name", "gender", "email", "birthday", "age", "is_marriage", "unit_code", "attachments", "avatar", "hobby_list", "material_type", "category", "is_available", "remark", "user_id", "user_code", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (861949024788320256, '00012', '库房', 'M', '1050216579@qq.com', '2024-08-28', 2, 't', 'EA', '[{"id": "861948954584059904", "url": "http://localhost:7892/attachments/861948954575671296.xls", "name": "报关单", "path": "861948954575671296.xls", "size": 65536, "fullName": "报关单.xls", "fullPath": "attachments/861948954575671296.xls", "extension": "xls", "groupName": "attachments", "contentType": "application/vnd.ms-excel"}, {"id": "861948954584059905", "url": "http://localhost:7892/attachments/861948954575671297.zip", "name": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode", "path": "861948954575671297.zip", "size": 408174, "fullName": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip", "fullPath": "attachments/861948954575671297.zip", "extension": "zip", "groupName": "attachments", "contentType": "application/zip"}]', '{"id": "861952755730780160", "url": "http://localhost:7892/images/861952755722391552.jpeg", "name": "avatar", "path": "861952755722391552.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "images/861952755722391552.jpeg", "extension": "jpeg", "groupName": "images", "contentType": "image/jpeg"}', '["BASKETBALL", "FOOTBALL"]', '[{"code": "M1"}, {"code": "M4"}]', 'PURCHASING_ORG', 't', '这里是简介', 786079661661646848, 'test', 1, '2024-09-02 10:50:40', 1, '2025-01-11 13:02:19', 'f');
INSERT INTO "public"."t_student" ("id", "code", "name", "gender", "email", "birthday", "age", "is_marriage", "unit_code", "attachments", "avatar", "hobby_list", "material_type", "category", "is_available", "remark", "user_id", "user_code", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (861953021922283520, '111', '李岁2', 'F', 'fsadfsaf@163.com', '2024-09-02', 23, 'f', 'KG', '[{"id": "861952982621655040", "url": "http://localhost:7892/attachments/861952982609072128.jpeg", "name": "avatar", "path": "861952982609072128.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "attachments/861952982609072128.jpeg", "extension": "jpeg", "groupName": "attachments", "contentType": "image/jpeg"}, {"id": "861952982625849344", "url": "http://localhost:7892/attachments/861952982613266432.csv", "name": "Google Passwords", "path": "861952982613266432.csv", "size": 13191, "fullName": "Google Passwords.csv", "fullPath": "attachments/861952982613266432.csv", "extension": "csv", "groupName": "attachments", "contentType": "text/csv"}, {"id": "861967956609896448", "url": "http://localhost:7892/attachments/861967956597313536.pdf", "name": "线上VI指南", "path": "861967956597313536.pdf", "size": 463981, "fullName": "线上VI指南.pdf", "fullPath": "attachments/861967956597313536.pdf", "extension": "pdf", "groupName": "attachments", "contentType": "application/pdf"}]', '{"id": "861976822831681536", "url": "http://localhost:7892/images/861976822642937856.webp", "name": "F5V8WS3asAAFJsC", "path": "861976822642937856.webp", "size": 81884, "fullName": "F5V8WS3asAAFJsC.webp", "fullPath": "images/861976822642937856.webp", "extension": "webp", "groupName": "images", "contentType": "image/webp"}', '["BASKETBALL", "FOOTBALL"]', '[{"code": "M1"}, {"code": "M3"}, {"code": "M4"}]', 'SALES_ORG', 'f', 'hello world', 1, 'admin', 1, '2024-09-02 11:06:33', 1, '2025-01-06 17:52:58', 'f');
INSERT INTO "public"."t_student" ("id", "code", "name", "gender", "email", "birthday", "age", "is_marriage", "unit_code", "attachments", "avatar", "hobby_list", "material_type", "category", "is_available", "remark", "user_id", "user_code", "create_by", "create_time", "update_by", "update_time", "is_deleted") VALUES (906928548739047424, '00035', 'TEST', 'M', '10502165791@qq.com', '2025-01-01', 90, 't', 'EA', '[{"id": "906928521023086592", "url": "http://localhost:7892/upload/906928520754651136.svg", "name": "采购讨论3", "path": "906928520754651136.svg", "size": 441964, "fullName": "采购讨论3.svg", "fullPath": "upload/906928520754651136.svg", "extension": "svg", "groupName": "upload", "contentType": "image/svg+xml"}]', '{"id": "906928534818152448", "url": "http://localhost:7892/images/906928534776209408.jpeg", "name": "1369-1", "path": "906928534776209408.jpeg", "size": 78402, "fullName": "1369-1.jpeg", "fullPath": "images/906928534776209408.jpeg", "extension": "jpeg", "groupName": "images", "contentType": "image/jpeg"}', '["FOOTBALL"]', '[{"code": "M1"}]', 'MATERIAL', 't', '00', 1, 'admin', 1, '2025-01-04 13:43:14', 1, '2025-01-11 13:02:58', 'f');
COMMIT;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_access_info_id_seq"
OWNED BY "public"."sys_access_info"."id";
SELECT setval('"public"."sys_access_info_id_seq"', 369, true);

-- ----------------------------
-- Primary Key structure for table core_code_sequence
-- ----------------------------
ALTER TABLE "public"."core_code_sequence" ADD CONSTRAINT "core_code_sequence_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table proveg_team
-- ----------------------------
ALTER TABLE "public"."proveg_team" ADD CONSTRAINT "proveg_team_code_unique" UNIQUE ("code");

-- ----------------------------
-- Checks structure for table proveg_team
-- ----------------------------
ALTER TABLE "public"."proveg_team" ADD CONSTRAINT "proveg_team_status_check" CHECK (status::text = ANY (ARRAY['PROCESSING'::character varying, 'SUBMITTED'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table proveg_team
-- ----------------------------
ALTER TABLE "public"."proveg_team" ADD CONSTRAINT "proveg_team_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Auto increment value for sys_access_info
-- ----------------------------
SELECT setval('"public"."sys_access_info_id_seq"', 369, true);

-- ----------------------------
-- Checks structure for table sys_access_info
-- ----------------------------
ALTER TABLE "public"."sys_access_info" ADD CONSTRAINT "sys_access_info_id_check" CHECK (id > 0);

-- ----------------------------
-- Primary Key structure for table sys_access_info
-- ----------------------------
ALTER TABLE "public"."sys_access_info" ADD CONSTRAINT "sys_access_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table sys_code_description
-- ----------------------------
ALTER TABLE "public"."sys_code_description" ADD CONSTRAINT "sys_code_description_category_check" CHECK (category::text = ANY (ARRAY['MATERIAL'::character varying, 'PURCHASING_ORG'::character varying, 'PACKAGING'::character varying, 'SALES_ORG'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table sys_code_description
-- ----------------------------
ALTER TABLE "public"."sys_code_description" ADD CONSTRAINT "sys_code_description_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_dict
-- ----------------------------
ALTER TABLE "public"."sys_dict" ADD CONSTRAINT "sys_dict_pkey" PRIMARY KEY ("type", "name");

-- ----------------------------
-- Primary Key structure for table sys_document
-- ----------------------------
ALTER TABLE "public"."sys_document" ADD CONSTRAINT "sys_document_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_form
-- ----------------------------
ALTER TABLE "public"."sys_form" ADD CONSTRAINT "sys_form_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_form_configurer
-- ----------------------------
ALTER TABLE "public"."sys_form_configurer" ADD CONSTRAINT "sys_form_configurer_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_form_cpn_configurer
-- ----------------------------
ALTER TABLE "public"."sys_form_cpn_configurer" ADD CONSTRAINT "sys_form_cpn_configurer_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_permission
-- ----------------------------
ALTER TABLE "public"."sys_permission" ADD CONSTRAINT "sys_permission_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_property
-- ----------------------------
ALTER TABLE "public"."sys_property" ADD CONSTRAINT "sys_property_pkey" PRIMARY KEY ("name");

-- ----------------------------
-- Primary Key structure for table sys_report
-- ----------------------------
ALTER TABLE "public"."sys_report" ADD CONSTRAINT "sys_report_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "public"."sys_role" ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table sys_role_permission
-- ----------------------------
ALTER TABLE "public"."sys_role_permission" ADD CONSTRAINT "sys_role_permission_pk" UNIQUE ("role_id", "permission_id");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table sys_user_role
-- ----------------------------
ALTER TABLE "public"."sys_user_role" ADD CONSTRAINT "sys_user_role_pk" UNIQUE ("user_id", "role_id");

-- ----------------------------
-- Checks structure for table t_complex_model
-- ----------------------------
ALTER TABLE "public"."t_complex_model" ADD CONSTRAINT "t_complex_model_category_check" CHECK (category = ANY (ARRAY['MATERIAL'::text, 'PURCHASING_ORG'::text, 'PACKAGING'::text, 'SALES_ORG'::text]));

-- ----------------------------
-- Primary Key structure for table t_complex_model
-- ----------------------------
ALTER TABLE "public"."t_complex_model" ADD CONSTRAINT "t_complex_model_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_student
-- ----------------------------
ALTER TABLE "public"."t_student" ADD CONSTRAINT "t_student_pkey" PRIMARY KEY ("id");
