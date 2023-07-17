package com.rick.demo.db;

import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOSupport;
import com.rick.demo.module.book.entity.Cat;
import com.rick.demo.module.book.entity.Dog;
import com.rick.demo.module.book.service.CatService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityDAOSupportTest {

    @Autowired
    private EntityDAOSupport entityDAOSupport;

    @Autowired(required = false)
    @Qualifier("catDAO")
    private EntityDAO catDAO;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CatService catService;

    @Order(1)
    @Test
    public void testEntityHandlerInsert() {
        EntityDAO<Cat, Long> catDAO = entityDAOSupport.getEntityDAO(Cat.class);
        catDAO.insert(Cat.builder().name("Tomcat").age(10).build());

        List<Cat> catList = catDAO.selectAll();
        catList.forEach(System.out::println);

        EntityDAO<Cat, Long> catDAO2 = entityDAOSupport.getEntityDAO(Cat.class);
        catDAO2.insert(Cat.builder().name("TomcatND").age(1).build());
    }

    @Order(2)
    @Test
    public void testEntityHandlerCascadeInsert() {
        EntityDAO<Cat, Long> catDAO = entityDAOSupport.getEntityDAO(Cat.class);
        catDAO.insert(Cat.builder().name("Tomcat2")
                .age(10)
                .rewardList(Arrays.asList(Cat.Reward.builder().title("吃饭冠军").build()))
                .build());
    }

    @Order(3)
    @Test
    public void testEntityHandlerSelectById() {
        EntityDAO<Cat, Long> catDAO = entityDAOSupport.getEntityDAO(Cat.class);
        Optional<Cat> optional = catDAO.selectById(706174000581087232L);
        Cat cat = optional.get();
        assertThat(cat.getName()).isEqualTo("Tomcat2");
        assertThat(cat.getRewardList().size()).isEqualTo(1);
        assertThat(cat.getRewardList().get(0).getTitle()).isEqualTo("吃饭冠军");
        System.out.println("------");
        // 没有包扫描处理的，无法通过 Autowired 注入。 spring可以通过 applicationContext.getBean("catDAO", EntityDAO.class);
        // @Autowired(required = false)
        // @Qualifier("catDAO")
        // private EntityDAO catEntityDAO;

//        catEntityDA0 = applicationContext.getBean("catDAO", EntityDAO.class);
        Optional<Cat> optional2 = this.catDAO.selectById(706172121272848384L);
        Cat cat2 = optional2.get();
        assertThat(cat2.getName()).isEqualTo("Tomcat2");
        assertThat(cat2.getRewardList().size()).isEqualTo(1);
        assertThat(cat2.getRewardList().get(0).getTitle()).isEqualTo("吃饭冠军");
    }

    @Order(3)
    @Test
    public void testInsertDog() {
        entityDAOSupport.getEntityDAO(Dog.class).insert(Dog.builder()
                .id("sq" + System.currentTimeMillis())
                .name("Tomcat2")
                .age(10)
                .build());
    }

    @Order(3)
    @Test
    public void testSaveCat() {
        catService.save(Cat.builder().name("save Tomcat").age(15).build());
    }

    @Order(4)
    @Test
    public void testDelete() {
        catDAO.deleteById(712508028410138624L);
    }

}
