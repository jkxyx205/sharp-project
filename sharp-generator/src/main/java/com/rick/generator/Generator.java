package com.rick.generator;


import com.rick.common.util.ClassUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.db.plugin.dao.core.TableGenerator;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/24 15:37
 */
public class Generator {

    private final TableGenerator tableGenerator;

    public static final String CONTROLLER = "controller";

    public static final String FORM_PAGE = "formPage";

    public static final String REPORT = "report";

    public static final String REPORT_TEST_PATH = "reportTestPath";

    public static final String REPORT_TEST_PACKAGE = "reportTestPackage";

    public Generator(TableGenerator tableGenerator) {
        this.tableGenerator = tableGenerator;
    }

    public void execute(Class<? extends SimpleEntity> entityClass, String rootPackagePath, Map<String, Object> config) throws IOException {
        String name = entityClass.getSimpleName();
        String rootPackageName = StringUtils.substringBeforeLast(entityClass.getPackage().getName(), ".");

        // 1. 创建 table
        tableGenerator.createTable(entityClass);
        // 2. 创建模版
        // 2.1 DAO
        generatorDAO(name, rootPackageName, rootPackagePath,  "dao", BaseCodeEntity.class.isAssignableFrom(entityClass));
        // 2.2 service
        generatorService(name, rootPackageName, rootPackagePath,  "service");
        // 2.3 controller
        if ((Boolean) config.get(CONTROLLER) == true) {
            generatorBaseFormController(name, rootPackageName, rootPackagePath,  "controller", (String)config.get(FORM_PAGE));
        }
        // 2.4 report
        if ((Boolean) config.get(REPORT) == true) {
            generatorReport(entityClass, name, (String) config.get(REPORT_TEST_PATH), (String) config.get(REPORT_TEST_PACKAGE));
            // 运行 report.java
            new Thread(() -> {
                try {
                    exec("mvn test -Dtest="+name+"Test#testReport");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    private void generatorDAO(String name, String rootPackageName, String rootPackagePath, String curPackageName, boolean isCodeEntity) throws IOException {
        String daoCodeTemplate = daoCodeTemplate(name, rootPackageName, isCodeEntity);
        // 写文件
        File daoPackage = mkdirPackage(rootPackagePath, curPackageName);
        FileUtils.writeStringToFile(new File(daoPackage,name + "DAO.java"), daoCodeTemplate, "UTF-8");
    }

    private String daoCodeTemplate(String name, String rootPackageName, boolean isCodeEntity) {
        String template = "package ${PACKAGE_NAME}.dao;\n" +
                "\n" +
                "import com.rick.db.plugin.dao.core.Entity"+(isCodeEntity ? "Code" : "")+"DAOImpl;\n" +
                "import ${PACKAGE_NAME}.entity.${NAME};\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "/**\n" +
                " * @author Rick.Xu\n" +
                " * @date "+ Time2StringUtils.format(LocalDateTime.now()) +"\n" +
                " */\n" +
                "@Repository\n" +
                "public class ${NAME}DAO extends Entity"+(isCodeEntity ? "Code" : "")+"DAOImpl<${NAME}, Long> {\n" +
                "\n" +
                "}";

        return template.replace("${PACKAGE_NAME}", rootPackageName)
                .replace("${NAME}", name);
    }

    private void generatorService(String name, String rootPackageName, String rootPackagePath, String curPackageName) throws IOException {
        String serviceCodeTemplate = serviceCodeTemplate(name, rootPackageName);
        // 写文件
        File servicePackage = mkdirPackage(rootPackagePath, curPackageName);
        FileUtils.writeStringToFile(new File(servicePackage,name + "Service.java"), serviceCodeTemplate, "UTF-8");
    }

    private String serviceCodeTemplate(String name, String rootPackageName) {
        String camelName = (name.substring(0,1).toLowerCase() + name.substring(1));
        String template = "package ${PACKAGE_NAME}.service;\n" +
                "\n" +
                "import ${PACKAGE_NAME}.dao.${NAME}DAO;\n" +
                "import ${PACKAGE_NAME}.entity.${NAME};\n" +
                "import com.rick.db.service.BaseServiceImpl;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "/**\n" +
                " * @author Rick.Xu\n" +
                " * @date "+ Time2StringUtils.format(LocalDateTime.now()) +"\n" +
                " */\n" +
                "@Service\n" +
                "public class ${NAME}Service extends BaseServiceImpl<${NAME}DAO, ${NAME}> {\n" +
                "\n" +
                "    public ${NAME}Service(${NAME}DAO "+camelName+"DAO) {\n" +
                "        super("+camelName+"DAO);\n" +
                "    }\n" +
                "}";



        return template.replace("${PACKAGE_NAME}", rootPackageName)
                .replace("${NAME}", name);
    }

    private void generatorBaseFormController(String name, String rootPackageName, String rootPackagePath, String curPackageName, String formPage) throws IOException {
        Assert.hasText(formPage);
        String controllerCodeTemplate = controllerCodeTemplate(name, rootPackageName, formPage);
        // 写文件
        File controllerPackage = mkdirPackage(rootPackagePath, curPackageName);
        FileUtils.writeStringToFile(new File(controllerPackage,name + "Controller.java"), controllerCodeTemplate, "UTF-8");
    }

    private String controllerCodeTemplate(String name, String rootPackageName, String formPage) {
        String camelName = (name.substring(0,1).toLowerCase() + name.substring(1));
        String template = "package ${PACKAGE_NAME}.controller;\n" +
                "\n" +
                "import com.rick.admin.common.api.BaseFormController;\n" +
                "import ${PACKAGE_NAME}.entity.${NAME};\n" +
                "import ${PACKAGE_NAME}.service.${NAME}Service;\n" +
                "import lombok.AccessLevel;\n" +
                "import lombok.experimental.FieldDefaults;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "\n" +
                "/**\n" +
                " * @author Rick.Xu\n" +
                " * @date "+ Time2StringUtils.format(LocalDateTime.now()) +"\n" +
                " */\n" +
                "@Controller\n" +
                "@RequestMapping(\""+camelName+"s\")\n" +
                "@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)\n" +
                "public class ${NAME}Controller extends BaseFormController<${NAME}, ${NAME}Service> {\n" +
                "\n" +
                "    public ${NAME}Controller(${NAME}Service "+camelName+"Service) {\n" +
                "        super("+camelName+"Service, \""+formPage+"\");\n" +
                "    }\n" +
                "}";

        return template.replace("${PACKAGE_NAME}", rootPackageName)
                .replace("${NAME}", name);
    }

    private void generatorReport(Class<? extends SimpleEntity> entityClass, String name, String reportTestPath, String reportTestPackage) throws IOException {
        Assert.hasText(reportTestPath);
        Assert.hasText(reportTestPackage);
        String reportCodeTemplate = reportCodeTemplate(entityClass, name, reportTestPackage);
        // 写文件
        FileUtils.writeStringToFile(new File(new File(reportTestPath),name + "Test.java"), reportCodeTemplate, "UTF-8");
    }

    private String reportCodeTemplate(Class<? extends SimpleEntity> entityClass, String name, String reportTestPackage) {
        EntityDAO entityDAO = EntityDAOManager.getEntityDAO(entityClass);
        Map<String, Column> columnNameMap = entityDAO.getTableMeta().getColumnNameMap();
        Map<String, Field> fieldMap = entityDAO.getTableMeta().getFieldMap();
        Map<String, String> columnNameToPropertyNameMap = entityDAO.getColumnNameToPropertyNameMap();

        StringBuilder queryFiledBuilder = new StringBuilder();
        StringBuilder columBuilder = new StringBuilder();

        List<String> columnNames = entityDAO.getTableMeta().getSortedColumns();

        int index = 1; // 控制样式
        for (String columnName : columnNames) {
            Column column = columnNameMap.get(columnName);

            if (SharpDbConstants.ID_COLUMN_NAME.equals(columnName) || SharpDbConstants.LOGIC_DELETE_COLUMN_NAME.equals(columnName)) {
                continue;
            }

            String comment = column == null ? "" : StringUtils.defaultIfBlank(column.comment(), "");

            String propertyName = columnNameToPropertyNameMap.get(columnName);
            Field field = fieldMap.get(propertyName);

            // 是否是字典
            boolean isDictValue = field.getType().isEnum() || field.getDeclaringClass() == DictValue.class;
            String type = null;
            if (isDictValue) {
                if (field.getType().isEnum()) {
                    type = field.getType().getSimpleName();
                } else {
                    String dictValueProperty = StringUtils.substringBefore(propertyName, ".");
                    try {
                        type = entityClass.getDeclaredField(dictValueProperty).getAnnotation(DictType.class).type();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // 设置 ReportColumn
            if (isDictValue) {
                queryFiledBuilder.append((index == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.SELECT, \""+type+"\"),\n");
            } else if (field.getType() == LocalDateTime.class) {
                queryFiledBuilder.append((index == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.DATE_RANGE),\n");
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                Class<?> clazz = ClassUtils.getFieldGenericClass(field);
                if (clazz == DictValue.class) {
                    type = field.getAnnotation(DictType.class).type();
                    queryFiledBuilder.append((index == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.MULTIPLE_SELECT, \""+type+"\"),\n");
                }
            } else {
                queryFiledBuilder.append((index == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\"),\n");
            }

            if (field.getType() == LocalDateTime.class) {
                columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false, null, Arrays.asList(\"localDateTimeConverter\")),\n");
            } else if (isDictValue) { // 字典
                columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"dictConverter\")),\n");
            } else if(Collection.class.isAssignableFrom(field.getType())) {
                Class<?> clazz = ClassUtils.getFieldGenericClass(field);
                if (clazz.isEnum()) {
                    type = clazz.getSimpleName();
                    columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"arrayDictConverter\")),\n");
                } else if (clazz == DictValue.class) {
                    type = field.getAnnotation(DictType.class).type();
                    columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"arrayDictConverter\")),\n");
                } else {
                    columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\"),\n");
                }
            } else {
                columBuilder.append((index == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\"),\n");
            }

            index++;
        }

        String template = "package "+reportTestPackage+";\n" +
                "\n" +
                "import com.rick.db.service.support.Params;\n" +
                "import com.rick.report.core.entity.Report;\n" +
                "import com.rick.report.core.model.HiddenReportColumn;\n" +
                "import com.rick.report.core.model.QueryField;\n" +
                "import com.rick.report.core.model.ReportColumn;\n" +
                "import com.rick.report.core.model.SordEnum;\n" +
                "import com.rick.report.core.service.ReportService;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.boot.test.context.SpringBootTest;\n" +
                "\n" +
                "import java.util.Arrays;\n" +
                "\n" +
                "/**\n" +
                " * @author Rick.Xu\n" +
                " * @date "+ Time2StringUtils.format(LocalDateTime.now()) +"\n" +
                " */\n" +
                "@SpringBootTest\n" +
                "public class ${NAME}Test {\n" +
                "\n" +
                "    @Autowired\n" +
                "    private ReportService reportService;\n" +
                "\n" +
                "    @Test\n" +
                "    public void testReport() {\n" +
                "        Report report = Report.builder()\n" +
                "                .code(\""+entityDAO.getTableMeta().getTableName()+"\")// 　建议和数据库表名保持一致\n" +
                "                .tplName(\"tpl/ajax_list\") // 拷贝模版页面到指定目录\n" +
                "//                .tplName(\"tpl/list\") // 没有特殊要求使用模版页面\n" +
                "//                .tplName(\"tpl/ajax_list\") // 没有特殊要求使用模版页面\n" +
                "                .name(\"学生信息\")\n" +
                "                .reportAdviceName(\"operatorReportAdvice\")\n" +
                "                .additionalInfo(Params.builder(1).pv(\"operator-bar\", true) // 显示操作按钮\n" +
                "                        .pv(\"endpoint\", \""+(name.toLowerCase() + "s")+"\")\n" +
                "                        .build()) // 显示操作按钮\n" +
                "                .querySql(\""+entityDAO.getSelectConditionSQL(Collections.emptyMap()).replace("\"", "\\\"").replaceAll(":is_deleted", "0")+"\")\n" +
                "                .queryFieldList(Arrays.asList(\n" +
                "                        "+queryFiledBuilder.deleteCharAt(queryFiledBuilder.length() - 2)+"" +
                "                ))\n" +
                "                .reportColumnList(Arrays.asList(\n" +
                "                        new HiddenReportColumn(\"id\"),\n" +
                "                        "+columBuilder.deleteCharAt(columBuilder.length() - 2)+"" +
                "                ))\n" +
                "                .pageable(true)\n" +
                "                .sidx(\"createTime\")\n" +
                "                .sord(SordEnum.DESC)\n" +
                "                .build();\n" +
                "\n" +
                "        reportService.saveOrUpdate(report);\n" +
                "        System.out.println(\"=========reportId = \" + report.getId());\n" +
                "    }\n" +
                "}\n";

        return template.replace("${NAME}", name);
    }

    private File mkdirPackage(String rootPackagePath, String curPackageName) {
        File daoPackage = new File(rootPackagePath, curPackageName);
        if (!daoPackage.exists()) {
            daoPackage.mkdirs();
        }
        return daoPackage;
    }

    private void exec(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        InputStream in = process.getInputStream();
        byte[] cache = new byte[1024];
        int readSize = 0;   //每次读取的字节长度
        ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
        while ((readSize = in.read(cache)) > 0) {
            infoStream.write(cache, 0, readSize);
        }

        System.out.println(infoStream);
    }
}
