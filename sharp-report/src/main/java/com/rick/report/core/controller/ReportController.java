package com.rick.report.core.controller;


import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.db.dto.Grid;
import com.rick.db.util.PaginationHelper;
import com.rick.excel.table.HtmlExcelTable;
import com.rick.report.core.model.ReportDTO;
import com.rick.report.core.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 通用的报表请求
 * @author: Rick.Xu
 * @date: 6/18/20 3:31 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@RequiredArgsConstructor
@RequestMapping("reports")
@Controller
public class ReportController {

    private final ReportService reportService;

    @GetMapping("{id}")
    public String index(@PathVariable  Long id, Model model, HttpServletRequest request) {
        ReportDTO reportDTO = reportService.list(id, HttpServletRequestUtils.getParameterMap(request));
        Grid gird = reportDTO.getGrid();
        model.addAttribute("report", reportDTO.getReport());
        model.addAttribute("grid", gird);
        model.addAttribute("id", id);
        model.addAttribute("pageInfo", PaginationHelper.limitPages(gird.getTotalPages(), gird.getPageSize(), gird.getPage()));
        return "list";
    }

    @GetMapping("{id}/export")
    public void export(@PathVariable  Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        reportService.export(request, response, id);
    }

    @PostMapping("/html")
    public void exportHtml(String name, String html, @RequestParam(name="columnsWidth[]") Integer[] columnsWidth, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HtmlExcelTable htmlExcelTable = new HtmlExcelTable();
        if (ArrayUtils.isNotEmpty(columnsWidth)) {
            int size = columnsWidth.length;
            for (int i = 0; i < size; i++) {
                htmlExcelTable.getExcelWriter().getActiveSheet().setColumnWidth(i, columnsWidth[i]);
            }
        }

        htmlExcelTable.write(html, HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, name + ".xlsx"));
    }
}
