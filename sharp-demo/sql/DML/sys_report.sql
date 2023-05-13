INSERT INTO `sharp-demo`.sys_report (id, pageable, sidx, sord, name, query_sql, summary_column_names, report_column_list, query_field_list, create_by, create_time, update_by, update_time, is_deleted) VALUES (619541501440958464, true, 'title', 'ASC', '图书报表', 'SELECT t_book.id, t_book.title, t_person.name, sys_dict.label "sexLabel"
FROM t_book,
     t_person LEFT JOIN sys_dict on t_person.sex = sys_dict.name AND type = \'sex\'
WHERE t_book.person_id = t_person.id
  AND t_book.title LIKE :title
  AND t_person.name = :name
  AND t_person.sex = :sex', null, '[{"name":"title","label":"书名","sortable":true,"align":"left","tooltip":false},{"name":"name","label":"作者","sortable":true,"align":"left","tooltip":false},{"name":"sexLabel","label":"性别","sortable":false,"align":"left","tooltip":false}]', '[{"name":"title","label":"书名","type":"TEXT"},{"name":"name","label":"作者","type":"TEXT"},{"name":"sex","label":"性别","type":"SELECT","extra_data":"sex"}]', 0, '2022-11-03 12:49:42', 0, '2023-03-28 21:07:36', false);
INSERT INTO `sharp-demo`.sys_report (id, pageable, sidx, sord, name, query_sql, summary_column_names, report_column_list, query_field_list, create_by, create_time, update_by, update_time, is_deleted) VALUES (668074769748373504, true, 'name', 'ASC', '人员信息表', 'SELECT t_user.name              AS "name",
#        t_user.gender            AS "gender",
       sd.label AS "gender",
       t_user.age               AS "age",
       t_user.birthday          AS "birthday",
       t_user.mobile            AS "mobile",
       t_user.score            AS "score",
       t_user.email             AS "email",
       t_user.native_place      AS "nativePlace",
       t_user.hobby             AS "hobbyList",
       IF(t_user.marriage, \'是\', \'否\')         AS "marriage",
       t_user.introduce         AS "introduce",
       t_user.attachment        AS "attachmentList",
       t_user.school_experience AS "schoolExperienceList",
       t_user.status            AS "status",
       t_user.create_by        AS "createdBy",
       t_user.create_time        AS "createdAt",
       t_user.update_by        AS "updatedBy",
       t_user.update_time        AS "updatedAt",
       t_user.is_deleted        AS "deleted",
       t_user.id                AS "id"
FROM t_user left JOIN sys_dict sd ON t_user.gender = sd.name WHERE is_deleted = 0 AND t_user.name like :name AND t_user.gender = :gender AND t_user.mobile = :mobile AND t_user.native_place IN (:nativePlace) AND t_user.status = :status AND t_user.hobby like :hobbyList AND t_user.birthday >= :birthday0 AND t_user.birthday <= :birthday1 AND marriage = :marriage', 'age, score', '[{"name":"name","label":"姓名","sortable":true,"align":"left","tooltip":false},{"name":"gender","label":"性别","sortable":false,"align":"left","tooltip":false},{"name":"age","label":"年龄","sortable":false,"align":"left","tooltip":false},{"name":"birthday","label":"出生日期","value_converter_name_list":["localDateConverter"],"sortable":true,"align":"left","tooltip":false},{"name":"mobile","label":"手机号码","sortable":false,"align":"left","tooltip":false},{"name":"score","label":"得分","sortable":false,"align":"left","tooltip":false},{"name":"email","label":"邮箱","sortable":false,"align":"left","tooltip":false},{"name":"nativePlace","label":"籍贯","sortable":false,"align":"left","tooltip":false},{"name":"hobbyList","label":"兴趣爱好","value_converter_name_list":["arrayDictConverter"],"context":"hobbyList","sortable":false,"align":"left","tooltip":false},{"name":"marriage","label":"婚否","sortable":false,"align":"left","tooltip":false},{"name":"introduce","label":"自我介绍","sortable":false,"align":"left","tooltip":false},{"name":"status","label":"状态","value_converter_name_list":["dictConverter"],"context":"status","sortable":false,"align":"left","tooltip":false}]', '[{"name":"name","label":"姓名","type":"TEXT"},{"name":"mobile","label":"手机号码","type":"TEXT"},{"name":"gender","label":"性别","type":"SELECT","extra_data":"gender"},{"name":"birthday","label":"出生日期","type":"DATE_RANGE"},{"name":"nativePlace","label":"籍贯","type":"MULTIPLE_SELECT","extra_data":"nativePlace"},{"name":"hobbyList","label":"兴趣爱好","type":"SELECT","extra_data":"hobbyList"},{"name":"status","label":"状态","type":"SELECT","extra_data":"status"},{"name":"marriage","label":"婚否","type":"SELECT","extra_data":"bol"}]', 0, '2023-03-17 11:03:35', 0, '2023-04-24 15:26:21', false);
