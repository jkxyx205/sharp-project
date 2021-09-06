package com.excel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.excel.core.ExcelReader;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelColumn;
import com.rick.excel.core.model.ExcelRow;
import com.rick.excel.table.GeneralExportTable;
import com.rick.excel.table.MapExcelTable;
import com.rick.excel.table.model.MapTableColumn;
import com.rick.excel.table.model.TableColumn;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 9:20 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class TableTest {

    @Test
    public void testBase() throws IOException {
        File file = new File("/Users/rick/Documents/1.xlsx");
        TableColumn column1 = new TableColumn("姓名");
        TableColumn column2 = new TableColumn("年龄");

        List<TableColumn> columnList = Lists.newArrayList(column1, column2);
        List<Object[]> rows = Lists.newArrayList(new Object[]{"rick", 23}, new Object[] {"jim", 88});
        GeneralExportTable excelTable =  new GeneralExportTable(columnList, rows);

        excelTable.write(new FileOutputStream(file));
    }

    @Test
    public void testBaseWithStyle() throws IOException {
        File file = new File("/Users/rick/Documents/22.xlsx");

        TableColumn column1 = new TableColumn("姓名");
        TableColumn column2 = new TableColumn("年龄");

        List<TableColumn> columnList = Lists.newArrayList(column1, column2);
        List<Object[]> rows = Lists.newArrayList(new Object[]{"rick", 23}, new Object[] {"jim", 88});

        GeneralExportTable excelTable =  new GeneralExportTable(columnList, rows);

        XSSFCellStyle cellStyle = excelTable.getExcelWriter().getBook().createCellStyle();
        // 填充色
        XSSFColor color = new XSSFColor(Color.GREEN, new DefaultIndexedColorMap());
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 数据部分样式
        excelTable.setRowStyle(cellStyle);

        // 表头部分样式
//         excelTable.setColumnStyle(cellStyle);

        excelTable.write(new FileOutputStream(file));
    }

    @Test
    public void testBaseWithMap() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        MapTableColumn column1 = new MapTableColumn("name", "姓名");
        MapTableColumn column2 = new MapTableColumn("age", "年龄");
        List<MapTableColumn> columnList = Lists.newArrayList(column1, column2);

        Map<String, Object> map1 = Maps.newHashMapWithExpectedSize(2);
        map1.put("name", "rick");
        map1.put("age", 12);

        Map<String, Object> map2 = Maps.newHashMapWithExpectedSize(2);
        map2.put("name", "jim");
        map2.put("age", 88);

        List<Map<String, Object>> rows = Lists.newArrayList(map1, map2);
        MapExcelTable excelTable =  new MapExcelTable(columnList, rows);

        excelTable.write(new FileOutputStream(file));
    }

//    @Test
//    public void test5() throws IOException {
//        File file = new File("/Users/rick/Documents/7.xlsx");
//        MapTableColumn column1 = new MapTableColumn("name", "姓名2");
//        MapTableColumn column2 = new MapTableColumn("age", "年龄2");
//        List<MapTableColumn> columnList = Lists.newArrayList(column1, column2);
//
//        QueryResultExportTable exportTable = new QueryResultExportTable(null, null, null, null, columnList);
//
//        exportTable.write(new FileOutputStream(file));
//    }

    @Test
    public void writeColumn() throws IOException {
        File file = new File("/Users/rick/Documents/8.xlsx");
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.writeColumn(new ExcelColumn(2,2, new Object[] {1, 2, 3, 4, 5, "hello", true}));
        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void writeRow() throws IOException {
        File file = new File("/Users/rick/Documents/9.xlsx");
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.writeRow(new ExcelRow(2,2, new Object[] {1, 2, 3, 4, 5, "hello", true}));
        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteMerge() throws IOException {
        File file = new File("/Users/rick/Documents/0.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        ExcelCell cell = new ExcelCell(2,3, "hello");

        cell.setRowSpan(3);
        cell.setColSpan(2);

        excelWriter.writeCell(cell);

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testComplex() throws IOException {
        File file = new File("/Users/rick/Documents/7.xlsx");
        ExcelWriter excelWriter = new ExcelWriter();

        ExcelCell cell1 = new ExcelCell(1,1, "国内仓");
        cell1.setColSpan(3);
        cell1.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell2 = new ExcelCell(4,1, "香港仓");
        cell2.setRowSpan(2);
        cell2.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell3 = new ExcelCell(5,1, "香港直运仓");
        cell3.setRowSpan(2);
        cell3.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell4 = new ExcelCell(6,1, "供应链未完成数量");
        cell4.setRowSpan(2);
        cell4.setStyle(createStyle(excelWriter.getBook()));

        ExcelRow row1 = new ExcelRow(1, 2, new Object[] {"苏州成品仓", "苏州样品仓", "深圳成品仓"});
        row1.setStyle(createStyle(excelWriter.getBook()));

        ExcelRow row2 = new ExcelRow(1, 3, new Object[] {"50", "0", "1"});
        row2.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell5 = new ExcelCell(1,4, "国内仓锁货数量");
        cell5.setColSpan(3);
        cell5.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell6 = new ExcelCell(1,5, "9");
        cell6.setColSpan(3);
        cell6.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell7 = new ExcelCell(4,3, "29");
        cell7.setRowSpan(3);
        cell7.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell8 = new ExcelCell(5,3, "39");
        cell8.setRowSpan(3);
        cell8.setStyle(createStyle(excelWriter.getBook()));

        ExcelCell cell9 = new ExcelCell(6,3, 88);
        cell9.setRowSpan(3);
        cell9.setStyle(createStyle(excelWriter.getBook()));

        ExcelColumn column = new ExcelColumn(7, 1, new Object[] {"新增", "2", "23", 23, 34.2f});
        column.setStyle(createStyle(excelWriter.getBook()));

        excelWriter.writeCell(cell1);
        excelWriter.writeCell(cell2);
        excelWriter.writeCell(cell3);
        excelWriter.writeCell(cell4);
        excelWriter.writeCell(cell5);
        excelWriter.writeCell(cell6);
        excelWriter.writeCell(cell7);
        excelWriter.writeCell(cell8);
        excelWriter.writeCell(cell9);
        excelWriter.writeRow(row1);
        excelWriter.writeRow(row2);
        excelWriter.writeColumn(column);

        excelWriter.getActiveSheet().setColumnWidth(0, 5600);
        excelWriter.getActiveSheet().setColumnWidth(1, 5600);
        excelWriter.getActiveSheet().setColumnWidth(2, 5600);
        excelWriter.getActiveSheet().setColumnWidth(3, 5600);
        excelWriter.getActiveSheet().setColumnWidth(4, 5600);
        excelWriter.getActiveSheet().setColumnWidth(5, 5600);
        excelWriter.getActiveSheet().setColumnWidth(6, 5600);

        excelWriter.toFile(new FileOutputStream(file));

    }

    private XSSFCellStyle createStyle(XSSFWorkbook book) {

        XSSFCellStyle cellStyle = book.createCellStyle();
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

    @Test
    public void testRead() throws Exception {
        File file = new File("/Users/rick/Documents/1.xlsx");

        ExcelReader.readExcelContent(new FileInputStream(file), (index, data, sheetIndex, sheetName) -> {
            System.out.print("index: " + index + ", ");
            for (Object d : data) {
                System.out.print(d + ",");
            }
            System.out.println();

            return true;
        });
    }

}
