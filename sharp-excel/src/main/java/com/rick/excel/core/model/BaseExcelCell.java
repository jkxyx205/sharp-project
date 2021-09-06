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
public class BaseExcelCell<T> {

    private int x;

    private int y;

    private T value;

    private float heightInPoints = 12.75f;

    private XSSFCellStyle style;

    public BaseExcelCell(int x, int y, T value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}
