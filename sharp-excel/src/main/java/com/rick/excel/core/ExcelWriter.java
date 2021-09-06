package com.rick.excel.core;

import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelColumn;
import com.rick.excel.core.model.ExcelRow;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 6:51 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class ExcelWriter {

    @Getter
    private XSSFWorkbook book;

    private List<XSSFSheet> sheetList = new ArrayList<>();

    @Getter
    private XSSFSheet activeSheet;

    public ExcelWriter() {
        this(null);
    }

    public ExcelWriter(String sheetName) {
        book = new XSSFWorkbook();
        createSheetAndActive(sheetName);
    }

    public void createSheetAndActive(String sheetName) {
        XSSFSheet sheet = this.activeSheet = createSheet(sheetName);
        sheetList.add(sheet);
    }

    private XSSFSheet createSheet(String sheetName) {
        if (StringUtils.isBlank(sheetName)) {
            return book.createSheet();
        }

        return book.createSheet(sheetName);
    }

    public XSSFSheet getSheetAndActive(int index) {
         return this.activeSheet = book.getSheetAt(index);
    }

    public void writeCell(ExcelCell ecell) {

        int rowIndex = ecell.getY() - 1;
        int colIndex = ecell.getX() - 1;

        XSSFRow row = (XSSFRow) CellUtil.getRow(rowIndex, activeSheet);

        if (Objects.isNull(row)) {
            row = activeSheet.createRow(rowIndex);
        }

        XSSFCell cell = row.createCell(colIndex, convertCellType(ecell.getValue()));

        float currentHeightInPoints = row.getHeightInPoints();

        if (currentHeightInPoints < ecell.getHeightInPoints()) {
            row.setHeightInPoints(ecell.getHeightInPoints());
        }

        if (Objects.nonNull(ecell.getStyle())) {
            cell.setCellStyle(ecell.getStyle());
        }

        setCellValue(cell, ecell.getValue());

        // 处理合并
        if (ecell.getColSpan() != 1 || ecell.getRowSpan() != 1) {
            CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex + ecell.getRowSpan() - 1, colIndex, colIndex + ecell.getColSpan() -1);
            activeSheet.addMergedRegion(region);
            setRegionStyle(activeSheet, region);
        }

    }

    public void writeRow(ExcelRow row) {
        Object[] values = row.getValue();

        if (ArrayUtils.isEmpty(values)) {
            return;
        }

        int length = values.length;

        for (int i = 0; i < length; i++) {
            ExcelCell cell = new ExcelCell(row.getX() + i, row.getY(), values[i]);
            cell.setHeightInPoints(row.getHeightInPoints());
            cell.setStyle(row.getStyle());
            writeCell(cell);
        }
    }

    public void writeColumn(ExcelColumn column) {
        Object[] values = column.getValue();

        if (ArrayUtils.isEmpty(values)) {
            return;
        }

        int length = values.length;

        for (int i = 0; i < length; i++) {
            ExcelCell cell = new ExcelCell(column.getX(), column.getY() + i, values[i]);
            cell.setHeightInPoints(column.getHeightInPoints());
            cell.setStyle(column.getStyle());
            writeCell(cell);
        }
    }

    public void toFile(OutputStream os) throws IOException {
        book.write(os);
        book.close();
    }

    private CellType convertCellType(Object object) {

        if (Objects.isNull(object)) {
            return CellType.BLANK;
        } else if (object instanceof Boolean) {
            return CellType.BOOLEAN;
        } else if (object instanceof Number || object.getClass() == LocalDate.class || object.getClass() == java.util.Date.class || object.getClass() == LocalDateTime.class || object.getClass() == Calendar.class) {
            return CellType.NUMERIC;
        }

        return CellType.STRING;
    }

    private void setCellValue(XSSFCell cell, Object object) {
        if (Objects.isNull(object)) {
            return;
        }

        if (object instanceof Boolean) {
            cell.setCellValue((Boolean) object);
        } else if (object instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) object);
        } else if (object instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) object);
        } else if (object instanceof LocalDate) {
            cell.setCellValue((LocalDate)object);
        } else if (object instanceof Calendar) {
            cell.setCellValue((Calendar)object);
        } else if (object instanceof Number) {
            cell.setCellValue(Double.valueOf(String.valueOf(object)));
        } else {
            cell.setCellValue(String.valueOf(object));
        }
    }

    /**
     * 设置边框
     //	 * @param region
     */
    private void setRegionStyle(XSSFSheet sheet, CellRangeAddress region) {
        //全部完成之后
        XSSFRow xrow = (XSSFRow) CellUtil.getRow(region.getFirstRow(), sheet);
        XSSFCell xccell = (XSSFCell) CellUtil.getCell(xrow, region.getFirstColumn());

        XSSFCellStyle style = xccell.getCellStyle();

        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            XSSFRow row = (XSSFRow) CellUtil.getRow(i, sheet);
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                XSSFCell cell = (XSSFCell) CellUtil.getCell(row, j);
                cell.setCellStyle(style);
            }
        }
    }

}
