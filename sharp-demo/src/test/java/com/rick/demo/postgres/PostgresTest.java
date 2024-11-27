package com.rick.demo.postgres;

import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.demo.module.postgres.entity.PgUser;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresTest {

    @Autowired
    private SharpDatabaseProperties sharpDatabaseProperties;

    @Autowired
    private EntityDAO<PgUser, Long> pgUserDAO;

    @Test
    public void testInset() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            PgUser pgUser = PgUser.builder().name("Tom").build();
            pgUserDAO.insert(pgUser);
        }
    }

    @Test
    public void testQuery() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            Optional<PgUser> optional = pgUserDAO.selectById(1L);
            PgUser pgUser = optional.get();
            System.out.println(pgUser.getName());
        }
    }

}
