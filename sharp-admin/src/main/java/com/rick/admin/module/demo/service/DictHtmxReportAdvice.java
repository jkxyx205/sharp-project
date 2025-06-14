package com.rick.admin.module.demo.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.springframework.stereotype.Component;

/**
 * @author Rick.Xu
 * @date 2025/6/14 16:52
 */
@Component
public class DictHtmxReportAdvice implements ReportAdvice {

    @Override
    public void init(Report report) {
        // 添加按钮
        report.getAdditionalInfo().put("js", "$('#id964844123011960832 .operator-group').append('<a class=\"btn btn-secondary ml-2\" href=\"javascript:;\" onclick=\"alert(document.getElementById(\\'id964844123011960832\\').alpineData.getSelectedData().join(\\', \\'))\"><i class=\"fa fa-upload\"></i> 自定义</a>')");
        report.getAdditionalInfo().put("css", "#id964844123011960832 {background-color: red}");
    }
}
