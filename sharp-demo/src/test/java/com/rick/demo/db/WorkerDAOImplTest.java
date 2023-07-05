package com.rick.demo.db;

import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.dao.WorkerDAO;
import com.rick.demo.module.project.domain.entity.Worker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-03-22 12:26:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkerDAOImplTest {

    @Autowired
    private WorkerDAO workerDAO;

    @Test
    public void testSave() {
        Worker worker = Worker.builder()
                .name("Rick")
                .build();
        workerDAO.insert(worker);
        assertThat(worker.getId()).isNotNull();
    }

    @Test
    public void testSaveByInsert() {
        int affectRows = SQLUtils.insert("t_worker",
                Params.builder(1).pv("name", "QQ").build());

        assertThat(affectRows).isEqualTo(1);
    }

    @Test
    public void testSaveByInsertGetId() {
        Number id = SQLUtils.insertAndReturnKey("t_worker",
                Params.builder(1).pv("name", "QQ2").build(), "id");
        assertThat(id.longValue() > 70).isEqualTo(true);
    }

    @Test
    public void testDeleteAll() {
        workerDAO.deleteAll();
    }


}
