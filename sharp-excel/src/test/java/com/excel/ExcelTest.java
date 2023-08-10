package com.excel;

import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelRow;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/17/20 3:32 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class ExcelTest {

//    @Test
//    public void testWrite() throws Exception {
//        File file = new File("/Users/rick/Documents/2.xlsx");
//
//        ExcelWorkbook excelWorkbook = new ExcelWorkbook();
//        XSSFSheet sheet = excelWorkbook.createSheet("test");
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 0, "hello1"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(1, 1, "hello2"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 1, "hello2"));
//        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(1, 0, "hello2"));
//
//        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 1);
//		sheet.addMergedRegion(region);
//
////        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 1, "hello2"));
////        excelWorkbook.createCell(sheet, new ExcelCell.ExcelCellBuilder(0, 2, "hello3"));
//
////        excelWorkbook.createRow(sheet, new ExcelRow.ExcelRowBuilder(0, 0, new String[] {"2","3", "4"}));
//        excelWorkbook.write(file);
//    }

    @Test
    public void testNative() throws IOException {
        File file = new File("/Users/rick/Documents/2.xlsx");

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet("sheet-rick");

        // 设置第一列的框
        sheet.setColumnWidth(0, 3766);

        XSSFRow row = sheet.createRow(0);


        // 设置高度
        row.setHeightInPoints(24);
//        row.setRowStyle();
        XSSFCell cell = row.createCell(1);
        cell.setCellValue("hello");
//        cell.setCellStyle();


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
        cellStyle.setFont(font);

        // 设置边框(合并这个不生效) 需要单独在CellRangeAddress设置
//        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
//        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color3);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置样式
        cell.setCellStyle(cellStyle);

        // 合并单元格
        CellRangeAddress region = new CellRangeAddress(0, 1, 1, 2);
        sheet.addMergedRegion(region);

        //
        System.out.println(cellStyle.getBorderBottom() == BorderStyle.NONE);
        setRegionStyle(sheet, region);

        book.write(new FileOutputStream(file));
        book.close();

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
                System.out.println("-----");
			}
		}
	 }


    @Test
    public void testCell() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        excelWriter.writeCell(new ExcelCell(4,5, "hello"));

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteCellWithStyle() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        ExcelCell cell = new ExcelCell(2,1, "hello");

        cell.setHeightInPoints(50f);
        cell.setStyle(createStyle(excelWriter.getBook()));
        excelWriter.writeCell(cell);

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteRowWithStyle() throws IOException {
        File file = new File("/Users/rick/Documents/3.xlsx");

        ExcelWriter excelWriter = new ExcelWriter();

        excelWriter.getActiveSheet().setColumnWidth(2, 5600);

        ExcelRow row = new ExcelRow(2,2, new Object[] {1.2d, 23, "3", true, LocalDate.now()});
        row.setStyle(createStyle(excelWriter.getBook()));
        excelWriter.writeRow(row);

        excelWriter.toFile(new FileOutputStream(file));
    }

    @Test
    public void testWriteFromTemplate() throws IOException {
         String template = "/Users/rick/Space/Workspace/sharp-admin/src/main/resources/templates/excel/po.xlsx";
        String dist = "/Users/rick/Space/Workspace/sharp-admin/src/main/resources/templates/excel/dist.xlsx";

        // 这样就不会改变模版文件
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(template));

        ExcelWriter excelWriter = new ExcelWriter(new XSSFWorkbook(new ByteArrayInputStream(bytes)));

        excelWriter.writeCell(new ExcelCell(7, 3, "PO NO: PY20230726-100"));

        excelWriter.writeCell(new ExcelCell(1, 5, "供方（Vendor）：苏州云点信息系统工程有限公司"));
        excelWriter.writeCell(new ExcelCell(2, 6, "李峰 18898876623"));
        excelWriter.writeCell(new ExcelCell(2, 7, "0512-88359511"));
        excelWriter.writeCell(new ExcelCell(2, 8, "0512-88359511"));
        excelWriter.writeCell(new ExcelCell(1, 9, "ADD：  江苏省南京市武侯区遥观镇郑村村委63号"));


//        excelWriter.writeCell(new ExcelCell(7, 5, "需方：普源电机制造（苏州）有限公司"));
//        excelWriter.writeCell(new ExcelCell(7, 6, "慧博士 18898876623"));
//        excelWriter.writeCell(new ExcelCell(7, 7, "0512-77359511"));
//        excelWriter.writeCell(new ExcelCell(7, 8, "0512-77359511"));
//        excelWriter.writeCell(new ExcelCell(7, 10, "交货地点：苏州市高新区石阳路28号一号厂房大厅"));


        ArrayList<Object[]> data = new ArrayList<>();
        data.add(new Object[]{1, "资材编号1", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{2, "资材编号2", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{3, "资材编号3", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{4, "资材编号4", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{1, "资材编号1", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{2, "资材编号2", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{3, "资材编号3", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{4, "资材编号4", "品 名", "型号规格", 3, "单位", 1, 12, "注意"});
        data.add(new Object[]{1, "资材编号1", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{2, "资材编号2", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{3, "资材编号3", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{4, "资材编号4", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{1, "资材编号1", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{2, "资材编号2", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});
        data.add(new Object[]{3, "资材编号3", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16"});
        data.add(new Object[]{4, "资材编号4", "品 名", "型号规格", 3, "单位", 1, 12, "2022-11-16"});

        int rowSize = data.size();

        // 获取 cell 样式
        XSSFRow row = excelWriter.getActiveSheet().getRow(11);
        int physicalNumberOfCells = row.getPhysicalNumberOfCells();
        XSSFCellStyle[] cellStyles = new XSSFCellStyle[physicalNumberOfCells];
        for (int i = 0; i < physicalNumberOfCells; i++) {
            cellStyles[i] = row.getCell(i).getCellStyle();
        }

        XSSFColor redColor = new XSSFColor(Color.RED, new DefaultIndexedColorMap());

        // 文字色
        XSSFFont font = excelWriter.getBook().createFont();
        font.setColor(redColor);
        font.setBold(true);

        excelWriter.insertAndWriteRow(1, 12, data, row.getHeightInPoints(), cellStyles, (ecell, cell) -> {
            if (ecell.getX() == 9 && !String.valueOf(ecell.getValue()).matches("\\d{4}-\\d{2}-\\d{2}")) {
                XSSFCellStyle newStyle = cell.getCellStyle().copy();
                font.setFamily(newStyle.getFont().getFamily());
                font.setFontName(newStyle.getFont().getFontName());
                font.setFontHeight(newStyle.getFont().getFontHeight());
                newStyle.setFont(font);
                cell.setCellStyle(newStyle);
            }
        });

        excelWriter.writeCell(new ExcelCell(3, 13 + rowSize, "¥46.00"));
        excelWriter.writeCell(new ExcelCell(3, 14 + rowSize, "RMB46"));
        excelWriter.writeCell(new ExcelCell(2, 15 + rowSize, "哈哈"));
        excelWriter.writeCell(new ExcelCell(4, 28 + rowSize, "本公司确认：方慧2023/08/10"));

        excelWriter.getBook().setSheetName(0, "20230810");

        // 插入图片
        //创建一个excel文件，名称为：
        XSSFWorkbook workbook = excelWriter.getBook();
        //创建一个sheet，名称为工作簿1
        XSSFSheet sheet = excelWriter.getActiveSheet();

        BufferedImage bufferImg;
        //先把读进来的图片放到一个ByteArrayOutputStream中，以便产生ByteArray
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        String imageUrl = "/Users/rick/Space/Share/seal.png";

        //获取图片后缀
        bufferImg = ImageIO.read(new File(imageUrl));
        ImageIO.write(bufferImg, "png", byteArrayOut);

        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        XSSFDrawing patriarch = sheet.createDrawingPatriarch();
        //anchor主要用于设置图片的属性
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) 7, 19 + rowSize, (short) 9, 30 + rowSize);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        //插入图片
        patriarch.createPicture(anchor, workbook.addPicture(byteArrayOut.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));

        excelWriter.toFile(new FileOutputStream(dist));
    }

    /**
     * 获取 Excel 的图片
     * @throws IOException
     */
    @Test
    public void getSheetPictrues07() throws IOException {
        String template = "/Users/rick/Space/Workspace/sharp-admin/src/main/resources/templates/excel/dist.xlsx";

        byte[] bytes = IOUtils.toByteArray(new FileInputStream(template));

        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        XSSFSheet sheet = workbook.getSheetAt(0);

        Map<String, PictureData> sheetIndexPicMap = new HashMap<>();

        for (POIXMLDocumentPart dr : sheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;

                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;

                    XSSFClientAnchor anchor = pic.getPreferredSize();

                    CTMarker ctMarker = anchor.getFrom();
                    String picIndex = 0 + "_"
                            + ctMarker.getRow() + "_" + ctMarker.getCol();
                    sheetIndexPicMap.put(picIndex, pic.getPictureData());


                }
            }
        }

        workbook.close();
        System.out.println(sheetIndexPicMap);
    }

    private XSSFCellStyle createStyle(XSSFWorkbook book) {
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

}
