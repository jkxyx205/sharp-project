package com.rick.demo.db;

import com.rick.demo.module.project.dao.Message2DAO;
import com.rick.demo.module.project.dao.MessageDAO;
import com.rick.demo.module.project.domain.entity.Message;
import com.rick.demo.module.project.domain.entity.Message2;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTest {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private Message2DAO message2DAO;


    @Order(0)
    @Test
    public void testInsert() {
        messageDAO.insert(Message.builder().text("hello world").build());
    }

    @Order(1)
    @Test
    public void testSelect() {
        Message message = messageDAO.selectByParams("text=hello world").get(0);
    }


    @Order(3)
    @Test
    public void testInsert2() {
        message2DAO.insert(Message2.builder().text("hello world").build());
    }

    @Order(4)
    @Test
    public void testSelect2() {
        Message2 message = message2DAO.selectById(617773294569054208L).get();
        assertThat("hello world").isEqualTo(message.getText());
    }
}
