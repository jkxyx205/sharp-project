package com.rick.excel.core;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

/**
 * Excel读取
 * @author Rick.Xu
 * @Date 2014-12-16
 *
 */
public class ExcelReader {

	/**
	 * @param is
	 * @param ers
	 * @return 读取excel
	 * @throws Exception
	 */
	public static void readExcelContent(InputStream is, ExcelResultSet ers) throws Exception {
		readExcelContent(is, ers, new CellValueExtractor() {});
	}

	/**
	 * @param is
	 * @param ers
	 * @return 读取excel
	 * @throws Exception
	 */
	public static void readExcelContent(InputStream is, ExcelResultSet ers, CellValueExtractor cellValueExtractor) throws Exception {
		ers.beforeReader();

		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet sheet;
		XSSFRow row;

		int sheetIndexes = wb.getNumberOfSheets();

		for (int sheetIndex = 0; sheetIndex < sheetIndexes; sheetIndex++) {
			sheet = wb.getSheetAt(sheetIndex);
			// 得到总行数
			int rowNum = sheet.getLastRowNum();
			row = sheet.getRow(sheet.getFirstRowNum());
			if (row == null) {
				break;
			}

			String sheetName = sheet.getSheetName();

			// 正文
			for (int i = 0; i <= rowNum; i++) {
				row = sheet.getRow(i);
				Object[] data;
				if(row == null) {
					data = new Object[]{};
				} else {
					int colNum = Math.max(row.getLastCellNum(), row.getPhysicalNumberOfCells());
					int j = 0;
					data = new Object[colNum];
					while (j < colNum) {
						data[j] = cellValueExtractor.getCellValue(row.getCell(j));
						j++;
					}
				}

				if(!ers.rowMapper(i, data, sheetIndex,sheetName)) {
					break;
				}
			}
		}
		ers.afterReader();
		is.close();
		wb.close();
	}

}
