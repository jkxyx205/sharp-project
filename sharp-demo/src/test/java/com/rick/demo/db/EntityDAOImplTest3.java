package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.dao.ProjectDAO2;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.entity.group.Project;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityDAOImplTest3 {

    @Autowired
    private ProjectDAO2 projectDAO;

    @AfterAll
    public static void init() {
        DataInit.init();
    }

    @Order(1)
    @Test
    public void testSaveProject() {
        Project project = createProject();
        project.setTitle("项目标题-batch1");
        projectDAO.insert(project);
        assertThat(project.getId()).isNotNull();
    }

    @Order(2)
    @Test
    public void testSaveProjectBatch() {
        Project project1 = createProject();
        project1.setTitle("项目标题-batch1");

        Project project = createProject();
        project.setTitle("项目标题-batch2");

        projectDAO.insert(Lists.newArrayList(project1, project));
    }

    @Order(3)
    @Test
    public void testSaveParams() {
        // title,description,cover_url,owner_id,sex,address,status,list,phone_number,id,create_by,create_time,update_by,update_time,is_deleted
        projectDAO.insert(new Object[] {
                "title-params", "description", "http://", 1, "0", "{}", "LOCKED", "[]","816-18898876654", null, null, null, null, null, null
        });
    }

    @Order(4)
    @Test
    public void testMapSaveParams() {
        // title,description,cover_url,owner_id,sex,address,status,list,phone_number,id,create_by,create_time,update_by,update_time,is_deleted
        projectDAO.insert(new Object[] {
                "title-map", "description", "http://", 1, "1", "{}", "LOCKED", "[]","232-17787823",null, null, null, null, null, null
        });
    }

    @Order(5)
    @Test
    public void testSaveParamsBatch() {
        // title,description,cover_url,owner_id,sex,address,status,list,phone_number,id,create_by,create_time,update_by,update_time,is_deleted
        List<Object[]> paramsList = Lists.newArrayList(
                new Object[]{
                        "title-save-params-batch-1", "description", "http://", 1, "1", "{}", "LOCKED", "[]","23-233223223",null, null, null, null, null, null
                },
                new Object[]{
                        "title-save-params-batch-2", "description", "http://", 1, "1", "{}", "LOCKED", "[]","231-232332", null, null, null, null, null, null
                });
        projectDAO.insert(paramsList);
    }

    @Order(6)
    @Test
    public void testDeleteById() {
        projectDAO.deleteById(479720583324925952L);
    }

    @Order(7)
    @Test
    public void testDeleteByCondition() {
        projectDAO.delete(new Object[] {479722488834981888L, 479722488839176192L}, "id in(?, ?)");
    }

    @Order(8)
    @Test
    public void testDeleteByIds() {
        projectDAO.deleteByIds("479724114912116736, 479724114912116737");
    }

    @Order(9)
    @Test
    public void testUpdate() {
        // title,description,cover_url,owner_id,sex,address,status,list,phone_number,update_by,update_time,is_deleted
        int count = projectDAO.update(new Object[]{
                "title-update", "description", "http://", 1, "1", "{}", "LOCKED", "[{\"code\":\"001\",\"detail\":\"苏州\"}]", "2311-223323", null, null, false
        }, 479723134929764352L);

        assertThat(count).isEqualTo(1);
    }

    @Order(10)
    @Test
    public void testUpdate2() {
        int count = projectDAO.updateById("title, sex, status, update_time, update_by", new Object[]{
                "title-update2", 0, "NORMAL", null, null
        }, 479723134929764352L);

        assertThat(count).isEqualTo(1);
    }

    @Order(11)
    @Test
    public void testUpdate3() {
        int count = projectDAO.update("title, sex, status", new Object[] {
                "title-update", 1, "LOCKED", 479723134929764352L, 479723663504343041L
        }, "id in(?, ?) AND is_deleted = 0");

        assertThat(count).isEqualTo(2);
    }

    @Order(12)
    @Test
    public void testUpdate4() {
        Project project = createProject();
        project.setTitle("title-entity-update");
        project.setId(479723134929764352L);
        projectDAO.update(project);
    }

    @Order(13)
    @Test
    public void testFindById() {
        Optional<Project> optional = projectDAO.selectById(479723134929764352L);
        Project project = optional.get();
        assertThat(project.getTitle()).isEqualTo("title-entity-update");
        assertThat(project.getSex()).isEqualTo(SexEnum.FEMALE);
        assertThat(project.getStatus()).isEqualTo(UserStatusEnum.NORMAL);
        assertThat(project.getAddress().getCode()).isEqualTo("001");
        assertThat(project.getList().get(0).getCode()).isEqualTo("001");
        assertThat(project.getPhoneNumber().getNumber()).isEqualTo("18888888888");

        Optional<Project> optional2 = projectDAO.selectById(0L);
        assertThat(optional2.isPresent()).isFalse();
    }

    @Order(14)
    @Test
    public void testFindByIds() {
        List<Project> list = projectDAO.selectByIds("479723134929764352, 479723663504343040L");
        assertThat(list.size()).isEqualTo(2);

        List<Project> list2 = projectDAO.selectByIds(Arrays.asList(479723134929764352L, 479723663504343040L));
        assertThat(list2.size()).isEqualTo(2);
    }

    @Order(15)
    @Test
    public void testParams() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("title", "title-update");
        params.put("description", "description");
        List<Project> list = projectDAO.selectByParams(params);
        assertThat(list.size()).isEqualTo(1);
    }

    @Order(16)
    @Test
    public void testParams2() {
        List<Project> list = projectDAO.selectByParams("title=title&sex=1");
        assertThat(list.size()).isEqualTo(1);

        List<Project> list2 = projectDAO.selectByParams("title=title&sex=1");
        assertThat(list2.size()).isEqualTo(1);

        List<Project> list3 = projectDAO.selectByParams("title=title&sex=1");
        assertThat(list3.size()).isEqualTo(1);
    }

    @Order(17)
    @Test
    public void testParams3() {
        List<Project> list = projectDAO.selectByParams("title=title&id=479723134929764352,479723663504343040", "title like :title AND id IN(:id)");
        assertThat(list.size()).isEqualTo(2);
    }

    @Order(18)
    @Test
    public void testFindAll() {
        List<Project> list = projectDAO.selectAll();
        System.out.println(list.size());
    }

    @Order(19)
    @Test
    public void testSelectByIdsAsMap() {
        Map<Long, Project> serializableProjectMap1 = projectDAO.selectByIdsAsMap(Lists.newArrayList(479723134929764352L, 479723663504343040L));
        Map<Long, Project> serializableProjectMap2 = projectDAO.selectByIdsAsMap("47972313492976435,47972313492976432");
        assertThat(serializableProjectMap1.size()).isEqualTo(2);
        assertThat(serializableProjectMap1.get(479723134929764352L).getSex()).isEqualTo(SexEnum.FEMALE);
        assertThat(serializableProjectMap1.get(479723663504343040L).getAddress().getCode()).isEqualTo("001");
        assertThat(serializableProjectMap1.get(479723663504343040L).getList().get(0).getCode()).isEqualTo("001");

        assertThat(serializableProjectMap2.size()).isEqualTo(0);
    }

    @Order(20)
    @Test
    public void testBatchUpdate() {
        List<Project> projects = projectDAO.selectByIds(Arrays.asList(479723663504343043L, 479723663504343042L));
        projects.forEach(project -> project.setTitle("batch: "+ System.currentTimeMillis()));

        Object[] params = new Object[]{"description: " + System.currentTimeMillis()};
        List<Object[]> paramsList = new ArrayList<>();
        paramsList.add(params);

        projectDAO.update(projects);
        projectDAO.update("description",
                paramsList,
                "id IN (479723663504343042, 479723663504343043)");
    }

    @Order(21)
    @Test
    public void testDeleteLogically() {
        int count1 = projectDAO.deleteLogicallyByIds(Lists.newArrayList(479723663504343042L, 479723663504343043L));
        int count2 = projectDAO.deleteLogicallyById(479723663504343042L);
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(0);
    }

    @Order(22)
    @Test
    public void testFindDeleteLogically() {
        Optional<Project> project = projectDAO.selectById(479723663504343043L);
        assertThat(project.isPresent()).isEqualTo(false);
    }

    @Order(23)
    @Test
    public void testDeleteByConditionCascade() {
        projectDAO.delete(new Object[] { "kill"}, "title = ?");
    }

    @Order(24)
    @Test
    public void testUpdateMap() {
        int count = projectDAO.update(" owner_id , sex, description",
                Params.builder().pv("cover_url", "http://xhope.top")
                        .pv("id", 479723663504343040L)
                        .pv("description", "new-description222").build(),
                "id = :id AND title like :title");

        assertThat(count).isEqualTo(1);
        assertThat(projectDAO.selectByParams("description=new-description222").size()).isEqualTo(1);
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
        project.setPhoneNumber(PhoneNumber.builder().code("816").number("18888888888").build());
        project.setDeleted(false);
        return project;
    }

}