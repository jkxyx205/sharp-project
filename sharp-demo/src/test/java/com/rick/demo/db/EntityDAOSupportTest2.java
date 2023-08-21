package com.rick.demo.db;

import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.demo.module.book.dao.DogDAO;
import com.rick.demo.module.book.entity.Dog;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Rick.Xu
 * @date 2023/8/22 01:37
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityDAOSupportTest2 {

    @Resource
    private EntityDAO<Dog, Long> dogDAO2;

    @Resource
    private DogDAO dogDAO;

    @Test
    public void test() {
        System.out.println(dogDAO2 == dogDAO);
        Assertions.assertThat(dogDAO2 == dogDAO).isEqualTo(true);
    }
}
