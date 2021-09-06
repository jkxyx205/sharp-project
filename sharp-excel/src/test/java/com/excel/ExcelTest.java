package com.excel;

import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelRow;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 3:32 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class ExcelTest {

//    @Test
//    public void testWrite() throws Exception {
//        File file = new File("/Users/rick/Documents/2.xlsx");
//
//        ExcelWorkbook excelWorkbook = new ExcelWorkbook();
//        XSSFSheet sheet = excelWorkbook.createSheet("test");
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 0, "hello1"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(1, 1, "hello2"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 1, "hello2"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(1, 0, "hello2"));
//
//        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 1);
//		sheet.addMergedRegion(region);
//
////        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 1, "hello2"));
////        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 2, "hello3"));
//
////        excelWorkbook.createRow(sheet, new ExcelRow.ExcelRowBuilder(0, 0, new String[] {"2","3", "4"}));
//        excelWorkbook.write(file);
//    }

    @Test
    public void testNative() throws IOException {
        File file = new File("/Users/rick/Documents/2.xlsx");

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet("sheet-rick");

        // 设置第一列的框
        sheet.setColumnWidth(0, 3766);

        XSSFRow row = sheet.createRow(0);


        // 设置高度
        row.setHeightInPoints(24);
//        row.setRowStyle();
        XSSFCell cell = row.createCell(1);
        cell.setCellValue("hello");
//        cell.setCellStyle();


        XSSFCellStyle cellStyle = book.createCellStyle();

        // 定义颜色
        XSSFColor color = new XSSFColor(java.awt.Color.BLUE, new DefaultIndexedColorMap());
        XSSFColor color2 = new XSSFColor(Color.RED, new DefaultIndexedColorMap());
        XSSFColor color3 = new XSSFColor(Color.GREEN, new DefaultIndexedColorMap());

        // 填充色
        cellStyle.setFillForegroundColor(color2);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 文字色
        XSSFFont font = book.createFont();
        font.setColor(color);
        cellStyle.setFont(font);

        // 设置边框(合并这个不生效) 需要单独在CellRangeAddress设置
//        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
//        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color3);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置样式
        cell.setCellStyle(cellStyle);

        // 合并单元格
        CellRangeAddress region = new CellRangeAddress(0, 1, 1, 2);
        sheet.addMergedRegion(region);

        //
        System.out.println(cellStyle.getBorderBottom() == BorderStyle.NONE);
        setRegionStyle(sheet, region);

        book.write(new FileOutputStream(file));
        book.close();

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
                System.out.println("-----");
			}
		}
	 }


    @Test
    public void testCell() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        excelWriter.writeCell(new ExcelCell(4,5, "hello"));

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteCellWithStyle() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        ExcelCell cell = new ExcelCell(2,1, "hello");

        cell.setHeightInPoints(50f);
        cell.setStyle(createStyle(excelWriter.getBook()));
        excelWriter.writeCell(cell);

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteRowWithStyle() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        excelWriter.getActiveSheet().setColumnWidth(2, 5600);

        ExcelRow row = new ExcelRow(2,2, new Object[] {1.2d, 23, "3", true, LocalDate.now()});
        row.setStyle(createStyle(excelWriter.getBook()));
        excelWriter.writeRow(row);

        excelWriter.toFile(new FileOutputStream(file));
    }



    private XSSFCellStyle createStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();

        // 定义颜色
        XSSFColor color = new XSSFColor(java.awt.Color.BLUE, new DefaultIndexedColorMap());
        XSSFColor color2 = new XSSFColor(Color.RED, new DefaultIndexedColorMap());
        XSSFColor color3 = new XSSFColor(Color.GREEN, new DefaultIndexedColorMap());

        // 填充色
        cellStyle.setFillForegroundColor(color2);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 文字色
        XSSFFont font = book.createFont();
        font.setColor(color);
        font.setFontHeight(23);
        cellStyle.setFont(font);

        // 设置边框(合并这个不生效) 需要单独在CellRangeAddress设置
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color3);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return cellStyle;
    }

}
