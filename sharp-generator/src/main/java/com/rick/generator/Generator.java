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
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.generator.control.ControlGeneratorManager;
import com.rick.generator.control.RenderTypeEnum;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.rick.common.util.StringUtils.camelToSpinal;

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

    public static final String CONTROL_PATH = "controlPath";

    public static final String CONTROL_LABEL = "controlLabel";

    public static final String CONTROL_RENDER_TYPE = "controlRenderType";

    public Generator(TableGenerator tableGenerator) {
        this.tableGenerator = tableGenerator;
    }

    public void execute(Class<? extends SimpleEntity> entityClass, String rootPackagePath, Map<String, Object> config) throws IOException {
        String entityName = entityClass.getSimpleName();
        String rootPackageName = StringUtils.substringBeforeLast(entityClass.getPackage().getName(), ".");

        // 1. 创建 table
        tableGenerator.createTable(entityClass);
        // 2. 创建模版
        // 2.1 DAO
        generatorDAO(entityName, rootPackageName, rootPackagePath,  "dao", BaseCodeEntity.class.isAssignableFrom(entityClass));
        // 2.2 service
        generatorService(entityName, rootPackageName, rootPackagePath,  "service");
        // 2.3 controller
        if ((Boolean) config.get(CONTROLLER) == true) {
            generatorBaseFormController(entityName, rootPackageName, rootPackagePath,  "controller", (String)config.get(FORM_PAGE));
        }
        // 2.4 report
        if ((Boolean) config.get(REPORT) == true) {
            generatorReport(entityClass, entityName, (String) config.get(REPORT_TEST_PATH), (String) config.get(REPORT_TEST_PACKAGE));
            // 运行 report.java
            new Thread(() -> {
                try {
                    exec("mvn test -Dtest="+entityName+"Test#testReport");
//                    exec("open -a \"Google Chrome\" http://localhost:8081/");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        // 2.5 html
        if (config.get(CONTROL_RENDER_TYPE == null) == null) {
            for (RenderTypeEnum value : RenderTypeEnum.values()) {
                generatorHtml(entityClass, (String) config.get(CONTROL_PATH), value, (Boolean) ObjectUtils.defaultIfNull(config.get(CONTROL_LABEL), false));
            }
        } else {
            generatorHtml(entityClass, (String) config.get(CONTROL_PATH), (RenderTypeEnum) config.get(CONTROL_RENDER_TYPE), (Boolean) ObjectUtils.defaultIfNull(config.get(CONTROL_LABEL), false));
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
                "import com.rick.admin.common.exception.ResourceNotFoundException;\n" +
                "import com.rick.admin.module.student.entity.Student;\n" +
                "import com.rick.admin.module.student.service.StudentService;\n" +
                "import com.rick.db.plugin.dao.core.EntityDAOManager;\n" +
                "import lombok.AccessLevel;\n" +
                "import lombok.experimental.FieldDefaults;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "import org.springframework.web.bind.annotation.PathVariable;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.ResponseBody;\n" +
                "\n" +
                "import java.util.Optional;\n" +
                "\n" +
                "/**\n" +
                " * @author Rick.Xu\n" +
                " * @date "+ Time2StringUtils.format(LocalDateTime.now()) +"\n" +
                " */\n" +
                "@Controller\n" +
                "@RequestMapping(\""+camelName+"s\")\n" +
                "@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)\n" +
                "public class StudentController extends BaseFormController<${NAME}, StudentService> {\n" +
                "\n" +
                "    public StudentController(StudentService studentService) {\n" +
                "        super(studentService, \""+formPage+"\");\n" +
                "    }\n" +
                "\n" +
                "    @GetMapping(\"{id}\")\n" +
                "    @ResponseBody\n" +
                "    public ${NAME} findById(@PathVariable Long id) {\n" +
                "        Optional<${NAME}> byId = baseService.findById(id);\n" +
                "        return getEntityFromOptional(byId, id);\n" +
                "    }\n" +
                "\n" +
                "    protected ${NAME} getEntityFromOptional(Optional<${NAME}> optional, Object key) {\n" +
                "        return optional.orElseThrow(() -> getResourceNotFoundException(key));\n" +
                "    }\n" +
                "\n" +
                "    protected ResourceNotFoundException getResourceNotFoundException(Object key) {\n" +
                "        return new ResourceNotFoundException(comment() + \" id = \" + key + \"不存在\");\n" +
                "    }\n" +
                "\n" +
                "    protected String comment() {\n" +
                "        return EntityDAOManager.getTableMeta(baseService.getBaseDAO().getEntityClass()).getTable().comment();\n" +
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
        StringBuilder queryFiledBuilder = new StringBuilder();
        StringBuilder columBuilder = new StringBuilder();

        AtomicInteger index = new AtomicInteger(1); // 控制样式
        EntityDAO entityDAO = tableResolver(entityClass, resolverInfo -> {
            Field field = resolverInfo.field;
            String columnName = resolverInfo.columnName;
            String propertyName = resolverInfo.propertyName;
            String comment = resolverInfo.comment;

            String type = null;
            boolean isDictValue = resolverInfo.isDictValue;

            if (isDictValue) {
                if (field.getType().isEnum()) {
                    type = field.getType().getSimpleName();
                } else {
                    String dictValueProperty = StringUtils.substringBefore(resolverInfo.propertyName, ".");
                    try {
                        type = entityClass.getDeclaredField(dictValueProperty).getAnnotation(DictType.class).type();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // 设置 ReportColumn
            if (isDictValue) {
                queryFiledBuilder.append((index.get() == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.SELECT, \""+type+"\"),\n");
            } else if (field.getType() == LocalDateTime.class) {
                queryFiledBuilder.append((index.get() == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.DATE_RANGE),\n");
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                Class<?> clazz = ClassUtils.getFieldGenericClass(field);
                if (clazz == DictValue.class) {
                    type = field.getAnnotation(DictType.class).type();
                    queryFiledBuilder.append((index.get() == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\", QueryField.Type.MULTIPLE_SELECT, \""+type+"\"),\n");
                }
            } else {
                queryFiledBuilder.append((index.get() == 1 ? "" : "                        ") + "new QueryField(\""+columnName+"\", \""+comment+"\"),\n");
            }

            if (field.getType() == LocalDateTime.class) {
                columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false, null, Arrays.asList(\"localDateTimeConverter\")),\n");
            } else if (isDictValue) { // 字典
                columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"dictConverter\")),\n");
            } else if(Collection.class.isAssignableFrom(field.getType())) {
                Class<?> clazz = ClassUtils.getFieldGenericClass(field);
                if (clazz.isEnum()) {
                    type = clazz.getSimpleName();
                    columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"arrayDictConverter\")),\n");
                } else if (clazz == DictValue.class) {
                    type = field.getAnnotation(DictType.class).type();
                    columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\", false,\""+type+"\", Arrays.asList(\"arrayDictConverter\")),\n");
                } else {
                    columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\"),\n");
                }
            } else {
                columBuilder.append((index.get() == 1 ? "" : "                        ") + "new ReportColumn(\""+propertyName+"\", \""+comment+"\"),\n");
            }

            index.incrementAndGet();
        });

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
                "                        .pv(\"endpoint\", \""+(camelToSpinal(name) + "s")+"\")\n" +
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

    private void generatorHtml(Class<? extends SimpleEntity> entityClass, String controlPath, RenderTypeEnum renderType, boolean ifGeneratorLabel) throws IOException {
        Assert.hasText(controlPath);

        StringBuilder htmlStringBuilder = new StringBuilder();
        tableResolver(entityClass, resolverInfo -> {
            Field field = resolverInfo.field;

            String type = resolverInfo.dictTypeValue;

            if (resolverInfo.isDictValue) {
                // select
                htmlStringBuilder.append(ControlGeneratorManager.generate(CpnTypeEnum.SELECT, resolverInfo.camelEntityName, resolverInfo.camelPropertyName, resolverInfo.comment, type, renderType, ifGeneratorLabel)).append("\n");
            } else if (field.getType() == String.class) {
                // input
                htmlStringBuilder.append(ControlGeneratorManager.generate(CpnTypeEnum.TEXT, resolverInfo.camelEntityName, resolverInfo.camelPropertyName, resolverInfo.comment, type, renderType, ifGeneratorLabel)).append("\n");
                // textarea
                htmlStringBuilder.append(ControlGeneratorManager.generate(CpnTypeEnum.TEXTAREA, resolverInfo.camelEntityName, resolverInfo.camelPropertyName, resolverInfo.comment, type, renderType, ifGeneratorLabel)).append("\n");
            }
        });

        // 写文件
        FileUtils.writeStringToFile(new File(new File(controlPath), "control-"+renderType.name().toLowerCase()+".html"), htmlStringBuilder.toString(), "UTF-8");
    }

    private EntityDAO tableResolver(Class<? extends SimpleEntity> entityClass, Consumer<ResolverInfo> resolverInfoConsumer) {
        EntityDAO entityDAO = EntityDAOManager.getEntityDAO(entityClass);
        Map<String, Column> columnNameMap = entityDAO.getTableMeta().getColumnNameMap();
        Map<String, Field> fieldMap = entityDAO.getTableMeta().getFieldMap();
        Map<String, String> columnNameToPropertyNameMap = entityDAO.getColumnNameToPropertyNameMap();

        List<String> columnNames = entityDAO.getTableMeta().getSortedColumns();

        for (String columnName : columnNames) {
            if (SharpDbConstants.ID_COLUMN_NAME.equals(columnName) || SharpDbConstants.LOGIC_DELETE_COLUMN_NAME.equals(columnName)) {
                continue;
            }

            String camelEntityName = com.rick.common.util.StringUtils.stringToCamel(entityClass.getSimpleName());
            Column column = columnNameMap.get(columnName);
            String propertyName = columnNameToPropertyNameMap.get(columnName);

            String comment = column == null ? propertyName : column.comment();

            if (Objects.nonNull(column)) {
                if (StringUtils.isBlank(comment)) {
                    if (StringUtils.isNotBlank(column.columnDefinition())) {
                        String columnDefinition = column.columnDefinition();
                        if (columnDefinition.indexOf("comment") > -1) {
                            comment = StringUtils.substringAfterLast(columnDefinition, " ");
                            comment = comment.substring(1, comment.length() - 1);
                        }
                    } else {
                        comment = propertyName;
                    }
                }
            }

            Field field = fieldMap.get(propertyName);
            String camelPropertyName = com.rick.common.util.StringUtils.stringToCamel(propertyName.replace(".", "_"));

            // 是否是字典
            boolean isDictValue = field.getType().isEnum() || field.getType().getAnnotation(DictType.class) != null || field.getDeclaringClass() == DictValue.class;
            DictType dictType = null;
            String dictTypeValue = null;
            if (isDictValue) {
                if (field.getType().isEnum()) {
                    dictTypeValue = field.getType().getSimpleName();
                } else {
                    Field embeddedField = fieldMap.get(StringUtils.substringBefore(propertyName, "."));
                    dictType = ObjectUtils.defaultIfNull(field.getAnnotation(DictType.class), embeddedField.getAnnotation(DictType.class));
                    dictTypeValue = dictType.type();
                }
            }

            ResolverInfo resolverInfo = ResolverInfo.builder().entityDAO(entityDAO).column(column).dictType(dictType).isDictValue(isDictValue).dictTypeValue(dictTypeValue).field(field).columnName(columnName).camelPropertyName(camelPropertyName).camelEntityName(camelEntityName).propertyName(propertyName).comment(comment).build();
            resolverInfoConsumer.accept(resolverInfo);
        }

        return entityDAO;
    }

    @Builder
    private static class ResolverInfo {

        EntityDAO entityDAO;

        Column column;

        DictType dictType;

        String dictTypeValue;

        boolean isDictValue;

        Field field;

        String columnName;

        String comment;

        String camelEntityName;

        String propertyName;

        String camelPropertyName;
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
