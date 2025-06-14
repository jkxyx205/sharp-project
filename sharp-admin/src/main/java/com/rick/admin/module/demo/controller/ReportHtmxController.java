package com.rick.admin.module.demo.controller;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.report.core.controller.ReportController;
import com.rick.report.core.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rick.Xu
 * @date 2025/6/13 09:36
 */
@RequestMapping("reports/htmx")
@Controller
public class ReportHtmxController extends ReportController {

    public ReportHtmxController(ReportService reportService) {
        super(reportService);
    }

    @GetMapping("{id}")
    public String index(@PathVariable Long id, Boolean readonly, Model model, HttpServletRequest request) {
        String page = super.index(id, readonly, model, request);
        String fragment = "content";
        return HttpServletRequestUtils.isAjaxRequest(request) ? page +" :: "+fragment+"" : page;
    }
}
