package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.common.util.IdGenerator;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-11-25 17:44:00
 */
@SpringBootTest
public class ProjectMapByTableNameDAOTest {

    @Autowired
    private ProjectMapByTableNameDAO projectDAO;

    @AfterAll
    public static void init() {
        DataInit.init();
    }

    @Test
    public void testSaveProject() {
        Map<String, Object> project = createProject();
        project.put("title", "项目标题abcd");
        projectDAO.insert(project);
    }

    @Test
    public void testUpdate() {
        Map<String, Object> project = createProject();
        project.put("id", 479723134929764352L);
        project.put("title", "rick");
        project.put("description", "desc");
        projectDAO.update(project);
    }

    @Test
    public void testUpdate2() {
        projectDAO.update("title, description", Params.builder(3)
                .pv("title", "rick")
                .pv("description", "desc")
                .pv("id", 479723134929764352L)
                .build(), "id = :id");
    }

    @Test
    public void testFindById() {
        Optional<Map<String, Object>> optional4 = projectDAO.selectById(479723134929764352L);
        assertThat(optional4.get().get("title")).isEqualTo("rick");
    }

    @Test
    public void selectByParams() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("title", "rick");
        params.put("description", "desc");
        List<Map<String, Object>> list = projectDAO.selectByParams(params, "title,description", "title = :title AND description = :description");
        assertThat(list.size()).isEqualTo(1);
    }

    public Map<String, Object> createProject() {
        Map<String, Object> project = new HashMap<>();
        project.put("id", IdGenerator.getSequenceId());
        project.put("title", "项目标题 " + System.currentTimeMillis());
        project.put("description", "项目描述");
        project.put("sex", SexEnum.FEMALE);
        project.put("cover_url", "http://");
        project.put("owner_id", 9L);
        project.put("status", UserStatusEnum.NORMAL);
        project.put("address", Address.builder().code("001").detail("苏州").build());
        project.put("list", Lists.newArrayList(Address.builder().code("001").detail("苏州").build()));
        project.put("phone_number", PhoneNumber.builder().code("816").number("18888888888").build().toString());
        project.put(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, false);
        return project;
    }
}
