package com.rick.excel.table;

import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/19/20 11:07 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Data
public final class HtmlExcelTable {

    private static final int DEFAULT_COLUMN_WIDTH = 4096;

    private ExcelWriter excelWriter = new ExcelWriter();

    private Float rowHeight = 24f;

    private XSSFCellStyle cellStyle;

    public void write(String html, OutputStream os) throws IOException {
        if (StringUtils.isBlank(html)) {
            excelWriter.toFile(os);
            return;
        }

        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("tr");

        int rowSize = trs.size();
        int maxIndex = 0;

        for (int i = 0; i < rowSize; i++) {
            Element tr = trs.get(i);
            Elements tds = tr.select("td, th");

            int colSize = tds.size();
            int index = 0;
            for (int j = 0; j < colSize; j++) {
                Element td = tds.get(j);
                int rowSpan = spanCount(td.attr("rowspan"));
                int colSpan = spanCount(td.attr("colspan"));
                String text = td.text();

                ExcelCell excelCell = new ExcelCell(index + 1, i + 1 , text);
                excelCell.setRowSpan(rowSpan);
                excelCell.setColSpan(colSpan);
                if (Objects.nonNull(cellStyle)) {
                    excelCell.setStyle(cellStyle);
                } else {
                    excelCell.setStyle(getDefaultStyle());
                }

                excelCell.setHeightInPoints(rowHeight);
                excelWriter.writeCell(excelCell);
                index += colSpan;
            }

            maxIndex = maxIndex < index ? index : maxIndex;
        }

        // 设置列宽
        if (maxIndex > 0) {
            XSSFSheet sheet = excelWriter.getActiveSheet();
            for (int i = 0; i < maxIndex; i++) {
                if (sheet.getColumnWidth(i) == 2048) {
                    sheet.setColumnWidth(i, DEFAULT_COLUMN_WIDTH);
                }
            }
        }

        excelWriter.toFile(os);
    }

    private int spanCount(String span) {
        if (StringUtils.isBlank(span))
            return 1;

        return Integer.parseInt(span);
    }

    private XSSFCellStyle getDefaultStyle() {
        XSSFCellStyle cellStyle = excelWriter.getBook().createCellStyle();
        // 定义颜色
        XSSFColor color = new XSSFColor(Color.black, new DefaultIndexedColorMap());

        // 设置边框(合并这个不生效) 需要单独在CellRangeAddress设置
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);

        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, color);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, color);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, color);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return cellStyle;
    }

}
