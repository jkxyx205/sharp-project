-- 删除无引用的数据
WITH nonexists AS
         (select id
          from sys_form_cpn_configurer
          where NOT exists(select 1 from sys_form where sys_form.id = sys_form_cpn_configurer.form_id))
DELETE
sys_form_cpn_configurer
FROM sys_form_cpn_configurer
JOIN nonexists USING(id);


WITH nonexists AS
         (select id
          from sys_form_configurer
          where not exists(select 1
                           from sys_form_cpn_configurer
                           where sys_form_cpn_configurer.config_id = sys_form_configurer.id))
DELETE
sys_form_configurer
FROM sys_form_configurer
JOIN nonexists USING(id);

-- 查询重复
WITH duplicates AS
         (
             select id, name, ROW_NUMBER() over(
	PARTITION BY name order by create_time ASC
) rownum from sys_user
         )
select * from duplicates where rownum > 1;

-- 删除重复
WITH duplicates AS
         (
             select id, name, ROW_NUMBER() over(
	PARTITION BY name order by create_time ASC
) rownum from sys_user
         )
DELETE sys_user
FROM sys_user
JOIN duplicates USING(id)
WHERE duplicates.rownum > 1
