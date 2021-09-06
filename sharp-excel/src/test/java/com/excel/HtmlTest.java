package com.excel;

import com.rick.excel.table.HtmlExcelTable;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/19/20 11:09 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class HtmlTest {

    @Test
    public void testHtmlExport() throws IOException {
        HtmlExcelTable excelTable = new HtmlExcelTable();

        excelTable.getExcelWriter().getActiveSheet().setColumnWidth(0, 8000);

        excelTable.write("<table class=\"table table-responsive-sm table-bordered table-sm table-stock\">\n" +
                "                <tbody>\n" +
                "                    <tr>\n" +
                "                        <th colspan=\"4\">国内仓</th>\n" +
                "                        <th rowspan=\"2\">香港仓</th>\n" +
                "                        <th rowspan=\"2\">香港直运仓</th>\n" +
                "                        <th rowspan=\"2\">供应链未完成数量</th>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <th>深圳成品仓</th>\n" +
                "                        <th>苏州成品仓</th>\n" +
                "                        <th>深圳待处理仓</th>\n" +
                "                        <th>苏州样品仓</th>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td>13,558</td>\n" +
                "                        <td>0</td>\n" +
                "                        <td>18</td>\n" +
                "                        <td>0</td>\n" +
                "                        <td rowspan=\"3\">0</td>\n" +
                "                        <td rowspan=\"3\">0</td>\n" +
                "                        <td rowspan=\"3\">\n" +
                "                            <a href=\"javascript:;\" id=\"otherDetailBtn\">1,000,500</a>\n" +
                "                            \n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <th colspan=\"4\">国内仓锁货数量</th>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td colspan=\"4\">\n" +
                "                            <a href=\"javascript:;\" id=\"reserveDetailBtn\">12,000</a>\n" +
                "                            \n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </tbody>\n" +
                "            </table>", new FileOutputStream(new File("/Users/rick/Documents/a.xlsx")));
    }

    @Test
    public void testHtmlExport2() throws IOException {
        HtmlExcelTable excelTable = new HtmlExcelTable();

        excelTable.write(null, new FileOutputStream(new File("/Users/rick/Documents/b.xlsx")));
    }
}
