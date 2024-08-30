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
    public void testGeneratorThymeleaf() throws IOException {
        SQLUtils.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Params.builder()
                        .pv(Generator.CONTROLLER, true) // 是否创建 controller
                        .pv(Generator.FORM_PAGE, "demos/student/edit-thymeleaf") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")

                        // 表名默认 control.html
                        .pv(Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student")
//                        .pv(Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)

                        .build());

        executeInsertSql();
    }

    @Test
    public void testGeneratorVue() throws IOException {
        SQLUtils.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Params.builder()
                        .pv(Generator.CONTROLLER, true) // 是否创建 controller
                        .pv(Generator.FORM_PAGE, "demos/student/edit-vue") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")

                        // 表名默认 control.html
                        .pv(Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student")
                        .pv(Generator.CONTROL_LABEL, true)
//                        .pv(Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)

                        .build());

        executeInsertSql();
    }

    @Test
    public void testGeneratorReact() throws IOException {
        SQLUtils.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Params.builder()
                        .pv(Generator.CONTROLLER, true) // 是否创建 controller
                        .pv(Generator.FORM_PAGE, "demos/student/edit-react") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")

                        // 表名默认 control.html
                        .pv(Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student")
//                        .pv(Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)

                        .build());

        executeInsertSql();
    }

    private void executeInsertSql() {
        SQLUtils.execute("INSERT INTO `t_student` (`id`, `code`, `name`, `gender`, `email`, `birthday`, `unit_code`, `files`, `avatar`, `hobby_list`, `material_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)\n" +
                "VALUES\n" +
                "\t(858870425453637632, '0001', '张三', 'M', 'fsadfsaf@163.com', '1992-11-12', 'EA', '[{\\\"name\\\": \\\"Rick\\\", \\\"fullName\\\": \\\"Rick\\\", \\\"fullPath\\\": \\\"null/null\\\"}]', '{\\\"name\\\": \\\"Tom\\\", \\\"fullName\\\": \\\"Tom\\\", \\\"fullPath\\\": \\\"null/null\\\"}', '[\\\"FOOTBALL\\\", \\\"BASKETBALL\\\"]', '[\\\"M1\\\"]', 'fsdfdasf', 1, '2024-08-24 22:57:24', 1, '2024-08-30 13:59:14', 0);\n");
    }
}
