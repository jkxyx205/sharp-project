--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.5

-- Started on 2025-11-12 17:56:57 CST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 24711)
-- Name: sys_access_info; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_access_info (
    id integer NOT NULL,
    content text,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    CONSTRAINT sys_access_info_id_check CHECK ((id > 0))
);


ALTER TABLE public.sys_access_info OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 24699)
-- Name: sys_access_info_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_access_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;


ALTER SEQUENCE public.sys_access_info_id_seq OWNER TO postgres;

--
-- TOC entry 3532 (class 0 OID 0)
-- Dependencies: 217
-- Name: sys_access_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_access_info_id_seq OWNED BY public.sys_access_info.id;


--
-- TOC entry 218 (class 1259 OID 24710)
-- Name: sys_access_info_id_seq1; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.sys_access_info ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.sys_access_info_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 220 (class 1259 OID 24717)
-- Name: sys_code_description; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_code_description (
    id bigint NOT NULL,
    code character varying(32) NOT NULL,
    description character varying(32) DEFAULT NULL::character varying,
    category character varying(30) DEFAULT NULL::character varying,
    sort integer,
    create_by bigint,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean,
    CONSTRAINT sys_code_description_category_check CHECK (((category)::text = ANY (ARRAY[('MATERIAL'::character varying)::text, ('PURCHASING_ORG'::character varying)::text, ('PACKAGING'::character varying)::text, ('SALES_ORG'::character varying)::text])))
);


ALTER TABLE public.sys_code_description OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 24724)
-- Name: sys_dict; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dict (
    id bigint,
    type character varying(32) NOT NULL,
    name character varying(32) NOT NULL,
    label character varying(32) NOT NULL,
    sort integer,
    remark character varying(32) DEFAULT NULL::character varying,
    create_by bigint,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sys_dict OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 24731)
-- Name: sys_document; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_document (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    extension character varying(16) DEFAULT NULL::character varying,
    content_type character varying(128) DEFAULT NULL::character varying,
    size integer,
    group_name character varying(255) NOT NULL,
    path character varying(255) NOT NULL,
    create_by bigint,
    create_time timestamp(0) without time zone NOT NULL,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean
);


ALTER TABLE public.sys_document OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 24739)
-- Name: sys_form; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_form (
    id bigint NOT NULL,
    code character varying(32) DEFAULT NULL::character varying,
    name character varying(32) NOT NULL,
    form_advice_name character varying(32) DEFAULT NULL::character varying,
    table_name character varying(32) DEFAULT NULL::character varying,
    repository_name character varying(32) DEFAULT NULL::character varying,
    storage_strategy character varying(16) DEFAULT NULL::character varying,
    tpl_name character varying(32) DEFAULT NULL::character varying,
    additional_info text,
    create_by bigint NOT NULL,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint NOT NULL,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean NOT NULL
);


ALTER TABLE public.sys_form OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 24752)
-- Name: sys_form_configurer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_form_configurer (
    id bigint NOT NULL,
    name character varying(32) DEFAULT NULL::character varying,
    label character varying(16) NOT NULL,
    type character varying(32) NOT NULL,
    validators text,
    options character varying(5000) DEFAULT NULL::character varying,
    data_source character varying(32) DEFAULT NULL::character varying,
    default_value character varying(64) DEFAULT NULL::character varying,
    placeholder character varying(32) DEFAULT NULL::character varying,
    is_disabled boolean DEFAULT false,
    cpn_value_converter_name character varying(32) DEFAULT NULL::character varying,
    additional_info character varying(1024) DEFAULT NULL::character varying,
    create_by bigint NOT NULL,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint NOT NULL,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean NOT NULL
);


ALTER TABLE public.sys_form_configurer OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 24767)
-- Name: sys_form_cpn_configurer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_form_cpn_configurer (
    id bigint NOT NULL,
    form_id bigint NOT NULL,
    config_id bigint NOT NULL,
    order_num integer,
    additional_info text,
    create_by bigint NOT NULL,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint NOT NULL,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean NOT NULL
);


ALTER TABLE public.sys_form_cpn_configurer OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 24774)
-- Name: sys_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_permission (
    id bigint NOT NULL,
    code character varying(32) NOT NULL,
    name character varying(32) DEFAULT NULL::character varying,
    pid bigint,
    permission_order integer,
    create_by bigint,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean
);


ALTER TABLE public.sys_permission OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 24780)
-- Name: sys_property; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_property (
    name character varying(32) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.sys_property OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 24783)
-- Name: sys_report; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_report (
    id bigint NOT NULL,
    code character varying(32) DEFAULT NULL::character varying,
    name character varying(32) DEFAULT NULL::character varying,
    tpl_name character varying(32) DEFAULT NULL::character varying,
    pageable boolean,
    sidx character varying(32) DEFAULT NULL::character varying,
    sord character varying(16) DEFAULT NULL::character varying,
    query_sql text,
    summary boolean DEFAULT false,
    report_column_list text,
    summary_column_names text,
    query_field_list text,
    additional_info text,
    report_advice_name character varying(64) DEFAULT NULL::character varying,
    create_by bigint,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean
);


ALTER TABLE public.sys_report OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 24797)
-- Name: sys_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_role (
    id bigint NOT NULL,
    code character varying(32) NOT NULL,
    name character varying(32) DEFAULT NULL::character varying,
    create_by bigint,
    create_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone DEFAULT NULL::timestamp without time zone,
    is_deleted boolean
);


ALTER TABLE public.sys_role OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 24803)
-- Name: sys_role_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_role_permission (
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sys_role_permission OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 24807)
-- Name: sys_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_user (
    id bigint NOT NULL,
    code character varying(32) NOT NULL,
    name character varying(32),
    password character varying(128),
    birthday date,
    is_available boolean,
    attachment text,
    create_by bigint,
    create_time timestamp(0) without time zone,
    update_by bigint,
    update_time timestamp(0) without time zone,
    is_deleted boolean
);


ALTER TABLE public.sys_user OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 24812)
-- Name: sys_user_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sys_user_role OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 24816)
-- Name: t_complex_model; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.t_complex_model (
    id bigint NOT NULL,
    name character varying(32) DEFAULT NULL::character varying,
    material_type_code character varying(32) DEFAULT NULL::character varying,
    unit_code character varying(32) DEFAULT NULL::character varying,
    category text,
    work_status integer,
    category_code character varying(32) DEFAULT NULL::character varying,
    age integer NOT NULL,
    birthday date,
    category_list json NOT NULL,
    category_dict_list json NOT NULL,
    marriage boolean NOT NULL,
    attachment text,
    school_experience text,
    map text,
    embedded_value json,
    create_by bigint,
    create_time timestamp(6) without time zone,
    update_by bigint,
    update_time timestamp(6) without time zone,
    is_deleted boolean,
    CONSTRAINT t_complex_model_category_check CHECK ((category = ANY (ARRAY['MATERIAL'::text, 'PURCHASING_ORG'::text, 'PACKAGING'::text, 'SALES_ORG'::text])))
);


ALTER TABLE public.t_complex_model OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 24909)
-- Name: t_student; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.t_student (
    id bigint NOT NULL,
    code character varying(32) NOT NULL,
    name character varying(16) NOT NULL,
    gender character varying(1),
    email character varying(32),
    birthday date,
    age integer,
    is_marriage boolean DEFAULT false,
    unit_code character varying(32),
    attachments json,
    avatar json,
    hobby_list json,
    material_type json,
    category character varying(32),
    is_available boolean DEFAULT false,
    remark character varying(32),
    user_id bigint,
    user_code character varying(32),
    create_by bigint,
    create_time timestamp without time zone,
    update_by bigint,
    update_time timestamp without time zone,
    is_deleted boolean DEFAULT false,
    CONSTRAINT chk_category CHECK (((category)::text = ANY ((ARRAY['SALES_ORG'::character varying, 'PACKAGING'::character varying, 'PURCHASING_ORG'::character varying, 'MATERIAL'::character varying])::text[]))),
    CONSTRAINT chk_gender CHECK (((gender)::text = ANY ((ARRAY['M'::character varying, 'F'::character varying])::text[])))
);


ALTER TABLE public.t_student OWNER TO postgres;

--
-- TOC entry 3511 (class 0 OID 24711)
-- Dependencies: 219
-- Data for Name: sys_access_info; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_access_info (id, content, create_time) FROM stdin;
\.


--
-- TOC entry 3512 (class 0 OID 24717)
-- Dependencies: 220
-- Data for Name: sys_code_description; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_code_description (id, code, description, category, sort, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
946731527121694720	PG1	采购组织1	PURCHASING_ORG	0	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731527121694721	M1	采购组织2	PURCHASING_ORG	1	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731527209775104	M1	物料组1	MATERIAL	0	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731527209775105	M3	物料组3	MATERIAL	1	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731527209775106	M4	物料组4	MATERIAL	2	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
\.


--
-- TOC entry 3513 (class 0 OID 24724)
-- Dependencies: 221
-- Data for Name: sys_dict; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_dict (id, type, name, label, sort, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
965742343984533504	MATERIAL	1111	xx000	0	\N	1	2025-06-15 20:48:15	1	2025-06-16 17:15:32	t
966407499286597632	MATERIAL	23232	23322332	0	\N	1	2025-06-17 16:51:21	1	2025-06-18 06:35:58	f
966136233518452736	MATERIAL	2323232	2323	0	\N	1	2025-06-16 22:53:26	1	2025-06-16 22:53:26	f
966407543490367488	MATERIAL	2332	233232	2	\N	1	2025-06-17 16:51:31	1	2025-06-18 06:56:03	f
4	MATERIAL	M2	物料2	1	\N	1	2024-11-28 11:16:22	1	2025-06-18 06:55:58	f
5	MATERIAL	M3	物料3	2	\N	1	2024-11-28 11:16:22	1	2025-06-18 06:36:39	f
6	MATERIAL	M4	物料4	3	\N	1	2024-11-28 11:16:22	1	2024-11-28 11:16:22	f
966407581775974400	MATERIAL_TYPE	23322	23323	0	\N	1	2025-06-17 16:51:40	1	2025-06-17 16:51:40	f
965717198205112320	MATERIAL_TYPE	FEAT	产品	9	\N	1	2025-06-15 19:08:20	1	2025-06-15 19:08:20	f
695367653333712896	MATERIAL_TYPE	HIBE	耗材用品	8	\N	1	2024-11-28 11:16:22	1	2025-06-18 06:35:55	f
965742162547331072	MATERIAL_TYPE	UAT	SUATSUAT	92	\N	1	2025-06-15 20:47:32	1	2025-06-18 06:36:09	f
862375391526948864	UNIT	BAG	包	9	\N	1	2024-11-28 11:16:22	1	2025-06-18 08:18:34	f
2	UNIT	KG	千克	1	\N	1	2024-11-28 11:16:22	1	2025-06-16 14:11:53	f
965722206296363008	UNIT	KGG	拆	0	\N	1	2025-06-15 19:28:14	1	2025-06-17 15:44:02	f
966005952312528896	UNIT	tc	tctctc	0	\N	1	2025-06-16 14:15:44	1	2025-06-16 17:15:29	t
3	MATERIAL	M1	2222	0	\N	1	2024-11-28 11:16:22	1	2025-11-12 11:38:49	f
965718041910333440	MATERIAL	232323	擦2	0	\N	1	2025-06-15 19:11:41	1	2025-11-12 11:39:02	f
1	UNIT	EA	个	0	\N	1	2024-11-28 11:16:22	1	2025-11-12 12:02:00	f
\.


--
-- TOC entry 3514 (class 0 OID 24731)
-- Dependencies: 222
-- Data for Name: sys_document; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_document (id, name, extension, content_type, size, group_name, path, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
858097155292835840	点研营业执照new	png	image/png	2227837	ckeditor	858097155238309888.png	1	2024-08-22 19:44:42	1	2024-11-28 11:16:22	f
858097295894294528	点研营业执照new	png	image/png	2227837	ckeditor	858097295885905920.png	1	2024-08-22 19:45:16	1	2024-11-28 11:16:22	f
858097296171118593	note	\N	application/octet-stream	33	ckeditor	858097296171118592	1	2024-08-22 19:45:16	1	2024-11-28 11:16:22	f
858142447786569728	点研营业执照new	png	image/png	2227837	ckeditor	858142447694295040.png	1	2024-08-22 22:44:41	1	2024-11-28 11:16:22	f
858143002550382592	糖尿病肾病 试运行	docx	application/vnd.openxmlformats-officedocument.wordprocessingml.document	13535	ckeditor	858143002420359168.docx	1	2024-08-22 22:46:53	1	2024-11-28 11:16:22	f
858143015586279424	点研营业执照new	png	image/png	2227837	ckeditor	858143015569502208.png	1	2024-08-22 22:46:56	1	2024-11-28 11:16:22	f
858143018480349184	note	\N	application/octet-stream	33	ckeditor	858143018471960576	1	2024-08-22 22:46:57	1	2024-11-28 11:16:22	f
858143021252784128	投资任职情况查询报告	pdf	application/pdf	35345	ckeditor	858143021236006912.pdf	1	2024-08-22 22:46:58	1	2024-11-28 11:16:22	f
858358554791251968	图片	png	image/png	613008	customize-group	858358554619285504.png	1	2024-08-23 13:03:25	1	2024-11-28 11:16:22	f
858359173056868352	note	\N	application/octet-stream	33	ckeditor	858359172964593664	1	2024-08-23 13:05:52	1	2024-11-28 11:16:22	f
858375630050299904	点研营业执照new	png	image/png	2227837	link	858375629681201152.png	1	2024-08-23 14:11:16	1	2024-11-28 11:16:22	f
858378161484730368	mov_bbb	mp4	video/mp4	788493	link	858378161467953152.mp4	1	2024-08-23 14:21:19	1	2024-11-28 11:16:22	f
858404071571238912	mov_bbb	mp4	video/mp4	788493	link	858404071520907264.mp4	1	2024-08-23 16:04:17	1	2024-11-28 11:16:22	f
893483237844717568	1369-1	jpeg	image/jpeg	78402	upload	893483237785997312.jpeg	1	2024-11-28 11:16:22	1	2024-11-28 11:16:22	f
893484203360915456	1369-1	jpeg	image/jpeg	78402	upload	893484203331555328.jpeg	1	2024-11-28 11:20:12	1	2024-11-28 11:20:12	f
893484216723968000	865259019386826752	jpeg	image/jpeg	78402	images	893484216702996480.jpeg	1	2024-11-28 11:20:15	1	2024-11-28 11:20:15	f
906928521023086592	采购讨论3	svg	image/svg+xml	441964	upload	906928520754651136.svg	1	2025-01-04 13:43:07	1	2025-01-04 13:43:07	f
906928534818152448	1369-1	jpeg	image/jpeg	78402	images	906928534776209408.jpeg	1	2025-01-04 13:43:10	1	2025-01-04 13:43:10	f
1020041940458033152	default_avatar	png	image/png	4937	upload	1020041939996659712.png	1	2025-11-12 16:55:28	1	2025-11-12 16:55:28	f
1020047462330552320	default_avatar	png	image/png	4937	upload	1020047461781098496.png	1	2025-11-12 17:17:24	1	2025-11-12 17:17:24	f
1020049269270269952	default_avatar	png	image/png	4937	upload	1020049269224132608.png	1	2025-11-12 17:24:35	1	2025-11-12 17:24:35	f
1020049529069654016	default_avatar	png	image/png	4937	upload	1020049528541171712.png	1	2025-11-12 17:25:37	1	2025-11-12 17:25:37	f
1020050651729645568	default_avatar	png	image/png	4937	upload	1020050635141173248.png	1	2025-11-12 17:30:05	1	2025-11-12 17:30:05	f
1020050674383081472	default_avatar	png	image/png	4937	images	1020050674324361216.png	1	2025-11-12 17:30:10	1	2025-11-12 17:30:10	f
\.


--
-- TOC entry 3515 (class 0 OID 24739)
-- Dependencies: 223
-- Data for Name: sys_form; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_form (id, code, name, form_advice_name, table_name, repository_name, storage_strategy, tpl_name, additional_info, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
695312747063197696	sys_dict	字典	dictFormService	sys_dict	dictDAO	CREATE_TABLE	tpl/form/form-full	{"showSaveFormBtn":false}	1	2023-05-31 14:57:35	1	2025-04-24 09:46:02	f
859875429241106432	sys_user_form_tag	用户信息-tag	userFormAdvice	sys_user	userDAO	CREATE_TABLE	demos/student/form-tag	{"label-col":1}	1	2024-11-28 11:22:50	1	2025-04-24 09:46:01	f
694980924206493696	sys_user	用户信息	userFormAdvice	sys_user	userDAO	CREATE_TABLE	tpl/form/form	{"label-col":1}	0	2023-05-30 16:59:02	1	2025-06-12 20:20:17	f
\.


--
-- TOC entry 3516 (class 0 OID 24752)
-- Dependencies: 224
-- Data for Name: sys_form_configurer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_form_configurer (id, name, label, type, validators, options, data_source, default_value, placeholder, is_disabled, cpn_value_converter_name, additional_info, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
728895409610559488	type	分类	SELECT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	sys_dict_type	\N	请输入分类	f	\N	\N	1	2023-09-01 07:03:06	1	2025-04-24 09:46:02	f
728895409639919616	name	编码	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入编码	f	\N	\N	1	2023-09-01 07:03:06	1	2025-04-24 09:46:02	f
728895409639919617	label	显示值	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入显示值	f	\N	\N	1	2023-09-01 07:03:06	1	2025-04-24 09:46:02	f
728895409639919618	sort	排序号	INTEGER_NUMBER	[]	[]	\N	0	\N	f	\N	\N	1	2023-09-01 07:03:06	1	2025-04-24 09:46:02	f
728895456838422528	code	用户名	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入用户名	f	\N	{"tab-index":"1"}	1	2023-09-01 07:03:17	1	2025-06-19 15:11:54	f
728895456842616832	name	姓名	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入姓名	f	\N	{"tab-index":"1"}	1	2023-09-01 07:03:17	1	2025-06-19 15:11:54	f
728895456842616833	available	可用	SWITCH	[]	[]	\N	1	\N	f	\N	{"tab-index":"2"}	1	2023-09-01 07:03:17	1	2025-06-19 15:11:54	f
728895456842616834	roleIds	角色	CHECKBOX	[]	[]	sys_role	\N	\N	f	\N	{"tab-index":"2"}	1	2023-09-01 07:03:17	1	2025-06-19 15:11:54	f
893463693533122560	birthday	出生日期	DATE	[]	[]	\N	\N	请输入出生日期	f	\N	{"tab-index":"1"}	1	2024-11-28 09:58:42	1	2025-06-19 15:11:54	f
893463693533122561	attachment	附件	FILE	[]	[]	\N	\N	\N	f	\N	{"tab-index":"2"}	1	2024-11-28 09:58:42	1	2025-06-19 15:11:54	f
946731519580336128	code	用户名	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"regex":"^[0-9a-zA-Z_\\\\/%\\\\-]{1,}$","message":"CODE只能包含数字、字母、下划线、中划线","validatorType":"REGEX"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入用户名	f	\N	{"tab-index":"1"}	1	2025-04-24 09:46:01	1	2025-04-24 09:46:01	f
946731519584530432	name	姓名	TEXT	[{"required":true,"validatorType":"REQUIRED","message":"必填项需要填写"},{"min":0,"max":16,"validatorType":"LENGTH","message":"长度范围 0 - 16 个字符"}]	[]	\N	\N	请输入姓名	f	\N	{"tab-index":"1"}	1	2025-04-24 09:46:01	1	2025-04-24 09:46:01	f
946731519584530433	available	可用	SWITCH	[]	[]	\N	1	\N	f	\N	{"tab-index":"2"}	1	2025-04-24 09:46:01	1	2025-04-24 09:46:01	f
946731519584530434	roleIds	角色	MULTIPLE_SELECT	[]	[]	sys_role	\N	\N	f	\N	{"tab-index":"2"}	1	2025-04-24 09:46:01	1	2025-04-24 09:46:01	f
946731519584530435	attachment	附件	FILE	[]	[]	\N	\N	\N	f	\N	{"tab-index":"2"}	1	2025-04-24 09:46:01	1	2025-04-24 09:46:01	f
\.


--
-- TOC entry 3517 (class 0 OID 24767)
-- Dependencies: 225
-- Data for Name: sys_form_cpn_configurer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_form_cpn_configurer (id, form_id, config_id, order_num, additional_info, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
728895412680790016	695312747063197696	728895409610559488	0	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
728895412680790017	695312747063197696	728895409639919616	1	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
728895412680790018	695312747063197696	728895409639919617	2	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
728895412680790019	695312747063197696	728895409639919618	3	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
728895458704887808	694980924206493696	728895456838422528	0	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
728895458704887809	694980924206493696	728895456842616832	1	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
728895458704887810	694980924206493696	728895456842616833	3	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
728895458704887811	694980924206493696	728895456842616834	4	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
893463693935775746	694980924206493696	893463693533122560	2	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
893463693935775749	694980924206493696	893463693533122561	5	\N	1	2025-06-12 20:20:17	1	2025-06-12 20:20:17	f
946731520297562112	859875429241106432	946731519580336128	0	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
946731520297562113	859875429241106432	946731519584530432	1	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
946731520301756416	859875429241106432	946731519584530433	2	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
946731520301756417	859875429241106432	946731519584530434	3	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
946731520301756418	859875429241106432	946731519584530435	4	\N	1	2025-04-24 09:46:02	1	2025-04-24 09:46:02	f
\.


--
-- TOC entry 3518 (class 0 OID 24774)
-- Dependencies: 226
-- Data for Name: sys_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_permission (id, code, name, pid, permission_order, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
694587393189089280	dashboard	仪表盘	0	0	\N	2023-05-29 14:55:17	0	2023-05-29 16:30:36	f
694587393189089281	sys_management	系统管理	0	9	0	2023-05-29 14:55:17	0	2023-05-29 14:55:17	f
694587393189089283	role_management	角色管理	694587393189089281	1	\N	2023-05-29 14:55:17	\N	2023-05-29 14:55:17	f
696145672415481856	sys_user	用户管理	694587393189089281	0	1	2023-06-02 22:07:20	1	2023-06-02 22:07:20	f
696145672960741376	sys_user_read	查看	696145672415481856	0	1	2023-06-02 22:07:20	1	2023-06-02 22:07:20	f
696145673485029376	sys_user_add	新增	696145672415481856	1	1	2023-06-02 22:07:20	1	2023-06-02 22:07:20	f
696145674072231936	sys_user_edit	编辑	696145672415481856	2	1	2023-06-02 22:07:20	1	2023-06-02 22:07:20	f
696145674634268672	sys_user_delete	删除	696145672415481856	3	1	2023-06-02 22:07:20	1	2023-06-02 22:07:20	f
696146982695079936	sys_dict	字典管理	694587393189089281	2	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146982695079937	sys_dict-htmx-htmx	字典管理-htmx	694587393189089281	2	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146982695079938	sys_dict-adminlte-htmx	字典管理-adminlte	694587393189089281	2	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146983320031232	sys_dict_read	查看	696146982695079936	0	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146983320031233	sys_dict-htmx_read	查看	696146982695079937	0	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146983320031234	sys_dict-adminlte_read	查看	696146982695079938	0	1	2023-06-02 22:12:32	1	2023-06-02 22:12:32	f
696146983924011008	sys_dict_add	新增	696146982695079936	1	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146983924011009	sys_dict-htmx_add	新增	696146982695079937	1	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146983924011010	sys_dict-adminlte_add	新增	696146982695079938	1	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146984569933824	sys_dict_edit	编辑	696146982695079936	2	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146984569933825	sys_dict-htmx_edit	编辑	696146982695079937	2	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146984569933826	sys_dict-adminlte_edit	编辑	696146982695079938	2	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146985190690816	sys_dict_delete	删除	696146982695079936	3	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146985190690817	sys_dict-htmx_delete	删除	696146982695079937	3	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
696146985190690818	sys_dict-adminlte_delete	删除	696146982695079938	3	1	2023-06-02 22:12:33	1	2023-06-02 22:12:33	f
946731526249279488	manager	管理	\N	0	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731526370914304	t_supplier	供应商管理	866065023858941952	1	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731526417051648	t_supplier_read	查看	946731526370914304	0	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731526484160512	t_supplier_add	新增	946731526370914304	1	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731526521909248	t_supplier_edit	编辑	946731526370914304	2	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
946731526555463680	t_supplier_delete	删除	946731526370914304	3	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
\.


--
-- TOC entry 3519 (class 0 OID 24780)
-- Dependencies: 227
-- Data for Name: sys_property; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_property (name, value) FROM stdin;
\.


--
-- TOC entry 3520 (class 0 OID 24783)
-- Dependencies: 228
-- Data for Name: sys_report; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_report (id, code, name, tpl_name, pageable, sidx, sord, query_sql, summary, report_column_list, summary_column_names, query_field_list, additional_info, report_advice_name, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
966383991991062528	sys_dict-adminlte	字典管理-adminlte	adminlte/list	t	id	ASC	select id, type, name, label, sort from sys_dict where type = :type and is_deleted = FALSE order by type, sort asc	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"编码","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"label","label":"显示值","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"type","label":"分类","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"sort","label":"排序号","sortable":false,"columnWidth":30,"hidden":false,"align":"left","tooltip":false,"type":"text"}]	sort	[{"name":"type","label":"分类","type":"SELECT","extraData":"sys_dict_type"}]	{"formId":"695312747063197696","formAction":"drawer","select":true}	dictHtmxReportAdvice	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
858937583114170368	t_student	学生表	tpl/list/ajax_list	t	"createTime"	DESC	SELECT t_student.name AS "name",t_student.gender AS "gender",t_student.email AS "email",t_student.birthday AS "birthday",t_student.age AS "age",t_student.is_marriage AS "marriage",t_student.unit_code AS "unit.code",t_student.attachments AS "attachments",t_student.avatar AS "avatar",t_student.hobby_list AS "hobbyList",t_student.material_type AS "materialTypeList",t_student.category AS "category",t_student.is_available AS "available",t_student.remark AS "remark",t_student.code AS "code",t_student.create_by AS "createBy",t_student.create_time AS "createTime",t_student.update_by AS "updateBy",t_student.update_time AS "updateTime",t_student.is_deleted AS "deleted",t_student.id AS "id" FROM t_student WHERE name = :name AND gender = :gender AND email = :email AND birthday = :birthday AND age = :age AND is_marriage = :is_marriage AND is_marriage = :marriage AND unit_code = :unit_code AND unit_code = :unitCode AND attachments = :attachments AND avatar = :avatar AND hobby_list = :hobby_list AND hobby_list = :hobbyList AND material_type = :material_type AND material_type = :materialTypeList AND category = :category AND is_available = :is_available AND is_available = :available AND remark = :remark AND code = :code AND create_by = :create_by AND create_by = :createBy AND create_time = :create_time AND create_time = :createTime AND update_by = :update_by AND update_by = :updateBy AND update_time = :update_time AND update_time = :updateTime AND is_deleted = FALSE AND is_deleted = :deleted AND id = :id	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"code","label":"外部可见，唯一code","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"姓名","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"gender","label":"gender","valueConverterNameList":["dictConverter"],"context":"GenderEnum","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"email","label":"email","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"birthday","label":"出生日期","sortable":true,"hidden":false,"align":"center","tooltip":false,"type":"date"},{"name":"age","label":"年龄","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"marriage","label":"婚否","valueConverterNameList":["boolConverter"],"sortable":false,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"unit.code","label":"unit.code","valueConverterNameList":["dictConverter"],"context":"UNIT","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"attachments","label":"attachments","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"avatar","label":"avatar","sortable":false,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"hobbyList","label":"hobbyList","valueConverterNameList":["arrayDictConverter"],"context":"HobbyEnum","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"materialTypeList","label":"物料类型","valueConverterNameList":["arrayDictConverter"],"context":"MATERIAL","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"category","label":"分类","valueConverterNameList":["dictConverter"],"context":"CategoryEnum","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"available","label":"是否可用","valueConverterNameList":["boolConverter"],"sortable":false,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"remark","label":"简介","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"createBy","label":"创建人","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"createTime","label":"创建时间","valueConverterNameList":["sqlTimestampConverter"],"sortable":false,"hidden":false,"align":"center","tooltip":false,"type":"datetime"},{"name":"updateBy","label":"更新人","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"updateTime","label":"更新时间","valueConverterNameList":["sqlTimestampConverter"],"sortable":false,"hidden":false,"align":"center","tooltip":false,"type":"datetime"}]	\N	[{"name":"code","label":"外部可见，唯一code","type":"TEXT"},{"name":"name","label":"姓名","type":"TEXT"},{"name":"gender","label":"gender","type":"SELECT","extraData":"GenderEnum"},{"name":"email","label":"email","type":"TEXT"},{"name":"birthday","label":"出生日期","type":"DATE"},{"name":"age","label":"年龄","type":"TEXT"},{"name":"is_marriage","label":"婚否","type":"CHECKBOX"},{"name":"unit_code","label":"unit.code","type":"MULTIPLE_SELECT","extraData":"UNIT"},{"name":"attachments","label":"attachments","type":"TEXT"},{"name":"avatar","label":"avatar","type":"TEXT"},{"name":"hobby_list","label":"hobbyList","type":"SELECT","extraData":"HobbyEnum"},{"name":"material_type","label":"物料类型","type":"MULTIPLE_SELECT","extraData":"MATERIAL"},{"name":"category","label":"分类","type":"SELECT","extraData":"CategoryEnum"},{"name":"is_available","label":"是否可用","type":"CHECKBOX"},{"name":"remark","label":"简介","type":"TEXT"},{"name":"create_by","label":"创建人","type":"TEXT"},{"name":"create_time","label":"创建时间","type":"DATE_RANGE"},{"name":"update_by","label":"更新人","type":"TEXT"},{"name":"update_time","label":"更新时间","type":"DATE_RANGE"}]	{"operator-bar":true,"endpoint":"students"}	studentReportAdvice	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
858372486293622784	sys_document	图片管理	modules/link/list	t	create_time	ASC	select id, name, concat('http://localhost:7892/', group_name, '/', path) url, extension, content_type, ROUND(size / (1024 * 1024), 1) size, create_time, '' copy from sys_document where name like :name and group_name = 'link'	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"isImageType","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"url","label":"文件","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"名称","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"content_type","label":"类型","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"size","label":"大小（M）","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"create_time","label":"创建时间","valueConverterNameList":["sqlTimestampConverter"],"sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"copy","label":"复制","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"}]	\N	[{"name":"name","label":"名称","type":"TEXT"}]	\N	linkReportAdvice	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
964844123011960832	sys_dict-htmx	字典管理-htmx	demos/htmx/list	t	id	ASC	select id, type, name, label, sort from sys_dict where type = :type and is_deleted = FALSE order by type, sort asc	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"编码","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"label","label":"显示值","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"type","label":"分类","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"sort","label":"排序号","sortable":false,"columnWidth":30,"hidden":false,"align":"left","tooltip":false,"type":"text"}]	sort	[{"name":"type","label":"分类","type":"SELECT","extraData":"sys_dict_type"}]	{"formId":"695312747063197696","formAction":"drawer","select":true}	dictHtmxReportAdvice	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
695316160014499840	sys_dict	字典管理	tpl/list/list	f	id	ASC	select id, type, name, label, sort from sys_dict where type = :type and is_deleted = FALSE order by type, sort asc	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"编码","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"label","label":"显示值","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"type","label":"分类","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"sort","label":"排序号","sortable":false,"columnWidth":30,"hidden":false,"align":"left","tooltip":false,"type":"text"}]	\N	[{"name":"type","label":"分类","type":"SELECT","extraData":"sys_dict_type"}]	{"formId":"695312747063197696"}	\N	1	2023-05-31 15:11:09	1	2025-06-13 09:18:44	f
694714180413960192	sys_user	用户管理	tpl/list/list	t	id	ASC	 SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, \n   CASE WHEN sys_user.is_available THEN '是' ELSE '否' END as is_available, \n   t.name role_name, u.name create_name, TO_CHAR(sys_user.create_time, 'YYYY-MM-DD HH24:MI:SS') as create_time \n   FROM sys_user\n   LEFT JOIN sys_user u on u.id = sys_user.create_by\n   LEFT JOIN (\n     SELECT sys_user.id, string_agg(r.name, ',') AS name \n     FROM sys_user\n     LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = FALSE\n     LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = FALSE\n     GROUP BY sys_user.id \n     ORDER BY sys_user.id ASC\n   ) t on t.id = sys_user.id\n   WHERE sys_user.code LIKE :code \n     AND sys_user.name LIKE :name \n     AND sys_user.is_available = :is_available \n     AND sys_user.create_time >= :create_time0 \n     AND sys_user.create_time <= :create_time1 \n     AND sys_user.is_deleted = FALSE	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"code","label":"用户名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"姓名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"birthday","label":"出生日期","sortable":true,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"role_name","label":"角色","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"is_available","label":"是否可用","sortable":false,"columnWidth":80,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"create_name","label":"创建人","sortable":false,"columnWidth":100,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"create_time","label":"创建时间","sortable":false,"columnWidth":120,"hidden":false,"align":"center","tooltip":false,"type":"text"}]	\N	[{"name":"code","label":"用户名","type":"TEXT"},{"name":"name","label":"姓名","type":"TEXT"},{"name":"is_available","label":"是否可用","type":"SELECT","extraData":"bol"},{"name":"create_time","label":"创建时间","type":"DATE_RANGE"}]	{"formId":"694980924206493696","formAction":"link"}	userReportAdvice	0	2023-05-29 23:19:06	1	2025-06-12 20:20:32	f
786015805669142528	sys_user_search	用户查询	tpl/list/dialog_report_list	t	id	ASC	SELECT sys_user.id, sys_user.code, sys_user.name, CASE WHEN sys_user.is_available THEN '是' ELSE '否' END is_available, t.name role_name, u.name create_name, TO_CHAR(sys_user.create_time, 'YYYY-MM-DD HH24:MI:SS') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, STRING_AGG(r.name, ',')  name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = FALSE\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = FALSE\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = FALSE and sys_user.id = :id and sys_user.id IN (:ids)	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"code","label":"用户名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"姓名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"role_name","label":"角色","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"is_available","label":"是否可用","sortable":false,"columnWidth":80,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"create_name","label":"创建人","sortable":false,"columnWidth":100,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"create_time","label":"创建时间","sortable":false,"columnWidth":120,"hidden":false,"align":"center","tooltip":false,"type":"text"}]	\N	[{"name":"code","label":"用户名","type":"TEXT"},{"name":"name","label":"姓名","type":"TEXT"},{"name":"is_available","label":"是否可用","type":"SELECT","extraData":"bol"},{"name":"create_time","label":"创建时间","type":"DATE_RANGE"}]	\N	\N	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
859875793323470848	sys_user_form_tag	用户管理	tpl/list/list	t	id	DESC	SELECT sys_user.id, sys_user.code, sys_user.name, sys_user.birthday, CASE WHEN sys_user.is_available THEN '是' ELSE '否' END is_available, t.name role_name, u.name create_name, TO_CHAR(sys_user.create_time, 'YYYY-MM-DD HH24:MI:SS') create_time FROM sys_user\n LEFT JOIN sys_user u on u.id = sys_user.create_by\n LEFT JOIN ( SELECT sys_user.id, STRING_AGG(r.name, ',')  name FROM sys_user\n LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = FALSE\n LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = FALSE\n group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\nWHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = FALSE	f	[{"name":"id","sortable":false,"hidden":true,"align":"left","tooltip":false,"type":"text"},{"name":"code","label":"用户名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"name","label":"姓名","sortable":true,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"birthday","label":"出生日期","sortable":true,"hidden":false,"align":"center","tooltip":false,"type":"date"},{"name":"role_name","label":"角色","sortable":false,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"is_available","label":"是否可用","sortable":false,"columnWidth":80,"hidden":false,"align":"center","tooltip":false,"type":"text"},{"name":"create_name","label":"创建人","sortable":false,"columnWidth":100,"hidden":false,"align":"left","tooltip":false,"type":"text"},{"name":"create_time","label":"创建时间","sortable":false,"columnWidth":120,"hidden":false,"align":"center","tooltip":false,"type":"text"}]	\N	[{"name":"code","label":"用户名","type":"TEXT"},{"name":"name","label":"姓名","type":"TEXT"},{"name":"is_available","label":"是否可用","type":"SELECT","extraData":"bol"},{"name":"create_time","label":"创建时间","type":"DATE_RANGE"}]	{"formId":"859875429241106432","operator-bar":true,"formAction":"link"}	userReportAdvice	1	2023-05-29 23:19:06	1	2023-05-29 23:19:06	f
\.


--
-- TOC entry 3521 (class 0 OID 24797)
-- Dependencies: 229
-- Data for Name: sys_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_role (id, code, name, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
694587732420202496	ADMIN	管理员	1	2023-05-29 14:56:38	1	2025-06-30 18:49:42	f
971120333606133760	FSDTz	TEST	1	2025-06-30 16:58:28	1	2025-06-30 18:49:42	f
971148330165903360	yeedW	TEST2	1	2025-06-30 18:49:43	1	2025-06-30 18:49:43	f
\.


--
-- TOC entry 3522 (class 0 OID 24803)
-- Dependencies: 230
-- Data for Name: sys_role_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_role_permission (role_id, permission_id, is_deleted) FROM stdin;
971120333606133760	946731526249279488	f
971120333606133760	694587393189089280	f
971120333606133760	946731526370914304	f
971120333606133760	946731526417051648	f
971120333606133760	946731526484160512	f
971120333606133760	946731526521909248	f
971120333606133760	946731526555463680	f
694587732420202496	946731526249279488	f
694587732420202496	694587393189089280	f
694587732420202496	946731526370914304	f
694587732420202496	946731526417051648	f
694587732420202496	946731526484160512	f
694587732420202496	946731526521909248	f
694587732420202496	946731526555463680	f
694587732420202496	694587393189089281	f
694587732420202496	696145672415481856	f
694587732420202496	696145672960741376	f
694587732420202496	696145673485029376	f
694587732420202496	696145674072231936	f
694587732420202496	696145674634268672	f
694587732420202496	694587393189089283	f
694587732420202496	696146982695079938	f
694587732420202496	696146983320031234	f
694587732420202496	696146983924011010	f
694587732420202496	696146984569933826	f
694587732420202496	696146985190690818	f
694587732420202496	696146982695079937	f
694587732420202496	696146983320031233	f
694587732420202496	696146983924011009	f
694587732420202496	696146984569933825	f
694587732420202496	696146985190690817	f
694587732420202496	696146982695079936	f
694587732420202496	696146983320031232	f
694587732420202496	696146983924011008	f
694587732420202496	696146984569933824	f
694587732420202496	696146985190690816	f
\.


--
-- TOC entry 3523 (class 0 OID 24807)
-- Dependencies: 231
-- Data for Name: sys_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_user (id, code, name, password, birthday, is_available, attachment, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	ADMIN	管理员	$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS	2024-11-28	t	[]	1	2023-05-29 14:55:03	1	2024-12-10 19:37:01	f
786079661661646848	TEST	测试	$2a$10$03ELdomnPVX3GqBd9t3jPuF1eaxrwcLZlAJOg6P1nbZJs0oG4N5vS	2025-06-30	t	\N	1	2023-05-29 14:55:03	1	2025-11-12 13:59:37	f
\.


--
-- TOC entry 3524 (class 0 OID 24812)
-- Dependencies: 232
-- Data for Name: sys_user_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_user_role (user_id, role_id, is_deleted) FROM stdin;
1	694587732420202496	f
786079661661646848	694587732420202496	f
786079661661646848	971148330165903360	f
\.


--
-- TOC entry 3525 (class 0 OID 24816)
-- Dependencies: 233
-- Data for Name: t_complex_model; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_complex_model (id, name, material_type_code, unit_code, category, work_status, category_code, age, birthday, category_list, category_dict_list, marriage, attachment, school_experience, map, embedded_value, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
856759492044419072	Rick2	HIBE	EA	MATERIAL	1	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "SALES_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "texg", "dictValue": {"code": "HIBE"}}	1	2025-04-24 09:46:03	1	2025-04-24 09:46:03	f
856958035153653760	Rick.Xu	HIBE	EA	MATERIAL	0	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}	1	2024-08-19 16:18:15	1	2024-08-19 16:18:15	f
856958362212896768	Rick.Xu	HIBE	EA	MATERIAL	0	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}	1	2024-08-19 16:19:33	1	2024-08-19 16:19:33	f
858263799545769984	Rick.Xu	HIBE	EA	MATERIAL	0	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}	1	2024-08-23 06:46:53	1	2024-08-23 06:46:53	f
858272781039611904	Rick.Xu	HIBE	EA	MATERIAL	0	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}	1	2024-08-23 07:22:35	1	2024-08-23 07:22:35	f
858305555469479936	Rick.Xu	HIBE	EA	MATERIAL	0	SALES_ORG	34	2021-12-26	["MATERIAL", "PURCHASING_ORG"]	[{"code": "MATERIAL"}, {"code": "PURCHASING_ORG"}]	t	[{"name":"picture","url":"baidu.com"}]	[["2023-11-11","苏州大学"],["2019-11-11","苏州中学"]]	{"name":"picture","url":"baidu.com"}	{"text": "text", "dictValue": {"code": "HIBE", "label": "耗材用品"}}	1	2024-08-23 09:32:49	1	2024-08-23 09:32:49	f
\.


--
-- TOC entry 3526 (class 0 OID 24909)
-- Dependencies: 234
-- Data for Name: t_student; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.t_student (id, code, name, gender, email, birthday, age, is_marriage, unit_code, attachments, avatar, hobby_list, material_type, category, is_available, remark, user_id, user_code, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
2	0001	张三	M	fsadfsaf@163.com	1992-11-12	19	t	EA	[{"id": "861948954584059904", "url": "http://localhost:7892/attachments/861948954575671296.xls", "name": "报关单", "path": "861948954575671296.xls", "size": 65536, "fullName": "报关单.xls", "fullPath": "attachments/861948954575671296.xls", "extension": "xls", "groupName": "attachments", "contentType": "application/vnd.ms-excel"}, {"id": "861948954584059905", "url": "http://localhost:7892/attachments/861948954575671297.zip", "name": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode", "path": "861948954575671297.zip", "size": 408174, "fullName": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip", "fullPath": "attachments/861948954575671297.zip", "extension": "zip", "groupName": "attachments", "contentType": "application/zip"}]	{"id": "861952755730780160", "url": "http://localhost:7892/images/861952755722391552.jpeg", "name": "avatar", "path": "861952755722391552.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "images/861952755722391552.jpeg", "extension": "jpeg", "groupName": "images", "contentType": "image/jpeg"}	["FOOTBALL", "BASKETBALL"]	[{"code": "M1"}]	MATERIAL	f	fsdfdasf	\N	\N	1	2024-08-24 22:57:24	1	2025-01-04 13:43:21	f
861949024788320256	00012	库房	M	1050216579@qq.com	2024-08-28	2	t	EA	[{"id": "861948954584059904", "url": "http://localhost:7892/attachments/861948954575671296.xls", "name": "报关单", "path": "861948954575671296.xls", "size": 65536, "fullName": "报关单.xls", "fullPath": "attachments/861948954575671296.xls", "extension": "xls", "groupName": "attachments", "contentType": "application/vnd.ms-excel"}, {"id": "861948954584059905", "url": "http://localhost:7892/attachments/861948954575671297.zip", "name": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode", "path": "861948954575671297.zip", "size": 408174, "fullName": "鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip", "fullPath": "attachments/861948954575671297.zip", "extension": "zip", "groupName": "attachments", "contentType": "application/zip"}]	{"id": "861952755730780160", "url": "http://localhost:7892/images/861952755722391552.jpeg", "name": "avatar", "path": "861952755722391552.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "images/861952755722391552.jpeg", "extension": "jpeg", "groupName": "images", "contentType": "image/jpeg"}	["BASKETBALL", "FOOTBALL"]	[{"code": "M1"}, {"code": "M4"}]	PURCHASING_ORG	t	这里是简介	786079661661646848	test	1	2024-09-02 10:50:40	1	2025-01-11 13:02:19	f
861953021922283520	111	李岁2	F	fsadfsaf@163.com	2024-09-02	23	f	KG	[{"id": "861952982621655040", "url": "http://localhost:7892/attachments/861952982609072128.jpeg", "name": "avatar", "path": "861952982609072128.jpeg", "size": 68783, "fullName": "avatar.jpeg", "fullPath": "attachments/861952982609072128.jpeg", "extension": "jpeg", "groupName": "attachments", "contentType": "image/jpeg"}, {"id": "861952982625849344", "url": "http://localhost:7892/attachments/861952982613266432.csv", "name": "Google Passwords", "path": "861952982613266432.csv", "size": 13191, "fullName": "Google Passwords.csv", "fullPath": "attachments/861952982613266432.csv", "extension": "csv", "groupName": "attachments", "contentType": "text/csv"}, {"id": "861967956609896448", "url": "http://localhost:7892/attachments/861967956597313536.pdf", "name": "线上VI指南", "path": "861967956597313536.pdf", "size": 463981, "fullName": "线上VI指南.pdf", "fullPath": "attachments/861967956597313536.pdf", "extension": "pdf", "groupName": "attachments", "contentType": "application/pdf"}]	{"id": "861976822831681536", "url": "http://localhost:7892/images/861976822642937856.webp", "name": "F5V8WS3asAAFJsC", "path": "861976822642937856.webp", "size": 81884, "fullName": "F5V8WS3asAAFJsC.webp", "fullPath": "images/861976822642937856.webp", "extension": "webp", "groupName": "images", "contentType": "image/webp"}	["BASKETBALL", "FOOTBALL"]	[{"code": "M1"}, {"code": "M3"}, {"code": "M4"}]	SALES_ORG	f	hello world	1	admin	1	2024-09-02 11:06:33	1	2025-01-06 17:52:58	f
906928548739047424	00035	TEST	F	10502165791@qq.com	2025-11-12	90	t	EA	[{"name":"采购讨论3","extension":"svg","contentType":"image/svg+xml","size":441964,"groupName":"upload","path":"906928520754651136.svg","url":"http://localhost:7892/upload/906928520754651136.svg","id":"906928521023086592","fullPath":"upload/906928520754651136.svg","fullName":"采购讨论3.svg"}]	{"name":"1369-1","extension":"jpeg","contentType":"image/jpeg","size":78402,"groupName":"images","path":"906928534776209408.jpeg","url":"http://localhost:7892/images/906928534776209408.jpeg","id":"906928534818152448","fullPath":"images/906928534776209408.jpeg","fullName":"1369-1.jpeg"}	["FOOTBALL"]	[{"code":"M1"},{"code":"M1"}]	MATERIAL	t	00	1	admin	1	2025-01-04 13:43:14	1	2025-11-12 16:56:50.688	f
1020050707148984320	233	张三	F	nakolis912@lxheir.com	2025-11-12	11	t	KG	[{"name":"default_avatar","extension":"png","contentType":"image/png","size":4937,"groupName":"upload","path":"1020050635141173248.png","url":"http://127.0.0.1:7892/upload/1020050635141173248.png","id":"1020050651729645568","fullName":"default_avatar.png","fullPath":"upload/1020050635141173248.png"}]	{"name":"default_avatar","extension":"png","contentType":"image/png","size":4937,"groupName":"images","path":"1020050674324361216.png","url":"http://127.0.0.1:7892/images/1020050674324361216.png","id":"1020050674383081472","fullName":"default_avatar.png","fullPath":"images/1020050674324361216.png"}	["BASKETBALL","FOOTBALL"]	[{"code":"M1"},{"code":"232323"},{"code":"M1"}]	PURCHASING_ORG	t	11	786079661661646848	TEST	1	2025-11-12 17:30:18.019	1	2025-11-12 17:31:28.357	f
\.


--
-- TOC entry 3533 (class 0 OID 0)
-- Dependencies: 217
-- Name: sys_access_info_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sys_access_info_id_seq', 369, true);


--
-- TOC entry 3534 (class 0 OID 0)
-- Dependencies: 218
-- Name: sys_access_info_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sys_access_info_id_seq1', 1, false);


--
-- TOC entry 3333 (class 2606 OID 24842)
-- Name: sys_access_info sys_access_info_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_access_info
    ADD CONSTRAINT sys_access_info_pkey PRIMARY KEY (id);


--
-- TOC entry 3335 (class 2606 OID 24845)
-- Name: sys_code_description sys_code_description_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_code_description
    ADD CONSTRAINT sys_code_description_pkey PRIMARY KEY (id);


--
-- TOC entry 3337 (class 2606 OID 24847)
-- Name: sys_dict sys_dict_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict
    ADD CONSTRAINT sys_dict_pkey PRIMARY KEY (type, name);


--
-- TOC entry 3339 (class 2606 OID 24849)
-- Name: sys_document sys_document_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_document
    ADD CONSTRAINT sys_document_pkey PRIMARY KEY (id);


--
-- TOC entry 3343 (class 2606 OID 24853)
-- Name: sys_form_configurer sys_form_configurer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_form_configurer
    ADD CONSTRAINT sys_form_configurer_pkey PRIMARY KEY (id);


--
-- TOC entry 3345 (class 2606 OID 24855)
-- Name: sys_form_cpn_configurer sys_form_cpn_configurer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_form_cpn_configurer
    ADD CONSTRAINT sys_form_cpn_configurer_pkey PRIMARY KEY (id);


--
-- TOC entry 3341 (class 2606 OID 24851)
-- Name: sys_form sys_form_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_form
    ADD CONSTRAINT sys_form_pkey PRIMARY KEY (id);


--
-- TOC entry 3347 (class 2606 OID 24857)
-- Name: sys_permission sys_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_permission
    ADD CONSTRAINT sys_permission_pkey PRIMARY KEY (id);


--
-- TOC entry 3349 (class 2606 OID 24859)
-- Name: sys_property sys_property_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_property
    ADD CONSTRAINT sys_property_pkey PRIMARY KEY (name);


--
-- TOC entry 3351 (class 2606 OID 24861)
-- Name: sys_report sys_report_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_report
    ADD CONSTRAINT sys_report_pkey PRIMARY KEY (id);


--
-- TOC entry 3355 (class 2606 OID 24865)
-- Name: sys_role_permission sys_role_permission_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role_permission
    ADD CONSTRAINT sys_role_permission_pk UNIQUE (role_id, permission_id);


--
-- TOC entry 3353 (class 2606 OID 24863)
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);


--
-- TOC entry 3357 (class 2606 OID 24867)
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (id);


--
-- TOC entry 3359 (class 2606 OID 24869)
-- Name: sys_user_role sys_user_role_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pk UNIQUE (user_id, role_id);


--
-- TOC entry 3361 (class 2606 OID 24872)
-- Name: t_complex_model t_complex_model_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_complex_model
    ADD CONSTRAINT t_complex_model_pkey PRIMARY KEY (id);


--
-- TOC entry 3363 (class 2606 OID 24918)
-- Name: t_student t_student_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.t_student
    ADD CONSTRAINT t_student_pkey PRIMARY KEY (id);


-- Completed on 2025-11-12 17:56:57 CST

--
-- PostgreSQL database dump complete
--

