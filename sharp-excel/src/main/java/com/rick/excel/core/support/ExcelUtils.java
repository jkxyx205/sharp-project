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

}
