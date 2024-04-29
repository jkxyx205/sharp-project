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
 * @createdAt 2021-10-13 15:27:00
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
        String html = "<tr th:each=\"m : ${grid.rows}\" th:if=\"${mStat.index == 0}\">\n" +
                "  <td x:text=\"${mStat.index + 1 + (grid.page - 1) * grid.pageSize}\"></td>\n" +
                "  <td th:each=\"v : ${grid.rows[mStat.index]}\" th:with=\"index=${vStat.index + "+x+"}\" th:class=\"${'text-' + report.reportColumnList.get(vStat.index).align.name().toLowerCase()}\" th:if=\"${!report.reportColumnList.get(vStat.index).hidden}\">\n" +
                "      <span th:name=\"${report.reportColumnList.get(vStat.index).name}\" th:if=\"${vStat.index > 1}\" th:attr=\"xtext=${'${grid.rows[mStat.index][' + index + ']}'}\"></span>\n" +
                "  </td>\n" +
                "  <input type=\"hidden\" x:each=\"v : ${grid.rows[mStat.index]}\" x:if=\"${report.reportColumnList.get(vStat.index).hidden}\" x:name=\"${report.reportColumnList.get(vStat.index).name}\" x:value=\"${v}\" />\n" +
                "</tr>";

        String htmlContent = ThymeleafRenderHelper.renderByHtmlContent(html, params).replaceAll("(?m)^[ \\t]*\\r?\\n","");
//        System.out.println(htmlContent);

        // add thymeleaf tag
        htmlContent = htmlContent.replace("<tr>", "<tr th:each=\"m : ${grid.rows}\">")
                .replace("xtext", "th:text")
                .replace("x:", "th:");

        System.out.println(htmlContent);
    }

}
