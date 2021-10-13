package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.db.plugin.SQLUtils;
import com.rick.demo.module.project.dao.ProjectMapDAO;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class BaseDAOImplTest2 {

    @Autowired
    private ProjectMapDAO projectDAO;

    @AfterAll
    public static void init() {
        SQLUtils.deleteNotIn("t_project", "id", Arrays.asList(479723134929764352L, 479723663504343040L, 479723663504343041L));
    }

    @Test
    public void testSaveProject() {
        Map<String, Object> project = createProject();
        project.put("title", "项目标题");
        projectDAO.insert(project);
    }

    @Test
    public void testSaveProjectBatch() {
        Map<String, Object> project1 = createProject();
        project1.put("title", "项目标题-batch1");

        Map<String, Object> project2 = createProject();
        project2.put("title", "项目标题-batch2");

        projectDAO.insert(Lists.newArrayList(project1, project2));
    }

    @Test
    public void testSaveParams() {
        // title,description,cover_url,owner_id,sex,address,status,list,id,created_by,created_at,updated_by,updated_at,is_deleted
        projectDAO.insert(new Object[] {
                "title-params", "description", "http://", 1, "0", "{}", "LOCKED", "[]",null, null, null, null, null, null
        });
    }

    @Test
    public void testMapSaveParams() {
        // title,description,cover_url,owner_id,sex,address,status,list,id,created_by,created_at,updated_by,updated_at,is_deleted
        projectDAO.insert(new Object[] {
                "title-map", "description", "http://", 1, "1", "{}", "LOCKED", "[]",null, null, null, null, null, null
        });
    }

    @Test
    public void testSaveParamsBatch() {
        // title,description,cover_url,owner_id,sex,address,status,list,id,created_by,created_at,updated_by,updated_at,is_deleted
        List<Object[]> paramsList = Lists.newArrayList(
                new Object[]{
                        "title-save-params-batch-1", "description", "http://", 1, "1", "{}", "LOCKED", "[]",null, null, null, null, null, null
                },
                new Object[]{
                        "title-save-params-batch-2", "description", "http://", 1, "1", "{}", "LOCKED", "[]",null, null, null, null, null, null
                });
        projectDAO.insert(paramsList);
    }

    @Test
    public void testDeleteById() {
        projectDAO.deleteById(479720583324925952L);
    }

    @Test
    public void testDeleteByCondition() {
        projectDAO.delete(new Object[] {479722488834981888L, 479722488839176192L}, "id in(?, ?)");
    }

    @Test
    public void testDeleteByIds() {
        projectDAO.deleteByIds("479724114912116736, 479724114912116737");
    }

    @Test
    public void testUpdate() {
        // title,description,cover_url,owner_id,sex,address,status,list,created_by, created_at, updated_by,updated_at,is_deleted
        int count = projectDAO.update(new Object[]{
                "title-update", "description", "http://", 1, "1", "{}", "LOCKED", "[]", 11, LocalDateTime.now(), 12, LocalDateTime.now(), false
        }, 479723134929764352L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testUpdate2() {
        int count = projectDAO.update("title, sex, status, updated_at, updated_by", new Object[]{
                "title-update2", 0, "NORMAL", null, null
        }, 479723134929764352L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testUpdate3() {
        int count = projectDAO.update("title, sex, status", new Object[] {
                "title-update", 1, "LOCKED", 479723134929764352L, 479723663504343041L
        }, "id in(?, ?) AND is_deleted = 0");

        assertThat(count).isEqualTo(2);
    }

    @Test
    public void testUpdate4() {
        Map<String, Object> project = createProject();
        project.put("id", 479723134929764352L);
        project.put("title", "title-entity-update");
        projectDAO.update(project);
    }

    @Test
    public void testFindById() {
        Optional<Map> optional4 = projectDAO.selectById(479723134929764352L);
        assertThat(optional4.get().get("title")).isEqualTo("title-entity-update");
    }

    @Test
    public void testFindByIds() {
        List<Map> list = projectDAO.selectByIds("479723134929764352, 479723663504343040L");
        assertThat(list.size()).isEqualTo(2);

        List<Map> list2 = projectDAO.selectByIds(Arrays.asList(479723134929764352L, 479723663504343040L));
        assertThat(list2.size()).isEqualTo(2);
    }

    @Test
    public void testParams() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("title", "title-update");
        params.put("description", "description");
        List<Map> list = projectDAO.selectByParams(params);
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    public void testParams2() {
        List<Map> list = projectDAO.selectByParams("title=title&sex=1");
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    public void testParams3() {
        List<Map> list = projectDAO.selectByParams("title=title&id=479723134929764352,479723663504343040", "title like :title AND id IN(:id)");
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void testFindAll() {
        List<Map> list = projectDAO.selectAll();
        System.out.println(list.size());
    }

    public Map<String, Object> createProject() {
        Map<String, Object> project = new HashMap<>();
        project.put("title", "项目标题 " + System.currentTimeMillis());
        project.put("description", "项目描述");
        project.put("sex", SexEnum.FEMALE);
        project.put("cover_url", "http://");
        project.put("owner_id", 9L);
        project.put("status", UserStatusEnum.NORMAL);
        project.put("address", Address.builder().code("001").detail("苏州").build());
        project.put("list", Lists.newArrayList(Address.builder().code("001").detail("苏州").build()));
        return project;
    }

}