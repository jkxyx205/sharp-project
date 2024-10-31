package com.rick.excel.core.model;

import com.rick.excel.core.support.ExcelUtils;
import lombok.experimental.UtilityClass;
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
@UtilityClass
public class ExcelWriteSupport {

    public ExcelCell excelCell(String location, Object value) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], value);
    }

    public ExcelCell excelCell(String location, float heightInPoints, Object value) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], heightInPoints, value);
    }

    public ExcelCell excelCell(String location, float heightInPoints, XSSFCellStyle style, Object value) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], heightInPoints, style, value);
    }

    public ExcelCell excelCell(String location, Object value, int rowSpan, int colSpan) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], rowSpan, colSpan);
    }

    public ExcelCell excelCell(String location, float heightInPoints, Object value, int rowSpan, int colSpan) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], heightInPoints, value, rowSpan, colSpan);
    }

    public ExcelCell excelCell(String location, float heightInPoints, XSSFCellStyle style, Object value, int rowSpan, int colSpan) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelCell(coordinate[0], coordinate[1], heightInPoints, style, value, rowSpan, colSpan);
    }

    public ExcelRow excelRow(String location) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelRow(coordinate[0], coordinate[1]);
    }

    public ExcelRow excelRow(String location, Object[] values) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelRow(coordinate[0], coordinate[1], values);
    }

    public ExcelRow excelRow(String location, float heightInPoints, Object... values) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelRow(coordinate[0], coordinate[1], heightInPoints, values);
    }

    public ExcelRow excelRow(String location, float heightInPoints, XSSFCellStyle style, Object... values) {
        int[] coordinate = ExcelUtils.getCoordinateByLocation(location);
        return new ExcelRow(coordinate[0], coordinate[1],heightInPoints, style, values);
    }
}
