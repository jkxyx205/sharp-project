package com.rick.admin.sys.user.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/12/12 12:55
 */
@Component
public class UserReportAdvice implements ReportAdvice {
    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
        report.getAdditionalInfo().put("js", "$('table input[value=1]').parent().find('td').last().text('')");
    }
}
