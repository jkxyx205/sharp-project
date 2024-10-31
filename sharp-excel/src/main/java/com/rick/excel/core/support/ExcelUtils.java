package com.rick.excel.core.support;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * @author Rick.Xu
 * @date 2024/9/13 09:22
 */
@UtilityClass
public class ExcelUtils {

    /**
     * 获取行的样式
     *
     * @param sheet
     * @param y
     * @return
     */
    public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int y) {
        return getSheetXSSFCellStyle(sheet, 0, y);
    }


    /**
     * 获取行（单元格）的样式，如果是某个单元格的样式，则放在 [0] 中
     *
     * @param sheet
     * @param x
     * @param y     > 0
     * @return
     */
    public XSSFCellStyle[] getSheetXSSFCellStyle(XSSFSheet sheet, int x, int y) {
        // 获取 cell 样式
        XSSFRow row = sheet.getRow(y - 1);
        int physicalNumberOfCells = row.getPhysicalNumberOfCells();
        XSSFCellStyle[] cellStyles = new XSSFCellStyle[physicalNumberOfCells];
        for (int i = 0; i < physicalNumberOfCells; i++) {
            cellStyles[i] = row.getCell(i).getCellStyle();
            if (x - 1 == i) {
                return new XSSFCellStyle[]{cellStyles[i]};
            }
        }

        return cellStyles;
    }

    public CellStyle copyXSSFCellStyle(Workbook workbook, XSSFSheet sheet, int x, int y) {
        return copyXSSFCellStyle(workbook, getSheetXSSFCellStyle(sheet, x, y)[0]);
    }

    public CellStyle copyXSSFCellStyle(Workbook workbook, CellStyle source) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.cloneStyleFrom(source);
        return cellStyle;
    }

    public int[] getCoordinateByLocation(String location) {
        char[] chars = location.toCharArray();
        int x = 0;
        int y = 0;
        int numberIndex = 0;
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            int number;
            if (c >= 65 && c <= 90) {
                number = c - 64;
                x += number * Math.pow(26, numberIndex - i - 1);
            } else if (c >= 97 && c <= 122) {
                number = c - 96;
                x += number * Math.pow(26, numberIndex - i - 1);
            } else {
                // 数字
                number = c - 48;
                y += number * Math.pow(10, chars.length - i - 1);
                numberIndex = i;
            }
        }

        return new int[]{x, y};
    }

}
