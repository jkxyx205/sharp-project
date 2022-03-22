package com.rick.demo.db;

import com.rick.db.plugin.dao.core.TableGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;


@SpringBootTest
public class TableGeneratorTest {

    @Autowired
    private TableGenerator tableGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void createTable() {
//        jdbcTemplate.execute("DROP TABLE t_task");
//        tableGenerator.createTable(Task.class);
    }

}