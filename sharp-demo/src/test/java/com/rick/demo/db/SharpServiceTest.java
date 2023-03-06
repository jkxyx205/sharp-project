package com.rick.demo.db;

import com.rick.db.service.GridService;
import com.rick.db.service.SharpService;
import com.rick.demo.module.project.domain.entity.Project;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SharpServiceTest {

    @Autowired
    private GridService gridService;

    @Test
    public void testListAsMap() {
        // language=SQL
        String sql = "SELECT id, title, description, created_at, is_deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  AND created_by = ${createBy}";

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

        List<Map<String, Object>> list = gridService.query(sql, params);
        list.forEach(System.out::println);
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void testListAsClass() {
        // language=SQL
        String sql = "SELECT id, title, description, created_at, is_deleted AS deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});

        List<Project> list = gridService.query(sql, params, Project.class);
        list.forEach(System.out::println);
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0).getSex()).isEqualTo(SexEnum.FEMALE);
        assertThat(list.get(0).getStatus()).isEqualTo(UserStatusEnum.NORMAL);
    }

    @Test
    public void testListAsCustom() {
        // language=SQL
        String sql = "SELECT id, title, description, created_at, is_deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("title", "title");

        List<String> list = gridService.query(sql, params, new SharpService.JdbcTemplateCallback<String>() {
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
        String sql = "SELECT id, title, description, created_at, is_deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});

        Map<Object, Object> map = gridService.queryForKeyValue(sql, params);
        System.out.println(map);
    }

    @Test
    public void testQueryObject() {
        // language=SQL
        String sql = "SELECT id, title, description, created_at, is_deleted AS deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id = :id\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted" +
                "  ${createBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", 479723134929764352L);

        Optional<Map<String, Object>> optionalMap = gridService.queryForObject(sql, params);
        System.out.println(optionalMap.isPresent());
        if (optionalMap.isPresent()) {
            System.out.println(optionalMap.get());
        }
    }

    @Test
    public void testQueryBean() {
        // language=SQL
        String sql = "SELECT id, title, description, created_at, is_deleted AS deleted, created_by, sex, status\n" +
                "FROM t_project1\n" +
                "WHERE id IN (:id)\n" +
                "  AND title LIKE :title\n" +
                "  AND created_at > :createTime\n" +
                "  AND is_deleted = :deleted ${hello}" +
                " ${createdBy}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", new Long[] { 479723134929764352L, 479723663504343041L});
        params.put("createdBy", "AND created_by = 156629745675451");

        Optional<Project> optionalMap = gridService.queryForObject(sql, params, Project.class);
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
                "  AND created_at > :createdTime\n" +
                "AND created_by = ${createdBy}";

        Map<String, Object> params = new HashMap<>();
        // 条件
        params.put("id", new Long[] { 479723134929764352L, 479723663504343040L, 479723663504343041L});
//        params.put("createdTime", "2019-10-22 17:27:41");
        params.put("createdBy", 156629745675451L);

        // 设置值
//        params.put("title", "new title xx");
        params.put("description", "new description3");
        params.put("deleted", true);

        int count = gridService.update(sql, params);
        assertThat(count).isEqualTo(2);
    }
}