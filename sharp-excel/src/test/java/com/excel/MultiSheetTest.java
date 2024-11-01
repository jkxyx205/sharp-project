package com.excel;

import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelWriteSupport;
import com.rick.excel.core.support.ExcelUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Rick.Xu
 * @date 2024/10/31 09:58
 */
public class MultiSheetTest {

    @Test
    public void create2Sheet() throws IOException {
        ExcelWriter excelWriter = new ExcelWriter("sheet01");
        excelWriter.writeCell(new ExcelCell(1, 1, "a"));

        excelWriter.createSheetAndActive("sheet02");
        excelWriter.writeCell(new ExcelCell(2, 2, "b"));


        excelWriter.createSheetAndActive("sheet03");
        excelWriter.writeCell(new ExcelCell(3, 3, "c"));

        excelWriter.toFile(new FileOutputStream("/Users/rick/Space/tmp/abc.xlsx"));
    }

    @Test
    public void createCellType() throws IOException {
        ExcelWriter excelWriter = new ExcelWriter("sheet01");
        excelWriter.writeCell(new ExcelCell(1, 1, "1"));
        excelWriter.writeCell(new ExcelCell(1, 2, 2));
        excelWriter.toFile(new FileOutputStream("/Users/rick/Space/tmp/abc-type.xlsx"));
    }

    @Test
    public void createCellLocation() throws IOException {
        ExcelWriter excelWriter = new ExcelWriter("sheet01");
        excelWriter.writeCell(ExcelWriteSupport.excelCell("A1", "1"));
        excelWriter.writeRow(ExcelWriteSupport.excelRow("AB1", new Object[] {"ab"}));
        excelWriter.writeRow(ExcelWriteSupport.excelRow("ABF99", new Object[] {"abc", "cdf"}));

        excelWriter.toFile(new FileOutputStream("/Users/rick/Space/tmp/abc-location.xlsx"));
    }

    @Test
    public void testUtil() {
        ExcelUtils.getCoordinateByLocation("A1"); // 28 123
        ExcelUtils.getCoordinateByLocation("AB123"); // 28 123
        ExcelUtils.getCoordinateByLocation("ab123"); // 28 123
        ExcelUtils.getCoordinateByLocation("Ab123"); // 28 123
    }

    @Test
    public void testCopySheet() throws IOException {
        FileInputStream fis = new FileInputStream("/Users/rick/Space/tmp/test.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        wb.setSheetName(0, "copy-1");


        wb.cloneSheet(0, "copy-2");
        wb.write(Files.newOutputStream(new File("/Users/rick/Space/tmp/test-3.xlsx").toPath()));
        wb.close();
    }


    @Test
    public void testCopySheet2() throws IOException {
        FileInputStream fis = new FileInputStream("/Users/rick/Space/tmp/test.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(fis);

        ExcelWriter writer = new ExcelWriter(wb);
        writer.getBook().setSheetName(0, "sheet11");
        writer.writeCell(ExcelWriteSupport.excelCell("C20", "哈哈"));

        writer.cloneSheetAndActive(0, "sheet22");
        writer.writeCell(ExcelWriteSupport.excelCell("C21", "嘻嘻"));

        writer.toFile(new FileOutputStream("/Users/rick/Space/tmp/test-f.xlsx"));
    }
}
