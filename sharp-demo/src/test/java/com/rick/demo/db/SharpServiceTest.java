package com.rick.demo.db;

import com.rick.db.plugin.QueryUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.domain.entity.Project;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import com.rick.demo.module.school.model.SchoolBO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SharpServiceTest {

    @Autowired
    private SharpService sharpService;

    @Test
    public void testListAsMap() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  AND create_by = ${createBy}";

        Map<String, Object> params = new HashMap<>();
//        params.put("id", "1341369230607347714,1341299740150394882,1341734037772668930,1341313565406904322,1341368363682439169");
//        params.put("id", Arrays.asList(1341369230607347714L,
//                1341299740150394882L,1341734037772668930L,1341313565406904322L,1341368363682439169L));
//        params.put("id", Sets.newHashSet(1341369230607347714L,
//                1341299740150394882L, 1341734037772668930L, 1341313565406904322L, 1341368363682439169L));

        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});
        params.put("title", "title");
        params.put("createTime", "2020-10-22 17:27:41");
        params.put("createBy", "156629745675451");

        List<Map<String, Object>> list = sharpService.query(sql, params);
        list.forEach(System.out::println);
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void testListAsClass() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted AS deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});

        List<Project> list = sharpService.query(sql, params, Project.class);
        list.forEach(System.out::println);
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0).getSex()).isEqualTo(SexEnum.FEMALE);
        assertThat(list.get(0).getStatus()).isEqualTo(UserStatusEnum.NORMAL);
    }

    @Test
    public void testListAsCustom() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("title", "title");

        List<String> list = sharpService.query(sql, params, new SharpService.JdbcTemplateCallback<String>() {
            @Override
            public List<String> query(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, ?> paramMap) {
                return jdbcTemplate.query(sql, paramMap, new RowMapper<String>() {
                    public String mapRow(ResultSet rs, int var2) throws SQLException {
                        return rs.getString(1) + "-"  + rs.getString(2) + "-" + rs.getString("sex") + "-" + rs.getString("status");
                    }
                });
            }
        });
        list.forEach(System.out::println);
        assertThat(list.size()).isEqualTo(3);
    }

    @Test
    public void testKeyValue() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});

        Map<Object, Object> map = sharpService.queryForKeyValue(sql, params);
        System.out.println(map);
    }

    @Test
    public void testQueryObject() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted AS deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id = :id\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", 479723134929764352L);

        Optional<Map<String, Object>> optionalMap = sharpService.queryForObject(sql, params);
        System.out.println(optionalMap.isPresent());
        if (optionalMap.isPresent()) {
            System.out.println(optionalMap.get());
        }
    }

    @Test
    public void testQueryBean() {
        // language=SQL
        String sql = "SELECT id, title, description, create_time, is_deleted AS deleted, create_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND create_time > :createTime\n" +
                "  AND is_deleted = :deleted ${hello}" +
                " ${createdBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343041L});
        params.put("createdBy", "AND create_by = 156629745675451");

        Optional<Project> optionalMap = sharpService.queryForObject(sql, params, Project.class);
        if (optionalMap.isPresent()) {
            System.out.println(optionalMap.get());
        }
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void testUpdate() {
        // language=SQL
        String sql = "UPDATE t_project1 SET title = :title, description = :description, is_deleted = :deleted\n" +
                "WHERE id IN (:id)\n" +
                "  AND create_time > :createdTime\n" +
                "AND create_by = ${createdBy}";

        Map<String, Object> params = new HashMap<>();
        // 条件
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});
//        params.put("createdTime", "2019-10-22 17:27:41");
        params.put("createdBy", 156629745675451L);

        // 设置值
//        params.put("title", "new title xx");
        params.put("description", "new description3");
        params.put("deleted", true);

        int count = sharpService.update(sql, params);
        assertThat(count).isEqualTo(2);
    }

    @Test
    public void testCascadeQuery() {
        String sql = "select s.id,\n" +
                "       s.name,\n" +
                "       s.build_date as \"buildDate\",\n" +
                "       stu.id as \"stu.id\",\n" +
                "       stu.name as \"stu.name\",\n" +
                "       stu.grade as \"stu.grade\",\n" +
                "       stu.sex as \"stu.sex\"\n" +
                "from t_school s\n" +
                "         LEFT JOIN t_school_student stu on s.id = stu.school_id\n" +
                "where s.id = 552173736070144000";

        List<SchoolBO> list = sharpService.query(sql, null, SchoolBO.class);
    }

    @Test
    public void testQueryUtils() {
        String parentSql = "select s.id,\n" +
                "       s.name,\n" +
                "       s.build_date as \"buildDate\",\n" +
                "       sl.id as \"sl.id\",\n" +
                "       sl.number as \"sl.number\",\n" +
                "       sl.remark as \"sl.remark\"\n" +
                "from t_school s\n" +
                "         LEFT JOIN t_school_license sl on sl.id = s.school_license_id\n" +
                "where s.id IN (:id)";

        List<SchoolBO> schoolBOList = sharpService.query(parentSql, Params.builder(1).pv("id", "552173736070144000, 552181593272410112").build(), SchoolBO.class);

        if (CollectionUtils.isEmpty(schoolBOList)) {
            return;
        }

        Collection<?> refValues = schoolBOList.stream().map(SchoolBO::getId).collect(Collectors.toSet());

        // 1 <-> N
        // select id, name, grade, sex from t_school_student where school_id IN (552173736070144000, 552181593272410112);
        Map<?, List<SchoolBO.StudentBO>> schoolStudentMap = QueryUtils.subTableValueMap("id, name, grade, sex, school_id", "t_school_student", "school_id", refValues, SchoolBO.StudentBO.class);

        // N <-> N
        // select t.id, t.name, t.age, school_id from t_school_teacher_related r INNER JOIN t_school_teacher t ON r.teacher_id = t.id where school_id IN (552173736070144000, 552181593272410112)
        Map<?, List<SchoolBO.TeacherBO>> schoolTeacherMap = QueryUtils.subTableValueMap("t.id, t.name, t.age, school_id", "t_school_teacher_related r INNER JOIN t_school_teacher t ON r.teacher_id = t.id", "school_id", refValues, SchoolBO.TeacherBO.class);

        schoolBOList.forEach(t -> {
            t.setStudentList(ObjectUtils.defaultIfNull(schoolStudentMap.get(t.getId()), Collections.emptyList()));
            t.setTeacherList(ObjectUtils.defaultIfNull(schoolTeacherMap.get(t.getId()), Collections.emptyList()));
        });

        System.out.println(schoolBOList);
    }

}