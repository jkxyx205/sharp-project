package com.rick.excel.core;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * 单元格值转化接口
 * @author Rick
 * @createdAt 2022-11-21 09:54:00
 */
public interface CellValueExtractor {

    /**
     * 根据XSSFCell类型设置数据
     * @param cell
     * @return
     */
    default Object getCellValue(XSSFCell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC: {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case _NONE:
            case FORMULA:
                return cell.getRawValue();
            case ERROR:
                return null;
            default:
                return cell.getStringCellValue();
        }
    }
}
