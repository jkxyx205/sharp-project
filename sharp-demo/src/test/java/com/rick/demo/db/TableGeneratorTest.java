package com.rick.demo.db;

import com.rick.db.plugin.dao.core.TableGenerator;
import com.rick.demo.module.project.domain.entity.group.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class TableGeneratorTest {

    @Autowired
    private TableGenerator tableGenerator;

    @Test
    public void createTable() {
        tableGenerator.createTable(Task.class);
    }

}