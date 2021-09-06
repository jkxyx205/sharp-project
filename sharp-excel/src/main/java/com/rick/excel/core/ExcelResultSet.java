package com.rick.excel.core;

public interface ExcelResultSet {

	/**
	 *
	 * @return false 终止读取
	 */
	default void beforeReader() {}

	/**
	 *
	 * @param index
	 * @param data
	 * @param sheetIndex
	 * @param sheetName
	 * @return false 终止循环
	 * @throws Exception
	 */
	boolean rowMapper(int index, Object[] data, int sheetIndex, String sheetName) throws Exception;
	
	default void afterReader() {}
}
