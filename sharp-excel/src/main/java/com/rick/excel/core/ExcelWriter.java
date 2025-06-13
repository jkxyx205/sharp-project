package com.rick.excel.core;

import com.rick.common.util.Time2StringUtils;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelColumn;
import com.rick.excel.core.model.ExcelRow;
import com.rick.excel.core.support.ExcelUtils;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 6:51 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class ExcelWriter {

    @Getter
    private XSSFWorkbook book;

    private List<XSSFSheet> sheetList = new ArrayList<>();

    @Getter
    private XSSFSheet activeSheet;

    private static final Map<String, Integer> PICTURE_TYPE_MAPPING = new HashMap<>();

    static {
        PICTURE_TYPE_MAPPING.put("png", XSSFWorkbook.PICTURE_TYPE_PNG);
        PICTURE_TYPE_MAPPING.put("jpeg", XSSFWorkbook.PICTURE_TYPE_JPEG);
    }

    public ExcelWriter() {
        this(true);
    }

    public ExcelWriter(boolean initFirstSheet) {
        init(initFirstSheet, null);
    }

    public ExcelWriter(String sheetName) {
        init(true, sheetName);
    }

    public ExcelWriter(XSSFWorkbook book) {
        this.book = book;
        this.activeSheet = book.getSheetAt(0);
    }

    public void setActiveSheet(int index) {
        this.activeSheet= book.getSheetAt(index);
    }

    public void cloneSheetAndActive(int sheetNum) {
        cloneSheetAndActive(sheetNum, null);
    }

    public void cloneSheetAndActive(int sheetNum, String sheetName) {
        this.activeSheet = book.cloneSheet(sheetNum, sheetName);
        sheetList.add(this.activeSheet);
    }

    public void createSheetAndActive(String sheetName) {
        this.activeSheet = createSheet(sheetName);
        sheetList.add(this.activeSheet);
    }

    private XSSFSheet createSheet(String sheetName) {
        if (StringUtils.isBlank(sheetName)) {
            return book.createSheet();
        }

        return book.createSheet(sheetName);
    }

    public XSSFSheet getSheetAndActive(int index) {
         return this.activeSheet = book.getSheetAt(index);
    }

    public void insertAndWriteCell(ExcelCell ecell) {
        insertRows(ecell.getY(), 1);
        writeCell(ecell);
    }

    public void writeCell(ExcelCell ecell) {
        writeCell(ecell, null);
    }

    public void writeCell(ExcelCell ecell, ExcelWriterHook hook) {
        int rowIndex = ecell.getY() - 1;
        int colIndex = ecell.getX() - 1;

        XSSFRow row = (XSSFRow) CellUtil.getRow(rowIndex, activeSheet);

        if (Objects.isNull(row)) {
            row = activeSheet.createRow(rowIndex);
        }

        XSSFCell cell = row.getCell(colIndex);

        if (cell == null) {
            cell = row.createCell(colIndex, convertCellType(ecell.getValue()));
        }

        float currentHeightInPoints = row.getHeightInPoints();

        if (currentHeightInPoints < ecell.getHeightInPoints()) {
            row.setHeightInPoints(ecell.getHeightInPoints());
        }

        if (Objects.nonNull(ecell.getStyle())) {
            cell.setCellStyle(ecell.getStyle());
        }

        setCellValue(cell, ecell.getValue());

        // 处理合并
        if (ecell.getColSpan() != 1 || ecell.getRowSpan() != 1) {
            CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex + ecell.getRowSpan() - 1, colIndex, colIndex + ecell.getColSpan() -1);
            activeSheet.addMergedRegion(region);
            setRegionStyle(activeSheet, region);
        }

        if (Objects.nonNull(hook)) {
            hook.afterCellWrite(ecell, cell);
        }

    }

    public void writeRow(ExcelRow row) {
        writeRow(row, null);
    }
    public void writeRow(ExcelRow row, ExcelWriterHook hook) {
        Object[] values = row.getValue();

        if (ArrayUtils.isEmpty(values)) {
            return;
        }

        int length = values.length;

        for (int i = 0; i < length; i++) {
            ExcelCell cell = new ExcelCell(row.getX() + i, row.getY(), values[i]);
            cell.setHeightInPoints(row.getHeightInPoints());

            if (Objects.nonNull(row.getCellStyles()) && row.getCellStyles().length > i) {
                cell.setStyle(row.getCellStyles()[i]);
            } else {
                cell.setStyle(row.getStyle());
            }

            writeCell(cell, hook);
        }
    }

    public void writeColumn(ExcelColumn column) {
        writeColumn(column, null);
    }
    public void writeColumn(ExcelColumn column, ExcelWriterHook hook) {
        Object[] values = column.getValue();

        if (ArrayUtils.isEmpty(values)) {
            return;
        }

        int length = values.length;

        for (int i = 0; i < length; i++) {
            ExcelCell cell = new ExcelCell(column.getX(), column.getY() + i, values[i]);
            cell.setHeightInPoints(column.getHeightInPoints());
            cell.setStyle(column.getStyle());
            writeCell(cell, hook);
        }
    }

    /**
     * 数据从第一列（x = 1）开始写，复用插入行下面行的样式
     * @param y
     * @param dataList
     * @param hook
     */
    public void insertAndWriteRowWithAfterRowStyle(int y, List<Object[]> dataList, ExcelWriterHook hook) {
        XSSFCellStyle[] copyCellStyle = ExcelUtils.getSheetXSSFCellStyle(getActiveSheet(), y);
        Row row = activeSheet.getRow(y - 1);
        insertAndWriteRow(1, y, dataList, row.getHeightInPoints(), copyCellStyle, hook);
    }

    /**
     * 数据从第一列（x = 1）开始写，复用插入行下面行(样式、公式、格式化)
     * @param y
     * @param dataList
     * @param hook
     */
    public void insertAndWriteRowWithAfterRowStyle2(int y, List<Object[]> dataList, ExcelWriterHook hook) {
        if (CollectionUtils.isNotEmpty(dataList)) {
            Row row = getActiveSheet().getRow(y - 1);
            insertRows(y, dataList.size());

            for (int i = 0; i < dataList.size(); i++) {
                getActiveSheet().copyRows(Arrays.asList(row), y - 1 + i, new CellCopyPolicy());

                Object[] rowData = dataList.get(i);
                ExcelRow excelRow = new ExcelRow(1, y + i, rowData);
                writeRow(excelRow, hook);
            }
        }
    }

    public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle cellStyle) {
        insertAndWriteRow(x, y, dataList, heightInPoints, cellStyle, null);
    }

    public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle cellStyle, ExcelWriterHook hook) {
        insertAndWriteRow(x, y, dataList, heightInPoints, row -> row.setStyle(cellStyle), hook);
    }

    public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle[] cellStyles) {
        insertAndWriteRow(x, y, dataList, heightInPoints, cellStyles, null);
    }

    public void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, XSSFCellStyle[] cellStyles, ExcelWriterHook hook) {
        insertAndWriteRow(x, y, dataList, heightInPoints, row -> { if (cellStyles != null) row.setCellStyles(cellStyles);}, hook);
    }

    public void insertRows(int y, int n) {
        this.getActiveSheet().shiftRows(y - 1, activeSheet.getLastRowNum(), n, true, false);
    }

    public void removeRow(int y) {
        removeRows(y, 1);
    }

    public void removeRows(int y, int n) {
        Row row = getActiveSheet().getRow(y - 1);
        removeRows(row, n);
    }

    private void removeRows(Row row, int n) {
        int lastRowNum = activeSheet.getLastRowNum();
        int rowNum = row.getRowNum();
        if (rowNum >= 0 && rowNum < lastRowNum) {
            activeSheet.shiftRows(rowNum + 1, lastRowNum, -1 * n);
        }
        if (rowNum == lastRowNum) {
            if (row != null) {
                activeSheet.removeRow(row);
            }
        }
    }

    public void createPicture(PathType pathType, String path, String extension, int col1, int row1, int col2, int row2) throws IOException {
        BufferedImage bufferImg = ImageIO.read(pathType.read(path));

        //先把读进来的图片放到一个ByteArrayOutputStream中，以便产生ByteArray
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        ImageIO.write(bufferImg, extension, byteArrayOut);

        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        XSSFDrawing patriarch = activeSheet.createDrawingPatriarch();
        //anchor主要用于设置图片的属性
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0,  col1, row1, col2,  row2);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        //插入图片
        patriarch.createPicture(anchor, book.addPicture(byteArrayOut.toByteArray(),
                ObjectUtils.defaultIfNull(PICTURE_TYPE_MAPPING.get(extension.toLowerCase()), XSSFWorkbook.PICTURE_TYPE_PNG)));
    }

    public void toFile(OutputStream os) throws IOException {
        book.write(os);
        book.close();
    }

    private void init(boolean initFirstSheet, String sheetName) {
        book = new XSSFWorkbook();
        if (initFirstSheet) {
            createSheetAndActive(sheetName);
        }
    }

    private CellType convertCellType(Object object) {

        if (Objects.isNull(object)) {
            return CellType.BLANK;
        } else if (object instanceof Boolean) {
            return CellType.BOOLEAN;
        } else if (object instanceof Number || object.getClass() == LocalDate.class || object.getClass() == java.util.Date.class || object.getClass() == LocalDateTime.class || object.getClass() == Calendar.class) {
            return CellType.NUMERIC;
        }

        return CellType.STRING;
    }

    private void setCellValue(XSSFCell cell, Object object) {
        if (Objects.isNull(object)) {
            return;
        }

        if (object instanceof Boolean) {
            cell.setCellValue((Boolean) object);
        } else if (Date.class.isAssignableFrom(object.getClass())) {
            cell.setCellValue(Time2StringUtils.format((java.sql.Date) object));
        } else if (object.getClass() == LocalDate.class) {
            cell.setCellValue(Time2StringUtils.format((LocalDate) object));
        } else if (object.getClass() == LocalDateTime.class) {
            cell.setCellValue(Time2StringUtils.format((LocalDateTime) object));
        }/*else if (object instanceof java.util.Date) { // 显示数字，表示 是一个 Excel 格式的序列号日期，可读性差
            cell.setCellValue((java.util.Date) object);
        } else if (object instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) object);
        } else if (object instanceof LocalDate) {
            cell.setCellValue((LocalDate)object);
        } */else if (object instanceof Calendar) {
            cell.setCellValue((Calendar)object);
        } else if (object instanceof Number) {
            cell.setCellValue(Double.valueOf(String.valueOf(object)));
        } else {
            cell.setCellValue(String.valueOf(object));
        }
    }

    /**
     * 设置边框
     //	 * @param region
     */
    private void setRegionStyle(XSSFSheet sheet, CellRangeAddress region) {
        //全部完成之后
        XSSFRow xrow = (XSSFRow) CellUtil.getRow(region.getFirstRow(), sheet);
        XSSFCell xccell = (XSSFCell) CellUtil.getCell(xrow, region.getFirstColumn());

        XSSFCellStyle style = xccell.getCellStyle();

        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            XSSFRow row = (XSSFRow) CellUtil.getRow(i, sheet);
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                XSSFCell cell = (XSSFCell) CellUtil.getCell(row, j);
                cell.setCellStyle(style);
            }
        }
    }

    private void insertAndWriteRow(int x, int y, List<Object[]> dataList, float heightInPoints, Consumer<ExcelRow> consumer, ExcelWriterHook hook) {
        if (CollectionUtils.isNotEmpty(dataList)) {
            insertRows(y, dataList.size());

            for (int i = 0; i < dataList.size(); i++) {
                Object[] rowData = dataList.get(i);
                ExcelRow excelRow = new ExcelRow(x, y++, rowData);
                consumer.accept(excelRow);
                if (heightInPoints >= 0) {
                    excelRow.setHeightInPoints(heightInPoints);
                }
                writeRow(excelRow, hook);
            }
        }
    }

    public static enum PathType {
        URL,
        CLASS_PATH,
        ABSOLUTE_PATH;

        public InputStream read(String path) throws IOException {
            if (this == URL) {
                URL pictureURL = new URL(path);
                return pictureURL.openStream();
            } else if (this == CLASS_PATH) {
                final ClassPathResource classPathResource = new ClassPathResource(path);
                return classPathResource.getInputStream();
            } else if (this == ABSOLUTE_PATH) {
                return new FileInputStream(path);
            }

            return null;
        }
    }
}
