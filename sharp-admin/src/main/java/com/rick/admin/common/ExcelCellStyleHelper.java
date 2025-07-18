package com.rick.admin.common;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;

import java.awt.*;

/**
 * @author Rick.Xu
 * @date 2023/9/4 10:40
 */
@UtilityClass
public class ExcelCellStyleHelper {

    public XSSFCellStyle createRowMergeCellStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();

        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 文字
        XSSFFont font = book.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }


    public XSSFCellStyle createTextCellStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();

        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    public XSSFCellStyle createLabelStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();
        XSSFColor color = new XSSFColor(Color.lightGray, new DefaultIndexedColorMap());
        // 填充色
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 文字
        XSSFFont font = book.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    public XSSFCellStyle createTitleStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();
        XSSFColor color = new XSSFColor(Color.lightGray, new DefaultIndexedColorMap());
        // 填充色
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 文字
        XSSFFont font = book.createFont();
        font.setBold(true);
        font.setFontHeight(23);
        cellStyle.setFont(font);
        return cellStyle;
    }

    public XSSFCellStyle createStyle(XSSFWorkbook book) {
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

    public static XSSFCellStyle setAllBorder(XSSFCellStyle cellStyle) {
        XSSFColor black = new XSSFColor(Color.BLACK, new DefaultIndexedColorMap());
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, black);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, black);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, black);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, black);
        return cellStyle;
    }
}
