package com.rick.admin.core;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.plugin.generator.TableGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class TableGeneratorTest {

    @Autowired
    private TableGenerator tableGenerator;

    @Test
    public void generateTable() {
//        tableGenerator.createTable(CodeDescription.class);
//        tableGenerator.createTable(ComplexModel.class);
        tableGenerator.createTable(Student.class);
    }
}
