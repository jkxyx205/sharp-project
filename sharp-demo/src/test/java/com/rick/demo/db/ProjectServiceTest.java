package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.dao.ProjectDAO;
import com.rick.demo.module.project.dao.ProjectDetailDAO;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.entity.Project;
import com.rick.demo.module.project.domain.entity.ProjectDetail;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import com.rick.demo.module.project.service.ProjectService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Rick
 * @createdAt 2022-04-09 10:20:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private ProjectDetailDAO projectDetailDAO;

    private static Long id = System.currentTimeMillis();

    @Order(0)
    @Test
    public void testSave() {
        Project project = createProject();
        // 指定id
        project.setId(id);
        projectService.save(project);
    }

    @Order(1)
    @Test
    public void testUpdate1() {
        Project project = createProject();
        project.setId(-1L);
        project.setTitle("updateValue");
        projectService.update(project);
        assertThat(projectService.update(project)).isEqualTo(false);
    }


    @Order(2)
    @Test
    public void testUpdate2() {
        Project project = createProject();
        project.setId(id);
        project.setTitle("updateValue");
        assertThat(projectService.update(project)).isEqualTo(true);
    }

    @Order(3)
    @Test
    public void testSaveOrUpdate() {
        Project project = createProject();
        project.setTitle("saveOrUpdate");
        projectService.saveOrUpdate(project);
    }

    @Order(4)
    @Test
    public void testFindById() {
        Optional<Project> project1 = projectDAO.selectById(479723663504343043L);
        Optional<Project> project2 = projectDAO.selectById(479723663504343042L);
        assertThat(project1.get().getProjectDetailList()).isNotNull();
        assertThat(project2.get().getProjectDetailList()).isNotNull();
    }

    @Order(5)
    @Test
    public void testFindById2() {
        List<Project> projects = projectDAO.selectByIds(479723663504343043L, 479723663504343042L);
        assertThat(projects.get(0).getProjectDetailList()).isNotNull();
        assertThat(projects.get(1).getProjectDetailList()).isNotNull();
    }

    @Order(6)
    @Test
    public void testFindById4() {
        Optional<Project> optional = projectService.findById(id);
        assertThat(optional.get().getSex() == SexEnum.FEMALE).isEqualTo(true);
    }

    @Order(6)
    @Test
    public void findAllWithoutCascade() {
        List<Project> list = projectService.findAllWithoutCascade();
        assertThat(list.size()).isGreaterThan(0);
        assertThat(list.get(0).getProjectDetailList()).isNull();
    }

    @Order(7)
    @Test
    public void testFindById3() {
        Optional<Project> projects = projectDAO.selectById(id);
        assertThat(projects.get().getProjectDetailList()).isNotNull();
    }

    @Order(7)
    @Test
    public void findAll() {
        List<Project> list = projectService.findAll();
        assertThat(list.size()).isGreaterThan(0);
        assertThat(list.get(0).getProjectDetailList()).isNotNull();
    }

    @Order(8)
    @Test
    public void findByIdWithoutCascade() {
        Optional<Project> optional = projectService.findByIdWithoutCascade(479723663504343042L);
        assertThat(optional.get().getProjectDetailList()).isNull();
    }

    @Order(9)
    @Test
    public void testDeleteById() {
        boolean deleted = projectService.deleteById(id);
        assertThat(deleted).isEqualTo(true);
    }


    @Order(12)
    @Test
    public void findProjectDetail() {
        assertThat(projectDetailDAO.selectById(1L).get().getProject()).isNotNull();
    }

    @Order(13)
    @Test
    public void checkId() {
        projectService.checkId(479723663504343043L);
    }

    @Order(14)
    @Test
    public void checkId2() {
        assertThrows(BizException.class, ()-> projectService.checkId(4797236635043430411L));
    }

    @Order(15)
    @Test
    public void checkIds() {
        projectService.checkIds(Arrays.asList(479723663504343043L));
    }

    @Order(16)
    @Test
    public void checkIds2() {
        assertThrows(BizException.class, ()-> projectService.checkIds(Arrays.asList(479723663504343043L, 12L)));
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
        project.setMap(Params.builder().pv("hello", "world").build());

        project.setProjectDetailList(Arrays.asList(ProjectDetail.builder()
                .title("add...")
                .build()));
        return project;
    }
}
