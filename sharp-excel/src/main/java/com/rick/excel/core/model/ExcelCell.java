package com.rick.excel.core.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 6:46 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Getter
@Setter
public class ExcelCell extends BaseExcelCell<Object> {

    private int rowSpan = 1;

    private int colSpan = 1;

    public ExcelCell(int x, int y, Object value) {
        super(x, y, value);
    }

    public ExcelCell(int x, int y, float heightInPoints, Object value) {
        super(x, y, heightInPoints, value);
    }

    public ExcelCell(int x, int y, float heightInPoints, XSSFCellStyle style, Object value) {
        super(x, y, heightInPoints, style, value);
    }

    public ExcelCell(int x, int y, Object value, int rowSpan, int colSpan) {
        this(x, y, value);
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public ExcelCell(int x, int y, float heightInPoints, Object value, int rowSpan, int colSpan) {
        super(x, y, heightInPoints, value);
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public ExcelCell(int x, int y, float heightInPoints, XSSFCellStyle style, Object value, int rowSpan, int colSpan) {
        super(x, y, heightInPoints, style, value);
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }
}
