package com.rick.admin.demo;

import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2024-08-25 03:23:48
 */
@SpringBootTest
public class StudentTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        Report report = Report.builder()
                .code("t_student")// 　建议和数据库表名保持一致
                .tplName("tpl/ajax_list") // 拷贝模版页面到指定目录
//                .tplName("tpl/list") // 没有特殊要求使用模版页面
//                .tplName("tpl/ajax_list") // 没有特殊要求使用模版页面
                .name("学生信息")
                .reportAdviceName("operatorReportAdvice")                .additionalInfo(Params.builder(1).pv("operator-bar", true) // 显示操作按钮
                        .pv("endpoint", "students")
                        .build()) // 显示操作按钮
                .querySql("SELECT t_student.name AS \"name\",t_student.gender AS \"gender\",t_student.unit_code AS \"unit.code\",t_student.files AS \"files\",t_student.avatar AS \"avatar\",t_student.hobby_list AS \"hobbyList\",t_student.material_type AS \"materialTypeList\",t_student.code AS \"code\",t_student.create_by AS \"createBy\",t_student.create_time AS \"createTime\",t_student.update_by AS \"updateBy\",t_student.update_time AS \"updateTime\",t_student.is_deleted AS \"deleted\",t_student.id AS \"id\" FROM t_student WHERE name = :name AND gender = :gender AND unit_code = :unit_code AND unit_code = :unitCode AND files = :files AND avatar = :avatar AND hobby_list = :hobby_list AND hobby_list = :hobbyList AND material_type = :material_type AND material_type = :materialTypeList AND code = :code AND create_by = :create_by AND create_by = :createBy AND create_time = :create_time AND create_time = :createTime AND update_by = :update_by AND update_by = :updateBy AND update_time = :update_time AND update_time = :updateTime AND is_deleted = 0 AND is_deleted = :deleted AND id = :id")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "外部可见，唯一code"),
                        new QueryField("name", "姓名"),
                        new QueryField("gender", "", QueryField.Type.SELECT, "GenderEnum"),
                        new QueryField("unit_code", "", QueryField.Type.SELECT, "UNIT"),
                        new QueryField("files", ""),
                        new QueryField("avatar", ""),
                        new QueryField("hobby_list", ""),
                        new QueryField("material_type", "物料类型"),
                        new QueryField("create_by", "创建人"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE),
                        new QueryField("update_by", "更新人"),
                        new QueryField("update_time", "更新时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "外部可见，唯一code"),
                        new ReportColumn("name", "姓名"),
                        new ReportColumn("gender", "", false,"GenderEnum", Arrays.asList("dictConverter")),
                        new ReportColumn("unit.code", "", false,"UNIT", Arrays.asList("dictConverter")),
                        new ReportColumn("files", ""),
                        new ReportColumn("avatar", ""),
                        new ReportColumn("hobbyList", "", false,"HobbyEnum", Arrays.asList("arrayDictConverter")),
                        new ReportColumn("materialTypeList", "物料类型", false,"MATERIAL_TYPE", Arrays.asList("arrayDictConverter")),
                        new ReportColumn("createBy", "创建人"),
                        new ReportColumn("createTime", "创建时间", false, null, Arrays.asList("localDateTimeConverter")),
                        new ReportColumn("updateBy", "更新人"),
                        new ReportColumn("updateTime", "更新时间", false, null, Arrays.asList("localDateTimeConverter"))
                ))
                .pageable(true)
                .sidx("createTime")
                .sord(SordEnum.DESC)
                .build();

        reportService.saveOrUpdate(report);
        System.out.println("=========reportId = " + report.getId());
    }
}
