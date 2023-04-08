package com.rick.demo.db;

import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.support.EntityCodeIdFillService;
import com.rick.demo.module.code.dao.SerialNumberDAO;
import com.rick.demo.module.code.entity.SerialNumber;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

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
    private EntityCodeIdFillService idFillService;

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
    public void testFillId1() {
        SerialNumber serialNumber = SerialNumber.builder().code("11").build();
        idFillService.fill(Arrays.asList(serialNumber));
        assertThat(serialNumber.getId()).isEqualTo(664984730659258368L);
    }

    @Test
    public void testFillId2() {
        SerialNumber serialNumber = SerialNumber.builder().code("11").build();
        idFillService.fill(serialNumber);
        assertThat(serialNumber.getId()).isEqualTo(664984730659258368L);
    }

    @Test
    public void testFillId3() {
        List<SerialNumber> list = idFillService.fill(SerialNumber.class, Arrays.asList("11"));
        assertThat(list.get(0).getId()).isEqualTo(664984730659258368L);
    }

    @Test
    public void testFillId4() {
        assertThrows(BizException.class, ()-> {
            idFillService.fill(SerialNumber.class, Arrays.asList("11", "12", "13"));
        });
    }
}