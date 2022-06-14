package com.rick.demo.db;

import com.rick.demo.module.project.dao.MaterialDAO;
import com.rick.demo.module.project.domain.entity.Material;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick
 * @createdAt 2022-03-22 12:26:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialDAOImplTest {

    @Autowired
    private MaterialDAO materialDAO;

    @Test
    public void testSave() {
        materialDAO.insert(Material.builder()
                .id("mldnfind" + System.currentTimeMillis())
                .name("Rick")
                .build());
    }

}
