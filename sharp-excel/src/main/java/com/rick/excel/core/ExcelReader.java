package com.rick.excel.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

/**
 * 
 * @author Rick.Xu
 * @Date 2014-12-16
 *
 */
@Slf4j
public class ExcelReader {
	/**
	 * @param is
	 * @param ers
	 * @return 总的行数
	 * @throws Exception
	 */
	public static void readExcelContent(InputStream is, ExcelResultSet ers) throws Exception {
		ers.beforeReader();

		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet sheet;
		XSSFRow row;
		
		int sheetIndexs = wb.getNumberOfSheets();
		
		for (int sheetIndex = 0; sheetIndex < sheetIndexs; sheetIndex++) {
			sheet = wb.getSheetAt(sheetIndex);
	        // 得到总行数
	        int rowNum = sheet.getLastRowNum();
	        row = sheet.getRow(0);
	        String sheetName = sheet.getSheetName();
	        int colNum = row.getPhysicalNumberOfCells();
	        // 正文
	        for (int i = 0; i <= rowNum; i++) {
	            row = sheet.getRow(i);
	            if(row == null)
	            	continue;
	            
	            int j = 0;
	            Object[] data = new Object[colNum];
	            while (j < colNum) {
					data[j] = getCellValue(row.getCell(j));
	                j++;
	            }
	            
	            if(!ers.rowMapper(i, data, sheetIndex,sheetName)) {
	            	break;
	            }
	        }
		 }
		ers.afterReader();
    }
  
	/**
     * 根据XSSFCell类型设置数据
     * @param cell
     * @return
     */
    private static Object getCellValue(XSSFCell cell) {
		switch (cell.getCellType()) {
			case NUMERIC: {
				if (DateUtil.isCellDateFormatted(cell)) { // 日期
					return cell.getDateCellValue();
				}
//				return BigDecimal.valueOf(cell.getNumericCellValue());
				return cell.getNumericCellValue();
			}
			case FORMULA:
				return cell.getCellFormula();
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case _NONE:
			case ERROR:
				return null;
			default:
				return cell.getStringCellValue();
		}
    }
}
