package com.rick.admin.core;

import com.rick.admin.module.student.entity.Student;
import com.rick.common.util.Maps;
import com.rick.db.repository.TableDAO;
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

    @Autowired
    TableDAO tableDAO;

    @Test
    public void testGeneratorThymeleaf() throws IOException {
        tableDAO.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Maps.of(
//                        ,Generator.GENERATOR_CODE, true) // 是否创建 code template；如果没有指定，则没有就创建，否则不覆盖
                        Generator.PROJECT, "admin",
                        Generator.FORM_PAGE, "demos/student/edit-thymeleaf",// 编辑页面路径

                        Generator.REPORT, true, // 是否创建 report
                        Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo",
                        Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo",

                        // 表名默认 control.html
                        Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student",
                        Generator.CONTROL_LABEL, true,
                        Generator.FORM_LAYOUT, FormLayoutEnum.HORIZONTAL)); // 默认就是 HORIZONTAL
//                        ,Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)



        executeInsertSql();
    }

    @Test
    public void testGeneratorVue() throws IOException {
        tableDAO.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Maps.of(
                        Generator.GENERATOR_CODE, false,// 是否创建 code template
                        Generator.PROJECT, "admin",
                        Generator.FORM_PAGE, "demos/student/edit-vue", // 编辑页面路径

                        Generator.REPORT, true, // 是否创建 report
                        Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo",
                        Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo",

                        // 表名默认 control.html
                        Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student",
                        Generator.CONTROL_LABEL, true,
                        Generator.FORM_LAYOUT, FormLayoutEnum.INLINE));
//                        ,Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)



        executeInsertSql();
    }

    @Test
    public void testGeneratorReact() throws IOException {
        tableDAO.execute("Drop TABLE t_student");

        generator.execute(Student.class,
                "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/java/com/rick/admin/module/student",
                Maps.of(Generator.GENERATOR_CODE, false, // 是否创建 code template
                        Generator.PROJECT, "admin",
                        Generator.FORM_PAGE, "demos/student/edit-react", // 编辑页面路径

                        Generator.REPORT, true, // 是否创建 report
                        Generator.REPORT_TEST_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/test/java/com/rick/admin/demo",
                        Generator.REPORT_TEST_PACKAGE, "com.rick.admin.demo",

                        // 表名默认 control.html
                        Generator.CONTROL_PATH, "/Users/rick/Space/Workspace/sharp-project/sharp-admin/src/main/resources/templates/demos/student",
                        Generator.CONTROL_LABEL, true));
//                        ,Generator.CONTROL_RENDER_TYPE, RenderTypeEnum.THYMELEAF)



        executeInsertSql();
    }

    private void executeInsertSql() {
        tableDAO.execute("INSERT INTO `t_student` (`id`, `code`, `name`, `gender`, `email`, `birthday`, `age`, `is_marriage`, `unit_code`, `attachments`, `avatar`, `hobby_list`, `material_type`, `category`, `is_available`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)\n" +
                "VALUES\n" +
                "\t(1, '0002', '李四', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '[]', NULL, 0, NULL, NULL, NULL, NULL, NULL, 0),\n" +
                "\t(2, '0001', '张三', 'M', 'fsadfsaf@163.com', '1992-11-12', 19, 1, 'EA', '[{\\\"name\\\": \\\"Rick\\\", \\\"fullName\\\": \\\"Rick\\\", \\\"fullPath\\\": \\\"null/null\\\"}]', '{\\\"id\\\": \\\"861952755730780160\\\", \\\"url\\\": \\\"http://localhost:7892/images/861952755722391552.jpeg\\\", \\\"name\\\": \\\"avatar\\\", \\\"path\\\": \\\"861952755722391552.jpeg\\\", \\\"size\\\": 68783, \\\"fullName\\\": \\\"avatar.jpeg\\\", \\\"fullPath\\\": \\\"images/861952755722391552.jpeg\\\", \\\"extension\\\": \\\"jpeg\\\", \\\"groupName\\\": \\\"images\\\", \\\"contentType\\\": \\\"image/jpeg\\\"}', '[\\\"FOOTBALL\\\", \\\"BASKETBALL\\\"]', '[{\\\"code\\\": \\\"M1\\\"}]', 'MATERIAL', 0, 'fsdfdasf', 1, '2024-08-24 22:57:24', 1, '2024-09-01 10:32:41', 0),\n" +
                "\t(861949024788320256, '00012', '库房', 'M', '1050216579@qq.com', '2024-08-28', 2, 1, 'EA', '[{\\\"id\\\": \\\"861948954584059904\\\", \\\"url\\\": \\\"http://localhost:7892/attachments/861948954575671296.xls\\\", \\\"name\\\": \\\"报关单\\\", \\\"path\\\": \\\"861948954575671296.xls\\\", \\\"size\\\": 65536, \\\"fullName\\\": \\\"报关单.xls\\\", \\\"fullPath\\\": \\\"attachments/861948954575671296.xls\\\", \\\"extension\\\": \\\"xls\\\", \\\"groupName\\\": \\\"attachments\\\", \\\"contentType\\\": \\\"application/vnd.ms-excel\\\"}, {\\\"id\\\": \\\"861948954584059905\\\", \\\"url\\\": \\\"http://localhost:7892/attachments/861948954575671297.zip\\\", \\\"name\\\": \\\"鱼皮 - Java 学习路线一条龙版本 V2.mindnode\\\", \\\"path\\\": \\\"861948954575671297.zip\\\", \\\"size\\\": 408174, \\\"fullName\\\": \\\"鱼皮 - Java 学习路线一条龙版本 V2.mindnode.zip\\\", \\\"fullPath\\\": \\\"attachments/861948954575671297.zip\\\", \\\"extension\\\": \\\"zip\\\", \\\"groupName\\\": \\\"attachments\\\", \\\"contentType\\\": \\\"application/zip\\\"}]', '{\\\"id\\\": \\\"861952755730780160\\\", \\\"url\\\": \\\"http://localhost:7892/images/861952755722391552.jpeg\\\", \\\"name\\\": \\\"avatar\\\", \\\"path\\\": \\\"861952755722391552.jpeg\\\", \\\"size\\\": 68783, \\\"fullName\\\": \\\"avatar.jpeg\\\", \\\"fullPath\\\": \\\"images/861952755722391552.jpeg\\\", \\\"extension\\\": \\\"jpeg\\\", \\\"groupName\\\": \\\"images\\\", \\\"contentType\\\": \\\"image/jpeg\\\"}', '[\\\"BASKETBALL\\\", \\\"FOOTBALL\\\"]', '[{\\\"code\\\": \\\"M1\\\"}, {\\\"code\\\": \\\"M4\\\"}]', 'PURCHASING_ORG', 1, '这里是简介', 1, '2024-09-02 10:50:40', 1, '2024-09-03 07:33:25', 0),\n" +
                "\t(861953021922283520, '111', '李岁2', 'F', 'fsadfsaf@163.com', '2024-09-02', 23, 0, 'KG', '[{\\\"id\\\": \\\"861952982621655040\\\", \\\"url\\\": \\\"http://localhost:7892/attachments/861952982609072128.jpeg\\\", \\\"name\\\": \\\"avatar\\\", \\\"path\\\": \\\"861952982609072128.jpeg\\\", \\\"size\\\": 68783, \\\"fullName\\\": \\\"avatar.jpeg\\\", \\\"fullPath\\\": \\\"attachments/861952982609072128.jpeg\\\", \\\"extension\\\": \\\"jpeg\\\", \\\"groupName\\\": \\\"attachments\\\", \\\"contentType\\\": \\\"image/jpeg\\\"}, {\\\"id\\\": \\\"861952982625849344\\\", \\\"url\\\": \\\"http://localhost:7892/attachments/861952982613266432.csv\\\", \\\"name\\\": \\\"Google Passwords\\\", \\\"path\\\": \\\"861952982613266432.csv\\\", \\\"size\\\": 13191, \\\"fullName\\\": \\\"Google Passwords.csv\\\", \\\"fullPath\\\": \\\"attachments/861952982613266432.csv\\\", \\\"extension\\\": \\\"csv\\\", \\\"groupName\\\": \\\"attachments\\\", \\\"contentType\\\": \\\"text/csv\\\"}, {\\\"id\\\": \\\"861967956609896448\\\", \\\"url\\\": \\\"http://localhost:7892/attachments/861967956597313536.pdf\\\", \\\"name\\\": \\\"线上VI指南\\\", \\\"path\\\": \\\"861967956597313536.pdf\\\", \\\"size\\\": 463981, \\\"fullName\\\": \\\"线上VI指南.pdf\\\", \\\"fullPath\\\": \\\"attachments/861967956597313536.pdf\\\", \\\"extension\\\": \\\"pdf\\\", \\\"groupName\\\": \\\"attachments\\\", \\\"contentType\\\": \\\"application/pdf\\\"}]', '{\\\"id\\\": \\\"861976822831681536\\\", \\\"url\\\": \\\"http://localhost:7892/images/861976822642937856.webp\\\", \\\"name\\\": \\\"F5V8WS3asAAFJsC\\\", \\\"path\\\": \\\"861976822642937856.webp\\\", \\\"size\\\": 81884, \\\"fullName\\\": \\\"F5V8WS3asAAFJsC.webp\\\", \\\"fullPath\\\": \\\"images/861976822642937856.webp\\\", \\\"extension\\\": \\\"webp\\\", \\\"groupName\\\": \\\"images\\\", \\\"contentType\\\": \\\"image/webp\\\"}', '[\\\"BASKETBALL\\\", \\\"FOOTBALL\\\"]', '[{\\\"code\\\": \\\"M1\\\"}, {\\\"code\\\": \\\"M3\\\"}, {\\\"code\\\": \\\"M4\\\"}]', 'SALES_ORG', 0, 'hello world', 1, '2024-09-02 11:06:33', 1, '2024-09-03 07:35:37', 0);\n");
    }
}
