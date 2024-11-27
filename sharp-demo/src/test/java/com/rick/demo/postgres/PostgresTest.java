package com.rick.demo.postgres;

import com.rick.db.config.SharpDatabaseProperties;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.demo.module.postgres.entity.PgUser;
import com.rick.demo.module.postgres.entity.PgUserItem;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
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

    @Autowired
    private EntityDAO<PgUserItem, Long> pgUserItemDAO;

    @Test
    public void testInset() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            PgUser pgUser = PgUser.builder().age((short) 32).name("Tom").build();
            pgUser.setPgUserItemList(Arrays.asList(PgUserItem.builder().name("Item-1").build()));
            pgUserDAO.insert(pgUser);
        }
    }

    @Test
    public void testUpdate() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            PgUser pgUser = PgUser.builder().id(893206773396156416L).age((short) 40).name("Tom").build();
            pgUser.setPgUserItemList(Arrays.asList(
                    PgUserItem.builder().id(893206773593288704L).name("Item-11").build(),
                    PgUserItem.builder().name("Item-2").build())
            );
            pgUserDAO.update(pgUser);
        }
    }

    @Test
    public void testUpdate2() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            PgUser pgUser = PgUser.builder().id(893206773396156416L).age((short) 99).name("Tom").build();
            pgUser.setPgUserItemList(Arrays.asList(
                    PgUserItem.builder().id(893206773593288704L).name("Item-11").build(),
                    PgUserItem.builder().name("Item-3").build()) // 删除 Item-2 添加 Item-3
            );
            pgUserDAO.update(pgUser);
        }
    }

    @Test
    public void testQuery() {
        if ("postgres".equals(sharpDatabaseProperties.getType())) {
            Optional<PgUser> optional = pgUserDAO.selectById(893206773396156416L);
            PgUser pgUser = optional.get();
            System.out.println(pgUser.getName());

            PgUserItem pgUserItem = pgUserItemDAO.selectById(893206773593288704L).get();
            System.out.println(pgUserItem.getName());
        }
    }

}
