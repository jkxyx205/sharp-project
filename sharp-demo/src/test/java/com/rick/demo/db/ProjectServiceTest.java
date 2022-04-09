package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.entity.Project;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import com.rick.demo.module.project.service.ProjectService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-04-09 10:20:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

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

        Assertions.assertThatThrownBy(() -> {
            projectService.update(project);
        }).isInstanceOf(BizException.class);
    }


    @Order(2)
    @Test
    public void testUpdate2() {
        Project project = createProject();
        project.setId(id);
        project.setTitle("updateValue");
        projectService.update(project);
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
        Optional<Project> optional = projectService.findById(id);
        assertThat(optional.get().getSex() == SexEnum.FEMALE).isEqualTo(true);
    }

    @Order(5)
    @Test
    public void testDeleteById() {
        int count = projectService.deleteById(id);
        assertThat(count).isEqualTo(1);
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
        return project;
    }
}
