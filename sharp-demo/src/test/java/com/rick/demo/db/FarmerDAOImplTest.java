package com.rick.demo.db;

import com.rick.demo.module.project.dao.FarmerDAO;
import com.rick.demo.module.project.domain.entity.Farmer;
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
public class FarmerDAOImplTest {

    @Autowired
    private FarmerDAO farmerDAO;

    @Test
    public void testSave() {
        farmerDAO.insert(Farmer.builder()
                .name("Rick")
                .build());
    }

}
