package com.rick.excel.core;

import com.rick.excel.core.model.ExcelCell;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * @author Rick.Xu
 * @date 2023/8/10 09:45
 */
@FunctionalInterface
public interface ExcelWriterHook {

    void afterCellWrite(ExcelCell ecell, XSSFCell cell);
}
