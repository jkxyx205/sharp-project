package com.rick.demo.db;

import com.rick.demo.module.project.dao.ProjectDAO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick
 * @createdAt 2022-05-01 14:36:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectDeleteTest {

    @Autowired
    private ProjectDAO projectDAO;

    @Order(1)
    @Test
    public void testDelete1() {
        projectDAO.deleteLogicallyById(667039017908334592L);
    }

    @Order(2)
    @Test
    public void testDelete2() {
        projectDAO.delete("id", Arrays.asList(667039017908334592L));
    }

    @Order(3)
    @Test
    public void testDelete3() {
        projectDAO.delete("id", Arrays.asList(667039017912528896L));
    }



}
