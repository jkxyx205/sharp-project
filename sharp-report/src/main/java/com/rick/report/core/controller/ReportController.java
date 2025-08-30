package com.rick.report.core.controller;


import com.google.common.collect.Lists;
import com.rick.common.http.HttpRequestDeviceUtils;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Maps;
import com.rick.db.plugin.page.Grid;
import com.rick.db.plugin.page.PageModel;
import com.rick.db.util.PaginationHelper;
import com.rick.excel.table.HtmlExcelTable;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.ReportDTO;
import com.rick.report.core.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * ajax_list的页面
     * @param id
     * @param model
     * @param request
     * @return
     */
    @GetMapping("{id}/page")
    public String ajaxIndex(@PathVariable Long id, Model model, HttpServletRequest request) {
        Report report = reportService.findById(id).get();
        reportService.init(report);

        model.addAttribute("report", report);
        // 不能处理有 summary 的table
        model.addAttribute("summaryIndex", Collections.emptyList());
        model.addAttribute("id", id);
        model.addAttribute("params", HttpServletRequestUtils.getParameterMap(request));
        return StringUtils.defaultString(report.getTplName(), "list");
    }

    @GetMapping("{id}/json")
    @ResponseBody
    public Result<Grid<Map<String, Object>>> value(@PathVariable Long id, HttpServletRequest request) {
        ReportDTO reportDTO = getReportDTO(id, request);
        return ResultUtils.success(reportDTO.getGridMap());
    }

    @GetMapping("{id}/json/summary")
    @ResponseBody
    public Result valueAndSummary(@PathVariable Long id, HttpServletRequest request) {
        ReportDTO reportDTO = getReportDTO(id, request);
        return ResultUtils.success(Maps.of("grid", reportDTO.getGridMap(), "summaryData", reportDTO.getSummaryMap()));
    }

    private ReportDTO getReportDTO(Long id, HttpServletRequest request) {
        ReportDTO reportDTO = reportService.list(id, HttpServletRequestUtils.getParameterMap(request));
        for (Map<String, Object> row : reportDTO.getGridMap().getRows()) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getValue() != null && entry.getKey().equals("id")) {
                    // id 需要序列成字符串
                    row.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return reportDTO;
    }

    @GetMapping("{id}/json/count")
    @ResponseBody
    public Result<List<BigDecimal>> count(@PathVariable  Long id, HttpServletRequest request) {
        ReportDTO reportDTO = reportService.list(id, HttpServletRequestUtils.getParameterMap(request));
        return ResultUtils.success(reportDTO.getSummaryMap().values().stream().collect(Collectors.toList()));
    }

    @GetMapping("{id}")
    public String index(@PathVariable Long id, Boolean readonly, Model model, HttpServletRequest request) {
        model.addAttribute("readonly", ObjectUtils.defaultIfNull(readonly, false));

        Report report = reportService.findById(id).get();
        if (report.getTplName() != null && report.getTplName().indexOf("ajax") > -1) {
            return ajaxIndex(id, model, request);
        }

        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        ReportDTO reportDTO = reportService.list(id, params);

        Grid<Object[]> grid = reportDTO.getGridArray();
        model.addAttribute("report", reportDTO.getReport());

        if (MapUtils.isNotEmpty(reportDTO.getSummaryMap())) {
            model.addAttribute("summary", reportDTO.getSummaryMap());

            List<String> visibleReportColumnList = reportDTO.getReport().getReportColumnList().stream().filter(reportColumn -> !reportColumn.getHidden()).map(reportColumn -> reportColumn.getName()).collect(Collectors.toList());
            List<Integer> summaryIndexList = Lists.newArrayListWithExpectedSize(reportDTO.getSummaryMap().size());
            for (Map.Entry<String, BigDecimal> en : reportDTO.getSummaryMap().entrySet()) {
                summaryIndexList.add(visibleReportColumnList.indexOf(en.getKey()) + 1 + ("multiple".equals(params.get("mode")) ? 1 : 0));
            }
            model.addAttribute("summaryIndex", summaryIndexList);
        } else {
            model.addAttribute("summary", Collections.EMPTY_MAP);
            model.addAttribute("summaryIndex", Collections.emptyList());
        }

        model.addAttribute("grid", grid);
        model.addAttribute("id", id);
        model.addAttribute("params", params);
        model.addAttribute("pageInfo", PaginationHelper.limitPages(grid.getTotalPages(), HttpRequestDeviceUtils.isMobileDevice(request) ? 5 : 15, grid.getPage()));
        return StringUtils.defaultString(reportDTO.getReport().getTplName(), "list");
    }

    @GetMapping("{id}/{instanceId}")
    @ResponseBody
    public Map<String, Object> detailById(@PathVariable Long id, @PathVariable Long instanceId) {
        return reportService.list(id, Maps.of("id", instanceId, PageModel.PARAM_SIZE, -1)).getGridMap().getRows().get(0);
    }

    @GetMapping("{id}/more/{instanceIds}")
    @ResponseBody
    public List<Map<String, Object>> detailByIds(@PathVariable Long id, @PathVariable String instanceIds) {
        return reportService.list(id, Maps.of("ids", instanceIds, PageModel.PARAM_SIZE, -1)).getGridMap().getRows();
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
