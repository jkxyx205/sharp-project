package com.rick.admin.core;

import com.rick.admin.module.student.entity.Student;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.support.Params;
import com.rick.generator.Generator;
import com.rick.generator.control.FormLayoutEnum;
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
//                        .pv(Generator.GENERATOR_CODE, true) // 是否创建 code template；如果没有指定，则没有就创建，否则不覆盖
                        .pv(Generator.FORM_PAGE, "demos/student/edit-thymeleaf") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")

                        // 表名默认 control.html
                        .pv(Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student")
                        .pv(Generator.CONTROL_LABEL, true)
                        .pv(Generator.FORM_LAYOUT, FormLayoutEnum.HORIZONTAL) // 默认就是 HORIZONTAL
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
                        .pv(Generator.GENERATOR_CODE, false) // 是否创建 code template
                        .pv(Generator.FORM_PAGE, "demos/student/edit-vue") // 编辑页面路径

                        .pv(Generator.REPORT, true) // 是否创建 report
                        .pv(Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo")
                        .pv(Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo")

                        // 表名默认 control.html
                        .pv(Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student")
                        .pv(Generator.CONTROL_LABEL, true)
                        .pv(Generator.FORM_LAYOUT, FormLayoutEnum.INLINE)
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
                        .pv(Generator.GENERATOR_CODE, false) // 是否创建 code template
                        .pv(Generator.FORM_PAGE, "demos/student/edit-react") // 编辑页面路径

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

    private void executeInsertSql() {
        SQLUtils.execute("INSERT INTO `t_student` (`id`, `code`, `name`, `gender`, `email`, `birthday`, `age`, `is_marriage`, `unit_code`, `attachments`, `avatar`, `hobby_list`, `material_type`, `category`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)\n" +
                "VALUES\n" +
                "\t(1, '0002', '李四', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '[]', NULL, NULL, NULL, NULL, NULL, NULL, 0),\n" +
                "\t(2, '0001', '张三', 'M', 'fsadfsaf@163.com', '1992-11-12', 19, 1, 'EA', '[{\\\"name\\\": \\\"Rick\\\", \\\"fullName\\\": \\\"Rick\\\", \\\"fullPath\\\": \\\"null/null\\\"}]', '{\\\"name\\\": \\\"Tom\\\", \\\"fullName\\\": \\\"Tom\\\", \\\"fullPath\\\": \\\"null/null\\\"}', '[\\\"FOOTBALL\\\", \\\"BASKETBALL\\\"]', '[{\\\"code\\\": \\\"M1\\\"}]', 'MATERIAL', 'fsdfdasf', 1, '2024-08-24 22:57:24', 1, '2024-09-01 10:32:41', 0)");
    }
}
