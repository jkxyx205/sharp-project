package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.db.plugin.SQLUtils;
import com.rick.demo.module.project.dao.ProjectDAO;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.Project;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class BaseDAOImplTest {

    @Autowired
    private ProjectDAO projectDAO;

//    @Autowired
//    private ProjectDAO2 projectDAO;

    @AfterAll
    public static void init() {
        SQLUtils.deleteNotIn("t_project", "id", Arrays.asList(479723134929764352L, 479723663504343040L, 479723663504343041L));
    }

    @Test
    public void testSaveProject() {
        Project project = createProject();
        project.setTitle("项目标题-batch1");
        projectDAO.insert(project);
        assertThat(project.getId()).isNotNull();
    }

    @Test
    public void testSaveProjectBatch() {
        Project project1 = createProject();
        project1.setTitle("项目标题-batch1");

        Project project2 = createProject();
        project2.setTitle("项目标题-batch2");

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
        // title,description,cover_url,owner_id,sex,address,status,list,updated_by,updated_at,is_deleted
        int count = projectDAO.update(new Object[]{
                "title-update", "description", "http://", 1, "1", "{}", "LOCKED", "[]", null, null, false
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
        Project project = createProject();
        project.setTitle("title-entity-update");
        project.setId(479723134929764352L);
        projectDAO.update(project);
    }

    @Test
    public void testFindById() {
        Optional<Project> optional = projectDAO.selectById(479723134929764352L);
        Project project = optional.get();
        assertThat(project.getTitle()).isEqualTo("title-entity-update");
        assertThat(project.getSex()).isEqualTo(SexEnum.FEMALE);
        assertThat(project.getStatus()).isEqualTo(UserStatusEnum.NORMAL);
        assertThat(project.getAddress().getCode()).isEqualTo("001");
        assertThat(project.getList().get(0).getCode()).isEqualTo("001");

        Optional<Project> optional2 = projectDAO.selectById(0);
        assertThat(optional2.isPresent()).isFalse();
    }

    @Test
    public void testFindByIds() {
        List<Project> list = projectDAO.selectByIds("479723134929764352, 479723663504343040L");
        assertThat(list.size()).isEqualTo(2);

        List<Project> list2 = projectDAO.selectByIds(Arrays.asList(479723134929764352L, 479723663504343040L));
        assertThat(list2.size()).isEqualTo(2);
    }

    @Test
    public void testParams() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("title", "title-update");
        params.put("description", "description");
        List<Project> list = projectDAO.selectByParams(params);
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    public void testParams2() {
        List<Project> list = projectDAO.selectByParams("title=title&sex=1");
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    public void testParams3() {
        List<Project> list = projectDAO.selectByParams("title=title&id=479723134929764352,479723663504343040", "title like :title AND id IN(:id)");
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void testFindAll() {
        List<Project> list = projectDAO.selectAll();
        System.out.println(list.size());
    }

    @Test
    public void testSelectByIdsAsMap() {
        Map<Serializable, Project> serializableProjectMap1 = projectDAO.selectByIdsAsMap(Lists.newArrayList(479723134929764352L, 479723663504343040L));
        Map<Serializable, Project> serializableProjectMap2 = projectDAO.selectByIdsAsMap("47972313492976435,47972313492976432");
        assertThat(serializableProjectMap1.size()).isEqualTo(2);
        assertThat(serializableProjectMap1.get(479723134929764352L).getSex()).isEqualTo(SexEnum.MALE);
        assertThat(serializableProjectMap1.get(479723663504343040L).getAddress().getCode()).isEqualTo("001");
        assertThat(serializableProjectMap1.get(479723663504343040L).getList().get(0).getCode()).isEqualTo("001");

        assertThat(serializableProjectMap2.size()).isEqualTo(0);
    }

    public Project createProject() {
        Project project = new Project();
        project.setTitle("项目标题 " + System.currentTimeMillis());
        project.setDescription("项目描述");
        project.setSex(SexEnum.FEMALE);
        project.setCoverUrl("http://");
        project.setOwnerId(9L);
        project.setStatus(UserStatusEnum.NORMAL);
        project.setAddress(Address.builder().code("001").detail("苏州").build());
        project.setList(Lists.newArrayList(Address.builder().code("001").detail("苏州").build()));
        return project;
    }

}