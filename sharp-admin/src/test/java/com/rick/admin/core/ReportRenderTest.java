package com.rick.admin.core;

import com.rick.admin.common.util.ThymeleafRenderHelper;
import com.rick.db.dto.Grid;
import com.rick.report.core.model.ReportDTO;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2024-04-28 15:27:00
 */
@SpringBootTest
public class ReportRenderTest {

    @Autowired
    private ReportService reportService;

    /**
     * 添加字段
     */
    @Test
    public void testRender() {
        Long reportId = 816284792017412096L;
        int x = 0;

        Map<String, Object> params = new HashMap<>();
        ReportDTO reportDTO = reportService.list(reportId, new HashMap<>());
        Grid<Object[]> grid = reportDTO.getGridArray();
        params.put("report", reportDTO.getReport());
        params.put("grid", grid);

       /*
         链接显示，区别 span
         <a href="javascript:;" th:onclick="showPurchaseDocument('查看', [[${grid.rows[mStat.index][1]}]])">
            <span th:text="${grid.rows[mStat.index][3]}" name="code"></span>
         </a>
         */
        // language=HTML
        String bodyHtml = "<tr th:each=\"m : ${grid.rows}\" th:if=\"${mStat.index == 0}\">\n" +
                "  <td x:text=\"${mStat.index + 1 + (grid.page - 1) * grid.pageSize}\"></td>\n" +
                "  <td th:each=\"v : ${grid.rows[mStat.index]}\" th:with=\"index=${vStat.index + "+x+"}\" th:class=\"${'text-' + report.reportColumnList.get(vStat.index).align.name().toLowerCase()}\" th:if=\"${!report.reportColumnList.get(vStat.index).hidden}\">\n" +
                "      <span th:name=\"${report.reportColumnList.get(vStat.index).name}\" th:if=\"${vStat.index > 1}\" th:attr=\"xtext=${'${grid.rows[mStat.index][' + index + ']}'}\"></span>\n" +
                "  </td>\n" +
                "  <input type=\"hidden\" x:each=\"v : ${grid.rows[mStat.index]}\" x:if=\"${report.reportColumnList.get(vStat.index).hidden}\" x:name=\"${report.reportColumnList.get(vStat.index).name}\" x:value=\"${v}\" />\n" +
                "</tr>";

        String bodyRenderedHtmlContent = ThymeleafRenderHelper.renderByHtmlContent(bodyHtml, params).replaceAll("(?m)^[ \\t]*\\r?\\n","");
//        System.out.println(bodyRenderedHtmlContent);

        // add thymeleaf tag
        bodyRenderedHtmlContent = bodyRenderedHtmlContent.replace("<tr>", "<tr th:each=\"m : ${grid.rows}\">")
                .replace("xtext", "th:text")
                .replace("x:", "th:");

        System.out.println(bodyRenderedHtmlContent);

        System.out.println("========================================================================");

        // header
        String headerHtml = "<tr>\n" +
                "    <th style=\"width: 40px\">序号</th>\n" +
                "    <th th:each=\"n : ${report.reportColumnList}\" th:text=\"${n.label}\" th:data-name=\"${n.name}\" th:class=\"${(n.sortable ? 'sortable text-' + n.align.name().toLowerCase() : 'text-' + n.align.name().toLowerCase()) + (n.name == report.sidx ? ' active' : '') }\" th:style=\"${n.columnWidth == null ? '': 'width: ' + n.columnWidth + 'px'}\" th:if=\"${!n.hidden}\"></th>\n" +
                "</tr>";

        String headerRenderedHtmlContent = ThymeleafRenderHelper.renderByHtmlContent(headerHtml, params).replaceAll("(?m)^[ \\t]*\\r?\\n","");
        System.out.println(headerRenderedHtmlContent);
        System.out.println("========================================================================");

        // form
        /**
         * 如果必要， 手动在 form 中添加 hidden input
         *  <input type="hidden" name="batch_id" id="batch_id">
         */
        String formHtml = "<th:block th:each=\"f : ${report.queryFieldList}\" th:if=\"${report.queryFieldList != null && report.queryFieldList.size() > 0}\">\n" +
                "    <div class=\"form-group\">\n" +
                "        <label class=\"col-form-label mr-2\" th:for=\"${f.name}\" th:text=\"${f.label}\"></label>\n" +
                "        <th:block th:if=\"${f.type.name() ne 'DATE_RANGE'}\">\n" +
                "            <input class=\"form-control\" th:id=\"${f.name}\" type=\"text\" th:name=\"${f.name}\" autocomplete th:if=\"${f.type.name() == 'TEXT'}\" th:value=\"${f.value}\" th:placeholder=\"${f.placeholder}\">\n" +
                "            <sp:select  th:id=\"${f.name}\" th:name=\"${f.name}\" th:attr=\"key=${f.extraData}\" class=\"form-control\" th:if=\"${f.type.name() == 'SELECT'}\"/>\n" +
                "            <sp:select  th:id=\"${f.name}\" th:name=\"${f.name}\" th:attr=\"key=${f.extraData}\" class=\"form-control\" group th:if=\"${f.type.name() == 'GROUP_SELECT'}\"/>\n" +
                "            <sp:select  th:id=\"${f.name}\" th:name=\"${f.name}\" multiple th:attr=\"key=${f.extraData}\" class=\"form-control\" th:if=\"${f.type.name() == 'MULTIPLE_SELECT'}\" hideAllItem/>\n" +
                "        </th:block>\n" +
                "\n" +
                "        <th:block style=\"margin-bottom: 10px;\" th:if=\"${f.type.name() == 'DATE_RANGE'}\">\n" +
                "            <div class=\"input-group input-daterange\" th:classappend=\"${f.name}\">\n" +
                "                <input class=\"form-control\" th:id=\"${f.name + '0'}\" th:name=\"${f.name + '0'}\" type=\"text\" placeholder=\"开始时间\" autocomplete=\"off\">\n" +
                "                <label class=\"col-form-label\" style=\"padding-left: 0;\">～</label>\n" +
                "                <input class=\"form-control\" th:id=\"${f.name + '1'}\" th:name=\"${f.name + '1'}\" type=\"text\" placeholder=\"结束时间\" autocomplete=\"off\">\n" +
                "            </div>\n" +
                "        </th:block>\n" +
                "    </div>\n" +
                "</th:block>";

        String formRenderedHtmlContent = ThymeleafRenderHelper.renderByHtmlContent(formHtml, params).replaceAll("(?m)^[ \\t]*\\r?\\n","");
        System.out.println(formRenderedHtmlContent);
    }

}
