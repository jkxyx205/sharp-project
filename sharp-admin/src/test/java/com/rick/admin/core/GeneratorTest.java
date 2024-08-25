package com.rick.admin.core;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.support.Params;
import com.rick.generator.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @author Rick.Xu
 * @date 2024/8/24 15:55
 */
@SpringBootTest
public class GeneratorTest {

    @Autowired
    private Generator generator;

    @Test
    public void testGenerator() throws IOException {
        SQLUtils.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Params.builder()
                        .pv(Generator.CONTROLLER, true) // 是否创建 controller
                        .pv(Generator.FORM_PAGE, "demos/student/edit") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")
                        .build());

        SQLUtils.execute("INSERT INTO `t_student` (`id`, `code`, `name`, `gender`, `unit_code`, `files`, `avatar`, `hobby_list`, `material_type`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)\n" +
                "VALUES\n" +
                "\t(858870425453637632, '0001', '张三', 'M', 'EA', '[{\\\"name\\\": \\\"Rick\\\"}]', '{\\\"name\\\": \\\"Tom\\\"}', '[\\\"FOOTBALL\\\", \\\"BASKETBALL\\\"]', '[\\\"HIBE\\\"]', 1, '2024-08-24 22:57:24', 1, '2024-08-24 22:57:24', 0)");
    }
}
