package com.rick.excel.table;

import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelRow;
import com.rick.excel.table.model.TableColumn;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:18 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Getter
@Setter
public abstract class AbstractExportTable {

    protected List<? extends TableColumn> tableColumnList;

    protected List<?> rows;

    protected ExcelWriter excelWriter = new ExcelWriter();

    private Float columnHeight = 24f;

    protected Float rowHeight = 24f;

    private XSSFCellStyle columnStyle;

    protected XSSFCellStyle rowStyle;

    private XSSFCellStyle defaultColumnStyle;

    protected XSSFCellStyle defaultRowStyle;

    public AbstractExportTable(List<? extends TableColumn> tableColumnList, List<?> rows) {
        this.tableColumnList = tableColumnList;
        this.defaultColumnStyle = createDefaultColumnStyle();
        this.defaultRowStyle = createDefaultRowStyle();
        this.rows = rows;
    }

    public void write(OutputStream os) throws IOException {
        write(os, null);
    }

    public void write(OutputStream os, Consumer<AbstractExportTable> beforeToFileConsumer) throws IOException {
        writeColumns();
        writeRows();
        if (Objects.nonNull(beforeToFileConsumer)) {
            beforeToFileConsumer.accept(this);
        }
        excelWriter.toFile(os);
    }

    private void writeColumns() {
        if (CollectionUtils.isEmpty(tableColumnList)) {
            return;
        }

        int columnSize = tableColumnList.size();
        Object[] labels = new Object[columnSize];
        XSSFCellStyle[] cellStyles = new XSSFCellStyle[columnSize];

        for (int i = 0; i < columnSize; i++) {
            TableColumn tableColumn = tableColumnList.get(i);
            labels[i] = tableColumn.getLabel();
            excelWriter.getActiveSheet().setColumnWidth(i, tableColumn.getColumnWidth());

            // cell style
            if (tableColumn.getAlign() != null) {
                XSSFCellStyle defaultRowStyle = createDefaultColumnStyle();
                defaultRowStyle.setAlignment((HorizontalAlignment.valueOf(tableColumn.getAlign().name())));
                cellStyles[i] = defaultRowStyle;
            }
        }

        ExcelRow row = new ExcelRow(1, 1, labels);
        row.setHeightInPoints(columnHeight);
        row.setCellStyles(cellStyles);

        if (Objects.nonNull(columnStyle)) {
            row.setStyle(columnStyle);
        } else {
            row.setStyle(defaultColumnStyle);
        }

        excelWriter.writeRow(row);
    }

    protected void writeRows() {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }

        if (CollectionUtils.isEmpty(tableColumnList)) {
            return;
        }
        // 设置样式
        int columnSize = tableColumnList.size();
        XSSFCellStyle[] cellStyles = new XSSFCellStyle[columnSize];

        for (int i = 0; i < columnSize; i++) {
            TableColumn tableColumn = tableColumnList.get(i);

            // cell style
            if (tableColumn.getAlign() != null) {
                XSSFCellStyle defaultRowStyle = createDefaultRowStyle();
                defaultRowStyle.setAlignment((HorizontalAlignment.valueOf(tableColumn.getAlign().name())));
                cellStyles[i] = defaultRowStyle;
            }
        }

        int startY = CollectionUtils.isEmpty(tableColumnList) ? 1 : 2;
        int size = rows.size();

        for (int i = 0; i < size; i++) {
            ExcelRow row = new ExcelRow(1, i + startY, resolve(rows.get(i)));
            row.setHeightInPoints(rowHeight);
            row.setCellStyles(cellStyles);

            if (Objects.nonNull(rowStyle)) {
                row.setStyle(rowStyle);
            } else {
                row.setStyle(defaultRowStyle);
            }

            excelWriter.writeRow(row);
        }
    }

    protected abstract Object[] resolve(Object row);

    private XSSFCellStyle createDefaultColumnStyle() {
        XSSFCellStyle cellStyle = excelWriter.getBook().createCellStyle();
        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 字体
        XSSFFont font = excelWriter.getBook().createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // 背景
        XSSFColor color = new XSSFColor(Color.lightGray, new DefaultIndexedColorMap());
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return cellStyle;
    }

    protected XSSFCellStyle createDefaultRowStyle() {
        return excelWriter.getBook().createCellStyle();
    }
}
