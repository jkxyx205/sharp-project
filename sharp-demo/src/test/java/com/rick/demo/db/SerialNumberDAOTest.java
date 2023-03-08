package com.rick.demo.db;

import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.support.BaseCodeEntityIdFillService;
import com.rick.demo.module.code.dao.SerialNumberDAO;
import com.rick.demo.module.code.entity.SerialNumber;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Rick
 * @createdAt 2023-03-08 22:22:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SerialNumberDAOTest {

    @Autowired
    private SerialNumberDAO serialNumberDAO;

    @Autowired
    private BaseCodeEntityIdFillService idFillService;

    @Test
    public void testSave() {
        assertThrows(BizException.class, ()-> {
            serialNumberDAO.insert(SerialNumber.builder()
                    .code("11")
                    .status(SerialNumber.SerialNumberEnum.AVAILABLE)
                    .build());
        });
    }

    @Test
    public void testSaveOrUpdate() {
        serialNumberDAO.insertOrUpdate(SerialNumber.builder()
                .code("11")
                .status(SerialNumber.SerialNumberEnum.SOLD)
                .build());
    }

    @Test
    public void testFindByCode() {
        SerialNumber serialNumber = serialNumberDAO.selectByCode("11").get();
        assertThat(serialNumber.getStatus()).isEqualTo(SerialNumber.SerialNumberEnum.SOLD);
    }

    @Test
    public void testFillId() {
        SerialNumber serialNumber = SerialNumber.builder().code("11").build();
        idFillService.fill(serialNumber);
        assertThat(serialNumber.getId()).isEqualTo(664984730659258368L);
    }
}